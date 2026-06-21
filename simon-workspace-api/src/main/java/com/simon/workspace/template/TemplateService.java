package com.simon.workspace.template;

import com.simon.workspace.template.dto.TemplateFieldRequest;
import com.simon.workspace.template.dto.TemplateFieldResponse;
import com.simon.workspace.template.dto.TemplateFieldsRequest;
import com.simon.workspace.template.dto.TemplateResponse;
import com.simon.workspace.template.dto.TemplateUpdateRequest;
import com.simon.workspace.template.model.TemplateFile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Service
public class TemplateService {

    private final JdbcTemplate jdbcTemplate;
    private final Path storageRoot;

    public TemplateService(
            JdbcTemplate jdbcTemplate,
            @Value("${app.file-storage.root:./data/files}") String storageRoot
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.storageRoot = Paths.get(storageRoot).toAbsolutePath().normalize();
    }

    public List<TemplateResponse> list(String keyword) {
        if (StringUtils.hasText(keyword)) {
            String like = "%" + keyword.trim() + "%";
            return jdbcTemplate.query("""
                            SELECT *
                            FROM template_file
                            WHERE deleted = 0
                              AND (template_name LIKE ? OR original_filename LIKE ? OR template_type LIKE ?)
                            ORDER BY updated_time DESC, id DESC
                            """,
                    (rs, rowNum) -> TemplateResponse.from(TemplateRowMapper.mapTemplate(rs)),
                    like,
                    like,
                    like
            );
        }

        return jdbcTemplate.query("""
                        SELECT *
                        FROM template_file
                        WHERE deleted = 0
                        ORDER BY updated_time DESC, id DESC
                        """,
                (rs, rowNum) -> TemplateResponse.from(TemplateRowMapper.mapTemplate(rs))
        );
    }

    public TemplateResponse detail(long id) {
        return TemplateResponse.from(findRequired(id));
    }

