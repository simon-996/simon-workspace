# Public Course Materials Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add public course pages with documents, courseware, and resources, managed from the workspace.

**Architecture:** Extend the existing course module instead of creating a parallel teaching platform. Store public materials in a single `course_material` table, expose unauthenticated read APIs separately from protected workspace management APIs, and reuse the existing file storage/reference model for uploaded materials.

**Tech Stack:** Spring Boot, JdbcTemplate, Flyway SQL migrations, Sa-Token permissions, Vue 3, Naive UI, Vue Router, md-editor-v3.

---

### Task 1: Backend Data Model And API

**Files:**
- Create: `simon-workspace-api/src/main/resources/db/migration/V13__create_course_material_tables.sql`
- Create: `simon-workspace-api/src/main/java/com/simon/workspace/course/dto/CourseMaterialRequest.java`
- Create: `simon-workspace-api/src/main/java/com/simon/workspace/course/dto/CourseMaterialResponse.java`
- Create: `simon-workspace-api/src/main/java/com/simon/workspace/course/dto/PublicCourseDetailResponse.java`
- Modify: `simon-workspace-api/src/main/java/com/simon/workspace/course/CourseService.java`
- Modify: `simon-workspace-api/src/main/java/com/simon/workspace/course/CourseController.java`
- Modify: `simon-workspace-api/src/main/java/com/simon/workspace/file/FileResourceService.java`

- [x] Write service/controller tests for public courses and material validation.
- [x] Add Flyway migration for public course fields and `course_material`.
- [x] Add DTOs for material create/update, material response, and public course detail.
- [x] Extend `CourseService` with public list/detail, material CRUD, and file reference sync.
- [x] Add protected workspace material endpoints under `/api/courses/{id}/materials`.
- [x] Add public read endpoints under `/api/public/courses`.
- [x] Add public file view/download helpers that only allow public active files.
- [x] Run backend tests and commit.

### Task 2: Frontend API And Public Pages

**Files:**
- Modify: `simon-workspace-web/src/api/workspace.ts`
- Modify: `simon-workspace-web/src/router/index.ts`
- Create: `simon-workspace-web/src/views/course/PublicCourseListView.vue`
- Create: `simon-workspace-web/src/views/course/PublicCourseDetailView.vue`
- Create: `simon-workspace-web/src/views/course/PublicMarkdownView.vue`
- Modify: `simon-workspace-web/src/utils/appHeaderNav.ts`
- Modify: `simon-workspace-web/src/i18n/locales/en.ts`
- Modify: `simon-workspace-web/src/i18n/locales/zh-CN.ts`
- Modify: `simon-workspace-web/src/i18n/locales/th-TH.ts`

- [x] Write frontend source tests for course routes, API functions, and public views.
- [x] Add public course API types and functions.
- [x] Add `/courses`, `/courses/:id`, and `/courses/:id/materials/:materialId/read` routes.
- [x] Add course navigation support matching blog-style active behavior.
- [x] Build list/detail/Markdown reader pages using the existing minimal visual style.
- [x] Run frontend tests/build and commit.

### Task 3: Workspace Material Management

**Files:**
- Modify: `simon-workspace-web/src/views/workspace/CourseManagementView.vue`
- Modify: `simon-workspace-web/src/api/workspace.ts`
- Modify: locale files under `simon-workspace-web/src/i18n/locales/`

- [x] Write frontend source tests for material manager controls.
- [x] Extend course form with public visibility and sort order.
- [x] Add a selected-course material panel with Documents, Courseware, and Resources tabs.
- [x] Support adding file materials with public uploads.
- [x] Support adding link materials with URL validation.
- [x] Support editing status/sort/title/description and deleting materials.
- [x] Run frontend tests/build and commit.
