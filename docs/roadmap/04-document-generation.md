# 04 Document Generation 阶段文档

## 阶段目标

在不依赖 AI 的前提下，先跑通“结构化数据 + 模板填充 + 文件导出 + 下载记录”的完整流程。

本阶段完成后，即使没有 AI，用户也可以通过表单生成 Word 教案和 Excel 教学日历。

## 功能范围

```text
教案生成表单
教学日历生成表单
结构化内容预览
用户编辑确认
Word 模板填充
Excel 模板填充
生成任务状态流转
生成结果保存
文件下载
终端命令打开生成表单
```

## 不做什么

```text
不接 AI API
不做批量生成
不做复杂模板字段自动识别
不做 PDF 转换
不做 ZIP 打包
```

## 后端任务

### 1. 生成任务状态

状态：

```text
PENDING
PREVIEW_READY
FILLING_TEMPLATE
SUCCESS
FAILED
CANCELED
```

本阶段没有 `GENERATING`，因为暂不调用 AI。

### 2. 教案生成

接口：

```text
POST /api/generation/lessons/preview
PUT /api/generation/tasks/{taskId}/document
POST /api/generation/tasks/{taskId}/export
GET /api/generation/tasks/{taskId}
```

流程：

```text
用户选择课程、班级、学期、周次、模板
系统根据表单生成初始 JSON
前端展示可编辑预览
用户确认
后端使用 poi-tl 填充 Word 模板
保存 generate_result 和 file_resource
返回下载入口
```

### 3. 教学日历生成

接口：

```text
POST /api/generation/calendars/preview
PUT /api/generation/tasks/{taskId}/document
POST /api/generation/tasks/{taskId}/export
```

流程：

```text
用户选择课程和学期
系统读取 semester_calendar
用户填写或粘贴每周教学内容
系统生成表格 JSON
前端预览可编辑
后端使用 EasyExcel 或 POI 导出 Excel
```

### 4. 模板填充

Word：

```text
使用 poi-tl
占位符格式：{{fieldKey}}
支持文本、列表、表格
```

Excel：

```text
优先使用 EasyExcel
复杂模板可使用 Apache POI
支持按行写入周次、日期、内容、学时、作业、备注
```

### 5. 数据表

本阶段落地：

```text
document_data
generate_result
```

`document_data` 保存：

```text
data_json
edited_json
version
```

`generate_result` 保存：

```text
task_id
file_resource_id
file_name
file_type
result_type
```

## 前端任务

### 1. 生成入口

页面：

```text
/workspace/generate/lesson
/workspace/generate/calendar
```

工作台首页提供快捷入口。

终端命令：

```text
generate lesson
generate calendar
generate lesson --course "Java Web" --week 3 --topic "Spring Boot 入门"
```

命令只打开表单并预填参数，不直接生成最终文件。

### 2. 分步表单

教案生成：

```text
选择课程
选择班级
选择学期
选择周次
选择模板
填写主题
预览内容
确认导出
```

教学日历生成：

```text
选择课程
选择学期
选择模板
填写每周内容
预览表格
确认导出
```

### 3. 任务状态展示

前端需要展示：

```text
任务状态
进度
错误信息
下载按钮
任务详情
```

## 测试与验收

后端：

```text
mvn -f simon-workspace-api/pom.xml test
```

至少覆盖：

```text
教案 preview 创建任务
编辑 document_data
Word 导出成功
教学日历 preview 创建任务
Excel 导出成功
导出失败时任务状态为 FAILED
文件下载权限校验
```

前端：

```text
npm run build --prefix simon-workspace-web
```

人工验收：

```text
能通过图形界面生成教案 Word
能通过图形界面生成教学日历 Excel
能预览并修改生成内容
能在生成记录中看到任务
能在文件中心下载文件
终端命令能打开并预填生成表单
```

## 推荐提交

```text
feat(api): add generation task data model
feat(api): add lesson preview and export flow
feat(api): add calendar preview and export flow
feat(api): add word template renderer
feat(api): add excel template renderer
feat(web): add lesson generation wizard
feat(web): add calendar generation wizard
feat(web): add generation task detail and download actions
test(api): cover document generation flow
docs: update document generation progress
```

## 阶段完成标准

```text
不依赖 AI 也能生成 Word 和 Excel
生成任务状态可追踪
生成内容可预览和编辑
生成文件可下载
生成记录可查询
测试和构建通过
```

## 阶段执行 Todo

当前状态：数据模型与状态基础已完成，教案手动 preview 和文档数据编辑后端接口进行中，已能创建预览任务、保存初始文档 JSON，并保存人工编辑 JSON。依赖第 03 阶段课程、班级、学期、模板、文件中心和生成记录。最后核查时间：2026-06-21。

### 数据模型与状态

