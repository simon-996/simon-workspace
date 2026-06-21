CREATE TABLE IF NOT EXISTS `file_resource` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '文件资源 ID',
    `owner_user_id` BIGINT NOT NULL COMMENT '所属用户 ID',
    `source_type` VARCHAR(32) NOT NULL DEFAULT 'GENERATED' COMMENT '来源类型：UPLOAD / GENERATED / TEMPLATE / OTHER',
    `original_filename` VARCHAR(255) NOT NULL COMMENT '原始文件名',
    `storage_path` VARCHAR(512) NOT NULL COMMENT '存储相对路径',
    `file_size` BIGINT NOT NULL DEFAULT 0 COMMENT '文件大小',
    `content_type` VARCHAR(128) NULL COMMENT '文件 MIME 类型',
    `file_extension` VARCHAR(32) NULL COMMENT '文件扩展名',
    `status` VARCHAR(32) NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE / ARCHIVED',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    KEY `idx_file_resource_owner` (`owner_user_id`),
    KEY `idx_file_resource_source` (`source_type`),
    KEY `idx_file_resource_created_time` (`created_time`),
    CONSTRAINT `fk_file_resource_owner` FOREIGN KEY (`owner_user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文件资源';
