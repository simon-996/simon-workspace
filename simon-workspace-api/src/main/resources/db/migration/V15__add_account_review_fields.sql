ALTER TABLE `user`
    ADD COLUMN `reviewed_time` DATETIME NULL COMMENT 'Account review time' AFTER `last_login_time`,
    ADD COLUMN `review_remark` VARCHAR(255) NULL COMMENT 'Account review remark' AFTER `reviewed_time`;

CREATE INDEX `idx_user_reviewed_time` ON `user` (`reviewed_time`);
