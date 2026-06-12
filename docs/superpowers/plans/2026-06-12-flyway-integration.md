# Flyway Integration Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add Flyway-based MySQL schema migration for the Simon Workspace API foundation stage.

**Architecture:** Spring Boot will manage database migrations at API startup using Flyway scripts under `src/main/resources/db/migration`. Tests use the `test` profile to avoid requiring a local MySQL instance while a migration structure test verifies the baseline SQL file contains the required foundation tables.

**Tech Stack:** Spring Boot 3, Flyway, MySQL Connector/J, Maven, JUnit 5.

---

### Task 1: Migration Guard Test

**Files:**
- Create: `simon-workspace-api/src/test/java/com/simon/workspace/infrastructure/persistence/InitialSchemaMigrationTests.java`

- [ ] **Step 1: Write failing migration structure test**

Add a test that loads `db/migration/V1__init_schema.sql` from the classpath and asserts that it creates `user`, `role`, `permission`, `user_role`, `role_permission`, and `login_log`.

- [ ] **Step 2: Run test and verify it fails**

Run: `mvn -f simon-workspace-api/pom.xml test`

Expected: fail because `V1__init_schema.sql` does not exist yet.

### Task 2: Flyway Dependencies and Configuration

**Files:**
- Modify: `simon-workspace-api/pom.xml`
- Modify: `simon-workspace-api/src/main/resources/application.yml`
- Create: `simon-workspace-api/src/test/resources/application-test.yml`
- Modify: `simon-workspace-api/src/test/java/com/simon/workspace/SimonWorkspaceApiApplicationTests.java`

- [ ] **Step 1: Add dependencies**

Add `spring-boot-starter-jdbc`, `mysql-connector-j`, `flyway-core`, and `flyway-mysql`.

- [ ] **Step 2: Add runtime configuration**

Configure datasource and Flyway from environment variables.

- [ ] **Step 3: Add test profile**

Disable datasource and Flyway auto-configuration in the test profile so `mvn test` does not require MySQL.

### Task 3: Initial Schema Migration

**Files:**
- Create: `simon-workspace-api/src/main/resources/db/migration/V1__init_schema.sql`
- Modify: `deploy/docker-compose.yml`

- [ ] **Step 1: Create initial SQL migration**

Create foundation tables: `user`, `role`, `permission`, `user_role`, `role_permission`, and `login_log`.

- [ ] **Step 2: Pass database env vars to API container**

Add MySQL environment variables to the API service in Docker Compose.

- [ ] **Step 3: Run verification**

Run:

```bash
mvn -f simon-workspace-api/pom.xml test
npm run build --prefix simon-workspace-web
```

Expected: both commands pass.

- [ ] **Step 4: Commit and push**

Run:

```bash
git add .
git commit -m "feat(api): add Flyway database migrations"
git push
```
