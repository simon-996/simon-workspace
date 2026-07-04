package com.simon.workspace.file;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class FileReferenceService {

    private final JdbcTemplate jdbcTemplate;

    public FileReferenceService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public void syncReferences(
            String resourceType,
            String resourceId,
            String usageType,
            Collection<Long> fileIds
    ) {
        String normalizedResourceType = normalize(resourceType, "引用资源类型不能为空");
        String normalizedResourceId = required(resourceId, "引用资源 ID 不能为空");
        String normalizedUsageType = normalize(usageType, "文件使用类型不能为空");
        Set<Long> desiredFileIds = normalizeFileIds(fileIds);

        List<Long> existingFileIds = jdbcTemplate.queryForList("""
                        SELECT file_id
                        FROM file_reference
                        WHERE resource_type = ? AND resource_id = ? AND usage_type = ? AND deleted = 0
                        """,
                Long.class,
                normalizedResourceType,
                normalizedResourceId,
                normalizedUsageType
        );

        jdbcTemplate.update("""
                        UPDATE file_reference
                        SET deleted = 1
                        WHERE resource_type = ? AND resource_id = ? AND usage_type = ? AND deleted = 0
                        """,
                normalizedResourceType,
                normalizedResourceId,
                normalizedUsageType
        );

        desiredFileIds.forEach(fileId -> {
            jdbcTemplate.update("""
                            INSERT INTO file_reference (file_id, resource_type, resource_id, usage_type, deleted)
                            VALUES (?, ?, ?, ?, 0)
                            ON DUPLICATE KEY UPDATE deleted = 0, updated_time = CURRENT_TIMESTAMP
                            """,
                    fileId,
                    normalizedResourceType,
                    normalizedResourceId,
                    normalizedUsageType
            );
            markFileActive(fileId);
        });

        existingFileIds.stream()
                .filter(fileId -> !desiredFileIds.contains(fileId))
                .forEach(this::markFileOrphanedIfUnreferenced);
    }

    public void markFileOrphanedIfUnreferenced(long fileId) {
        jdbcTemplate.update("""
                        UPDATE file_resource f
                        SET f.status = 'ORPHANED',
                            f.orphaned_time = COALESCE(f.orphaned_time, CURRENT_TIMESTAMP)
                        WHERE f.id = ?
                          AND f.deleted = 0
                          AND NOT EXISTS (
                              SELECT 1
                              FROM file_reference r
                              WHERE r.file_id = f.id AND r.deleted = 0
                          )
                        """,
                fileId
        );
    }

    private void markFileActive(long fileId) {
        jdbcTemplate.update("""
                        UPDATE file_resource
                        SET status = 'ACTIVE', orphaned_time = NULL
                        WHERE id = ? AND deleted = 0
                        """,
                fileId
        );
    }

    private Set<Long> normalizeFileIds(Collection<Long> fileIds) {
        Set<Long> normalized = new LinkedHashSet<>();
        if (fileIds == null) {
            return normalized;
        }
        fileIds.stream()
                .filter(fileId -> fileId != null && fileId > 0)
                .forEach(normalized::add);
        return normalized;
    }

    private String normalize(String value, String message) {
        return required(value, message).toUpperCase(Locale.ROOT);
    }

    private String required(String value, String message) {
        if (!StringUtils.hasText(value)) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }
}
