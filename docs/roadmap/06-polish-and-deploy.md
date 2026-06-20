# 06 Polish and Deploy 阶段文档

## 阶段目标

在核心业务跑通后，完善交互体验、移动端适配、安全边界、日志监控和 Jenkins + Docker 部署闭环，让项目可以长期自用和持续迭代。

## 功能范围

```text
首页动效增强
终端体验增强
移动端工作台优化
权限加固
接口限流
文件访问加固
日志与错误追踪
Docker 镜像构建
Jenkins 自动部署
备份与恢复策略
基础运维文档
```

## 不做什么

```text
不做多租户商业化
不做复杂微服务拆分
不做 Kubernetes
不做完整可观测性平台
不做复杂 AI Agent 编排
```

## 前端优化任务

### 1. 首页动效

使用：

```text
CSS transition
GSAP
ScrollTrigger
少量 Three.js 或 TresJS 后置
```

动效原则：

```text
首页可以有记忆点
工作台保持克制
移动端减少复杂动画
动效不能影响文字可读性
```

### 2. 终端增强

增强项：

```text
命令历史上下切换
基础自动补全
常用命令按钮
根据登录状态展示 help
根据当前页面推荐命令
移动端全屏终端
```

仍然不做：

```text
真实 Shell
任意脚本执行
复杂管道
绕过图形界面的后台操作
```

### 3. 移动端工作台

优化：

```text
底部 Tab
常用功能卡片
分步生成表单
文件下载入口
生成记录查看
全屏终端
```

移动端不强求：

```text
复杂模板编辑
批量生成
复杂表格维护
```

## 后端加固任务

### 1. 权限

确保：

```text
工作台接口必须登录
文件下载必须校验 owner 或权限
AI 任务只能由创建人查看
管理接口需要管理员权限
```

### 2. 限流和防重复提交

Redis 场景：

```text
登录失败限制
AI 生成接口限流
文件导出防重复提交
下载频率限制
任务状态缓存
```

### 3. 日志

记录：

```text
登录日志
生成任务日志
AI 调用日志
文件下载日志
异常日志
```

日志中禁止输出：

```text
密码
Token
AI API Key
完整敏感配置
```

## 部署任务

### 1. Docker 镜像

需要验证：

```text
API Dockerfile 可构建
Web Dockerfile 可构建
镜像启动后健康检查可用
```

### 2. Docker Compose

服务：

```text
simon-workspace-api
simon-workspace-web
mysql
redis
minio
```

命令：

```bash
docker compose --env-file deploy/.env -f deploy/docker-compose.yml up -d --build
```

### 3. Jenkins

流水线阶段：

```text
Checkout
API Test and Package
Web Install and Build
Docker Build
Push Images
Deploy
Smoke Test
```

部署阶段建议：

```text
通过 SSH 登录服务器
拉取最新镜像或代码
执行 docker compose up -d
执行健康检查
失败时停止发布并保留日志
```

### 4. 备份

需要文档化：

```text
MySQL 备份
MinIO / 本地文件备份
deploy/.env 保管
恢复步骤
```

## 测试与验收

本地：

```text
mvn -f simon-workspace-api/pom.xml test
npm run build --prefix simon-workspace-web
```

部署环境：

```text
docker compose --env-file deploy/.env -f deploy/docker-compose.yml config
docker compose --env-file deploy/.env -f deploy/docker-compose.yml up -d --build
curl http://localhost:8080/api/health
curl http://localhost
```

人工验收：

```text
首页动效流畅且不遮挡内容
移动端核心流程可用
终端增强功能可用
登录和权限边界有效
生成文件不能被未授权访问
Jenkins 能完成构建和部署
部署后健康检查通过
```

## 推荐提交

```text
feat(web): polish home motion
feat(web): enhance terminal interactions
feat(web): optimize mobile workspace
feat(api): harden auth and file permissions
feat(api): add rate limits and operation logs
chore(deploy): complete Jenkins deployment stage
chore(deploy): add backup and restore docs
docs: update deployment runbook
```

## 阶段完成标准

```text
项目能通过 Jenkins 部署到服务器
Docker Compose 服务稳定运行
公开站点和工作台可正常访问
核心接口有权限和限流保护
日志足够排查生成失败和部署问题
移动端具备基本可用体验
测试、构建、部署验收通过
```

## 阶段执行 Todo

当前状态：未开始。已有 Docker/Jenkins 雏形，但未形成部署闭环。最后核查时间：2026-06-20。

### 前端体验优化

- [ ] 首页动效方案评审，确认只增强公开页、不干扰工作台
- [ ] 使用 CSS transition 增强基础交互
- [ ] 使用 GSAP 增强首页关键动效
- [ ] 评估是否需要 ScrollTrigger
- [ ] 移动端降低或禁用复杂动效
- [ ] 检查动效不遮挡文字
- [ ] 检查动效不影响首屏加载
- [ ] 工作台保持克制、稳定、可扫描

