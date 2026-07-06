package com.simon.workspace.file;

import com.simon.workspace.auth.session.AuthContextHolder;
import com.simon.workspace.file.dto.FileResourceResponse;
import com.simon.workspace.file.model.FileDownload;
import com.simon.workspace.file.model.FileResource;
import com.simon.workspace.storage.StorageObjectDownload;
import com.simon.workspace.storage.StorageProviderRegistry;
import com.simon.workspace.storage.StorageProviderStateService;
import com.simon.workspace.storage.StorageVisibility;
import com.simon.workspace.storage.StoredObject;
import com.simon.workspace.storage.WorkspaceStorageProvider;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Service
public class FileResourceService {

    private final JdbcTemplate jdbcTemplate;
    private final StorageProviderStateService storageProviderStateService;
    private final StorageProviderRegistry storageProviderRegistry;

    public FileResourceService(
            JdbcTemplate jdbcTemplate,
            StorageProviderStateService storageProviderStateService,
            StorageProviderRegistry storageProviderRegistry
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.storageProviderStateService = storageProviderStateService;
        this.storageProviderRegistry = storageProviderRegistry;
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
        WorkspaceStorageProvider provider = storageProviderRegistry.provider(fileResource.storageProvider());
        StorageObjectDownload object = provider.download(objectKeyOf(fileResource));
        return new FileDownload(
                object.resource(),
                fileResource.originalFilename(),
                fileResource.contentType(),
                object.fileSize() > 0 ? object.fileSize() : fileResource.fileSize()
        );
    }

    public FileDownload publicDownload(long id) {
        FileResource fileResource = findPublicRequired(id);
        WorkspaceStorageProvider provider = storageProviderRegistry.provider(fileResource.storageProvider());
        StorageObjectDownload object = provider.download(objectKeyOf(fileResource));
        return new FileDownload(
                object.resource(),
                fileResource.originalFilename(),
                fileResource.contentType(),
                object.fileSize() > 0 ? object.fileSize() : fileResource.fileSize()
        );
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
        return saveResource(ownerUserId, sourceType, inputStream, originalFilename, contentType, StorageVisibility.PRIVATE);
    }

    @Transactional
    public FileResourceResponse upload(MultipartFile file, String sourceType, String visibility) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件内容不能为空");
        }

        try (InputStream inputStream = file.getInputStream()) {
            return saveResource(
                    AuthContextHolder.requireUser().id(),
                    sourceType,
                    inputStream,
                    file.getOriginalFilename(),
                    file.getContentType(),
                    normalizeVisibility(visibility)
            );
        } catch (Exception exception) {
            if (exception instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            throw new IllegalStateException("上传文件失败", exception);
        }
    }

    @Transactional
    public FileResourceResponse saveResource(
            long ownerUserId,
            String sourceType,
            InputStream inputStream,
            String originalFilename,
            String contentType,
            StorageVisibility visibility
    ) {
        if (inputStream == null) {
            throw new IllegalArgumentException("文件内容不能为空");
        }

        String safeFilename = sanitizeOriginalFilename(originalFilename);
        WorkspaceStorageProvider provider = storageProviderStateService.activeProvider();
        StoredObject storedObject = provider.store(inputStream, safeFilename, contentType, visibility);

        try {
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                PreparedStatement statement = connection.prepareStatement("""
                                INSERT INTO file_resource (
                                    owner_user_id, source_type, original_filename, storage_path,
                                    storage_provider, object_key, visibility, public_url,
                                    file_size, content_type, file_extension, status
                                )
                                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                                """,
                        Statement.RETURN_GENERATED_KEYS
                );
                statement.setLong(1, ownerUserId);
                statement.setString(2, normalizeSourceType(sourceType));
                statement.setString(3, safeFilename);
                statement.setString(4, storedObject.storagePath());
                statement.setString(5, storedObject.providerCode());
                statement.setString(6, storedObject.objectKey());
                statement.setString(7, visibility.name());
                statement.setString(8, visibility == StorageVisibility.PUBLIC ? storedObject.publicUrl() : null);
                statement.setLong(9, storedObject.fileSize());
                statement.setString(10, blankToNull(contentType));
                statement.setString(11, extensionNameOf(safeFilename));
                statement.setString(12, "ACTIVE");
                return statement;
            }, keyHolder);

            long id = Objects.requireNonNull(keyHolder.getKey()).longValue();
            return FileResourceResponse.from(findRequired(id));
        } catch (RuntimeException exception) {
            deleteStoredObjectQuietly(storedObject);
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

    private FileResource findPublicRequired(long id) {
        return jdbcTemplate.query("""
                        SELECT *
                        FROM file_resource
                        WHERE id = ? AND visibility = 'PUBLIC' AND status = 'ACTIVE' AND deleted = 0
                        LIMIT 1
                        """,
                (rs, rowNum) -> FileResourceRowMapper.map(rs),
                id
        ).stream().findFirst().orElseThrow(() -> new IllegalArgumentException("文件不存在或不是公开文件"));
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

    private void deleteStoredObjectQuietly(StoredObject storedObject) {
        try {
            storageProviderRegistry.provider(storedObject.providerCode()).delete(storedObject.objectKey());
        } catch (RuntimeException ignored) {
            // Metadata rollback is more important than cleanup; orphan cleanup can run separately.
        }
    }

    private String normalizeSourceType(String sourceType) {
        if (!StringUtils.hasText(sourceType)) {
            return "GENERATED";
        }

        String normalized = sourceType.trim().toUpperCase(Locale.ROOT);
        if (!"UPLOAD".equals(normalized) && !"GENERATED".equals(normalized)
                && !"TEMPLATE".equals(normalized) && !"AVATAR".equals(normalized)
                && !"BLOG_EDITOR".equals(normalized) && !"COURSE_MATERIAL".equals(normalized)
                && !"OTHER".equals(normalized)) {
            throw new IllegalArgumentException("文件来源类型不合法");
        }
        return normalized;
    }

    private StorageVisibility normalizeVisibility(String visibility) {
        if (!StringUtils.hasText(visibility)) {
            return StorageVisibility.PRIVATE;
        }

        String normalized = visibility.trim().toUpperCase(Locale.ROOT);
        if (!"PUBLIC".equals(normalized) && !"PRIVATE".equals(normalized)) {
            throw new IllegalArgumentException("文件可见性不合法");
        }
        return StorageVisibility.valueOf(normalized);
    }

    private String objectKeyOf(FileResource fileResource) {
        return StringUtils.hasText(fileResource.objectKey()) ? fileResource.objectKey() : fileResource.storagePath();
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

}
