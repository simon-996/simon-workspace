# Frontend Dependency and Bundle Maintenance Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Remove the two high-severity npm findings and lazy-load every route view so feature-only code is excluded from the initial bundle.

**Architecture:** Keep the router's paths, names, metadata, and guard unchanged. Replace only view module bindings with dynamic imports, and upgrade only the two vulnerable direct dependencies plus their lockfile entries.

**Tech Stack:** Vue 3, Vue Router, TypeScript, Vite, Vitest, npm

---

### Task 1: Upgrade the vulnerable direct dependencies

**Files:**
- Modify: `simon-workspace-web/package.json`
- Modify: `simon-workspace-web/package-lock.json`
- Test: npm's installed dependency audit

- [ ] **Step 1: Verify the security regression is present**

Run: `npm audit --audit-level=high`

Expected: exit code 1 with Axios and PostCSS high-severity findings.

- [ ] **Step 2: Install the safe direct dependency ranges**

Run: `npm install axios@^1.19.0 postcss@^8.5.25`

Expected package declarations:

```json
"axios": "^1.19.0",
"postcss": "^8.5.25"
```

- [ ] **Step 3: Verify the security regression is resolved**

Run: `npm audit --audit-level=high`

Expected: exit code 0 with zero vulnerabilities.

- [ ] **Step 4: Run the frontend regression suite**

Run: `npm test`

Expected: 49 test files and 230 tests pass before the route regression test is added.

- [ ] **Step 5: Commit the dependency fix**

```powershell
git add simon-workspace-web/package.json simon-workspace-web/package-lock.json
git commit -m "fix(web): update vulnerable dependencies"
```

### Task 2: Specify lazy route loading

**Files:**
- Modify: `simon-workspace-web/src/router/blogRoutes.test.ts`
- Test: `simon-workspace-web/src/router/blogRoutes.test.ts`

- [ ] **Step 1: Add a source regression test**

Add this test inside the existing `describe` block:

```typescript
it('lazy loads every route view', () => {
  expect(routerSource.match(/^import .+View from '\.\.\/views\/.+\.vue'$/gm)).toBeNull()
  expect(routerSource.match(/^const .+View = \(\) => import\('\.\.\/views\/.+\.vue'\)$/gm)).toHaveLength(22)
})
```

- [ ] **Step 2: Verify the test fails for the intended reason**

Run: `npm test -- src/router/blogRoutes.test.ts`

Expected: the new test fails because 22 view modules are still statically imported.

### Task 3: Convert view bindings to dynamic imports

**Files:**
- Modify: `simon-workspace-web/src/router/index.ts`
- Test: `simon-workspace-web/src/router/blogRoutes.test.ts`

- [ ] **Step 1: Replace static view imports with lazy bindings**

Keep the auth store import static and replace every view import with the matching form:

```typescript
const HomeView = () => import('../views/HomeView.vue')
const LoginView = () => import('../views/LoginView.vue')
```

Apply the same one-to-one transformation to all 22 existing view imports without changing the route table.

- [ ] **Step 2: Verify the focused test passes**

Run: `npm test -- src/router/blogRoutes.test.ts`

Expected: both blog route tests pass.

- [ ] **Step 3: Verify all frontend tests pass**

Run: `npm test`

Expected: 49 test files and 231 tests pass.

- [ ] **Step 4: Verify production chunking**

Run: `npm run build`

Expected: build succeeds, route chunks are emitted separately, and Vite emits no chunk larger than 500 kB warning.

- [ ] **Step 5: Commit the route fix**

```powershell
git add simon-workspace-web/src/router/index.ts simon-workspace-web/src/router/blogRoutes.test.ts
git commit -m "perf(web): lazy load route views"
```

### Task 4: Final verification

**Files:**
- Inspect: all files changed from `master`

- [ ] **Step 1: Reinstall from the committed lockfile**

Run: `npm ci`

Expected: install succeeds and reports zero vulnerabilities.

- [ ] **Step 2: Run all required checks from clean dependencies**

Run each command separately:

```powershell
npm audit --audit-level=high
npm test
npm run build
```

Expected: audit, 231 tests, and production build all succeed without a large chunk warning.

- [ ] **Step 3: Review the final diff**

Run: `git diff master...HEAD --check`

Expected: no whitespace errors; the diff contains only the dependency declarations, lockfile, route lazy bindings, regression test, design, and plan.
