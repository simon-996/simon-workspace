# OWNER Initialization SQL Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a repository-managed, manually editable SQL script that creates a new enabled user and grants the OWNER role without overwriting existing users.

**Architecture:** Keep bootstrap behavior outside Flyway in `simon-workspace-api/scripts/init-owner.sql`. The script uses explicit session variables, guards placeholder and duplicate usernames, hashes the password with the application's existing `sha256:` format, and inserts the user plus role relationship in one transaction. A focused JUnit contract test protects the script's security and data-flow invariants, while a disposable MySQL schema verifies real execution behavior.

**Tech Stack:** MySQL 8/9 SQL, Spring Boot 3.3, Java 17, JUnit 5, AssertJ, Maven.

---

### Task 1: Add the SQL contract test and implementation

**Files:**
- Create: `simon-workspace-api/src/test/java/com/simon/workspace/infrastructure/persistence/OwnerInitializationScriptTests.java`
- Create: `simon-workspace-api/scripts/init-owner.sql`

- [ ] **Step 1: Write the failing contract test**

```java
package com.simon.workspace.infrastructure.persistence;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class OwnerInitializationScriptTests {

    private static final Path SCRIPT = Path.of("scripts/init-owner.sql");

    @Test
    void requiresExplicitCredentialsAndUsesTheApplicationPasswordFormat() throws IOException {
        String script = readScript();

        assertThat(script)
                .contains("SET @owner_username = 'CHANGE_ME_USERNAME';")
                .contains("SET @owner_nickname = 'CHANGE_ME_NICKNAME';")
                .contains("SET @owner_email = 'CHANGE_ME_EMAIL';")
                .contains("SET @owner_password = 'CHANGE_ME_PASSWORD';")
                .contains("CONCAT('sha256:', SHA2(@owner_password, 256))");
    }

    @Test
    void createsOnlyANewEnabledUserAndBindsTheOwnerRole() throws IOException {
        String script = readScript();

        assertThat(script)
                .contains("START TRANSACTION;")
                .contains("INSERT INTO `user`")
                .contains("'ENABLED'")
                .contains("INSERT INTO `user_role`")
                .contains("role_code = 'OWNER'")
                .contains("USERNAME_EXISTS")
                .contains("OWNER_CREATED")
                .contains("COMMIT;")
                .doesNotContain("UPDATE `user`");
    }

    private String readScript() throws IOException {
        assertThat(SCRIPT).as("owner initialization script should exist").exists();
        return Files.readString(SCRIPT);
    }
}
```

- [ ] **Step 2: Run the focused test and verify RED**

Run:

```bash
mvn -s maven-settings.xml -Dtest=OwnerInitializationScriptTests test
```

Expected: FAIL at `owner initialization script should exist` because `scripts/init-owner.sql` has not been created.

- [ ] **Step 3: Add the minimal owner initialization SQL**

```sql
-- Configure these values for the target environment before running this file.
-- Restore the CHANGE_ME values immediately after execution so credentials are not committed.
SET @owner_username = 'CHANGE_ME_USERNAME';
SET @owner_nickname = 'CHANGE_ME_NICKNAME';
SET @owner_email = 'CHANGE_ME_EMAIL';
SET @owner_password = 'CHANGE_ME_PASSWORD';

SET @config_ready = (
    @owner_username IS NOT NULL
    AND TRIM(@owner_username) <> ''
    AND @owner_username <> 'CHANGE_ME_USERNAME'
    AND @owner_nickname IS NOT NULL
    AND TRIM(@owner_nickname) <> ''
    AND @owner_nickname <> 'CHANGE_ME_NICKNAME'
    AND @owner_password IS NOT NULL
    AND @owner_password <> 'CHANGE_ME_PASSWORD'
    AND CHAR_LENGTH(@owner_password) >= 8
);
SET @normalized_owner_email = NULLIF(NULLIF(TRIM(@owner_email), ''), 'CHANGE_ME_EMAIL');
SET @owner_role_id = (
    SELECT id
    FROM `role`
    WHERE role_code = 'OWNER' AND deleted = 0
    LIMIT 1
);
SET @username_exists = EXISTS(
    SELECT 1
    FROM `user`
    WHERE username = @owner_username
);
SET @created_user_id = NULL;
SET @owner_role_bound = 0;

START TRANSACTION;

INSERT INTO `user` (
    username,
    password_hash,
    nickname,
    email,
    status,
    reviewed_time,
    review_remark
)
SELECT
    @owner_username,
    CONCAT('sha256:', SHA2(@owner_password, 256)),
    @owner_nickname,
    @normalized_owner_email,
    'ENABLED',
    NOW(),
    'Initialized by scripts/init-owner.sql'
FROM DUAL
WHERE @config_ready = 1
  AND @owner_role_id IS NOT NULL
  AND @username_exists = 0;

SET @created_user_id = IF(ROW_COUNT() = 1, LAST_INSERT_ID(), NULL);

INSERT INTO `user_role` (user_id, role_id)
SELECT @created_user_id, @owner_role_id
FROM DUAL
WHERE @created_user_id IS NOT NULL;

SET @owner_role_bound = ROW_COUNT();

DELETE FROM `user`
WHERE id = @created_user_id
  AND @created_user_id IS NOT NULL
  AND @owner_role_bound <> 1;

SET @created_user_id = IF(@owner_role_bound = 1, @created_user_id, NULL);

COMMIT;

SELECT
    CASE
        WHEN @config_ready = 0 THEN 'CONFIG_REQUIRED'
        WHEN @owner_role_id IS NULL THEN 'OWNER_ROLE_MISSING'
        WHEN @username_exists = 1 THEN 'USERNAME_EXISTS'
        WHEN @created_user_id IS NOT NULL THEN 'OWNER_CREATED'
        ELSE 'OWNER_NOT_CREATED'
    END AS initialization_status,
    @created_user_id AS owner_user_id,
    IF(@created_user_id IS NOT NULL, @owner_username, NULL) AS owner_username;
```