    @Transactional
    public TemplateResponse upload(
            MultipartFile file,
            String templateName,
            String templateType,
            String description,
            String status
    ) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("模板文件不能为空");
        }

        String originalFilename = sanitizeOriginalFilename(file.getOriginalFilename());
        String storedPath = storeTemplateFile(file, originalFilename);

        try {
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                PreparedStatement statement = connection.prepareStatement("""
                                INSERT INTO template_file (
                                    template_name, template_type, original_filename, storage_path,
                                    file_size, content_type, description, status
                                )
                                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                                """,
                        Statement.RETURN_GENERATED_KEYS
                );
                statement.setString(1, normalizeTemplateName(templateName, originalFilename));
                statement.setString(2, normalizeTemplateType(templateType, originalFilename));
                statement.setString(3, originalFilename);
                statement.setString(4, storedPath);
                statement.setLong(5, file.getSize());
                statement.setString(6, blankToNull(file.getContentType()));
                statement.setString(7, blankToNull(description));
                statement.setString(8, normalizeTemplateStatus(status));
                return statement;
            }, keyHolder);

            long id = Objects.requireNonNull(keyHolder.getKey()).longValue();
            return detail(id);
        } catch (RuntimeException exception) {
            deleteStoredFileQuietly(storedPath);
            throw exception;
        }
    }

    @Transactional
    public TemplateResponse update(long id, TemplateUpdateRequest request) {
        TemplateFile templateFile = findRequired(id);
        int affected = jdbcTemplate.update("""
                        UPDATE template_file
                        SET template_name = ?, template_type = ?, description = ?, status = ?
                        WHERE id = ? AND deleted = 0
                        """,
                request.templateName().trim(),
                normalizeTemplateType(request.templateType(), templateFile.originalFilename()),
                blankToNull(request.description()),
                normalizeTemplateStatus(request.status()),
                id
        );

        if (affected == 0) {
            throw new IllegalArgumentException("模板不存在");
        }

        return detail(id);
    }

    @Transactional
    public void delete(long id) {
        findRequired(id);
        jdbcTemplate.update("UPDATE template_field SET deleted = 1 WHERE template_id = ? AND deleted = 0", id);
        int affected = jdbcTemplate.update("UPDATE template_file SET deleted = 1 WHERE id = ? AND deleted = 0", id);
        if (affected == 0) {
            throw new IllegalArgumentException("模板不存在");
        }
    }

    public List<TemplateFieldResponse> fields(long templateId) {
        findRequired(templateId);
        return jdbcTemplate.query("""
                        SELECT *
                        FROM template_field
                        WHERE template_id = ? AND deleted = 0
                        ORDER BY sort_order ASC, id ASC
                        """,
                (rs, rowNum) -> TemplateFieldResponse.from(TemplateRowMapper.mapField(rs)),
                templateId
        );
    }

    @Transactional
    public List<TemplateFieldResponse> updateFields(long templateId, TemplateFieldsRequest request) {
        findRequired(templateId);
        if (request == null || request.fields() == null) {
            throw new IllegalArgumentException("字段列表不能为空");
        }

        List<TemplateFieldRequest> fields = request.fields();
        validateFields(fields);

        jdbcTemplate.update("UPDATE template_field SET deleted = 1 WHERE template_id = ? AND deleted = 0", templateId);
        for (int index = 0; index < fields.size(); index++) {
            TemplateFieldRequest field = fields.get(index);
            jdbcTemplate.update("""
                            INSERT INTO template_field (
                                template_id, field_key, field_label, field_type, `required`,
                                default_value, sort_order, remark, status
                            )
                            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                            """,
                    templateId,
                    field.fieldKey().trim(),
                    blankToNull(field.fieldLabel()),
                    normalizeFieldType(field.fieldType()),
                    Boolean.TRUE.equals(field.required()),
                    blankToNull(field.defaultValue()),
                    field.sortOrder() == null ? index + 1 : field.sortOrder(),
                    blankToNull(field.remark()),
                    normalizeFieldStatus(field.status())
            );
        }

        return fields(templateId);
    }

    private TemplateFile findRequired(long id) {
        return jdbcTemplate.query("""
                        SELECT *
                        FROM template_file
                        WHERE id = ? AND deleted = 0
                        LIMIT 1
                        """,
                (rs, rowNum) -> TemplateRowMapper.mapTemplate(rs),
                id
        ).stream().findFirst().orElseThrow(() -> new IllegalArgumentException("模板不存在"));
    }

    private String storeTemplateFile(MultipartFile file, String originalFilename) {
        LocalDate today = LocalDate.now();
        Path targetDirectory = storageRoot
                .resolve("templates")
                .resolve(String.valueOf(today.getYear()))
                .resolve(String.format("%02d", today.getMonthValue()))
                .normalize();

        if (!targetDirectory.startsWith(storageRoot)) {
            throw new IllegalStateException("模板存储目录不合法");
        }

        String storedFilename = UUID.randomUUID() + extensionOf(originalFilename);
        Path targetPath = targetDirectory.resolve(storedFilename).normalize();
        if (!targetPath.startsWith(targetDirectory)) {
            throw new IllegalStateException("模板存储路径不合法");
        }

        try {
            Files.createDirectories(targetDirectory);
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }
            return storageRoot.relativize(targetPath).toString().replace('\\', '/');
        } catch (IOException exception) {
            throw new IllegalStateException("保存模板文件失败", exception);
        }
    }

    private void deleteStoredFileQuietly(String storedPath) {
        try {
            Path path = storageRoot.resolve(storedPath).normalize();
            if (path.startsWith(storageRoot)) {
                Files.deleteIfExists(path);
            }
        } catch (IOException ignored) {
            // The database transaction will roll back; a later cleanup can remove orphaned files.
        }
    }

    private void validateFields(List<TemplateFieldRequest> fields) {
        Set<String> fieldKeys = new HashSet<>();
        for (TemplateFieldRequest field : fields) {
            if (!StringUtils.hasText(field.fieldKey())) {
                throw new IllegalArgumentException("字段键不能为空");
            }

            String normalizedKey = field.fieldKey().trim().toLowerCase(Locale.ROOT);
            if (!fieldKeys.add(normalizedKey)) {
                throw new IllegalArgumentException("字段键不能重复：" + field.fieldKey().trim());
            }

            normalizeFieldType(field.fieldType());
            normalizeFieldStatus(field.status());
        }
    }

    private String normalizeTemplateName(String templateName, String originalFilename) {
        if (StringUtils.hasText(templateName)) {
            return templateName.trim();
        }
        return stripExtension(originalFilename);
    }

    private String normalizeTemplateType(String templateType, String originalFilename) {
        if (StringUtils.hasText(templateType)) {
            String normalized = templateType.trim().toUpperCase(Locale.ROOT);
            if (!"WORD".equals(normalized) && !"EXCEL".equals(normalized) && !"OTHER".equals(normalized)) {
                throw new IllegalArgumentException("模板类型不合法");
            }
            return normalized;
        }

        String extension = extensionOf(originalFilename).toLowerCase(Locale.ROOT);
        if (".doc".equals(extension) || ".docx".equals(extension)) {
            return "WORD";
        }
        if (".xls".equals(extension) || ".xlsx".equals(extension)) {
            return "EXCEL";
        }
        return "OTHER";
    }

    private String normalizeTemplateStatus(String status) {
        if (!StringUtils.hasText(status)) {
            return "ACTIVE";
        }

        String normalized = status.trim().toUpperCase(Locale.ROOT);
        if (!"ACTIVE".equals(normalized) && !"ARCHIVED".equals(normalized)) {
            throw new IllegalArgumentException("模板状态不合法");
        }
        return normalized;
    }

    private String normalizeFieldType(String fieldType) {
        if (!StringUtils.hasText(fieldType)) {
            return "TEXT";
        }

        String normalized = fieldType.trim().toUpperCase(Locale.ROOT);
        if (!"TEXT".equals(normalized) && !"NUMBER".equals(normalized)
                && !"DATE".equals(normalized) && !"JSON".equals(normalized)) {
            throw new IllegalArgumentException("字段类型不合法");
        }
        return normalized;
    }

    private String normalizeFieldStatus(String status) {
        if (!StringUtils.hasText(status)) {
            return "ACTIVE";
        }

        String normalized = status.trim().toUpperCase(Locale.ROOT);
        if (!"ACTIVE".equals(normalized) && !"DISABLED".equals(normalized)) {
            throw new IllegalArgumentException("字段状态不合法");
        }
        return normalized;
    }

    private String sanitizeOriginalFilename(String originalFilename) {
        String cleaned = StringUtils.hasText(originalFilename)
                ? StringUtils.cleanPath(originalFilename.trim()).replace('\\', '/')
                : "template";
        int slashIndex = cleaned.lastIndexOf('/');
        String filename = slashIndex >= 0 ? cleaned.substring(slashIndex + 1) : cleaned;
        if (!StringUtils.hasText(filename) || ".".equals(filename) || "..".equals(filename)) {
            filename = "template";
        }
        return filename.length() > 255 ? filename.substring(filename.length() - 255) : filename;
    }

    private String stripExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex <= 0) {
            return filename;
        }
        return filename.substring(0, dotIndex);
    }

    private String extensionOf(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == filename.length() - 1) {
            return "";
        }
        String extension = filename.substring(dotIndex);
        return extension.length() > 16 ? "" : extension;
    }

    private String blankToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
