ALTER TABLE `generate_task`
    MODIFY `status` VARCHAR(32) NOT NULL DEFAULT 'PENDING'
        COMMENT 'PENDING / PREVIEW_READY / FILLING_TEMPLATE / SUCCESS / FAILED / CANCELED';

CREATE TABLE IF NOT EXISTS `document_data` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'document data ID',
    `task_id` BIGINT NOT NULL COMMENT 'generation task ID',
    `document_type` VARCHAR(32) NOT NULL COMMENT 'TEACHING_PLAN / TEACHING_CALENDAR',
    `data_json` JSON NOT NULL COMMENT 'initial generated JSON',
    `edited_json` JSON NULL COMMENT 'user edited JSON',
    `version` VARCHAR(32) NOT NULL DEFAULT 'v1' COMMENT 'document data version',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'created time',
    `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'updated time',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT 'logical delete',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_document_data_task` (`task_id`),
    KEY `idx_document_data_type` (`document_type`),
    CONSTRAINT `fk_document_data_task` FOREIGN KEY (`task_id`) REFERENCES `generate_task` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='document preview and edit data';

CREATE TABLE IF NOT EXISTS `generate_result` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'generation result ID',
    `task_id` BIGINT NOT NULL COMMENT 'generation task ID',
    `file_resource_id` BIGINT NOT NULL COMMENT 'file resource ID',
    `file_name` VARCHAR(255) NOT NULL COMMENT 'generated file name',
    `file_type` VARCHAR(32) NOT NULL COMMENT 'DOCX / XLSX / OTHER',
    `result_type` VARCHAR(32) NOT NULL COMMENT 'TEACHING_PLAN / TEACHING_CALENDAR',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'created time',
    `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'updated time',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT 'logical delete',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_generate_result_task_file` (`task_id`, `file_resource_id`),
    KEY `idx_generate_result_task` (`task_id`),
    KEY `idx_generate_result_file` (`file_resource_id`),
    KEY `idx_generate_result_type` (`result_type`),
    CONSTRAINT `fk_generate_result_task` FOREIGN KEY (`task_id`) REFERENCES `generate_task` (`id`),
    CONSTRAINT `fk_generate_result_file` FOREIGN KEY (`file_resource_id`) REFERENCES `file_resource` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='generated document result';
