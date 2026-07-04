# Simon Workspace 产品与架构总纲

最后更新：2026-07-04

## 1. 产品定位

Simon Workspace 是一个个人主页与教学工作台结合的全栈项目。

对外，它是陈希萌的个人主页，用简洁的方式展示个人介绍、技术方向、博客和项目。

对内，它是授权工作台，用来维护课程、班级、学期、模板、文件、生成记录、权限和站点配置，后续继续扩展教案、教学日历和 AI 辅助生成。

一句话：

> 一个面向访客的简洁个人主页，加一个面向自己的教学资料生产工作台。

## 2. 目标用户

第一阶段主要服务自己：

- 大学教师
- Web / 小程序 / Flutter 开发者
- 需要长期维护课程资料、模板和教学文件的人

后续可扩展给其他教师或课程负责人使用，但当前不做复杂多租户。

## 3. 设计原则

视觉风格以 [docs/design-style.md](docs/design-style.md) 为准。

核心原则：

- 首页像个人主页，不像后台系统。
- 工作台像生产力工具，不像营销页。
- 访客只看到公开内容。
- 授权账号才看到工作台能力。
- 角色和权限控制页面入口与接口访问。
- 复杂能力必须有图形界面，终端只作为快捷入口。

## 4. 系统结构

```text
Public Site
├── 首页
├── 个人介绍
├── 博客
└── 项目展示

Workspace
├── 总览
├── 课程管理
├── 班级管理
├── 学期管理
├── 模板管理
├── 文件中心
├── 生成记录
├── 权限管理
└── 站点配置
```

后续新增：

```text
Document Generation
├── 教案生成
├── 教学日历生成
├── 预览编辑
├── Word 导出
└── Excel 导出

AI Integration
├── Prompt 构建
├── AI JSON 生成
├── JSON 校验
├── 预览编辑
└── 复用导出流程
```

## 5. 技术栈

前端：

- Vue 3
- Vite
- TypeScript
- Vue Router
- Pinia
- Naive UI
- Tailwind CSS
- vue-i18n

后端：

- Spring Boot
- MySQL
- Redis
- Flyway
- Maven

部署：

- Docker
- Docker Compose
- Nginx
- Jenkins

## 6. 权限模型

采用角色 + 权限模式。

```text
User N - N Role
Role N - N Permission
```

当前方向：

- 访客：只能访问公开页面。
- VIEWER：可查看授权资源。
- EDITOR：可维护课程、班级、学期、模板、文件和生成记录。
- ADMIN：可管理站点配置和部分系统能力。
- OWNER：最高权限，不能删除最后一个 OWNER。

前端按权限控制导航和路由；后端按权限控制接口。

## 7. 数据模块

当前已落地或规划中的核心表：

```text
user
role
permission
user_role
role_permission
login_log
site_config
course
class_info
semester
semester_calendar
template_file
template_field
file_resource
generate_task
document_data
generate_result
```

博客和项目展示后续补充：

```text
article
article_category
article_tag
project
```

## 8. 文档生成策略

生成流程分两层：

1. 生成结构化 JSON。
2. 用程序把 JSON 填入 Word / Excel 模板。

原则：

- 不让 AI 直接生成最终文件。
- 用户可以预览和编辑 JSON 内容。
- 确认后再导出文件。
- 文件统一进入文件中心。
- 生成任务统一进入生成记录。

## 9. 终端定位

终端是网站特色入口，不是真实 Shell。

它可以：

- 跳转页面
- 展示帮助
- 根据权限打开工作台功能
- 后续预填生成表单

它不可以：

- 执行任意系统命令
- 绕过图形界面确认
- 绕过权限
- 暴露服务器路径、Token 或敏感配置

## 10. 阶段路线

详细 Todo 维护在 `docs/roadmap`：

- [00 总览](docs/roadmap/00-overview.md)
- [01 Foundation](docs/roadmap/01-foundation.md)
- [02 Public Site](docs/roadmap/02-public-site.md)
- [03 Workspace Core](docs/roadmap/03-workspace-core.md)
- [04 Document Generation](docs/roadmap/04-document-generation.md)
- [05 AI Integration](docs/roadmap/05-ai-integration.md)
- [06 Polish and Deploy](docs/roadmap/06-polish-and-deploy.md)

当前建议优先完成 04 阶段：教案/教学日历生成向导和最终导出闭环。
