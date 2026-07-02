INSERT INTO `role` (`role_code`, `role_name`, `description`)
VALUES
    ('OWNER', '站长', '个人站点所有者，拥有全部权限'),
    ('ADMIN', '管理员', '可管理站点内容和工作台核心数据'),
    ('EDITOR', '编辑', '可管理公开内容'),
    ('VIEWER', '访客', '登录后可查看授权内容')
ON DUPLICATE KEY UPDATE
    `role_name` = VALUES(`role_name`),
    `description` = VALUES(`description`),
    `deleted` = 0;

INSERT INTO `permission` (`permission_code`, `permission_name`, `resource_type`, `description`)
VALUES
    ('workspace:view', '查看工作台', 'WORKSPACE', '进入工作台总览'),
    ('course:manage', '课程管理', 'WORKSPACE', '维护课程信息'),
    ('class:manage', '班级管理', 'WORKSPACE', '维护班级信息'),
    ('semester:manage', '学期管理', 'WORKSPACE', '维护学期和周历'),
    ('template:manage', '模板管理', 'WORKSPACE', '维护模板和字段'),
    ('file:manage', '文件中心', 'WORKSPACE', '管理文件资源'),
    ('generation:lesson', '教案生成', 'GENERATION', '创建教案预览和导出任务'),
    ('generation:calendar', '教学日历生成', 'GENERATION', '创建教学日历预览和导出任务'),
    ('generation:history', '生成记录', 'GENERATION', '查看生成记录'),
    ('generation:document:edit', '编辑生成文档', 'GENERATION', '编辑预览文档 JSON'),
    ('blog:manage', '博客管理', 'PUBLIC_CONTENT', '维护博客文章'),
    ('project:manage', '项目管理', 'PUBLIC_CONTENT', '维护项目展示'),
    ('site:config', '站点配置', 'SITE', '维护站点功能开关和公开配置'),
    ('user:manage', '用户管理', 'SECURITY', '维护用户账号'),
    ('role:manage', '角色管理', 'SECURITY', '维护角色和权限')
ON DUPLICATE KEY UPDATE
    `permission_name` = VALUES(`permission_name`),
    `resource_type` = VALUES(`resource_type`),
    `description` = VALUES(`description`),
    `deleted` = 0;

INSERT INTO `role_permission` (`role_id`, `permission_id`)
SELECT r.id, p.id
FROM `role` r
CROSS JOIN `permission` p
WHERE r.role_code = 'OWNER'
ON DUPLICATE KEY UPDATE
    `deleted` = 0;

INSERT INTO `role_permission` (`role_id`, `permission_id`)
SELECT r.id, p.id
FROM `role` r
JOIN `permission` p ON p.permission_code IN (
    'workspace:view',
    'course:manage',
    'class:manage',
    'semester:manage',
    'template:manage',
    'file:manage',
    'generation:lesson',
    'generation:calendar',
    'generation:history',
    'generation:document:edit',
    'blog:manage',
    'project:manage',
    'site:config'
)
WHERE r.role_code = 'ADMIN'
ON DUPLICATE KEY UPDATE
    `deleted` = 0;

INSERT INTO `role_permission` (`role_id`, `permission_id`)
SELECT r.id, p.id
FROM `role` r
JOIN `permission` p ON p.permission_code IN ('blog:manage', 'project:manage')
WHERE r.role_code = 'EDITOR'
ON DUPLICATE KEY UPDATE
    `deleted` = 0;
