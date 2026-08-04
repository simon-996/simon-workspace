# Workspace UI and Navigation Refresh Design

Date: 2026-08-03

Status: Approved

## Context

The current workspace shell exposes every permitted module as a flat navigation item. On mobile, the same list becomes a horizontally scrolling bottom bar. The workspace home page repeats the module list as cards whose only secondary message is that the API is ready. This makes the interface describe the system structure instead of helping the user continue real work.

The homepage terminal is a signature interaction, but its current `login <username> <password>` flow accepts a password in a visible text field. The terminal also receives focus automatically when the public homepage loads. Both behaviors create avoidable usability and security concerns.

The file API already supports uploads, but the file center does not expose an upload interaction. This prevents the workspace home page from linking directly to a complete “upload material” task.

## Goals

- Make the workspace home page action-first instead of module-first.
- Give desktop navigation a stable hierarchy and keep mobile navigation to five fixed destinations.
- Add a maintainable file-upload interaction using the existing frontend API and backend endpoint.
- Remove credential entry from the terminal and stop the homepage from stealing keyboard focus.
- Keep permissions, dark mode, Chinese, English, Thai, loading, empty, and error states intact.
- Refactor touched code when it creates a clearer ownership boundary or prevents duplicated navigation and upload logic.

## Non-Goals

- Do not change lesson-plan generation, teaching-calendar generation, generation history behavior, AI features, or document export.
- Do not change backend endpoints or database schema.
- Do not redesign the public blog, public courses, login page, registration page, or account center.
- Do not introduce a new state-management library or UI library.
- Do not perform broad visual rewrites outside the pages and components required by this design.

## Experience Direction

The public site remains calm, personal, and expressive. The private workspace remains compact and operational. Both surfaces continue sharing the existing color, typography, border, radius, theme, and motion tokens, but the workspace should favor scanning speed and direct actions over decorative cards.

The approved workspace home direction is “task first.” The three primary actions are:

1. Course management.
2. Upload material.
3. Write an article.

## Information Architecture

### Desktop Navigation

The workspace side navigation is grouped into four sections. A group is hidden when none of its entries are permitted.

| Group | Entries |
| --- | --- |
| Teaching | Overview, Courses, Classes, Semesters |
| Content | Templates, Files, Storage, Blog Posts, Blog Settings |
| Records | Generation History |
| System | Security, Site Settings |

The current route remains visually active. Parent/group labels are non-interactive and do not consume route space.

### Mobile Navigation

The mobile bottom navigation has exactly five stable destinations:

1. Overview.
2. Courses.
3. Files.
4. Articles.
5. More.

Each direct destination is shown only when the user has its permission. “More” opens a drawer containing every remaining permitted workspace entry, organized with the same four groups as desktop. If one of the preferred direct destinations is not permitted, the bar keeps its stable order and does not substitute an unrelated module into that position.

The bottom bar does not scroll horizontally. Content receives enough bottom padding to remain visible above the bar. The “More” drawer supports keyboard focus, Escape-to-close, and a labelled close action.

## Workspace Home

### Primary Actions

The first section contains three visually distinct but restrained actions:

- Course management opens `/workspace/courses` and requires `course:manage`.
- Upload material opens `/workspace/files?action=upload` and requires `file:manage`.
- Write an article opens `/blog/new` and requires `blog:post:create`.

Actions are permission-filtered. The layout remains balanced with one, two, or three visible actions. No disabled or locked action is displayed to a user who lacks permission.

### Recent Work

Below the primary actions, the page shows up to three items in each permitted section:

- Recently updated courses.
- Recently uploaded or generated files.
- Article drafts.

The page reuses `fetchCourses`, `fetchFiles`, and `fetchManageBlogPosts({ status: 'DRAFT' })`. A personal workspace is expected to have bounded data during this phase, so a new aggregation endpoint is unnecessary. Each section owns its loading, error, retry, empty, and success state. Failure in one section does not blank or block the others.

Recent rows link to their existing management destination. Course and file rows do not invent unsupported detail routes. Draft rows link directly to the existing edit route.

## File Upload Flow

The file center gains an explicit Upload button and a focused upload dialog. The dialog owns:

- File selection.
- Private/public visibility selection, defaulting to private.
- Filename and size summary.
- Upload progress.
- Inline validation and upload failure state.
- Cancel and upload actions.

The implementation uses the existing `uploadFileResource` API. The API helper is extended with an optional progress callback instead of creating a duplicate upload function.

When the route contains `?action=upload`, the file center opens the dialog after mount and then removes only the consumed `action` query parameter with `router.replace`. This prevents the dialog from reopening on refresh or after a successful upload while preserving any unrelated query parameters.

After upload succeeds, the dialog closes, the file list reloads, and a persistent success message identifies the uploaded file. Upload failure leaves the selected file and visibility choice intact so the user can retry.

The dialog is implemented as a focused `FileUploadDialog.vue` component. The file center owns list loading, search, download, deletion, and the open/close state, while the dialog owns upload-specific form and request state.

