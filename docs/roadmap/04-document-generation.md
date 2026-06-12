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
