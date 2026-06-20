# 05 AI Integration 阶段文档

## 阶段目标

接入 AI 内容生成能力，让系统根据课程、学期、周次、主题和模板字段生成结构化 JSON，再由程序负责预览、编辑和模板导出。

核心原则：

```text
AI 只生成内容 JSON
程序负责模板填充
用户必须能预览和修改
AI 失败不能影响已有模板导出能力
```

## 功能范围

```text
AI 服务统一接口
模型配置
Prompt 模板
教案 JSON 生成
教学日历 JSON 生成
AI 原始响应保存
JSON 解析与校验
预览编辑
失败重试
任务状态 GENERATING
```

## 不做什么

```text
不做复杂多 Agent
不做 RAG
不做自动读取 PDF
不做自动识别模板字段
不做多模型自动路由
不让 AI 直接生成 docx 或 xlsx
```

## 后端任务

### 1. AI 服务抽象

接口：

```java
public interface AiService {
    AiChatResult chat(AiChatRequest request);
}
```

基础对象：

```text
AiChatRequest
AiChatResult
AiModelConfig
AiProvider
```

第一版只实现一个模型供应商。

### 2. 模型配置

配置来源：

```text
环境变量
application.yml
后续再做数据库配置
```

建议变量：

```text
AI_PROVIDER
AI_BASE_URL
AI_API_KEY
AI_MODEL
AI_TIMEOUT_SECONDS
```

### 3. Prompt 模板

Prompt 模板按任务类型管理：

```text
lesson-generation
calendar-generation
plan-generation 后置
summary-generation 后置
```

Prompt 输入：

```text
课程信息
班级信息
学期信息
周次或周历
模板字段
用户主题
生成要求
```

Prompt 输出必须要求 JSON。

### 4. JSON 校验

AI 返回后必须经过：

```text
提取 JSON
解析 JSON
校验必填字段
校验字段类型
保存 raw_response
保存 data_json
```

校验失败：

```text
任务状态 FAILED
保存错误信息
允许用户重试
```

### 5. 生成流程升级

教案：

```text
POST /api/generation/lessons/ai-preview
```

教学日历：

```text
POST /api/generation/calendars/ai-preview
```

状态流转：

```text
PENDING
GENERATING
PREVIEW_READY
FILLING_TEMPLATE
SUCCESS
FAILED
CANCELED
```

## 前端任务

### 1. AI 生成开关

生成表单提供：

```text
手动生成
AI 辅助生成
```

手动生成沿用第 04 阶段能力。

### 2. AI 生成过程

前端展示：

```text
正在生成
任务进度
生成失败原因
重试按钮
编辑预览
确认导出
```

### 3. 预览编辑

编辑器不需要一开始做复杂富文本，可以按字段分区：

```text
教学目标
教学重点
教学难点
教学方法
教学过程
作业布置
课程思政
教学反思
```

教学过程支持列表编辑。

### 4. 终端反馈

终端输出：

```text
[AI] 正在生成教案内容...
[SUCCESS] AI 内容已生成，请在右侧预览
[ERROR] AI 生成失败，已保存任务记录
```

## 数据模型

扩展 `document_data`：

```text
ai_model
prompt
raw_response
data_json
edited_json
version
```

可新增：

```text
ai_prompt_template
ai_call_log
```

第一版如果不想加表，Prompt 模板可以先放在代码资源文件中。

## 测试与验收

后端：

```text
mvn -f simon-workspace-api/pom.xml test
```

至少覆盖：

```text
AI 服务接口 mock 测试
Prompt 构建测试
JSON 解析成功
JSON 解析失败
AI 生成失败任务状态
AI 生成成功任务状态
```

前端：

```text
npm run build --prefix simon-workspace-web
```

人工验收：

```text
能用 AI 生成教案 JSON
能用 AI 生成教学日历 JSON
能预览和编辑 AI 内容
确认后能导出 Word / Excel
AI 失败时有明确错误提示
手动生成能力仍然可用
```

## 推荐提交

```text
feat(api): add AI service abstraction
feat(api): add lesson AI prompt flow
feat(api): add calendar AI prompt flow
feat(api): validate AI JSON responses
feat(web): add AI generation mode
feat(web): add AI preview editor
test(api): cover AI generation states
docs: update AI integration progress
```

## 阶段完成标准

```text
AI 生成内容可进入预览编辑
AI 输出被保存为结构化 JSON
用户确认后仍由程序导出文件
AI 失败有记录、有错误、有重试入口
手动生成不受 AI 影响
测试和构建通过
```

## 阶段执行 Todo

当前状态：未开始。依赖第 04 阶段手动生成和模板导出流程。最后核查时间：2026-06-20。