### 终端增强

- [ ] 支持命令历史上下切换
- [ ] 支持基础自动补全
- [ ] 增加常用命令按钮
- [ ] 根据登录状态展示不同 help
- [ ] 根据当前页面推荐命令
- [ ] 支持移动端全屏终端
- [ ] 明确终端不是真实 Shell
- [ ] 禁止任意脚本执行
- [ ] 禁止通过终端绕过图形界面权限

### 移动端工作台

- [ ] 增加移动端底部 Tab
- [ ] 优化常用功能卡片
- [ ] 优化课程、班级、学期页面的移动端列表
- [ ] 优化分步生成表单
- [ ] 优化文件下载入口
- [ ] 优化生成记录查看
- [ ] 优化全屏终端
- [ ] 确认移动端不强求复杂模板编辑
- [ ] 375px 宽度核心流程无明显错位

### 权限与安全加固

- [ ] 所有工作台接口必须登录
- [ ] 文件下载必须校验 owner 或权限
- [ ] AI 任务只能由创建人查看
- [ ] 管理接口需要管理员权限
- [ ] 登录失败限制
- [ ] AI 生成接口限流
- [ ] 文件导出防重复提交
- [ ] 下载频率限制
- [ ] 任务状态缓存策略明确
- [ ] Token、密码、AI API Key 不写入日志

### 日志与排查

- [ ] 记录登录日志
- [ ] 记录生成任务日志
- [ ] 记录 AI 调用日志
- [ ] 记录文件下载日志
- [ ] 记录异常日志
- [ ] 日志包含可排查任务 ID 或 traceId
- [ ] 日志不包含完整敏感配置
- [ ] 增加常见故障排查文档

### Docker 与 Compose

- [x] 创建 API Dockerfile 初版
- [x] 创建 Web Dockerfile 初版
- [x] 创建 Docker Compose 初版
- [ ] API Dockerfile 可构建
- [ ] Web Dockerfile 可构建
- [ ] API 镜像启动后健康检查可用
- [ ] Web 镜像启动后首页可访问
- [ ] `docker compose --env-file deploy/.env -f deploy/docker-compose.yml config` 通过
- [ ] `docker compose --env-file deploy/.env -f deploy/docker-compose.yml up -d --build` 通过
- [ ] Compose 中 MySQL 健康检查稳定
- [ ] Compose 中 Redis 健康检查稳定
- [ ] Compose 中 MinIO 可访问

### Jenkins 部署闭环

- [x] Jenkinsfile 包含 Checkout 阶段
- [x] Jenkinsfile 包含 API Test and Package 阶段
- [x] Jenkinsfile 包含 Web Install and Build 阶段
- [x] Jenkinsfile 包含 Docker Build 阶段
- [ ] Jenkinsfile 增加 Push Images 阶段
- [ ] Jenkinsfile 增加 Deploy 阶段
- [ ] Jenkinsfile 增加 Smoke Test 阶段
- [ ] 部署阶段通过 SSH 或其他明确方式连接服务器
- [ ] 部署阶段执行 docker compose up
- [ ] 部署后检查 API health
- [ ] 部署后检查 Web 首页
- [ ] 失败时停止发布并保留日志

### 备份与恢复

- [ ] 编写 MySQL 备份步骤
- [ ] 编写 MySQL 恢复步骤
- [ ] 编写 MinIO 或本地文件备份步骤
- [ ] 编写 MinIO 或本地文件恢复步骤
- [ ] 说明 `deploy/.env` 保管方式
- [ ] 说明服务器目录结构
- [ ] 说明版本回滚步骤
- [ ] 说明定期备份频率

## 阶段验收 Todo

- [ ] 首页动效流畅且不遮挡内容
- [ ] 移动端核心流程可用
- [ ] 终端增强功能可用
- [ ] 登录和权限边界有效
- [ ] 生成文件不能被未授权访问
- [ ] Jenkins 能完成构建和部署
- [ ] 部署后 API 健康检查通过
- [ ] 部署后 Web 首页可访问
- [ ] `mvn -f simon-workspace-api/pom.xml test` 通过
- [ ] `npm run build --prefix simon-workspace-web` 通过
- [ ] `docker compose --env-file deploy/.env -f deploy/docker-compose.yml config` 通过
- [ ] `docker compose --env-file deploy/.env -f deploy/docker-compose.yml up -d --build` 通过

## 进度记录

```text
2026-06-20:
- 已有 Dockerfile、Docker Compose、Jenkinsfile 初版
- Jenkins 部署阶段仍是 placeholder
- 当前环境没有 docker 命令，尚未验证 Compose 和镜像构建
```
