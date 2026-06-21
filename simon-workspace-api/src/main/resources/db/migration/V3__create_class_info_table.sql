CREATE TABLE IF NOT EXISTS `class_info` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '班级 ID',
    `class_name` VARCHAR(128) NOT NULL COMMENT '班级名称',
    `major` VARCHAR(128) NULL COMMENT '专业',
    `grade` VARCHAR(32) NULL COMMENT '年级',
    `student_count` INT NULL COMMENT '学生人数',
    `counselor` VARCHAR(64) NULL COMMENT '辅导员',
    `remark` VARCHAR(500) NULL COMMENT '备注',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    KEY `idx_class_info_name` (`class_name`),
    KEY `idx_class_info_grade` (`grade`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='班级信息';
