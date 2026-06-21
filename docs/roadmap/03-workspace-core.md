# 03 Workspace Core 阶段文档

## 阶段目标

完成登录后的图形化工作台基础能力，让课程、班级、学期、模板、文件和生成记录具备可维护入口。

这是后续文档生成和 AI 接入的业务底座。

## 功能范围

```text
登录与退出
当前用户信息
工作台首页
课程管理
班级管理
学期管理
模板管理
文件中心
生成记录
基础权限保护
```

## 不做什么

```text
不做多租户
不做复杂角色后台
不做 AI 生成
不做 Word / Excel 模板填充
不做模板字段自动识别
不做批量生成
```

## 后端任务

### 1. 认证

第一版可以使用简单 Token 或 Sa-Token。

接口：

```text
POST /api/auth/login
POST /api/auth/logout
GET /api/auth/me
```

登录成功返回：

```json
{
  "token": "string",
  "user": {
    "id": "string",
    "username": "simon",
    "nickname": "Simon"
  }
}
```

### 2. 课程管理

接口：

```text
GET /api/courses
GET /api/courses/{id}
POST /api/courses
PUT /api/courses/{id}
DELETE /api/courses/{id}
```

字段按主规划文档第 16 章 `course` 表执行。

### 3. 班级管理

接口：

```text
GET /api/classes
POST /api/classes
PUT /api/classes/{id}
DELETE /api/classes/{id}
```

### 4. 学期管理

接口：

```text
GET /api/semesters
GET /api/semesters/{id}
POST /api/semesters
PUT /api/semesters/{id}
POST /api/semesters/{id}/calendar/generate
GET /api/semesters/{id}/calendar
```

学期日历允许自动生成后人工微调。

### 5. 模板管理

接口：

```text
GET /api/templates
POST /api/templates/upload
GET /api/templates/{id}
PUT /api/templates/{id}
DELETE /api/templates/{id}
GET /api/templates/{id}/fields
PUT /api/templates/{id}/fields
```

第一版上传后可以让用户手动维护占位符字段。

### 6. 文件中心

接口：

```text
GET /api/files
GET /api/files/{id}
GET /api/files/{id}/download
DELETE /api/files/{id}
```

文件下载必须检查权限，不暴露真实磁盘路径。

### 7. 生成记录

接口：

```text
GET /api/generation/tasks
GET /api/generation/tasks/{id}
```

本阶段只展示任务记录，为第 04 阶段写入记录做准备。

## 前端任务

### 1. 工作台布局

桌面端：

```text
左侧导航
顶部用户信息
主内容区
右侧可选详情抽屉
```

移动端：

```text
底部 Tab 或顶部折叠菜单
单列表单
减少复杂表格
```

### 2. 页面

```text
/login
/workspace
/workspace/courses
/workspace/classes
/workspace/semesters
/workspace/templates
/workspace/files
/workspace/history
```

### 3. 表格与表单

每个管理页面至少具备：

```text
列表
搜索
新增
编辑
删除确认
空状态
加载状态
错误提示
```

## 数据模型

本阶段落地：

```text
course
class_info
semester
semester_calendar
template_file
template_field
file_resource
generate_task
```

`document_data` 和 `generate_result` 可在第 04 阶段落地。

## 测试与验收

后端：

```text
mvn -f simon-workspace-api/pom.xml test
```

至少覆盖：

```text
登录成功和失败
未登录访问工作台接口被拒绝
课程 CRUD
学期日历生成
模板字段保存
文件权限校验
```

前端：

```text
npm run build --prefix simon-workspace-web
```

人工验收：

```text
能登录
能进入工作台
能维护课程
能维护班级
能维护学期并生成周历
能上传模板并维护字段
能查看文件中心
能查看生成记录
刷新页面后登录态可恢复或合理失效
```

## 推荐提交

```text
feat(api): add authentication endpoints
feat(api): add course and class management
feat(api): add semester calendar management
feat(api): add template and file resource management
feat(web): add workspace layout and auth flow
feat(web): add course class semester pages
feat(web): add template file history pages
test(api): cover workspace core services
docs: update workspace core progress
```

## 阶段完成标准

```text
登录后的工作台可以日常维护基础资料
所有核心功能都有图形界面入口
基础资料能持久化到 MySQL
模板和文件资源能被后续生成流程引用
接口有基本权限保护
测试和构建通过
```

## 阶段执行 Todo

当前状态：认证链路、课程管理、班级管理、学期管理、模板管理、文件中心、生成记录查询和工作台主布局进行中，已有简单 Token 登录接口、课程/班级/学期/模板/文件中心/生成记录前后端和响应式工作台壳层，学期周历支持生成后人工微调。最后核查时间：2026-06-21。

