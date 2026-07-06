ALTER TABLE `site_config`
    ADD COLUMN `course_visible` TINYINT NOT NULL DEFAULT 1 COMMENT 'Whether to show the public course entry' AFTER `blog_visible`;
