# 01 Foundation 阶段文档

## 阶段目标

建立后续开发所需的基础工程能力，让前后端、数据库、部署和规范都有清晰边界。

本阶段不是做完整业务功能，而是让项目具备稳定扩展的底座。

## 功能范围

```text
后端基础分层
统一响应结构
统一异常处理
参数校验
数据库连接
Redis 连接
数据库迁移工具
基础用户表结构
基础配置文件
前端路由布局
API 客户端封装
环境变量规范
Jenkins 和 Docker 配置可执行化
```

## 不做什么

```text
不做完整登录业务
不做博客发布后台
不做 AI 接入
不做文件上传
不做复杂权限页面
不做精细首页动效
```

## 后端任务

### 1. 建立基础包结构

推荐结构：

```text
com.simon.workspace
├── common
│   ├── response
│   ├── exception
│   ├── validation
│   └── config
├── module
│   ├── auth
│   ├── user
│   ├── course
│   ├── semester
│   ├── template
│   ├── file
│   └── generation
└── infrastructure
    ├── persistence
    ├── redis
    └── storage
```

### 2. 统一 API 响应

保留或升级当前 `ApiResponse`，建议最终形态：

```json
{
  "code": 0,
  "message": "success",
  "data": {},
  "traceId": "optional"
}
```

### 3. 统一异常处理

需要覆盖：

```text
业务异常
参数校验异常
未登录异常
无权限异常
资源不存在
文件处理异常
AI 调用异常
未知系统异常
```

### 4. 数据库与 Redis 基础配置

引入：

```text
MySQL Driver
MyBatis-Plus
Flyway 或 Liquibase
Spring Data Redis
```

建议优先使用 Flyway：

```text
simon-workspace-api/src/main/resources/db/migration/V1__init_schema.sql
```

### 5. 基础数据表

第一阶段先落地：

```text
user
role
permission
user_role
role_permission
login_log
```

业务表可以在第 03 阶段再建。

## 前端任务

### 1. 建立基础目录

推荐结构：

```text
src/
├── api/
├── assets/
├── components/
├── layouts/
├── router/
├── stores/
├── styles/
├── types/
├── utils/
└── views/
```

### 2. 建立路由壳

基础路由：

```text
/               公开首页
/blog           博客列表
/projects       项目展示
/login          登录页或登录弹窗入口
/workspace      工作台首页
```

### 3. API 客户端

基于 Axios 封装：

```text
baseURL 从 VITE_API_BASE_URL 读取
统一处理响应 code
统一处理登录失效
统一处理错误提示
```

### 4. UI 基础规范

使用：

```text
Naive UI 作为表单、表格、弹窗、通知基础组件
Tailwind CSS 负责布局和视觉细节
```

## 配置与环境变量

后端：

```text
SPRING_PROFILES_ACTIVE
SERVER_PORT
MYSQL_HOST
MYSQL_PORT
MYSQL_DATABASE
MYSQL_USERNAME
MYSQL_PASSWORD
REDIS_HOST
REDIS_PORT
REDIS_PASSWORD
APP_FILE_STORAGE_ROOT
```

前端：

```text
VITE_API_BASE_URL
```

部署：

```text
deploy/.env.example 必须包含本地部署所需的全部变量
真实 deploy/.env 不提交
```

## 测试与验收

后端：

```text
mvn -f simon-workspace-api/pom.xml test
```

至少覆盖：

```text
应用上下文启动
健康检查接口
统一响应结构
异常处理
```

前端：

```text
npm run build --prefix simon-workspace-web
```

至少确认：

```text
首页能构建
工作台路由存在
API 客户端无类型错误
```

部署：

```text
docker compose --env-file deploy/.env -f deploy/docker-compose.yml config
```

如果本地没有 Docker，需要在 Jenkins 或部署机器上补跑。

## 推荐提交

```text
chore(api): add persistence and redis dependencies
feat(api): add common response and exception handling
chore(api): add database migration baseline
feat(web): add app layout and api client
chore(deploy): align compose environment variables
docs: update foundation progress
```

## 阶段完成标准

```text
后端能连接 MySQL 和 Redis
数据库迁移能创建基础用户权限表
前端能通过环境变量访问 API
Docker Compose 配置能解析
Jenkinsfile 中的测试和构建阶段路径正确
工作区干净且变更已提交
```

## 阶段执行 Todo

当前状态：进行中。最后核查时间：2026-06-20。

### 后端基础

- [x] 创建 Spring Boot API 工程入口 `SimonWorkspaceApiApplication`
- [x] 增加 `/api/health` 健康检查接口
- [x] 增加基础 `ApiResponse<T>` 响应结构
- [ ] 将 `ApiResponse` 包结构调整为 `common.response` 或确认当前 `common` 包作为长期约定
- [ ] 为 `ApiResponse` 增加可选 `traceId` 字段或明确第一版不需要 traceId
- [ ] 增加业务异常类 `BusinessException`
- [ ] 增加错误码枚举或常量，覆盖成功、参数错误、未登录、无权限、资源不存在、系统错误
- [ ] 增加全局异常处理器 `GlobalExceptionHandler`
- [ ] 统一处理 `MethodArgumentNotValidException`
- [ ] 统一处理 `ConstraintViolationException`
- [ ] 统一处理未知异常，避免把堆栈直接暴露给前端
- [ ] 增加基础请求日志或 traceId 过滤器
- [ ] 明确后端基础包结构：`common`、`module`、`infrastructure`

### 数据库与缓存

