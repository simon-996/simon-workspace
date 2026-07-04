# 05 AI Integration 阶段

最后更新：2026-07-04

## 阶段目标

在 04 阶段手动生成和模板导出闭环稳定后，接入 AI 内容生成。AI 只负责生成结构化 JSON，程序负责预览、编辑、模板填充和文件导出。

## 当前状态

未开始。该阶段依赖 04 阶段的生成向导、文档数据编辑、Word / Excel 导出闭环。

## 原则

- AI 不直接生成 Word 或 Excel 文件。
- AI 输出必须是结构化 JSON。
- 用户必须能预览和修改 AI 结果。
- AI 调用失败不能影响手动生成能力。
- API Key 不能出现在前端产物、日志或提交记录中。

## 待完成 Todo

### AI 服务抽象

- [ ] 创建 `AiService` 接口。
- [ ] 创建 `AiChatRequest`。
- [ ] 创建 `AiChatResult`。
- [ ] 创建 `AiModelConfig`。
- [ ] 创建 `AiProvider` 枚举。
- [ ] 实现第一版 AI Provider 客户端。
- [ ] 配置请求超时。
- [ ] 对日志中的 API Key 做脱敏。
- [ ] 增加 AI 服务 mock 测试。

### 配置

- [ ] 增加 `AI_PROVIDER`。
- [ ] 增加 `AI_BASE_URL`。
- [ ] 增加 `AI_API_KEY`。
- [ ] 增加 `AI_MODEL`。
- [ ] 增加 `AI_TIMEOUT_SECONDS`。
- [ ] 在后端 prod 配置中绑定环境变量。
- [ ] 在 `deploy/.env.example` 中增加 AI 配置示例。
- [ ] 在 README 中说明 AI 配置方式。

### Prompt

- [ ] 设计教案生成 Prompt。
- [ ] 设计教学日历生成 Prompt。
- [ ] Prompt 输入包含课程信息。
- [ ] Prompt 输入包含班级信息。
- [ ] Prompt 输入包含学期和周历信息。
- [ ] Prompt 输入包含模板字段。
- [ ] Prompt 输入包含用户主题或补充要求。
- [ ] Prompt 明确要求返回 JSON。
- [ ] 增加 Prompt 构建测试。

### JSON 解析与校验

- [ ] 提取 AI 响应中的 JSON。
- [ ] 解析 JSON。
- [ ] 校验必填字段。
- [ ] 校验字段类型。
- [ ] 保存 prompt。
- [ ] 保存 raw response。
- [ ] 保存解析后的 `data_json`。
- [ ] 解析失败时任务进入 `FAILED`。
- [ ] 保存解析失败原因。
- [ ] 增加 JSON 解析成功测试。
- [ ] 增加 JSON 解析失败测试。

### 生成流程升级

- [ ] 增加任务状态 `GENERATING`。
- [ ] 实现 `POST /api/generation/lessons/ai-preview`。
- [ ] 实现 `POST /api/generation/calendars/ai-preview`。
- [ ] AI 成功后进入 `PREVIEW_READY`。
- [ ] AI 失败后进入 `FAILED`。
- [ ] AI 成功后继续复用 04 阶段导出流程。
- [ ] 手动生成流程不依赖 AI 配置。
- [ ] 增加 AI 成功任务状态测试。
- [ ] 增加 AI 失败任务状态测试。

### 前端 AI 模式

- [ ] 教案生成向导增加“手动 / AI”模式切换。
- [ ] 教学日历生成向导增加“手动 / AI”模式切换。
- [ ] AI 生成时展示轻量进度状态。
- [ ] AI 失败时展示失败原因。
- [ ] AI 失败时提供重试。
- [ ] AI 成功后进入预览编辑。
- [ ] 终端命令展示 AI 生成状态反馈。

## 验收 Todo

- [ ] 能用 AI 生成教案 JSON。
- [ ] 能用 AI 生成教学日历 JSON。
- [ ] 能预览和编辑 AI 结果。
- [ ] 确认后仍由程序导出 Word / Excel。
- [ ] AI 失败有任务记录和明确错误。
- [ ] 手动生成不受 AI 影响。
- [ ] AI API Key 不出现在日志和前端产物中。
- [ ] `mvn -f simon-workspace-api/pom.xml test` 通过。
- [ ] `npm run build --prefix simon-workspace-web` 通过。

## 进度记录

```text
2026-07-04:
- AI 阶段未开始。
- 当前应先完成 04 阶段导出闭环，再接 AI。
```
