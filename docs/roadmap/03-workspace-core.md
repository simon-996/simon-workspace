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
