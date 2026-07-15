# Blog Editor and Reading Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Restore inline tag creation while retaining database search, fix blog detail alignment and mobile overflow, and move the mobile table of contents into a fixed-button drawer.

**Architecture:** Keep the existing Vue views and backend contract. Extract pure tag-option helpers for direct tests, then make focused template and CSS changes in the editor and detail views. Reuse Naive UI's select and drawer plus the installed Tabler icon set.

**Tech Stack:** Vue 3, TypeScript, Naive UI 2.44, md-editor-v3, Vitest, Vite.

---

### Task 1: Reusable and creatable tags

**Files:**
- Create: `simon-workspace-web/src/views/blog/blogTagOptions.ts`
- Create: `simon-workspace-web/src/views/blog/blogTagOptions.test.ts`
- Modify: `simon-workspace-web/src/views/blog/BlogEditorView.vue`
- Modify: `simon-workspace-web/src/views/blog/BlogEditorView.integration.test.ts`
- Modify: `simon-workspace-web/src/i18n/locales/en.ts`
- Modify: `simon-workspace-web/src/i18n/locales/zh-CN.ts`
- Modify: `simon-workspace-web/src/i18n/locales/th-TH.ts`

- [ ] **Step 1: Write failing helper and integration tests**

```ts
expect(normalizeSelectedTags([' Vue ', 'vue', 'Spring Boot'], 8)).toEqual(['Vue', 'Spring Boot'])
expect(createTagOption('  New   Tag ')).toEqual({ label: 'New Tag', value: 'New Tag' })
expect(buildTagOptions([{ name: 'Vue' }], ['vue'])).toEqual([{ label: 'vue', value: 'vue' }])
expect(editorSource).not.toContain(':remote="true"')
expect(editorSource).toContain(':on-create="createTagOption"')
expect(editorSource).toContain(':max-tag-count="\'responsive\'"')
```

- [ ] **Step 2: Run tests and confirm the requested behavior is absent**

Run: `npm.cmd test -- src/views/blog/blogTagOptions.test.ts src/views/blog/BlogEditorView.integration.test.ts`

Expected: FAIL because the helper module and non-remote creation contract do not exist.

- [ ] **Step 3: Add pure tag helpers**

```ts
export function normalizeTagName(value: string) {
  return value.trim().replace(/\s+/g, ' ')
}

export function createTagOption(value: string) {
  const tag = normalizeTagName(value)
  return { label: tag, value: tag }
}

export function normalizeSelectedTags(values: string[], limit = 8) {
  const normalized = new Map<string, string>()
  for (const value of values) {
    const tag = normalizeTagName(value)
    if (tag) normalized.set(tag.toLowerCase(), tag)
    if (normalized.size >= limit) break
  }
  return Array.from(normalized.values())
}
```

`buildTagOptions` must merge remote and selected names with the selected spelling winning for the same normalized key.

- [ ] **Step 4: Reconfigure the editor select**

Remove `:remote="true"`; add `clearable`, `:on-create="createTagOption"`, and `:max-tag-count="'responsive'"`. Replace immediate search with a 220ms timer and a monotonically increasing sequence so only the latest response updates `remoteTags`. Clear the timer on unmount.

- [ ] **Step 5: Update the three locale placeholders**

Use `Search tags or press Enter to create`, `搜索已有标签，回车创建`, and the equivalent Thai copy.

- [ ] **Step 6: Run focused and full tests**

Run: `npm.cmd test -- src/views/blog/blogTagOptions.test.ts src/views/blog/BlogEditorView.integration.test.ts`

Expected: both files pass.

- [ ] **Step 7: Commit**

```bash
git add simon-workspace-web/src/views/blog/blogTagOptions.ts simon-workspace-web/src/views/blog/blogTagOptions.test.ts simon-workspace-web/src/views/blog/BlogEditorView.vue simon-workspace-web/src/views/blog/BlogEditorView.integration.test.ts simon-workspace-web/src/i18n/locales/en.ts simon-workspace-web/src/i18n/locales/zh-CN.ts simon-workspace-web/src/i18n/locales/th-TH.ts
git commit -m "fix(blog): restore reusable tag creation"
```

### Task 2: Reading width and alignment

