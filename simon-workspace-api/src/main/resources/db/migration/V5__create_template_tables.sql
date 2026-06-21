CREATE TABLE IF NOT EXISTS `template_file` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '模板文件 ID',
    `template_name` VARCHAR(128) NOT NULL COMMENT '模板名称',
    `template_type` VARCHAR(32) NOT NULL COMMENT '模板类型：WORD / EXCEL / OTHER',
    `original_filename` VARCHAR(255) NOT NULL COMMENT '原始文件名',
    `storage_path` VARCHAR(512) NOT NULL COMMENT '存储相对路径',
    `file_size` BIGINT NOT NULL DEFAULT 0 COMMENT '文件大小',
    `content_type` VARCHAR(128) NULL COMMENT '文件 MIME 类型',
    `description` VARCHAR(500) NULL COMMENT '模板说明',
    `status` VARCHAR(32) NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE / ARCHIVED',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    KEY `idx_template_file_name` (`template_name`),
    KEY `idx_template_file_type` (`template_type`),
    KEY `idx_template_file_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='模板文件';

CREATE TABLE IF NOT EXISTS `template_field` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '模板字段 ID',
    `template_id` BIGINT NOT NULL COMMENT '模板文件 ID',
    `field_key` VARCHAR(128) NOT NULL COMMENT '字段键',
    `field_label` VARCHAR(128) NULL COMMENT '字段显示名',
    `field_type` VARCHAR(32) NOT NULL DEFAULT 'TEXT' COMMENT 'TEXT / NUMBER / DATE / JSON',
    `required` TINYINT NOT NULL DEFAULT 0 COMMENT '是否必填',
    `default_value` TEXT NULL COMMENT '默认值',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序',
    `remark` VARCHAR(500) NULL COMMENT '备注',
    `status` VARCHAR(32) NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE / DISABLED',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    KEY `idx_template_field_template` (`template_id`),
    KEY `idx_template_field_key` (`template_id`, `field_key`),
    CONSTRAINT `fk_template_field_template` FOREIGN KEY (`template_id`) REFERENCES `template_file` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='模板字段';
