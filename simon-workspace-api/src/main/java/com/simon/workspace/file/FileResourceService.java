package com.simon.workspace.file;

import com.simon.workspace.auth.session.AuthContextHolder;
import com.simon.workspace.file.dto.FileResourceResponse;
import com.simon.workspace.file.model.FileDownload;
import com.simon.workspace.file.model.FileResource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

@Service
public class FileResourceService {

    private final JdbcTemplate jdbcTemplate;
    private final Path storageRoot;

    public FileResourceService(
            JdbcTemplate jdbcTemplate,
            @Value("${app.file-storage.root:./data/files}") String storageRoot
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.storageRoot = Paths.get(storageRoot).toAbsolutePath().normalize();
    }

    public List<FileResourceResponse> list(String keyword) {
        long ownerUserId = AuthContextHolder.requireUser().id();
        if (StringUtils.hasText(keyword)) {
            String like = "%" + keyword.trim() + "%";
            return jdbcTemplate.query("""
                            SELECT *
                            FROM file_resource
                            WHERE owner_user_id = ? AND deleted = 0
                              AND (original_filename LIKE ? OR source_type LIKE ?)
                            ORDER BY created_time DESC, id DESC
                            """,
                    (rs, rowNum) -> FileResourceResponse.from(FileResourceRowMapper.map(rs)),
                    ownerUserId,
                    like,
                    like
            );
        }

        return jdbcTemplate.query("""
                        SELECT *
                        FROM file_resource
                        WHERE owner_user_id = ? AND deleted = 0
                        ORDER BY created_time DESC, id DESC
                        """,
                (rs, rowNum) -> FileResourceResponse.from(FileResourceRowMapper.map(rs)),
                ownerUserId
        );
    }

    public FileResourceResponse detail(long id) {
        return FileResourceResponse.from(findOwnedRequired(id));
    }

    public FileDownload download(long id) {
        FileResource fileResource = findOwnedRequired(id);
        Path path = resolveStoragePath(fileResource.storagePath());
        if (!Files.isRegularFile(path)) {
            throw new IllegalArgumentException("文件不存在或已被移除");
        }

        try {
            Resource resource = new UrlResource(path.toUri());
            return new FileDownload(
                    resource,
                    fileResource.originalFilename(),
                    fileResource.contentType(),
                    Files.size(path)
            );
        } catch (MalformedURLException exception) {
            throw new IllegalStateException("文件路径不合法", exception);
        } catch (IOException exception) {
            throw new IllegalStateException("读取文件失败", exception);
        }
    }

    @Transactional
    public void delete(long id) {
        long ownerUserId = AuthContextHolder.requireUser().id();
        int affected = jdbcTemplate.update(
                "UPDATE file_resource SET deleted = 1 WHERE id = ? AND owner_user_id = ? AND deleted = 0",
                id,
                ownerUserId
        );
        if (affected == 0) {
            throw new IllegalArgumentException("文件不存在或无权访问");
        }
    }

    @Transactional
    public FileResourceResponse saveResource(
            long ownerUserId,
            String sourceType,
            InputStream inputStream,
            String originalFilename,
            String contentType
    ) {
        if (inputStream == null) {
            throw new IllegalArgumentException("文件内容不能为空");
        }

        String safeFilename = sanitizeOriginalFilename(originalFilename);
        StoredFile storedFile = storeFile(inputStream, safeFilename);

        try {
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                PreparedStatement statement = connection.prepareStatement("""
                                INSERT INTO file_resource (
                                    owner_user_id, source_type, original_filename, storage_path,
                                    file_size, content_type, file_extension, status
                                )
                                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                                """,
                        Statement.RETURN_GENERATED_KEYS
                );
                statement.setLong(1, ownerUserId);
                statement.setString(2, normalizeSourceType(sourceType));
                statement.setString(3, safeFilename);
                statement.setString(4, storedFile.storagePath());
                statement.setLong(5, storedFile.fileSize());
                statement.setString(6, blankToNull(contentType));
                statement.setString(7, extensionNameOf(safeFilename));
                statement.setString(8, "ACTIVE");
                return statement;
            }, keyHolder);

