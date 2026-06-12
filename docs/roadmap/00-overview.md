# Simon Workspace 阶段路线图总览

## 文档目的

本目录是 Simon Workspace 后续开发的任务依据。主规划文档负责描述产品愿景和完整能力，本路线图负责把愿景拆成可执行、可验证、可提交的阶段任务。

后续开发时，优先遵循本目录中的阶段文档：

```text
docs/roadmap/00-overview.md
docs/roadmap/01-foundation.md
docs/roadmap/02-public-site.md
docs/roadmap/03-workspace-core.md
docs/roadmap/04-document-generation.md
docs/roadmap/05-ai-integration.md
docs/roadmap/06-polish-and-deploy.md
```

## 项目目标

Simon Workspace 是一个个人网站与 AI 教学工作台结合的全栈项目。

它需要同时承担三个身份：

```text
个人品牌网站
技术与教学博客
AI 教学资料生成工作台
```

第一阶段优先服务自己，重点是把教师日常工作流跑通：

```text
维护课程资料
维护学期日历
上传 Word / Excel 模板
生成教案
生成教学日历
预览和下载生成文件
沉淀生成记录
```

## 仓库结构

```text
simon-workspace/
├── simon-workspace-api/      Spring Boot API
├── simon-workspace-web/      Vue 3 frontend
├── deploy/                   Docker Compose, Nginx, database config
├── docs/roadmap/             阶段开发文档
├── Jenkinsfile               Jenkins pipeline
├── README.md
└── simon-workspace-doc.md    产品与架构规划文档
```

## 阶段划分

| 阶段 | 名称 | 目标 |
| --- | --- | --- |
| 01 | Foundation | 建立可持续开发基础，包括规范、配置、数据库迁移、统一响应、错误处理、CI 骨架 |
| 02 | Public Site | 完成公开首页、终端入口、博客和项目展示的基础浏览体验 |
| 03 | Workspace Core | 完成登录、工作台布局、课程、班级、学期、模板、文件中心和生成记录 |
| 04 | Document Generation | 完成不依赖 AI 的模板填充、教案导出、教学日历导出和文件下载 |
| 05 | AI Integration | 接入 AI，生成结构化 JSON，支持预览编辑后导出 |
| 06 | Polish and Deploy | 完成动效、移动端、权限加固、监控日志、Jenkins + Docker 部署闭环 |

## 开发原则

```text
先让功能真实可用，再做高级动效
先做图形界面完整流程，再做终端快捷体验
先做模板填充和文件导出，再接 AI
先做桌面端生产力体验，再优化移动端轻量体验
先单体架构跑通，再考虑服务拆分
每个阶段都必须有可验收结果
每个阶段都必须保持 API 测试和 Web 构建通过
```

## 通用验收标准

每个阶段完成时必须满足：

```text
后端测试通过：mvn -f simon-workspace-api/pom.xml test
前端构建通过：npm run build --prefix simon-workspace-web
Git 工作区干净
相关 README 或 roadmap 文档已同步
关键功能有明确页面入口或接口入口
涉及用户数据、文件、AI 结果的功能有权限边界
```

## 推荐提交粒度

每个阶段拆成多个小提交：

```text
chore: 配置、依赖、工程规范
feat(api): 后端实体、接口、服务
feat(web): 前端页面、组件、状态管理
test(api): 后端测试
docs: 文档同步
chore(deploy): Docker / Jenkins / Nginx 调整
```

避免一个阶段只做一个巨大提交。每个提交应能说清楚“完成了一个可理解的变化”。

## 阶段依赖关系

```text
01 Foundation
  ↓
02 Public Site
  ↓
03 Workspace Core
  ↓
04 Document Generation
  ↓
05 AI Integration
  ↓
06 Polish and Deploy
```

允许局部穿插：

```text
公开站点的博客页面可以在工作台之前做
部署配置可以随每个阶段逐步完善
移动端基础适配可以从第 02 阶段开始保持
```

不建议提前做：

```text
复杂 AI Agent
课程资料 RAG
多用户协作
复杂权限后台
自动解析 PDF / Word 大纲
模板字段自动识别
```

这些能力放在第一版稳定后再规划。
