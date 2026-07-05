# Header Terminal Theme and Error UX Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make avatars display consistently, make the homepage terminal useful, add a persistent theme switch, and harden API error messages.

**Architecture:** Add small frontend utilities for URL resolution, terminal parsing/actions, theme state, and error presentation instead of expanding existing components with one-off logic. Components keep layout responsibilities while stores/utilities own state and behavior.

**Tech Stack:** Vue 3, Pinia, Vue Router, Vue I18n, Naive UI, Axios, Vitest, TypeScript.

---

### Task 1: Avatar URL Display

**Files:**
- Create: `simon-workspace-web/src/utils/avatarUrl.ts`
- Create: `simon-workspace-web/src/utils/avatarUrl.test.ts`
- Modify: `simon-workspace-web/src/components/AppHeader.vue`
- Modify: `simon-workspace-web/src/components/AppHeader.test.ts`
- Modify: `simon-workspace-web/src/components/AccountCenterModal.vue`
- Modify: `simon-workspace-web/src/components/AccountCenterModal.test.ts`

- [x] **Step 1: Write failing avatar URL and header tests**

Run: `npm run test -- src/utils/avatarUrl.test.ts src/components/AppHeader.test.ts src/components/AccountCenterModal.test.ts`
Expected: FAIL because the avatar helper and header avatar rendering do not exist.

- [x] **Step 2: Implement URL resolver and wire avatar displays**

Resolve absolute URLs unchanged, resolve `/api/...` unchanged, resolve `/files/...` or `files/...` against the API base URL, and keep empty values empty. Use it in account center and header.

- [x] **Step 3: Verify and commit**

Run: `npm run test -- src/utils/avatarUrl.test.ts src/components/AppHeader.test.ts src/components/AccountCenterModal.test.ts`
Expected: PASS.
Commit: `feat(web): show account avatars consistently`

### Task 2: Real Terminal Commands

**Files:**
- Modify: `simon-workspace-web/src/components/terminalCommands.ts`
- Create: `simon-workspace-web/src/components/terminalCommands.auth.test.ts`
- Modify: `simon-workspace-web/src/components/TerminalPanel.vue`
- Modify: `simon-workspace-web/src/components/TerminalPanel.focus.test.ts`
- Modify: `simon-workspace-web/src/i18n/locales/en.ts`
- Modify: `simon-workspace-web/src/i18n/locales/zh-CN.ts`
- Modify: `simon-workspace-web/src/i18n/locales/th-TH.ts`

- [x] **Step 1: Write failing command parsing and action tests**

Run: `npm run test -- src/components/terminalCommands.auth.test.ts src/components/TerminalPanel.focus.test.ts`
Expected: FAIL because parameterized auth commands and empty default prompt are not implemented.

- [x] **Step 2: Implement parser and async command actions**

Add quoted argument parsing, async handlers for login/logout/whoami, and keep navigation permission-aware.

- [x] **Step 3: Verify and commit**

Run: `npm run test -- src/components/terminalCommands.auth.test.ts src/components/TerminalPanel.focus.test.ts src/i18n/language.test.ts`
Expected: PASS.
Commit: `feat(web): make homepage terminal actionable`

### Task 3: Global Theme Switch

**Files:**
- Create: `simon-workspace-web/src/stores/theme.ts`
- Create: `simon-workspace-web/src/stores/theme.test.ts`
- Modify: `simon-workspace-web/src/App.vue`
- Modify: `simon-workspace-web/src/components/AppHeader.vue`
- Modify: `simon-workspace-web/src/components/AppHeader.test.ts`
- Modify: `simon-workspace-web/src/style.css`

- [ ] **Step 1: Write failing theme store and header tests**

Run: `npm run test -- src/stores/theme.test.ts src/components/AppHeader.test.ts`
Expected: FAIL because theme state and switch do not exist.

- [ ] **Step 2: Implement persistent light/dark theme**

Persist theme to localStorage, expose toggle/set helpers, wire Naive UI dark theme, and add a moon/sun switch in the header.

- [ ] **Step 3: Verify and commit**

Run: `npm run test -- src/stores/theme.test.ts src/components/AppHeader.test.ts`
Expected: PASS.
Commit: `feat(web): add global theme switch`

### Task 4: Robust Error Messages

**Files:**
- Modify: `simon-workspace-web/src/api/errors.ts`
- Create: `simon-workspace-web/src/api/errors.test.ts`
- Create: `simon-workspace-web/src/api/errorMessage.ts`
- Create: `simon-workspace-web/src/api/errorMessage.test.ts`
- Modify: components/stores that catch API errors and show messages.

- [ ] **Step 1: Write failing error normalization tests**

Run: `npm run test -- src/api/errors.test.ts src/api/errorMessage.test.ts`
Expected: FAIL for network/down backend/non-standard error coverage.

- [ ] **Step 2: Implement normalized error display helper**

Ensure no response maps to `NETWORK_ERROR`, timeout maps to `REQUEST_TIMEOUT`, backend `errorCode` maps through i18n, and unknown HTTP responses still show usable messages.

- [ ] **Step 3: Verify and commit**

Run: `npm run test -- src/api/errors.test.ts src/api/errorMessage.test.ts src/i18n/language.test.ts`
Expected: PASS.
Commit: `fix(web): harden api error feedback`

### Task 5: Full Verification

- [ ] **Step 1: Run frontend tests**

Run: `npm run test`
Expected: PASS.

- [ ] **Step 2: Run frontend build**

Run: `npm run build`
Expected: PASS, allowing the existing chunk size warning.

- [ ] **Step 3: Push commits**

Run: `git push`
Expected: `master -> master`.
