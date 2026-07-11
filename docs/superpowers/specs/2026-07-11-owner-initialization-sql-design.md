# 通用 OWNER 初始化 SQL 设计

## 目标

在数据库迁移完成后，通过仓库内长期维护的通用 SQL 手动创建可登录的 OWNER 账号。脚本适用于开发、测试和生产等不同环境，不由 Flyway 自动执行，也不在仓库中保存真实账号密码。

## 交付内容

- 新增 `simon-workspace-api/scripts/init-owner.sql`。
- 在后端 README 中说明配置、执行、结果和安全注意事项。
- 添加脚本契约测试，并在一次性测试库中验证实际 MySQL 行为。

## SQL 配置

脚本顶部集中定义以下 MySQL 会话变量：

- `@owner_username`
- `@owner_nickname`
- `@owner_email`
- `@owner_password`

仓库版本使用 `CHANGE_ME_*` 占位值。使用者切换到目标环境后修改这些变量，执行脚本，然后将文件恢复为仓库版本，避免误提交真实凭据。

## 执行流程

1. 检查四项配置是否仍为占位值或必填项为空。
2. 检查数据库是否存在未删除的 `OWNER` 角色。
3. 检查指定用户名是否已存在。
4. 在事务中插入状态为 `ENABLED` 的用户，并设置审核时间。
5. 使用 MySQL `SHA2(password, 256)` 生成哈希，并以 `sha256:` 为前缀保存，使其与 `PasswordHashVerifier` 兼容。
6. 仅为本次新创建的用户写入 `user_role`，绑定 `OWNER` 角色。
7. 提交事务并输出明确的执行状态。

## 重复执行和多 OWNER 行为

- 数据库已有 OWNER 时，仍允许使用不同用户名新增 OWNER。
- 指定用户名已存在时，不覆盖用户资料或密码，也不为已有普通用户补授 OWNER。
- 使用同一用户名重复执行不会产生重复用户或角色关系。
- 缺少 OWNER 角色、配置未完成或插入失败时，不留下只有用户而没有角色的部分数据。

## 执行状态

脚本最终返回以下状态之一：

- `OWNER_CREATED`：OWNER 创建并绑定成功。
- `CONFIG_REQUIRED`：配置仍为占位值或缺少必填项。
- `OWNER_ROLE_MISSING`：基础角色迁移尚未完成。
- `USERNAME_EXISTS`：用户名已存在，未修改任何数据。
- `OWNER_NOT_CREATED`：其他条件导致未创建。

## 使用方式

使用者先运行后端或 Flyway，使数据库完成至少 V1、V9 和 V15 迁移，再编辑并执行脚本：

```bash
mysql -h <host> -P <port> -u <user> -p <database> < simon-workspace-api/scripts/init-owner.sql
```

执行成功后立即恢复脚本中的占位值，并通过登录接口验证账号。

## 测试策略

- 契约测试检查仓库版本不包含真实默认凭据。
- 契约测试检查脚本使用 `sha256:`、`SHA2`、`ENABLED`、事务和 OWNER 角色绑定。
- 在一次性 MySQL 测试库中依次验证：首次创建、同用户名重复执行、不同用户名创建第二个 OWNER。
- 验证生成的密码哈希可被当前 `PasswordHashVerifier` 接受。
- 运行完整后端测试，确保现有认证与迁移测试不回归。

## 非目标

- 不通过 Flyway 自动创建 OWNER。
- 不在应用启动时读取初始化账号环境变量。
- 不覆盖已有用户的密码或角色。
- 不自动删除、降级或合并已有 OWNER。
