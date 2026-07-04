# 01 Foundation 阶段

最后更新：2026-07-04

## 阶段目标

建立项目可持续开发的基础：前后端工程、数据库迁移、统一 API 响应、环境配置、CI/CD 雏形和基础文档。

## 已完成

- [x] 创建 Spring Boot API 工程入口。
- [x] 创建 Vue 3 + Vite + TypeScript 前端工程。
- [x] 引入 Vue Router、Pinia、Naive UI、Tailwind CSS。
- [x] 创建 `/api/health` 健康检查接口。
- [x] 创建基础 `ApiResponse<T>`。
- [x] 引入 MySQL Driver、Spring JDBC、Flyway。
- [x] 创建初始数据库迁移。
- [x] 创建用户、角色、权限、用户角色、角色权限、登录日志等基础表。
- [x] 创建 Axios API 客户端。
- [x] 创建基础路由 `/`、`/login`、`/workspace`。
- [x] 创建 API / Web Dockerfile。
- [x] 创建 Docker Compose 和 Jenkinsfile 初版。
- [x] 拆分本地 dev 与线上 prod 配置。
- [x] 前端支持 `VITE_API_BASE_URL` 配置。

## 待完成 Todo

### 后端基础

- [ ] 完善统一异常处理：业务异常、参数异常、未登录、无权限、资源不存在、未知异常。
- [ ] 为错误响应补充稳定错误码。
- [ ] 评估是否增加 `traceId`，用于排查日志。
- [ ] 增加基础请求日志过滤器，避免记录敏感信息。
- [ ] 明确 `common`、`module`、`infrastructure` 包职责。

### 数据库与 Redis

- [ ] 使用真实 MySQL 或集成测试验证 Flyway 迁移。
- [ ] 增加 Redis 连接配置验证。
- [ ] 明确第一版是否使用 MyBatis-Plus；如果暂不使用，在文档中说明原因。
- [ ] 检查所有 SQL 文件编码为 UTF-8。

### 前端基础

- [x] 注册 Naive UI `NConfigProvider`、`NMessageProvider`。
- [x] 增加全局语言切换。
- [x] 增加全局 Naive UI 主题覆盖。
- [x] 建立全局视觉变量和通用工作台样式。
- [ ] 为 API 客户端补充统一登录失效处理。
- [ ] 为 API 客户端补充统一错误消息策略。
- [ ] 增加共享类型目录或明确当前类型随 API 模块维护。

### 部署基础

- [x] 前端容器外暴露端口规划为 `9526`。
- [x] 后端容器外暴露端口规划为 `9527`。
- [x] MySQL、Redis 改为远程连接，不再由 Compose 部署。
- [x] 删除 MinIO Compose 服务依赖。
- [ ] 在服务器上验证 Dockerfile 可完整构建。
- [ ] 在服务器上验证 `docker compose config`。
- [ ] 为 API / Web 容器补充健康检查。
- [ ] 完成 Jenkins 部署阶段。

## 验收 Todo

- [ ] `mvn -f simon-workspace-api/pom.xml test` 通过。
- [x] `npm run build --prefix simon-workspace-web` 通过。2026-07-04 已验证。
- [ ] `docker compose --env-file deploy/.env -f deploy/docker-compose.yml config` 通过。
- [ ] README 中的本地启动、部署、环境变量说明与代码一致。

## 进度记录

```text
2026-07-04:
- 前端构建通过。
- 全局主题、语言切换和视觉变量已统一。
- 文档从乱码状态整理为 UTF-8 可维护版本。
```
