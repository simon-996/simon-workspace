CREATE TABLE IF NOT EXISTS `storage_provider_state` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '存储配置状态 ID',
    `provider_code` VARCHAR(64) NOT NULL COMMENT '存储配置编码',
    `provider_type` VARCHAR(64) NOT NULL COMMENT '存储类型',
    `display_name` VARCHAR(128) NOT NULL COMMENT '展示名称',
    `enabled` TINYINT NOT NULL DEFAULT 0 COMMENT '是否启用',
    `active` TINYINT NOT NULL DEFAULT 0 COMMENT '是否默认写入存储',
    `last_test_status` VARCHAR(32) NULL COMMENT '最近连接测试状态',
    `last_test_message` VARCHAR(255) NULL COMMENT '最近连接测试信息',
    `last_test_time` DATETIME NULL COMMENT '最近连接测试时间',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_storage_provider_code` (`provider_code`),
    KEY `idx_storage_provider_active` (`active`),
    KEY `idx_storage_provider_enabled` (`enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='存储配置状态';

INSERT INTO `storage_provider_state` (`provider_code`, `provider_type`, `display_name`, `enabled`, `active`)
VALUES
    ('LOCAL', 'LOCAL', '本地存储', 1, 1),
    ('TENCENT_COS', 'TENCENT_COS', '腾讯云 COS', 0, 0),
    ('ALIYUN_OSS', 'ALIYUN_OSS', '阿里云 OSS', 0, 0),
    ('CLOUDFLARE_R2', 'CLOUDFLARE_R2', 'Cloudflare R2', 0, 0)
ON DUPLICATE KEY UPDATE
    `provider_type` = VALUES(`provider_type`),
    `display_name` = VALUES(`display_name`),
    `deleted` = 0;

ALTER TABLE `file_resource`
    ADD COLUMN `storage_provider` VARCHAR(64) NOT NULL DEFAULT 'LOCAL' COMMENT '存储配置编码' AFTER `storage_path`,
    ADD COLUMN `object_key` VARCHAR(512) NULL COMMENT '对象存储 Key' AFTER `storage_provider`,
    ADD COLUMN `visibility` VARCHAR(32) NOT NULL DEFAULT 'PRIVATE' COMMENT 'PUBLIC / PRIVATE' AFTER `object_key`,
    ADD COLUMN `public_url` VARCHAR(1024) NULL COMMENT '公开访问地址' AFTER `visibility`,
    ADD COLUMN `orphaned_time` DATETIME NULL COMMENT '进入孤儿状态时间' AFTER `status`,
    ADD KEY `idx_file_resource_storage_provider` (`storage_provider`),
    ADD KEY `idx_file_resource_status_orphaned` (`status`, `orphaned_time`);

UPDATE `file_resource`
SET `object_key` = `storage_path`
WHERE `object_key` IS NULL;

CREATE TABLE IF NOT EXISTS `file_reference` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '文件引用 ID',
    `file_id` BIGINT NOT NULL COMMENT '文件 ID',
    `resource_type` VARCHAR(64) NOT NULL COMMENT '引用资源类型',
    `resource_id` VARCHAR(64) NOT NULL COMMENT '引用资源 ID',
    `usage_type` VARCHAR(64) NOT NULL COMMENT '使用类型',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_file_reference` (`file_id`, `resource_type`, `resource_id`, `usage_type`),
    KEY `idx_file_reference_file` (`file_id`),
    KEY `idx_file_reference_resource` (`resource_type`, `resource_id`),
    CONSTRAINT `fk_file_reference_file` FOREIGN KEY (`file_id`) REFERENCES `file_resource` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文件引用关系';
