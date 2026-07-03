# Homepage Intro Animation Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace duplicate homepage navigation with a scroll-led personal introduction where the terminal exits and the intro expands into more detail.

**Architecture:** Keep the homepage as a single Vue view, but move scroll progress math into a small tested utility. Use CSS variables and Vue scroll listeners for animation, with reduced-motion support and responsive layouts for desktop and mobile.

**Tech Stack:** Vue 3, TypeScript, Vitest, CSS transforms/transitions, existing vue-i18n messages.

---

### Task 1: Scroll Animation Utility

**Files:**
- Create: `simon-workspace-web/src/utils/homeScroll.ts`
- Create: `simon-workspace-web/src/utils/homeScroll.test.ts`

- [x] **Step 1: Write the failing test**

```ts
import { describe, expect, it } from 'vitest'
import { buildHomeScrollStyle, getHomeScrollProgress } from './homeScroll'

describe('home scroll animation helpers', () => {
  it('clamps scroll progress between 0 and 1', () => {
    expect(getHomeScrollProgress(-20, 400)).toBe(0)
    expect(getHomeScrollProgress(200, 400)).toBe(0.5)
    expect(getHomeScrollProgress(900, 400)).toBe(1)
  })

  it('returns CSS variables consumed by the homepage animation', () => {
    expect(buildHomeScrollStyle(0.5)).toEqual({
      '--home-progress': '0.500',
      '--intro-scale': '1.060',
      '--terminal-offset': '120.0px',
      '--terminal-opacity': '0.400',
    })
  })
})
```

- [x] **Step 2: Run test to verify it fails**

Run: `npm run test -- src/utils/homeScroll.test.ts`
Expected: fail because `src/utils/homeScroll.ts` does not exist yet.

- [x] **Step 3: Implement the utility**

```ts
export function getHomeScrollProgress(scrollY: number, distance: number) {
  if (distance <= 0) return 1
  return Math.min(1, Math.max(0, scrollY / distance))
}
```

- [x] **Step 4: Run test to verify it passes**

Run: `npm run test -- src/utils/homeScroll.test.ts`
Expected: 2 tests pass.

### Task 2: Homepage Structure And Content

**Files:**
- Modify: `simon-workspace-web/src/views/HomeView.vue`
- Modify: `simon-workspace-web/src/i18n/locales/en.ts`
- Modify: `simon-workspace-web/src/i18n/locales/zh-CN.ts`
- Modify: `simon-workspace-web/src/i18n/locales/th-TH.ts`

- [x] **Step 1: Remove duplicate homepage links**

Delete the hero `link-row` and bottom `quiet-links`. Keep top navigation and language switcher.

- [x] **Step 2: Add personal intro content**

Add a short hero line, expanded about paragraph, tech groups, and contact email through i18n keys under `home.intro`.

- [x] **Step 3: Connect scroll animation**

Use `buildHomeScrollStyle(progress)` on the homepage root. Update progress on scroll with `requestAnimationFrame`; remove listeners on unmount.

- [x] **Step 4: Verify**

Run: `npm run test -- src/utils/homeScroll.test.ts src/i18n/language.test.ts`
Run: `npm run build`

### Task 3: Roadmap And Git

**Files:**
- Modify: `docs/roadmap/06-polish-and-deploy.md`

- [x] **Step 1: Mark homepage animation and personal intro progress**
- [x] **Step 2: Commit only the relevant homepage, i18n, test, utility, and roadmap files**
- [x] **Step 3: Push `master`**