            long id = Objects.requireNonNull(keyHolder.getKey()).longValue();
            return FileResourceResponse.from(findRequired(id));
        } catch (RuntimeException exception) {
            deleteStoredFileQuietly(storedFile.storagePath());
            throw exception;
        }
    }

    private FileResource findOwnedRequired(long id) {
        long ownerUserId = AuthContextHolder.requireUser().id();
        return jdbcTemplate.query("""
                        SELECT *
                        FROM file_resource
                        WHERE id = ? AND owner_user_id = ? AND deleted = 0
                        LIMIT 1
                        """,
                (rs, rowNum) -> FileResourceRowMapper.map(rs),
                id,
                ownerUserId
        ).stream().findFirst().orElseThrow(() -> new IllegalArgumentException("文件不存在或无权访问"));
    }

    private FileResource findRequired(long id) {
        return jdbcTemplate.query("""
                        SELECT *
                        FROM file_resource
                        WHERE id = ? AND deleted = 0
                        LIMIT 1
                        """,
                (rs, rowNum) -> FileResourceRowMapper.map(rs),
                id
        ).stream().findFirst().orElseThrow(() -> new IllegalArgumentException("文件不存在"));
    }

    private StoredFile storeFile(InputStream inputStream, String originalFilename) {
        LocalDate today = LocalDate.now();
        Path targetDirectory = storageRoot
                .resolve("files")
                .resolve(String.valueOf(today.getYear()))
                .resolve(String.format("%02d", today.getMonthValue()))
                .normalize();

        if (!targetDirectory.startsWith(storageRoot)) {
            throw new IllegalStateException("文件存储目录不合法");
        }

        String storedFilename = UUID.randomUUID() + extensionWithDotOf(originalFilename);
        Path targetPath = targetDirectory.resolve(storedFilename).normalize();
        if (!targetPath.startsWith(targetDirectory)) {
            throw new IllegalStateException("文件存储路径不合法");
        }

        try {
            Files.createDirectories(targetDirectory);
            long fileSize = Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
            String storagePath = storageRoot.relativize(targetPath).toString().replace('\\', '/');
            return new StoredFile(storagePath, fileSize);
        } catch (IOException exception) {
            throw new IllegalStateException("保存文件失败", exception);
        }
    }

    private Path resolveStoragePath(String storagePath) {
        Path path = storageRoot.resolve(storagePath).normalize();
        if (!path.startsWith(storageRoot)) {
            throw new IllegalStateException("文件路径不合法");
        }
        return path;
    }

    private void deleteStoredFileQuietly(String storedPath) {
        try {
            Path path = resolveStoragePath(storedPath);
            Files.deleteIfExists(path);
        } catch (IOException | RuntimeException ignored) {
            // Metadata rollback is more important than cleanup; orphan cleanup can run separately.
        }
    }

    private String normalizeSourceType(String sourceType) {
        if (!StringUtils.hasText(sourceType)) {
            return "GENERATED";
        }

        String normalized = sourceType.trim().toUpperCase(Locale.ROOT);
        if (!"UPLOAD".equals(normalized) && !"GENERATED".equals(normalized)
                && !"TEMPLATE".equals(normalized) && !"OTHER".equals(normalized)) {
            throw new IllegalArgumentException("文件来源类型不合法");
        }
        return normalized;
    }

    private String sanitizeOriginalFilename(String originalFilename) {
        String cleaned = StringUtils.hasText(originalFilename)
                ? StringUtils.cleanPath(originalFilename.trim()).replace('\\', '/')
                : "file";
        int slashIndex = cleaned.lastIndexOf('/');
        String filename = slashIndex >= 0 ? cleaned.substring(slashIndex + 1) : cleaned;
        if (!StringUtils.hasText(filename) || ".".equals(filename) || "..".equals(filename)) {
            filename = "file";
        }
        return filename.length() > 255 ? filename.substring(filename.length() - 255) : filename;
    }

    private String extensionWithDotOf(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == filename.length() - 1) {
            return "";
        }
        String extension = filename.substring(dotIndex);
        return extension.length() > 32 ? "" : extension;
    }

    private String extensionNameOf(String filename) {
        String extension = extensionWithDotOf(filename);
        return extension.startsWith(".") ? extension.substring(1) : null;
    }

    private String blankToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private record StoredFile(String storagePath, long fileSize) {
    }
}
