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

## 进度维护方式

本目录同时承担两类信息：

```text
阶段说明：每个阶段要解决什么问题、边界在哪里
执行 Todo：每个阶段当前还有哪些具体任务没有完成
```

维护原则：

```text
不要另建一套重复路线图
每个阶段文件末尾维护 Todo
完成一项就把 - [ ] 改为 - [x]
只把已经落地、验证或明确提交的内容标记为完成
每次阶段收尾都同步更新阶段完成记录
```

推荐使用方式：

```text
开始一个阶段前：阅读对应阶段文档的目标、范围、Todo
开发过程中：每完成一个小任务就勾选对应 Todo
阶段收尾前：逐项检查验收 Todo
阶段完成后：在阶段文件的进度记录里补充日期、命令和结果
```

## 当前进度快照

最后核查时间：2026-06-20。

| 阶段 | 当前判断 | 说明 |
| --- | --- | --- |
| 01 Foundation | 进行中 | 已有前后端脚手架、健康检查、基础响应、Flyway 初始迁移、Docker/Jenkins 初版；统一异常处理、真实迁移验证、Redis 配置、API 拦截器仍未完成 |
| 02 Public Site | 早期进行中 | 已有首页和静态终端展示；博客、项目页、真实终端命令、公开内容接口仍未完成 |
| 03 Workspace Core | 未开始 | 只有工作台占位页，没有登录、课程、班级、学期、模板、文件、历史等业务能力 |
| 04 Document Generation | 未开始 | 尚未实现模板填充、生成任务、Word/Excel 导出 |
| 05 AI Integration | 未开始 | 尚未接入 AI 服务、Prompt、JSON 校验和 AI 生成流程 |
| 06 Polish and Deploy | 未开始 | 已有部署配置雏形，但未完成部署闭环、移动端优化、安全加固、日志监控 |

## 已验证命令记录

2026-06-20 已验证：

```text
mvn -f simon-workspace-api/pom.xml test
结果：通过，2 个测试成功

npm ci --prefix simon-workspace-web
结果：通过，但 npm audit 报告 1 个 high severity vulnerability

npm run build --prefix simon-workspace-web
结果：通过，生成 dist 产物
```

2026-06-20 未验证：

```text
docker compose --env-file deploy/.env.example -f deploy/docker-compose.yml config
原因：当前环境未找到 docker 命令
```
