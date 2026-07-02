CREATE TABLE IF NOT EXISTS `site_config` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '配置 ID',
    `site_title` VARCHAR(128) NOT NULL COMMENT '站点标题',
    `owner_name` VARCHAR(64) NOT NULL COMMENT '站点所有者',
    `hero_title` VARCHAR(160) NOT NULL COMMENT '首页主标题',
    `hero_subtitle` VARCHAR(512) NULL COMMENT '首页副标题',
    `owner_role` VARCHAR(128) NULL COMMENT '所有者身份说明',
    `contact_email` VARCHAR(128) NULL COMMENT '公开联系邮箱',
    `github_url` VARCHAR(255) NULL COMMENT 'GitHub 地址',
    `profile_visible` TINYINT NOT NULL DEFAULT 1 COMMENT '是否公开个人简介',
    `blog_visible` TINYINT NOT NULL DEFAULT 1 COMMENT '是否公开博客入口',
    `projects_visible` TINYINT NOT NULL DEFAULT 1 COMMENT '是否公开项目入口',
    `workspace_entry_visible` TINYINT NOT NULL DEFAULT 0 COMMENT '是否在首页显示工作台入口',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='站点公开配置';

INSERT INTO `site_config` (
    `id`,
    `site_title`,
    `owner_name`,
    `hero_title`,
    `hero_subtitle`,
    `owner_role`,
    `contact_email`,
    `github_url`,
    `profile_visible`,
    `blog_visible`,
    `projects_visible`,
    `workspace_entry_visible`
)
VALUES (
    1,
    'Simon Workspace',
    'Simon',
    '个人主页、博客和教学工作台',
    '记录教学、开发和项目实践；公开页面给访客阅读，工作台留给授权账号使用。',
    '软件教师 / 独立开发者',
    NULL,
    'https://github.com/simon-996',
    1,
    1,
    1,
    0
)
ON DUPLICATE KEY UPDATE
    `deleted` = 0;
