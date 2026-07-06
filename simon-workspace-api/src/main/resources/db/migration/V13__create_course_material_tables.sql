ALTER TABLE `course`
    ADD COLUMN `public_visible` TINYINT NOT NULL DEFAULT 0 COMMENT 'Whether the course is visible publicly' AFTER `status`,
    ADD COLUMN `public_sort_order` INT NOT NULL DEFAULT 0 COMMENT 'Public course sort order' AFTER `public_visible`,
    ADD KEY `idx_course_public_visible` (`public_visible`, `public_sort_order`);

CREATE TABLE IF NOT EXISTS `course_material` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Course material ID',
    `course_id` BIGINT NOT NULL COMMENT 'Course ID',
    `section` VARCHAR(32) NOT NULL COMMENT 'DOCUMENT / COURSEWARE / RESOURCE',
    `material_type` VARCHAR(32) NOT NULL COMMENT 'FILE / LINK',
    `file_id` BIGINT NULL COMMENT 'File resource ID',
    `external_url` VARCHAR(1024) NULL COMMENT 'External URL',
    `title` VARCHAR(160) NOT NULL COMMENT 'Display title',
    `description` VARCHAR(512) NULL COMMENT 'Short description',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT 'Sort order',
    `status` VARCHAR(32) NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE / DISABLED',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created time',
    `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Updated time',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT 'Logical delete',
    PRIMARY KEY (`id`),
    KEY `idx_course_material_course_section` (`course_id`, `section`, `sort_order`),
    KEY `idx_course_material_file` (`file_id`),
    CONSTRAINT `fk_course_material_course` FOREIGN KEY (`course_id`) REFERENCES `course` (`id`),
    CONSTRAINT `fk_course_material_file` FOREIGN KEY (`file_id`) REFERENCES `file_resource` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Course public materials';