- [ ] **Step 4: Run the focused test and verify GREEN**

Run:

```bash
mvn -s maven-settings.xml -Dtest=OwnerInitializationScriptTests test
```

Expected: 2 tests run, 0 failures, 0 errors.

- [ ] **Step 5: Commit the script and contract test**

```bash
git add simon-workspace-api/scripts/init-owner.sql \
  simon-workspace-api/src/test/java/com/simon/workspace/infrastructure/persistence/OwnerInitializationScriptTests.java
git commit -m "feat(api): add owner initialization sql"
```

### Task 2: Document safe cross-environment usage

**Files:**
- Modify: `simon-workspace-api/README.md`

- [ ] **Step 1: Add the initialization instructions after the migration section**

```markdown
## Initialize an OWNER Account

After Flyway has applied the database migrations, edit the four variables at the top of:

```text
scripts/init-owner.sql
```

Run it against the target database:

```bash
mysql -h <host> -P <port> -u <user> -p <database> < scripts/init-owner.sql
```

The script creates a new enabled user and grants the `OWNER` role. Existing OWNER accounts do not block creating another OWNER, but an existing username is never modified or granted additional privileges.

Expected result:

```text
OWNER_CREATED
```

Immediately restore the placeholders after execution so the real password cannot be committed:

```bash
git restore scripts/init-owner.sql
```

Other result codes are `CONFIG_REQUIRED`, `OWNER_ROLE_MISSING`, `USERNAME_EXISTS`, and `OWNER_NOT_CREATED`.
```

- [ ] **Step 2: Run the focused test and check the diff**

Run:

```bash
mvn -s maven-settings.xml -Dtest=OwnerInitializationScriptTests test
git diff --check
```

Expected: 2 tests pass and `git diff --check` produces no output.

- [ ] **Step 3: Commit the documentation**

```bash
git add simon-workspace-api/README.md
git commit -m "docs(api): document owner initialization"
```

### Task 3: Verify real MySQL behavior and the complete backend suite

**Files:**
- Verify only; no repository files should change.

- [ ] **Step 1: Create a disposable schema and apply the required migrations**

Run:

```bash
docker exec mysql-docker sh -lc 'mysql -uroot -p"$MYSQL_ROOT_PASSWORD" -e "DROP DATABASE IF EXISTS simon_workspace_owner_init_test; CREATE DATABASE simon_workspace_owner_init_test CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"'
docker exec -i mysql-docker sh -lc 'mysql -uroot -p"$MYSQL_ROOT_PASSWORD" simon_workspace_owner_init_test' < simon-workspace-api/src/main/resources/db/migration/V1__init_schema.sql
docker exec -i mysql-docker sh -lc 'mysql -uroot -p"$MYSQL_ROOT_PASSWORD" simon_workspace_owner_init_test' < simon-workspace-api/src/main/resources/db/migration/V9__seed_rbac_permissions.sql
docker exec -i mysql-docker sh -lc 'mysql -uroot -p"$MYSQL_ROOT_PASSWORD" simon_workspace_owner_init_test' < simon-workspace-api/src/main/resources/db/migration/V15__add_account_review_fields.sql
```

Expected: commands exit 0 and the temporary schema contains `user`, `role`, and `user_role`.

- [ ] **Step 2: Create the first OWNER, verify duplicate protection, then create a second OWNER**

Run the script through substitutions without modifying the tracked file:

```bash
sed -e 's/CHANGE_ME_USERNAME/owner_one/' \
  -e 's/CHANGE_ME_NICKNAME/Owner One/' \
  -e 's/CHANGE_ME_EMAIL/owner-one@example.test/' \
  -e 's/CHANGE_ME_PASSWORD/owner-one-secret/' \
  simon-workspace-api/scripts/init-owner.sql \
  | docker exec -i mysql-docker sh -lc 'mysql -uroot -p"$MYSQL_ROOT_PASSWORD" simon_workspace_owner_init_test'
```

Expected: `OWNER_CREATED`.

Run the same command again. Expected: `USERNAME_EXISTS`.

Run it a third time with `owner_two`, `Owner Two`, `owner-two@example.test`, and `owner-two-secret`. Expected: `OWNER_CREATED`.

- [ ] **Step 3: Verify both users have OWNER and compatible hashes**

```bash
docker exec mysql-docker sh -lc 'mysql -uroot -p"$MYSQL_ROOT_PASSWORD" -N -e "SELECT u.username, u.status, r.role_code, u.password_hash LIKE '\''sha256:%'\'' FROM simon_workspace_owner_init_test.user u JOIN simon_workspace_owner_init_test.user_role ur ON ur.user_id = u.id JOIN simon_workspace_owner_init_test.role r ON r.id = ur.role_id ORDER BY u.username;"'
```

Expected:

```text
owner_one ENABLED OWNER 1
owner_two ENABLED OWNER 1
```

- [ ] **Step 4: Remove the disposable schema**

```bash
docker exec mysql-docker sh -lc 'mysql -uroot -p"$MYSQL_ROOT_PASSWORD" -e "DROP DATABASE simon_workspace_owner_init_test;"'
```

Expected: command exits 0.

- [ ] **Step 5: Run the complete backend test suite**

```bash
mvn -s maven-settings.xml test
git status --short
```

Expected: all backend tests pass; only previously existing user changes remain outside the implementation worktree.
