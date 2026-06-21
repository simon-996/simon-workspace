CREATE TABLE IF NOT EXISTS `semester` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '学期 ID',
    `academic_year` VARCHAR(32) NOT NULL COMMENT '学年',
    `semester_name` VARCHAR(32) NOT NULL COMMENT '学期名称',
    `start_date` DATE NOT NULL COMMENT '开学日期',
    `end_date` DATE NULL COMMENT '结束日期',
    `total_weeks` INT NOT NULL COMMENT '总周数',
    `exam_week` INT NULL COMMENT '考试周',
    `holiday_json` JSON NULL COMMENT '节假日配置',
    `adjustment_json` JSON NULL COMMENT '调课配置',
    `remark` VARCHAR(500) NULL COMMENT '备注',
    `status` VARCHAR(32) NOT NULL DEFAULT 'PLANNED' COMMENT 'PLANNED / ACTIVE / CLOSED',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    KEY `idx_semester_year_name` (`academic_year`, `semester_name`),
    KEY `idx_semester_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学期';

CREATE TABLE IF NOT EXISTS `semester_calendar` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '日历 ID',
    `semester_id` BIGINT NOT NULL COMMENT '学期 ID',
    `week_no` INT NOT NULL COMMENT '第几周',
    `start_date` DATE NOT NULL COMMENT '本周开始日期',
    `end_date` DATE NOT NULL COMMENT '本周结束日期',
    `is_exam_week` TINYINT NOT NULL DEFAULT 0 COMMENT '是否考试周',
    `is_holiday` TINYINT NOT NULL DEFAULT 0 COMMENT '是否含节假日',
    `holiday_note` VARCHAR(255) NULL COMMENT '节假日说明',
    `adjustment_note` VARCHAR(255) NULL COMMENT '调课说明',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    KEY `idx_semester_calendar_week` (`semester_id`, `week_no`),
    KEY `idx_semester_calendar_semester` (`semester_id`),
    CONSTRAINT `fk_semester_calendar_semester` FOREIGN KEY (`semester_id`) REFERENCES `semester` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学期周历';
