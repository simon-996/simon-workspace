# 04 Document Generation 阶段

最后更新：2026-07-04

## 阶段目标

在不依赖 AI 的前提下，先跑通“结构化数据 -> 预览编辑 -> 模板填充 -> 文件导出 -> 下载记录”的完整闭环。完成后，用户应该能通过图形界面生成教案 Word 和教学日历 Excel。

## 已完成

### 数据模型与状态

- [x] 创建 `generate_task`。
- [x] 创建 `document_data`。
- [x] 创建 `generate_result`。
- [x] 定义生成任务状态：`PENDING`、`PREVIEW_READY`、`FILLING_TEMPLATE`、`SUCCESS`、`FAILED`、`CANCELED`。
- [x] 保存任务失败原因。
- [x] 建立生成结果与文件资源的关联关系。

### 后端 preview 基础

- [x] 创建教案 preview 请求/响应 DTO。
- [x] 实现 `POST /api/generation/lessons/preview`。
- [x] 教案 preview 可根据课程、班级、学期、周次、模板生成初始 JSON。
- [x] 创建教学日历 preview 请求/响应 DTO。
- [x] 实现 `POST /api/generation/calendars/preview`。
- [x] 教学日历 preview 可读取 `semester_calendar` 并生成可编辑表格 JSON。
- [x] 实现 `PUT /api/generation/tasks/{taskId}/document`，保存人工编辑 JSON。
- [x] 生成记录列表和详情可查看任务。

## 待完成 Todo

### 后端导出

- [ ] 实现 `POST /api/generation/tasks/{taskId}/export`。
- [ ] 导出时读取 `document_data.edited_json`，没有编辑数据时回退到 `data_json`。
- [ ] 引入或确认 Word 模板渲染库。
- [ ] 实现 Word 模板填充服务。
- [ ] 支持文本占位符 `{{fieldKey}}`。
- [ ] 支持列表或表格占位符。
- [ ] 引入或确认 Excel 模板渲染库。
- [ ] 实现 Excel 模板填充服务。
- [ ] 导出成功后写入 `file_resource`。
- [ ] 导出成功后写入 `generate_result`。
- [ ] 导出失败后任务状态改为 `FAILED` 并保存原因。
- [ ] 文件下载继续走文件中心权限校验。

### 前端生成入口

- [ ] 创建 `/workspace/generate/lesson` 路由。
- [ ] 创建 `/workspace/generate/calendar` 路由。
- [ ] 工作台总览增加教案生成入口。
- [ ] 工作台总览增加教学日历生成入口。
- [ ] 终端 `generate lesson` 打开教案生成表单。
- [ ] 终端 `generate calendar` 打开教学日历生成表单。
- [ ] 支持命令参数预填课程、周次、主题。

### 教案生成向导

- [ ] 选择课程。
- [ ] 选择班级。
- [ ] 选择学期。
- [ ] 选择周次。
- [ ] 选择 Word 模板。
- [ ] 填写主题。
- [ ] 请求 preview。
- [ ] 展示可编辑预览。
- [ ] 保存编辑内容。
- [ ] 确认导出。
- [ ] 导出完成后显示下载入口。

### 教学日历生成向导

- [ ] 选择课程。
- [ ] 选择学期。
- [ ] 选择 Excel 模板。
- [ ] 读取周历。
- [ ] 填写或粘贴每周教学内容。
- [ ] 请求 preview。
- [ ] 展示可编辑表格。
- [ ] 保存编辑内容。
- [ ] 确认导出。
- [ ] 导出完成后显示下载入口。

### 测试

- [ ] 教案 preview 创建任务测试。
- [ ] 教学日历 preview 创建任务测试。
- [ ] `document_data` 编辑保存测试。
- [ ] Word 导出成功测试。
- [ ] Excel 导出成功测试。
- [ ] 导出失败状态测试。
- [ ] 文件下载权限测试。

## 验收 Todo

- [ ] 能通过图形界面生成教案 Word。
- [ ] 能通过图形界面生成教学日历 Excel。
- [ ] 能预览并修改生成内容。
- [ ] 生成记录能看到任务状态和详情。
- [ ] 文件中心能下载生成文件。
- [ ] 终端命令能打开并预填生成表单。
- [ ] 不配置 AI 也能完成导出。
- [ ] `mvn -f simon-workspace-api/pom.xml test` 通过。
- [ ] `npm run build --prefix simon-workspace-web` 通过。

## 进度记录

```text
2026-07-04:
- 后端 preview 和文档数据编辑基础已有。
- 前端还没有教案/教学日历生成向导。
- 最终 Word / Excel 导出闭环仍是本阶段最大缺口。
```