- [x] 引入 MySQL Driver
- [x] 引入 Spring JDBC
- [x] 引入 Flyway Core
- [x] 引入 Flyway MySQL
- [x] 配置 `application.yml` 中的 MySQL 数据源环境变量
- [x] 配置 `spring.flyway.locations`
- [x] 创建 `V1__init_schema.sql`
- [x] 创建基础 `user` 表
- [x] 创建基础 `role` 表
- [x] 创建基础 `permission` 表
- [x] 创建基础 `user_role` 表
- [x] 创建基础 `role_permission` 表
- [x] 创建基础 `login_log` 表
- [ ] 校验 `V1__init_schema.sql` 中文注释编码，确保仓库内为 UTF-8 且数据库导入后不乱码
- [ ] 使用真实 MySQL 或 Testcontainers 验证 Flyway 迁移可以完整执行
- [ ] 增加 Redis 依赖
- [ ] 配置 `REDIS_HOST`、`REDIS_PORT`、`REDIS_PASSWORD`
- [ ] 增加 Redis 连接健康验证或最小 smoke test
- [ ] 决定是否在第一阶段引入 MyBatis-Plus；若不引入，在文档中说明第一版使用 JDBC 或后续阶段再引入

### 后端测试

- [x] 增加 Spring Boot 上下文启动测试
- [x] 增加迁移文件存在和基础表名测试
- [ ] 增加 `/api/health` 接口测试，断言 `code=0`、`status=UP`
- [ ] 增加 `ApiResponse.ok` 和 `ApiResponse.fail` 单元测试
- [ ] 增加参数校验失败返回结构测试
- [ ] 增加业务异常返回结构测试
- [ ] 增加 Flyway 真实迁移测试
- [ ] 将测试 profile 中排除 JDBC/Flyway 的原因写入注释或调整为分层测试配置

### 前端基础

- [x] 创建 Vue 3 + Vite 前端工程
- [x] 引入 Vue Router
- [x] 引入 Pinia
- [x] 引入 Naive UI
- [x] 引入 Tailwind CSS 相关依赖
- [x] 创建 `App.vue` 根组件
- [x] 创建 `/` 首页路由
- [x] 创建 `/workspace` 工作台路由
- [x] 创建基础首页 `HomeView.vue`
- [x] 创建基础工作台页 `WorkspaceView.vue`
- [x] 创建静态终端展示组件 `TerminalPanel.vue`
- [x] 创建基础 `http.ts` Axios 实例
- [ ] 增加 `VITE_API_BASE_URL` 示例环境文件或 README 说明
- [ ] 为 `http.ts` 增加响应拦截器，统一解包 `{ code, message, data }`
- [ ] 为 `http.ts` 增加错误提示和登录失效处理策略
- [ ] 为前端增加基础类型定义目录 `src/types`
- [ ] 为前端增加工具目录 `src/utils`
- [ ] 统一前端目录结构，明确 `components`、`views`、`stores`、`api` 的职责

### 部署与 CI

- [x] 创建 API Dockerfile
- [x] 创建 Web Dockerfile
- [x] 创建 Web Nginx 配置
- [x] 创建 `deploy/docker-compose.yml`
- [x] 在 Docker Compose 中配置 API、Web、MySQL、Redis、MinIO
- [x] 创建 `deploy/.env.example`
- [x] 创建 Jenkinsfile
- [x] Jenkinsfile 包含 checkout、API test/package、Web install/build、Docker build 阶段
- [ ] 本地或部署机执行 `docker compose --env-file deploy/.env.example -f deploy/docker-compose.yml config`
- [ ] 修复 `deploy/.env.example` 与 Compose、后端配置之间不一致的变量名
- [ ] 为 API 容器增加健康检查
- [ ] 为 Web 容器增加健康检查或 smoke test
- [ ] 将 Jenkins `Deploy Placeholder` 替换为真实部署策略或明确保留到第 06 阶段
- [ ] 明确镜像 tag、registry、服务器环境变量管理方式

### 文档与维护

- [x] 创建根 README
- [x] 创建 API README
- [x] 创建 Web README
- [x] 创建 staged roadmap
- [ ] 在 README 中同步当前阶段状态
- [ ] 在 README 中补充首次启动顺序：后端测试、前端安装、前端构建、Docker 前置要求
- [ ] 在 README 中补充常见问题：Docker 不存在、npm audit、测试 profile 排除 JDBC/Flyway

## 阶段验收 Todo

- [x] `mvn -f simon-workspace-api/pom.xml test` 通过，2026-06-20 验证通过
- [x] `npm ci --prefix simon-workspace-web` 通过，2026-06-20 验证通过
- [x] `npm run build --prefix simon-workspace-web` 通过，2026-06-20 验证通过
- [ ] `npm audit --prefix simon-workspace-web --audit-level=high` 无 high severity vulnerability
- [ ] `docker compose --env-file deploy/.env.example -f deploy/docker-compose.yml config` 通过
- [ ] 后端真实 Flyway 迁移验证通过
- [ ] Redis 连接配置完成并验证
- [ ] 统一异常处理完成并有测试覆盖
- [ ] API 客户端响应拦截器完成
- [ ] Git 工作区只包含预期变更

## 进度记录

```text
2026-06-20:
- 后端测试通过：2 tests, 0 failures, 0 errors
- 前端依赖安装通过
- 前端构建通过
- Docker Compose 未验证：当前环境没有 docker 命令
- npm audit 发现 1 个 high severity vulnerability：axios -> form-data
```