### 认证与当前用户

- [x] 选择第一版认证方案：简单 Token、JWT 或 Sa-Token
- [x] 创建认证相关包结构
- [x] 创建登录请求 DTO
- [x] 创建登录响应 DTO
- [x] 创建当前用户响应 DTO
- [x] 实现 `POST /api/auth/login`
- [x] 实现 `POST /api/auth/logout`
- [x] 实现 `GET /api/auth/me`
- [x] 实现密码哈希验证
- [x] 实现登录失败记录
- [x] 实现登录成功后更新最后登录时间
- [x] 实现认证拦截器或过滤器
- [x] 明确公开接口和工作台接口的权限边界
- [x] 前端创建 `/login` 页面或登录弹窗
- [x] 前端保存 token
- [x] 前端刷新页面后恢复登录态或明确失效处理
- [x] 前端实现退出登录

### 工作台布局

- [x] 创建 `/workspace` 基础路由
- [x] 创建基础工作台占位页
- [x] 创建工作台主布局组件
- [x] 创建左侧导航
- [x] 创建顶部用户信息区
- [x] 创建主内容区布局
- [x] 创建移动端底部 Tab 或折叠菜单
- [ ] 创建通用页面标题组件
- [ ] 创建通用空状态组件
- [ ] 创建通用加载状态组件
- [ ] 创建通用错误提示策略
- [x] 为未登录访问工作台增加跳转或提示

### 课程管理

- [x] 创建 `course` 数据表迁移
- [x] 创建课程实体或记录模型
- [x] 创建课程请求/响应 DTO
- [x] 实现 `GET /api/courses`
- [x] 实现 `GET /api/courses/{id}`
- [x] 实现 `POST /api/courses`
- [x] 实现 `PUT /api/courses/{id}`
- [x] 实现 `DELETE /api/courses/{id}`
- [x] 增加课程名称、课程编码唯一性校验
- [ ] 增加课程 CRUD 测试
- [x] 创建 `/workspace/courses` 路由
- [x] 实现课程列表
- [x] 实现课程搜索
- [x] 实现课程新增表单
- [x] 实现课程编辑表单
- [x] 实现课程删除确认

### 班级管理

- [x] 创建 `class_info` 数据表迁移
- [x] 创建班级模型和 DTO
- [x] 实现 `GET /api/classes`
- [x] 实现 `POST /api/classes`
- [x] 实现 `PUT /api/classes/{id}`
- [x] 实现 `DELETE /api/classes/{id}`
- [ ] 增加班级 CRUD 测试
- [x] 创建 `/workspace/classes` 路由
- [x] 实现班级列表、搜索、新增、编辑、删除

### 学期管理

- [x] 创建 `semester` 数据表迁移
- [x] 创建 `semester_calendar` 数据表迁移
- [x] 创建学期模型和 DTO
- [x] 实现 `GET /api/semesters`
- [x] 实现 `GET /api/semesters/{id}`
- [x] 实现 `POST /api/semesters`
- [x] 实现 `PUT /api/semesters/{id}`
- [x] 实现 `POST /api/semesters/{id}/calendar/generate`
- [x] 实现 `GET /api/semesters/{id}/calendar`
- [x] 支持学期周历生成后人工微调
- [ ] 增加学期和周历测试
- [x] 创建 `/workspace/semesters` 路由
- [x] 实现学期列表、表单和周历维护界面

### 模板管理

- [x] 创建 `template_file` 数据表迁移
- [x] 创建 `template_field` 数据表迁移
- [x] 实现模板上传存储
- [x] 实现 `GET /api/templates`
- [x] 实现 `POST /api/templates/upload`
- [x] 实现 `GET /api/templates/{id}`
- [x] 实现 `PUT /api/templates/{id}`
- [x] 实现 `DELETE /api/templates/{id}`
- [x] 实现 `GET /api/templates/{id}/fields`
- [x] 实现 `PUT /api/templates/{id}/fields`
- [x] 第一版支持手动维护占位符字段
- [ ] 增加模板字段保存测试
- [x] 创建 `/workspace/templates` 路由
- [x] 实现模板列表、上传、字段维护、删除

### 文件中心

- [x] 创建 `file_resource` 数据表迁移
- [x] 实现本地文件存储服务
- [x] 实现文件元信息保存
- [x] 实现 `GET /api/files`
- [x] 实现 `GET /api/files/{id}`
- [x] 实现 `GET /api/files/{id}/download`
- [x] 实现 `DELETE /api/files/{id}`
- [x] 文件下载不暴露真实磁盘路径
- [x] 文件下载检查 owner 或权限
- [ ] 增加文件权限测试
- [x] 创建 `/workspace/files` 路由
- [x] 实现文件列表、下载、删除

