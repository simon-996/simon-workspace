CREATE TABLE IF NOT EXISTS `blog_category` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(64) NOT NULL,
    `slug` VARCHAR(128) NOT NULL,
    `description` VARCHAR(255) NULL,
    `sort_order` INT NOT NULL DEFAULT 100,
    `status` VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_blog_category_slug` (`slug`),
    KEY `idx_blog_category_status` (`status`, `sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Blog category';

CREATE TABLE IF NOT EXISTS `blog_tag` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(64) NOT NULL,
    `slug` VARCHAR(128) NOT NULL,
    `usage_count` BIGINT NOT NULL DEFAULT 0,
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_blog_tag_slug` (`slug`),
    KEY `idx_blog_tag_usage_count` (`usage_count`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Blog tag';

CREATE TABLE IF NOT EXISTS `blog_post` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `author_user_id` BIGINT NOT NULL,
    `category_id` BIGINT NULL,
    `title` VARCHAR(160) NOT NULL,
    `slug` VARCHAR(180) NOT NULL,
    `summary` VARCHAR(500) NULL,
    `content_md` MEDIUMTEXT NOT NULL,
    `status` VARCHAR(32) NOT NULL DEFAULT 'DRAFT',
    `view_count` BIGINT NOT NULL DEFAULT 0,
    `comment_count` BIGINT NOT NULL DEFAULT 0,
    `published_time` DATETIME NULL,
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_blog_post_slug` (`slug`),
    KEY `idx_blog_post_author` (`author_user_id`),
    KEY `idx_blog_post_category` (`category_id`),
    KEY `idx_blog_post_status_published` (`status`, `published_time`),
    CONSTRAINT `fk_blog_post_author` FOREIGN KEY (`author_user_id`) REFERENCES `user` (`id`),
    CONSTRAINT `fk_blog_post_category` FOREIGN KEY (`category_id`) REFERENCES `blog_category` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Blog post';

CREATE TABLE IF NOT EXISTS `blog_post_tag` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `post_id` BIGINT NOT NULL,
    `tag_id` BIGINT NOT NULL,
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_blog_post_tag` (`post_id`, `tag_id`),
    KEY `idx_blog_post_tag_tag` (`tag_id`),
    CONSTRAINT `fk_blog_post_tag_post` FOREIGN KEY (`post_id`) REFERENCES `blog_post` (`id`),
    CONSTRAINT `fk_blog_post_tag_tag` FOREIGN KEY (`tag_id`) REFERENCES `blog_tag` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Blog post tag';

CREATE TABLE IF NOT EXISTS `blog_comment` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `post_id` BIGINT NOT NULL,
    `author_user_id` BIGINT NOT NULL,
    `parent_id` BIGINT NULL,
    `content` VARCHAR(2000) NOT NULL,
    `status` VARCHAR(32) NOT NULL DEFAULT 'VISIBLE',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_blog_comment_post` (`post_id`, `created_time`),
    KEY `idx_blog_comment_author` (`author_user_id`),
    CONSTRAINT `fk_blog_comment_post` FOREIGN KEY (`post_id`) REFERENCES `blog_post` (`id`),
    CONSTRAINT `fk_blog_comment_author` FOREIGN KEY (`author_user_id`) REFERENCES `user` (`id`),
    CONSTRAINT `fk_blog_comment_parent` FOREIGN KEY (`parent_id`) REFERENCES `blog_comment` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Blog comment';

INSERT INTO `permission` (`permission_code`, `permission_name`, `resource_type`, `description`)
VALUES
    ('blog:post:create', 'Create blog post', 'PUBLIC_CONTENT', 'Create blog posts'),
    ('blog:post:update', 'Update blog post', 'PUBLIC_CONTENT', 'Update own blog posts'),
    ('blog:post:delete', 'Delete blog post', 'PUBLIC_CONTENT', 'Delete own blog posts'),
    ('blog:comment:create', 'Create blog comment', 'PUBLIC_CONTENT', 'Create blog comments'),
    ('blog:category:manage', 'Manage blog category', 'PUBLIC_CONTENT', 'Manage blog categories'),
    ('blog:moderate', 'Moderate blog content', 'PUBLIC_CONTENT', 'Moderate blog posts and comments')
ON DUPLICATE KEY UPDATE
    `permission_name` = VALUES(`permission_name`),
    `resource_type` = VALUES(`resource_type`),
    `description` = VALUES(`description`),
    `deleted` = 0;

INSERT INTO `role_permission` (`role_id`, `permission_id`)
SELECT r.id, p.id
FROM `role` r
JOIN `permission` p ON p.permission_code IN (
    'blog:post:create',
    'blog:post:update',
    'blog:post:delete',
    'blog:comment:create',
    'blog:category:manage',
    'blog:moderate'
)
WHERE r.role_code IN ('OWNER', 'ADMIN')
ON DUPLICATE KEY UPDATE `deleted` = 0;

INSERT INTO `role_permission` (`role_id`, `permission_id`)
SELECT r.id, p.id
FROM `role` r
JOIN `permission` p ON p.permission_code IN (
    'blog:post:create',
    'blog:post:update',
    'blog:post:delete',
    'blog:comment:create'
)
WHERE r.role_code = 'EDITOR'
ON DUPLICATE KEY UPDATE `deleted` = 0;