## Terminal Behavior

The `login` terminal command becomes a navigation command. These inputs all open `/login` without accepting credentials:

- `login`
- `login username`
- `login username password`

Arguments are ignored and are never passed to `auth.login`, rendered back into terminal output, or retained as command data. Help text changes from `login <username> <password>` to `login`.

The terminal continues supporting logout, identity, navigation, language, theme, and permission-aware workspace commands.

`HomeView` no longer passes `auto-focus` to `TerminalPanel`. The terminal input focuses when the user clicks or taps inside the terminal. The optional component prop may be removed if tests confirm that it has no other consumer.

## Maintainable Component Boundaries

### Navigation Configuration

Create a pure navigation configuration module that owns:

- Navigation item keys, routes, permission codes, icons, and translation keys.
- Desktop group membership.
- Preferred mobile destination membership.
- Pure filtering helpers for permissions and group visibility.

`WorkspaceView.vue` renders the result and owns drawer open/close state. It must not contain a second hard-coded copy of route metadata for mobile.

### Workspace Home Data

Keep request orchestration in a small `useWorkspaceOverview` composable. It exposes separate state objects for courses, files, and drafts, plus individual retry functions. `WorkspaceHomeView.vue` owns layout and navigation only.

Do not create a generic dashboard-card framework. The three domains have different row content and permissions; small explicit render sections are easier to understand than an abstract schema with conditional slots.

### File Upload

Keep upload-specific state inside `FileUploadDialog.vue`. Expose only `show`, close, and uploaded events. Do not place route parsing or list reload logic inside the dialog.

### Terminal Commands

Keep command parsing and evaluation pure in `terminalCommands.ts`. `TerminalPanel.vue` executes only side effects explicitly described by a command result. Login must resolve to a standard navigation result rather than a special authentication status.

## Feedback and Accessibility

- Important failures are rendered inline with retry actions; Toast messages are supplementary.
- Buttons with icons have visible text or accessible labels.
- Navigation drawers and dialogs have labelled controls, focus management, Escape handling, and predictable return focus.
- Active states do not depend on color alone.
- New transitions use transform and opacity and respect reduced-motion preferences.
- Fixed mobile navigation and dialogs are checked at a 390 px viewport.
- Thai copy is allowed to wrap; navigation labels must not use fixed widths that truncate essential meaning.
- New colors use existing CSS variables so dark mode remains complete.

## Internationalization

Add or revise translation keys for:

- Desktop navigation group labels.
- Mobile “More” navigation and drawer labels.
- Workspace home action titles, descriptions, recent-section titles, empty states, error states, and retry actions.
- File upload dialog labels, visibility choices, progress, validation, success, and failure messages.
- Terminal login help and navigation feedback.

All new user-visible strings must exist in Chinese, English, and Thai. Translation keys remain semantic and do not embed URLs or email addresses.

## Documentation Changes

Update `docs/design-interaction.md` so the workspace mobile rule describes the approved five-item bottom navigation. The public-site header continues using its top collapsed menu; the two navigation rules must be documented separately to remove the current contradiction.

## Testing Strategy

Implementation follows a red-green-refactor sequence.

### Pure Unit Tests

- Permission filtering hides unavailable navigation items and empty groups.
- Mobile navigation contains only the approved stable destinations and never becomes a horizontal module list.
- Terminal `login` with zero, one, or multiple arguments returns navigation to `/login` without credential arguments.

### Component and Integration Tests

- Workspace desktop groups and mobile destinations render from the same configuration.
- “More” contains remaining permitted routes and opens/closes accessibly.
- Workspace home renders only permitted primary actions.
- Workspace home loads and retries courses, files, and drafts independently.
- `/workspace/files?action=upload` opens the upload dialog once and consumes the query parameter.
- Upload validation, progress, success reload, and retry-after-failure preserve the correct state.
- HomeView does not autofocus the terminal, while pointer interaction still focuses its input.

### Regression and Build Verification

- Run the complete frontend unit and integration suite.
- Run the frontend production build.
- Verify desktop and 390 px mobile layouts in light and dark themes.
- Verify Chinese, English, and Thai labels at the changed breakpoints.
- Verify keyboard navigation for the side navigation, mobile drawer, upload dialog, and terminal.

## Acceptance Criteria

- The workspace home page leads with course management, upload material, and write-article actions instead of API-ready module cards.
- Recent courses, files, and drafts fail and retry independently.
- Desktop navigation is visibly grouped without duplicating configuration.
- Mobile navigation has no horizontal scrolling and shows Overview, Courses, Files, Articles, and More when permitted.
- The More drawer exposes all remaining permitted workspace routes.
- The file center can upload a file through the existing API and can be opened directly into that flow from the workspace home page.
- No terminal command sends, displays, or retains a password.
- Entering the homepage does not steal keyboard focus.
- Changed pages remain usable at 390 px, in dark mode, and in Chinese, English, and Thai.
- No generation, export, AI, backend, database, public-blog, or public-course behavior changes.