### 生成记录

- [x] 创建 `generate_task` 数据表迁移
- [x] 实现 `GET /api/generation/tasks`
- [x] 实现 `GET /api/generation/tasks/{id}`
- [ ] 增加生成记录查询测试
- [x] 创建 `/workspace/history` 路由
- [x] 实现生成记录列表
- [x] 实现生成记录详情入口

## 阶段验收 Todo

- [ ] 能登录
- [ ] 能退出
- [ ] 未登录访问工作台接口被拒绝
- [ ] 能进入工作台
- [ ] 能维护课程
- [ ] 能维护班级
- [ ] 能维护学期并生成周历
- [ ] 能上传模板并维护字段
- [ ] 能查看文件中心
- [ ] 能查看生成记录
- [ ] 刷新页面后登录态可恢复或合理失效
- [ ] `mvn -f simon-workspace-api/pom.xml test` 通过
- [ ] `npm run build --prefix simon-workspace-web` 通过

## 进度记录

```text
2026-06-20:
- 只有 /workspace 基础占位页
- 尚未实现登录、业务表、业务接口和工作台管理页面

2026-06-21:
- 后端选择简单 Token 作为第一版认证方案
- 新增 `/api/auth/login`、`/api/auth/logout`、`/api/auth/me`
- 登录读取 `user` 表，支持 `sha256:<hex>` 密码哈希校验
- 登录成功写入 `login_log` 并更新 `last_login_time`
- 登录失败写入 `login_log`
- 认证拦截器保护 `/api/**`，公开 `/api/health`、`/api/auth/login` 和 actuator
- 前端新增 `/login` 页面
- 前端使用 Pinia 保存 token 和当前用户
- axios 请求自动携带 Bearer token
- `/workspace` 路由增加未登录跳转
- 工作台顶部显示当前用户并支持退出登录
- 新增 `course` 表迁移
- 新增 `/api/courses` 课程 CRUD 接口
- 课程列表支持按名称或编码搜索
- 新增课程名称、课程编码唯一性校验
- 新增 `class_info` 表迁移
- 新增 `/api/classes` 班级 CRUD 接口
- 班级列表支持按名称、专业、年级搜索
- 新增班级名称唯一性校验
- 新增 `semester` 和 `semester_calendar` 表迁移
- 新增 `/api/semesters` 学期 CRUD 接口
- 新增学期周历生成和查询接口
- 学期列表支持按学年或学期名称搜索
- 新增学年与学期名称组合唯一性校验
- 新增 `template_file` 和 `template_field` 表迁移
- 新增 `/api/templates` 模板列表、详情、更新和删除接口
- 新增 `/api/templates/upload` 模板上传接口，文件保存到 `app.file-storage.root`
- 新增模板字段查询和保存接口，支持手动维护占位符字段
- 新增 `file_resource` 表迁移
- 新增 `/api/files` 文件列表、详情、下载和删除接口
- 文件中心按当前登录用户 owner 查询，下载不暴露真实磁盘路径
- 新增文件资源本地存储和元信息保存服务，供后续生成流程复用
- 新增 `generate_task` 表迁移
- 新增 `/api/generation/tasks` 生成记录列表和详情查询接口
- 生成记录查询按当前登录用户 owner 过滤
- 前端将 `/workspace` 改为工作台壳层，包含桌面左侧导航、顶部用户区、主内容区和移动端底部 Tab
- 前端新增工作台总览页，作为嵌套路由默认页面
- 前端新增 `/workspace/courses` 课程管理页
- 课程管理页支持列表、搜索、新增、编辑、删除确认、加载、错误和空状态
- 前端新增 `/workspace/classes` 班级管理页
- 班级管理页支持列表、搜索、新增、编辑、删除确认、加载、错误和空状态
- 前端新增 `/workspace/semesters` 学期管理页
- 学期管理页支持列表、搜索、新增、编辑、生成周历、查看周历、加载、错误和空状态
- 新增学期周历单周更新接口 `PUT /api/semesters/{id}/calendar/{calendarId}`
- 学期管理页支持编辑单周起止日期、考试周、节假日和调课说明
- 前端新增 `/workspace/templates` 模板管理页
- 模板管理页支持列表、搜索、上传、编辑、删除确认和手动字段维护
- 前端新增 `/workspace/files` 文件中心页
- 文件中心页支持列表、搜索、下载、删除确认、加载、错误和空状态
- 前端新增 `/workspace/history` 生成记录页
- 生成记录页支持列表、搜索、详情查看、加载、错误和空状态
```