### AI 服务抽象

- [ ] 创建 `AiService` 接口
- [ ] 创建 `AiChatRequest`
- [ ] 创建 `AiChatResult`
- [ ] 创建 `AiModelConfig`
- [ ] 创建 `AiProvider` 枚举
- [ ] 实现第一版 AI 供应商客户端
- [ ] 配置 AI 请求超时
- [ ] 屏蔽日志中的 API Key
- [ ] 增加 AI 服务 mock 测试

### 配置

- [ ] 增加 `AI_PROVIDER`
- [ ] 增加 `AI_BASE_URL`
- [ ] 增加 `AI_API_KEY`
- [ ] 增加 `AI_MODEL`
- [ ] 增加 `AI_TIMEOUT_SECONDS`
- [ ] 在 `application.yml` 中绑定 AI 配置
- [ ] 在 `deploy/.env.example` 中增加 AI 配置示例变量
- [ ] 在 README 中说明 AI 配置方式

### Prompt 模板

- [ ] 设计教案生成 Prompt 模板
- [ ] 设计教学日历生成 Prompt 模板
- [ ] 将 Prompt 模板放入代码资源文件或配置目录
- [ ] Prompt 输入包含课程信息
- [ ] Prompt 输入包含班级信息
- [ ] Prompt 输入包含学期信息
- [ ] Prompt 输入包含周次或周历
- [ ] Prompt 输入包含模板字段
- [ ] Prompt 输入包含用户主题
- [ ] Prompt 明确要求返回 JSON
- [ ] 增加 Prompt 构建测试

### JSON 解析与校验

- [ ] 实现 AI 响应 JSON 提取
- [ ] 实现 JSON 解析
- [ ] 实现必填字段校验
- [ ] 实现字段类型校验
- [ ] 解析成功时保存 `raw_response`
- [ ] 解析成功时保存 `data_json`
- [ ] 解析失败时任务状态设为 `FAILED`
- [ ] 解析失败时保存错误信息
- [ ] 允许失败任务重试
- [ ] 增加 JSON 解析成功测试
- [ ] 增加 JSON 解析失败测试

### 生成流程升级

- [ ] 增加任务状态 `GENERATING`
- [ ] 实现 `POST /api/generation/lessons/ai-preview`
- [ ] 实现 `POST /api/generation/calendars/ai-preview`
- [ ] AI 教案生成成功后进入 `PREVIEW_READY`
- [ ] AI 教学日历生成成功后进入 `PREVIEW_READY`
- [ ] AI 调用失败后进入 `FAILED`
- [ ] AI 成功后仍沿用第 04 阶段导出流程
- [ ] 手动生成流程不依赖 AI 配置
- [ ] 增加 AI 生成成功任务状态测试
- [ ] 增加 AI 生成失败任务状态测试

### 前端 AI 模式

- [ ] 教案生成表单增加“手动生成 / AI 辅助生成”模式切换
- [ ] 教学日历生成表单增加“手动生成 / AI 辅助生成”模式切换
- [ ] AI 生成时展示正在生成状态
- [ ] AI 生成时展示任务进度或状态文本
- [ ] AI 失败时展示失败原因
- [ ] AI 失败时展示重试按钮
- [ ] AI 成功后进入预览编辑
- [ ] 预览编辑支持教学目标
- [ ] 预览编辑支持教学重点
- [ ] 预览编辑支持教学难点
- [ ] 预览编辑支持教学方法
- [ ] 预览编辑支持教学过程列表
- [ ] 预览编辑支持作业布置
- [ ] 预览编辑支持课程思政
- [ ] 预览编辑支持教学反思

### 终端反馈

- [ ] 终端执行 AI 生成命令时输出正在生成
- [ ] AI 成功时终端输出成功提示
- [ ] AI 失败时终端输出错误提示
- [ ] 终端提示用户到图形界面预览和确认导出

## 阶段验收 Todo

- [ ] 能用 AI 生成教案 JSON
- [ ] 能用 AI 生成教学日历 JSON
- [ ] 能预览和编辑 AI 内容
- [ ] 确认后能导出 Word / Excel
- [ ] AI 失败时有明确错误提示
- [ ] AI 失败时有任务记录
- [ ] AI 失败时有重试入口
- [ ] 手动生成能力仍然可用
- [ ] AI API Key 不出现在日志和前端产物中
- [ ] `mvn -f simon-workspace-api/pom.xml test` 通过
- [ ] `npm run build --prefix simon-workspace-web` 通过

## 进度记录

```text
2026-06-20:
- 尚未实现 AI 配置、服务抽象、Prompt、JSON 校验和 AI 生成流程
```
