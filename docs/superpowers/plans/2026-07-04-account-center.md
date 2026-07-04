# Account Center Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a shared account center from the header account menu where logged-in users can edit profile data, upload/crop an avatar, and change password without losing the current session.

**Architecture:** Backend adds self-service auth endpoints under `/api/auth/me` so users can update only their own profile and password. Avatar upload reuses the existing file storage pipeline with a new `AVATAR` source type and public visibility, while the frontend crops locally before uploading the final image. Frontend keeps the account center in a focused `AccountCenterModal.vue` component opened from `AppHeader.vue`.

**Tech Stack:** Spring Boot, Sa-Token, JdbcTemplate, MySQL, Vue 3, Pinia, Naive UI, TypeScript, vue-advanced-cropper, Vitest, Maven tests.

---

### Task 1: Backend Self-Service Account API

**Files:**
- Create: `simon-workspace-api/src/main/java/com/simon/workspace/auth/dto/ProfileUpdateRequest.java`
- Create: `simon-workspace-api/src/main/java/com/simon/workspace/auth/dto/PasswordUpdateRequest.java`
- Modify: `simon-workspace-api/src/main/java/com/simon/workspace/auth/AuthController.java`
- Modify: `simon-workspace-api/src/main/java/com/simon/workspace/auth/AuthService.java`
- Modify: `simon-workspace-api/src/main/java/com/simon/workspace/auth/AuthAccountService.java`
- Test: `simon-workspace-api/src/test/java/com/simon/workspace/auth/AuthServiceProfileTests.java`

- [ ] **Step 1: Write failing service tests**

Create tests proving:
- `updateProfile` trims nickname/email/avatar URL and returns refreshed `CurrentUser`.
- `updatePassword` verifies the current password, writes `sha256:<hash>`, and does not call logout.
- wrong current password throws `AUTH_BAD_CREDENTIALS`.

Run: `mvn -q -Dtest=AuthServiceProfileTests test`
Expected: FAIL because `ProfileUpdateRequest`, `PasswordUpdateRequest`, and service methods do not exist.

- [ ] **Step 2: Implement request DTOs and service methods**

Add request records with Jakarta validation:
- `ProfileUpdateRequest(String nickname, String email, String avatarUrl)`
- `PasswordUpdateRequest(String currentPassword, String newPassword)`

Add `AuthService.updateProfile` and `AuthService.updatePassword`.

- [ ] **Step 3: Expose controller routes**

Add:
- `PUT /api/auth/me/profile`
- `PUT /api/auth/me/password`

Both use `AuthContextHolder.requireUser().id()` and return `CurrentUserResponse` or `Void`.

- [ ] **Step 4: Run backend targeted tests**

Run: `mvn -q -Dtest=AuthServiceProfileTests test`
Expected: PASS.

- [ ] **Step 5: Commit backend account API**

Commit message: `feat(api): add account self-service endpoints`

### Task 2: Avatar Source Type

**Files:**
- Modify: `simon-workspace-api/src/main/java/com/simon/workspace/file/FileResourceService.java`
- Test: `simon-workspace-api/src/test/java/com/simon/workspace/file/FileResourceServiceSourceTypeTests.java`

- [ ] **Step 1: Write failing source type test**

Create a focused test proving `sourceType=AVATAR` is accepted and persisted as `AVATAR`.

Run: `mvn -q -Dtest=FileResourceServiceSourceTypeTests test`
Expected: FAIL because `AVATAR` is rejected.

- [ ] **Step 2: Add `AVATAR` to source type normalization**

Update `normalizeSourceType` to allow `AVATAR`.

- [ ] **Step 3: Run backend targeted tests**

Run: `mvn -q -Dtest=FileResourceServiceSourceTypeTests test`
Expected: PASS.

- [ ] **Step 4: Commit avatar upload source support**

Commit message: `feat(api): support avatar file resources`

### Task 3: Frontend API and Auth Store

**Files:**
- Modify: `simon-workspace-web/src/stores/auth.ts`
- Create: `simon-workspace-web/src/stores/auth.profile.test.ts`
- Modify: `simon-workspace-web/src/api/workspace.ts`

- [ ] **Step 1: Write failing frontend store tests**

Add raw-source tests proving the auth store has:
- `updateProfile(payload)`
- `updatePassword(payload)`
- `uploadAvatar(file)`
- `this.user = updatedUser` after profile save.

Run: `npm run test -- src/stores/auth.profile.test.ts`
Expected: FAIL because methods do not exist.

- [ ] **Step 2: Implement API helpers and store actions**

Add TypeScript payload types and actions:
- `updateProfile`
- `updatePassword`
- `uploadAvatar` using `FormData` with `sourceType=AVATAR` and `visibility=PUBLIC`.

- [ ] **Step 3: Run frontend targeted tests**

Run: `npm run test -- src/stores/auth.profile.test.ts`
Expected: PASS.

- [ ] **Step 4: Commit frontend account API plumbing**

Commit message: `feat(web): add account profile api actions`

### Task 4: Account Center Modal

