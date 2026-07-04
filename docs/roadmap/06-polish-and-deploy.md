# 06 Polish and Deploy 阶段

最后更新：2026-07-04

## 阶段目标

在核心功能可用后，完善视觉一致性、移动端体验、安全边界、日志排查、Docker/Jenkins 部署、备份恢复和运维文档。

## 已完成

### 视觉与体验

- [x] 首页改为个人主页风格。
- [x] 首页支持滚动进度动画。
- [x] 首页加载配置前使用骨架屏。
- [x] 登录页从深色后台风格统一为首页同系浅色风格。
- [x] 工作台外壳统一为浅色、简洁、低文案风格。
- [x] 管理页统一指标条、工具条、表格、弹窗、空状态、错误状态、骨架屏。
- [x] Naive UI 全局主题覆盖已接入。
- [x] 设计风格基准已写入 [docs/design-style.md](../design-style.md)。
- [x] 中文、英文、泰文文案已收短。

### 部署方向

- [x] 前端容器外端口规划为 `9526`。
- [x] 后端容器外端口规划为 `9527`。
- [x] MySQL 使用远程连接。
- [x] Redis 使用远程连接。
- [x] Compose 不再部署 MySQL、Redis、MinIO。
- [x] 前端支持配置后端访问地址。
- [x] 文件存储支持通过环境变量配置 LOCAL、腾讯 COS、阿里 OSS、Cloudflare R2。

## 待完成 Todo

### 视觉复查

- [ ] 用浏览器检查首页 390px、768px、1440px。
- [ ] 用浏览器检查登录页 390px、768px、1440px。
- [ ] 用浏览器检查工作台总览 390px、768px、1440px。
- [ ] 用浏览器检查课程、班级、学期、模板、文件、记录、权限、站点配置页面。
- [ ] 为图标按钮增加 tooltip。
- [ ] 检查所有按钮文字在中英泰三种语言下不溢出。
- [ ] 检查移动端底部导航在权限较多时可横向滚动。

### 移动端优化

- [x] 工作台移动端底部导航已存在。
- [ ] 将复杂表格在手机上优化为列表或摘要卡片。
- [ ] 优化模板字段编辑在手机上的布局。
- [ ] 优化学期周历在手机上的布局。
- [ ] 确认 375px 宽度核心流程无明显错位。

### 终端增强

- [ ] 支持命令历史上下切换。
- [ ] 支持基础自动补全。
- [ ] 根据登录状态展示不同 help。
- [ ] 根据当前页面推荐命令。
- [ ] 支持移动端全屏终端。
- [ ] 明确终端不是 Shell，禁止任意脚本执行。

### 安全与日志

- [ ] 所有工作台接口必须登录。
- [ ] 文件下载必须校验 owner 或权限。
- [ ] 生成任务只能由创建人或授权角色查看。
- [ ] 管理接口必须校验管理员权限。
- [ ] 登录失败增加限流策略。
- [ ] 下载增加频率限制。
- [ ] 导出接口防重复提交。
- [ ] 日志不输出密码、Token、AI API Key。
- [ ] 日志包含可排查的任务 ID 或 traceId。
- [ ] 编写常见故障排查文档。

### Docker 与部署

- [ ] API Dockerfile 在服务器可构建。
- [ ] Web Dockerfile 在服务器可构建。
- [ ] `docker compose --env-file deploy/.env -f deploy/docker-compose.yml config` 通过。
- [ ] `docker compose --env-file deploy/.env -f deploy/docker-compose.yml up -d --build` 通过。
- [ ] API 启动后健康检查可用。
- [ ] Web 启动后首页可访问。
- [ ] Nginx 前后端跨域或反代策略确认。
- [ ] 域名和子域名解析完成。

### Jenkins

- [x] Jenkinsfile 有 Checkout 阶段。
- [x] Jenkinsfile 有 API Test and Package 阶段。
- [x] Jenkinsfile 有 Web Install and Build 阶段。
- [x] Jenkinsfile 有 Docker Build 阶段。
- [ ] Jenkinsfile 增加 Push Images 阶段。
- [ ] Jenkinsfile 增加 Deploy 阶段。
- [ ] Jenkinsfile 增加 Smoke Test 阶段。
- [ ] 部署失败时停止发布并保留日志。

### 备份与恢复

- [ ] 编写 MySQL 备份步骤。
- [ ] 编写 MySQL 恢复步骤。
- [ ] 编写本地文件备份步骤。
- [ ] 编写本地文件恢复步骤。
- [ ] 说明 `deploy/.env` 保管方式。
- [ ] 说明服务器目录结构。
- [ ] 说明版本回滚步骤。

## 验收 Todo

- [ ] 首页、登录页、工作台视觉一致。
- [ ] 手机和电脑端核心页面可用。
- [ ] 登录和权限边界有效。
- [ ] 生成文件不能被未授权访问。
- [ ] Jenkins 能完成构建和部署。
- [ ] 部署后 API 健康检查通过。
- [ ] 部署后 Web 首页可访问。
- [x] `npm run build --prefix simon-workspace-web` 通过。2026-07-04 已验证。
- [x] `mvn -f simon-workspace-api/pom.xml test` 通过。2026-07-04 已验证。
- [ ] Docker Compose 在服务器通过。

## 进度记录

```text
2026-07-04:
- 全站界面已向首页风格统一。
- 文档已恢复为可读 UTF-8，并新增设计风格基准。
- 部署闭环、日志、安全加固和移动端细节仍需继续。
```
