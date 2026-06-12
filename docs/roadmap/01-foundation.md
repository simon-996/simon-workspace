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