- [x] 创建 `document_data` 数据表迁移
- [x] 创建 `generate_result` 数据表迁移
- [x] 扩展或确认 `generate_task` 状态字段
- [x] 定义生成任务状态：`PENDING`
- [x] 定义生成任务状态：`PREVIEW_READY`
- [x] 定义生成任务状态：`FILLING_TEMPLATE`
- [x] 定义生成任务状态：`SUCCESS`
- [x] 定义生成任务状态：`FAILED`
- [x] 定义生成任务状态：`CANCELED`
- [x] 定义任务失败错误信息保存方式
- [x] 定义生成结果和文件资源的关联关系

### 教案生成后端

- [x] 创建教案 preview 请求 DTO
- [x] 创建教案 preview 响应 DTO
- [x] 创建文档数据更新请求 DTO
- [x] 实现 `POST /api/generation/lessons/preview`
- [x] preview 时根据课程、班级、学期、周次、模板生成初始 JSON
- [x] preview 时创建生成任务记录
- [x] preview 时保存 `document_data.data_json`
- [x] 实现 `PUT /api/generation/tasks/{taskId}/document`
- [x] 编辑时保存 `document_data.edited_json`
- [ ] 实现 `POST /api/generation/tasks/{taskId}/export`
- [ ] 导出时读取最终 JSON
- [ ] 导出时调用 Word 模板填充服务
- [ ] 导出成功时保存 `generate_result`
- [ ] 导出成功时保存 `file_resource`
- [ ] 导出失败时任务状态设为 `FAILED`
- [ ] 实现 `GET /api/generation/tasks/{taskId}`

### 教学日历生成后端

- [ ] 创建教学日历 preview 请求 DTO
- [ ] 创建教学日历 preview 响应 DTO
- [ ] 实现 `POST /api/generation/calendars/preview`
- [ ] preview 时读取 `semester_calendar`
- [ ] preview 时生成可编辑表格 JSON
- [ ] preview 时创建生成任务记录
- [ ] 实现教学日历导出复用 `POST /api/generation/tasks/{taskId}/export`
- [ ] 导出时调用 Excel 模板填充服务
- [ ] 导出失败时任务状态设为 `FAILED`

### 模板渲染

- [ ] 引入 poi-tl 或明确 Word 模板渲染库
- [ ] 实现 Word 模板渲染服务接口
- [ ] 支持文本占位符 `{{fieldKey}}`
- [ ] 支持列表占位符
- [ ] 支持表格占位符
- [ ] 增加 Word 模板渲染单元测试
- [ ] 引入 EasyExcel 或明确 Excel 渲染库
- [ ] 实现 Excel 模板渲染服务接口
- [ ] 支持按周次写入日期、内容、学时、作业、备注
- [ ] 增加 Excel 模板渲染单元测试
- [ ] 为测试准备最小 Word 模板样例
- [ ] 为测试准备最小 Excel 模板样例

### 前端生成入口

- [ ] 创建 `/workspace/generate/lesson` 路由
- [ ] 创建 `/workspace/generate/calendar` 路由
- [ ] 工作台首页增加教案生成快捷入口
- [ ] 工作台首页增加教学日历生成快捷入口
- [ ] 终端 `generate lesson` 打开教案生成表单
- [ ] 终端 `generate calendar` 打开教学日历生成表单
- [ ] 终端命令参数能预填课程、周次、主题

### 教案分步表单

- [ ] 创建教案生成页面
- [ ] 步骤 1：选择课程
- [ ] 步骤 2：选择班级
- [ ] 步骤 3：选择学期
- [ ] 步骤 4：选择周次
- [ ] 步骤 5：选择模板
- [ ] 步骤 6：填写主题
- [ ] 步骤 7：预览内容
- [ ] 步骤 8：确认导出
- [ ] 展示任务状态
- [ ] 展示错误信息
- [ ] 展示下载按钮

### 教学日历分步表单

- [ ] 创建教学日历生成页面
- [ ] 步骤 1：选择课程
- [ ] 步骤 2：选择学期
- [ ] 步骤 3：选择模板
- [ ] 步骤 4：填写每周内容
- [ ] 步骤 5：预览表格
- [ ] 步骤 6：确认导出
- [ ] 展示任务状态
- [ ] 展示错误信息
- [ ] 展示下载按钮

### 测试

- [ ] 测试教案 preview 创建任务
- [ ] 测试编辑 `document_data`
- [ ] 测试 Word 导出成功
- [ ] 测试教学日历 preview 创建任务
- [ ] 测试 Excel 导出成功
- [ ] 测试导出失败时任务状态为 `FAILED`
- [ ] 测试文件下载权限校验

## 阶段验收 Todo

- [ ] 能通过图形界面生成教案 Word
- [ ] 能通过图形界面生成教学日历 Excel
- [ ] 能预览并修改生成内容
- [ ] 能在生成记录中看到任务
- [ ] 能在文件中心下载文件
- [ ] 终端命令能打开并预填生成表单
- [ ] 不依赖 AI 也能完成 Word 和 Excel 导出
- [ ] `mvn -f simon-workspace-api/pom.xml test` 通过
- [ ] `npm run build --prefix simon-workspace-web` 通过

## 进度记录

```text
2026-06-20:
- 尚未实现文档生成相关数据表、接口、模板渲染和前端页面
```