**Files:**
- Create: `simon-workspace-web/src/components/AccountCenterModal.vue`
- Create: `simon-workspace-web/src/components/AccountCenterModal.test.ts`
- Modify: `simon-workspace-web/src/components/AppHeader.vue`
- Modify: `simon-workspace-web/src/components/AppHeader.test.ts`
- Modify: `simon-workspace-web/src/i18n/locales/en.ts`
- Modify: `simon-workspace-web/src/i18n/locales/zh-CN.ts`
- Modify: `simon-workspace-web/src/i18n/locales/th-TH.ts`
- Modify: `simon-workspace-web/src/i18n/language.test.ts`

- [ ] **Step 1: Write failing modal and header tests**

Tests prove:
- Account menu contains `profile` and `logout`, not `workspace`.
- Header renders `AccountCenterModal`.
- Modal includes profile, avatar, password tabs.
- Password confirmation mismatch sets an error status and disables save.
- i18n account center labels exist in every locale.

Run: `npm run test -- src/components/AppHeader.test.ts src/components/AccountCenterModal.test.ts src/i18n/language.test.ts`
Expected: FAIL.

- [ ] **Step 2: Implement modal UI**

Use Naive UI modal/tabs/form inputs/buttons. Keep styling aligned with the homepage/header: quiet white surface, 8px radius, restrained colors, compact copy.

- [ ] **Step 3: Wire header account menu**

Replace the workspace action with profile action. Open modal on profile selection. Keep logout behavior unchanged.

- [ ] **Step 4: Run frontend targeted tests**

Run: `npm run test -- src/components/AppHeader.test.ts src/components/AccountCenterModal.test.ts src/i18n/language.test.ts`
Expected: PASS.

- [ ] **Step 5: Commit account center UI**

Commit message: `feat(web): add account center modal`

### Task 5: Avatar Cropper

**Files:**
- Create: `simon-workspace-web/src/utils/avatarCrop.ts`
- Create: `simon-workspace-web/src/utils/avatarCrop.test.ts`
- Modify: `simon-workspace-web/src/components/AccountCenterModal.vue`
- Modify: `simon-workspace-web/src/components/AccountCenterModal.test.ts`

- [ ] **Step 1: Write failing crop utility tests**

Test that crop math returns a centered square crop and clamps crop position.

Run: `npm run test -- src/utils/avatarCrop.test.ts`
Expected: FAIL because utility does not exist.

- [ ] **Step 2: Implement crop utility and canvas export**

Create helpers for centered crop state, clamped crop state, and canvas-to-blob export.

- [ ] **Step 3: Wire modal avatar flow**

The modal allows image selection, preview crop box, upload cropped blob, then saves returned `publicUrl` as `avatarUrl`.

- [ ] **Step 4: Run frontend targeted tests**

Run: `npm run test -- src/utils/avatarCrop.test.ts src/components/AccountCenterModal.test.ts`
Expected: PASS.

- [ ] **Step 5: Commit avatar crop UI**

Commit message: `feat(web): add avatar crop upload`

### Task 6: Full Verification

**Files:**
- Verify all changed files.

- [ ] **Step 1: Run backend tests**

Run: `mvn -q test`
Expected: PASS.

- [ ] **Step 2: Run frontend tests**

Run: `npm run test`
Expected: PASS.

- [ ] **Step 3: Run frontend build**

Run: `npm run build`
Expected: PASS, allowing only the existing Vite chunk-size warning.

- [ ] **Step 4: Push all completed feature commits**

Run: `git push`
Expected: `master -> master`.

### Task 7: Mature Avatar Cropper and Compact Account Modal

**Files:**
- Modify: `simon-workspace-web/package.json`
- Modify: `simon-workspace-web/package-lock.json`
- Modify: `simon-workspace-web/src/components/AccountCenterModal.vue`
- Modify: `simon-workspace-web/src/components/AccountCenterModal.test.ts`
- Modify: `simon-workspace-web/src/i18n/locales/en.ts`
- Modify: `simon-workspace-web/src/i18n/locales/zh-CN.ts`
- Modify: `simon-workspace-web/src/i18n/locales/th-TH.ts`
- Delete: `simon-workspace-web/src/utils/avatarCrop.ts`
- Delete: `simon-workspace-web/src/utils/avatarCrop.test.ts`

- [x] **Step 1: Write failing account center refinement tests**

Tests prove:
- The avatar flow imports `vue-advanced-cropper` and no longer uses manual sliders.
- The PC account center uses fixed modal/content styles instead of a full-screen surface.
- The profile form keeps avatar changes in the avatar tab instead of exposing a manual avatar URL input.

Run: `npm run test -- src/components/AccountCenterModal.test.ts`
Expected: FAIL before the implementation is updated.

- [x] **Step 2: Install and wire a mature cropper**

Add `vue-advanced-cropper`, render a square cropper with preview, and export the cropper canvas to a WebP file before upload.

- [x] **Step 3: Redesign the PC account center layout**

Use a compact two-column account center on desktop:
- left summary with avatar, display name, username, and roles;
- right content area with profile, avatar, and password tabs;
- fixed modal width and internal scroll.

Keep the mobile layout single-column and comfortable to use.

- [x] **Step 4: Update localized copy**

Remove obsolete manual crop slider labels and add concise preview/cropper copy in English, Chinese, and Thai.

- [x] **Step 5: Verify and commit**

Run:
- `npm run test -- src/components/AccountCenterModal.test.ts src/i18n/language.test.ts`
- `npm run test`
- `npm run build`

Expected: PASS, allowing only the existing Vite chunk-size warning.