**Files:**
- Modify: `simon-workspace-web/src/views/blog/BlogDetailView.vue`
- Modify: `simon-workspace-web/src/views/blog/BlogDetailView.integration.test.ts`

- [ ] **Step 1: Write failing source-contract tests**

```ts
expect(detailSource).not.toContain('max-width: 14ch;')
expect(detailSource).toContain('text-align: left;')
expect(detailSource).toContain('min-width: 0;')
expect(detailSource).toContain('box-sizing: border-box;')
expect(detailSource).toContain('overflow-wrap: anywhere;')
```

- [ ] **Step 2: Run the focused test and confirm failure**

Run: `npm.cmd test -- src/views/blog/BlogDetailView.integration.test.ts`

Expected: FAIL on the old title width, centered preview margin, and missing mobile shrink constraints.

- [ ] **Step 3: Implement the layout constraints**

Set the page title to `max-width: 100%`. Give `.md-editor` and `.md-editor-preview` `min-width: 0`, and give the preview `width: 100%`, `box-sizing: border-box`, `margin: 0`, and `text-align: left`. At 620px remove outer article-body padding and use `padding: 24px 18px` on the preview. Keep tables and preformatted code horizontally scrollable.

- [ ] **Step 4: Run the focused test**

Run: `npm.cmd test -- src/views/blog/BlogDetailView.integration.test.ts`

Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add simon-workspace-web/src/views/blog/BlogDetailView.vue simon-workspace-web/src/views/blog/BlogDetailView.integration.test.ts
git commit -m "fix(blog): improve responsive reading width"
```

### Task 3: Mobile table-of-contents drawer

**Files:**
- Modify: `simon-workspace-web/src/views/blog/BlogDetailView.vue`
- Modify: `simon-workspace-web/src/views/blog/BlogDetailView.integration.test.ts`

- [ ] **Step 1: Write failing mobile TOC tests**

```ts
expect(detailSource).toContain('NDrawer')
expect(detailSource).toContain('ListDetails')
expect(detailSource).toContain('mobileTocOpen')
expect(detailSource).toContain('class="mobile-toc-trigger"')
expect(detailSource).toContain('.article-toc--desktop')
expect(detailSource).toContain('display: none;')
```

- [ ] **Step 2: Run the focused test and confirm failure**

Run: `npm.cmd test -- src/views/blog/BlogDetailView.integration.test.ts`

Expected: FAIL because the mobile drawer and trigger are absent.

- [ ] **Step 3: Add drawer state and markup**

Import `NDrawer`, `NDrawerContent`, and `ListDetails`. Add `mobileTocOpen = ref(false)`, close it inside `scrollToHeading`, render a fixed icon button for non-empty TOCs, and render a right-side drawer containing the same heading hierarchy and active state.

- [ ] **Step 4: Add responsive styles**

Keep `.article-toc--desktop` sticky above 920px. Below 920px hide it and show a stable 46px fixed trigger at the lower-right safe area. Style drawer buttons with the same depth, active border, and left alignment as desktop.

- [ ] **Step 5: Run the focused test**

Run: `npm.cmd test -- src/views/blog/BlogDetailView.integration.test.ts`

Expected: PASS.

- [ ] **Step 6: Commit**

```bash
git add simon-workspace-web/src/views/blog/BlogDetailView.vue simon-workspace-web/src/views/blog/BlogDetailView.integration.test.ts
git commit -m "feat(blog): add mobile table of contents drawer"
```

### Task 4: Full verification

**Files:**
- Verify only; no source changes expected.

- [ ] **Step 1: Run all frontend tests**

Run: `npm.cmd test`

Expected: all test files pass with zero failures.

- [ ] **Step 2: Build production assets**

Run: `npm.cmd run build`

Expected: TypeScript and Vite complete successfully.

- [ ] **Step 3: Restart the frontend and verify desktop**

At 1440x900, `/blog/4` must have left-anchored Markdown text, a full-width page title, no page overflow, and a sticky desktop TOC.

- [ ] **Step 4: Verify mobile**

At 390x844, `/blog/4` must have `scrollWidth <= clientWidth` for the article frame and preview, no inline TOC above the article, a fixed lower-right TOC trigger, a working drawer, active heading highlighting, and working heading navigation.

