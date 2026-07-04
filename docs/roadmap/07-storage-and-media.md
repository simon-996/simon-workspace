# 07 Storage and Media 阶段

最后更新：2026-07-04

## 阶段目标

把项目里的文件、图片和后续博客媒体资源统一纳入可切换的存储体系。第一期先做到“可配置、可切换、可测试、可追踪引用”，后续再补齐清理、迁移、签名访问和图片处理。

## 本期已完成

### 多存储接入第一期

- [x] 新增 `app.storage` 配置结构，密钥通过 yml / 环境变量注入，不写入数据库。
- [x] 支持 `LOCAL`、`TENCENT_COS`、`ALIYUN_OSS`、`CLOUDFLARE_R2` 四类 provider。
- [x] 腾讯 COS、阿里 OSS、Cloudflare R2 第一期统一走 S3-compatible 适配层，减少依赖和构建成本。
- [x] 新增 `storage_provider_state` 表，记录 provider 是否默认、最近测试状态和测试时间。
- [x] 新增 `/api/storage/providers` 列表接口。
- [x] 新增 `/api/storage/providers/{code}/test` 连接测试接口。
- [x] 新增 `/api/storage/providers/{code}/activate` 默认存储切换接口。
- [x] 新增 `/workspace/storage` 工作台页面，可查看配置状态、测试连接、切换默认存储。
- [x] 工作台导航按 `file:manage` 权限展示存储入口。

### 文件元数据改造

- [x] `file_resource` 新增 `storage_provider`、`object_key`、`visibility`、`public_url`、`orphaned_time` 字段。
- [x] 文件保存改为写入当前激活 provider。
- [x] 文件下载改为按文件记录中的 provider 和 object key 读取。
- [x] 新增 `POST /api/files` 上传接口，供后续博客、项目页、编辑器复用。
- [x] 文件中心展示存储 provider 和公开/私有状态。

### 幽灵文件预防第一版

- [x] 新增 `file_reference` 表，记录业务内容和文件之间的引用关系。
- [x] 新增 `FileReferenceService.syncReferences(...)`，业务保存内容时可同步当前引用列表。
- [x] 当引用被移除且没有其他有效引用时，把文件标记为 `ORPHANED` 并记录 `orphaned_time`。
- [x] 当文件重新被引用时，把状态恢复为 `ACTIVE`。
- [x] 暂不立即物理删除孤儿文件，避免误删；后续由清理任务按保留期处理。

## 待完成 Todo

### 博客和编辑器接入

- [ ] 博客文章新增/编辑时支持上传图片。
- [ ] 博客文章保存时解析正文图片，调用 `FileReferenceService.syncReferences(...)`。
- [ ] 博客文章删除或下架时断开文章文件引用。
- [ ] 项目展示、个人资料等富文本内容复用同一套引用同步。
- [ ] 前端编辑器插入图片时保存 `fileId`，避免只靠 URL 反查文件。

### 孤儿文件清理

- [ ] 增加孤儿文件保留期配置，例如 7 天或 30 天。
- [ ] 增加定时任务扫描 `status = ORPHANED` 且超过保留期的文件。
- [ ] 清理任务先软删除元数据，再删除 provider 中的物理对象。
- [ ] 记录清理日志：fileId、provider、objectKey、原因、结果。
- [ ] 工作台增加孤儿文件筛选、恢复、手动清理入口。
- [ ] 增加“只扫描不删除”的 dry-run 模式。

### 存储运维能力

- [ ] 增加 provider 级别的容量、对象数量和最近错误展示。
- [ ] 增加 provider 之间的文件迁移工具。
- [ ] 增加迁移校验：源对象存在、目标对象存在、文件大小一致。
- [ ] 增加私有文件临时签名 URL。
- [ ] 增加图片缩略图、压缩和格式转换策略。
- [ ] 增加对象存储回收站或版本保留策略说明。
- [ ] 将 `file:manage` 细拆为 `file:manage` 和 `storage:manage`。

### 真实环境验收

- [ ] 在服务器用 LOCAL 上传、下载、删除一轮。
- [ ] 用腾讯 COS 真实配置测试连接。
- [ ] 用阿里 OSS 真实配置测试连接。
- [ ] 用 Cloudflare R2 真实配置测试连接。
- [ ] 分别上传图片、文档，确认 object key、public URL、下载都正确。
- [ ] 确认密钥不会出现在日志、接口响应和前端页面中。

## 当前设计说明

- 密钥留在 yml / 环境变量中，工作台只显示 endpoint、bucket、public URL 和测试状态。
- 默认写入 provider 存在数据库中，方便你在工作台切换，不需要重启服务。
- 文件记录同时保留 `storage_path` 和 `object_key`，前者兼容历史数据，后者作为对象存储主键。
- 幽灵文件第一期只做“引用追踪 + 孤儿标记”，真正删除放到有保留期的清理任务里。
- 三家云第一期共用 S3-compatible 适配层；如果后续遇到 ACL、签名或区域特殊能力，再引入对应官方 SDK。

## 验收 Todo

- [x] `mvn -f simon-workspace-api/pom.xml test` 通过。2026-07-04 已验证。
- [x] `npm run build --prefix simon-workspace-web` 通过。2026-07-04 已验证。
- [ ] 工作台 `/workspace/storage` 可打开并显示四个 provider。
- [ ] LOCAL provider 测试连接成功。
- [ ] 切换默认存储后，新上传文件写入目标 provider。
- [ ] 删除业务正文中的图片引用后，对应文件会变为 `ORPHANED`。

## 进度记录

```text
2026-07-04:
- 完成多存储第一期后端配置、provider 抽象、管理接口和工作台页面。
- 完成文件引用关系和孤儿标记的第一版预防机制。
- 当前还未接博客编辑器，孤儿文件也暂不做物理清理。
```
