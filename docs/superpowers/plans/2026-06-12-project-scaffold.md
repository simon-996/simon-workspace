# Simon Workplace Project Scaffold Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Create a monorepo scaffold for the Simon Workplace API, web app, Docker deployment, and Jenkins pipeline.

**Architecture:** The repository is a single Git monorepo with separate `simon-workplace-api` and `simon-workplace-web` applications plus shared deployment files at the root. Jenkins builds both apps and produces Docker images that can be orchestrated by Docker Compose.

**Tech Stack:** Spring Boot 3, Maven, Java 17, Vue 3, Vite, TypeScript, Pinia, Vue Router, Naive UI, Tailwind CSS, Docker, Jenkins.

---

### Task 1: Repository Baseline

**Files:**
- Create: `.gitignore`
- Create: `README.md`
- Create: `docs/superpowers/plans/2026-06-12-project-scaffold.md`
- Existing: `simon-workplace-doc.md`

- [ ] **Step 1: Initialize Git repository**

Run: `git init`

Expected: a new repository is created in `D:\CodeFile\simon\simon-workplace`.

- [ ] **Step 2: Add root documentation and ignore rules**

Create `.gitignore` with Java, Node, IDE, runtime, and local deployment exclusions.

- [ ] **Step 3: Commit baseline**

Run: `git add .gitignore README.md simon-workplace-doc.md docs/superpowers/plans/2026-06-12-project-scaffold.md && git commit -m "chore: initialize monorepo documentation"`

Expected: one baseline commit containing documentation and repository hygiene.

### Task 2: API Scaffold

**Files:**
- Create: `simon-workplace-api/pom.xml`
- Create: `simon-workplace-api/src/main/java/com/simon/workplace/SimonWorkplaceApiApplication.java`
- Create: `simon-workplace-api/src/main/java/com/simon/workplace/common/ApiResponse.java`
- Create: `simon-workplace-api/src/main/java/com/simon/workplace/health/HealthController.java`
- Create: `simon-workplace-api/src/main/resources/application.yml`
- Create: `simon-workplace-api/src/test/java/com/simon/workplace/SimonWorkplaceApiApplicationTests.java`
- Create: `simon-workplace-api/Dockerfile`
- Create: `simon-workplace-api/README.md`

- [ ] **Step 1: Add Maven Spring Boot skeleton**

Create the files above with a minimal API application, a health endpoint, and test context loading.

- [ ] **Step 2: Verify API build**

Run: `mvn -f simon-workplace-api/pom.xml test`

Expected: Maven exits with code 0.

- [ ] **Step 3: Commit API scaffold**

Run: `git add simon-workplace-api && git commit -m "feat(api): scaffold Spring Boot service"`

Expected: one commit containing the backend scaffold.

### Task 3: Web Scaffold

**Files:**
- Create: `simon-workplace-web/package.json`
- Create: `simon-workplace-web/src/**`
- Create: `simon-workplace-web/index.html`
- Create: `simon-workplace-web/vite.config.ts`
- Create: `simon-workplace-web/Dockerfile`
- Create: `simon-workplace-web/nginx.conf`
- Create: `simon-workplace-web/README.md`

- [ ] **Step 1: Generate Vue 3 TypeScript app**

Use Vite to generate the Vue TypeScript project into `simon-workplace-web`.

- [ ] **Step 2: Add project libraries**

Install Pinia, Vue Router, Naive UI, Axios, Tailwind CSS, PostCSS, Autoprefixer, GSAP, and VueUse Motion.

- [ ] **Step 3: Replace demo screen with project shell**

Create a minimal public home/workspace shell with routing and Naive UI provider.

- [ ] **Step 4: Verify web build**

Run: `npm run build --prefix simon-workplace-web`

Expected: Vite exits with code 0 and writes `simon-workplace-web/dist`.

- [ ] **Step 5: Commit web scaffold**

Run: `git add simon-workplace-web && git commit -m "feat(web): scaffold Vue workplace frontend"`

Expected: one commit containing the frontend scaffold.

### Task 4: Deployment Scaffold

**Files:**
- Create: `deploy/docker-compose.yml`
- Create: `deploy/nginx/default.conf`
- Create: `deploy/mysql/init/01-init.sql`
- Create: `deploy/.env.example`
- Create: `Jenkinsfile`

- [ ] **Step 1: Add Docker Compose deployment skeleton**

Create services for API, web, MySQL, Redis, and MinIO.

- [ ] **Step 2: Add Jenkins pipeline**

Create stages for checkout, API test/package, web install/build, Docker build, and deploy placeholder.

- [ ] **Step 3: Commit deployment scaffold**

Run: `git add deploy Jenkinsfile README.md && git commit -m "chore(deploy): add Docker and Jenkins scaffold"`

Expected: one commit containing deployment and CI/CD files.

### Task 5: Final Verification

**Files:**
- Read: repository status and recent commit log

- [ ] **Step 1: Run API verification**

Run: `mvn -f simon-workplace-api/pom.xml test`

Expected: Maven exits with code 0.

- [ ] **Step 2: Run web verification**

Run: `npm run build --prefix simon-workplace-web`

Expected: Vite exits with code 0.

- [ ] **Step 3: Inspect Git status and log**

Run: `git status --short` and `git log --oneline --decorate -5`

Expected: no uncommitted scaffold changes except generated local build output ignored by `.gitignore`; recent commits show the planned commit sequence.
