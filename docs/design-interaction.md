# Simon Workspace Interaction Guidelines

Last updated: 2026-08-04

## Purpose

This document records the interaction direction for Simon Workspace. Use it when designing or refactoring public pages, blog pages, course pages, and workspace screens.

Simon Workspace combines:

- Personal homepage.
- Public blog.
- Public course materials.
- Private workspace.

The public site should feel simple, personal, technical, and slightly futuristic. The workspace should feel dense, stable, and efficient.

## Overall Experience

- [ ] Blend personal homepage, blog, and courses into one coherent public site.
- [ ] Keep the first screen focused on the owner name and interactive terminal.
- [ ] Allow a small amount of technology-inspired styling and animation.
- [ ] Keep visual effects restrained.
- [ ] Prefer smooth, almost invisible motion over obvious decorative animation.
- [ ] Avoid large marketing-style sections unless they serve the actual content.
- [ ] Avoid over-explaining features inside the UI.
- [ ] Keep public pages calm and readable.
- [ ] Keep workspace pages efficient and compact.

## Navigation

### Public Desktop Navigation

- [ ] Use a shared top header across public pages.
- [ ] Keep these primary entries: Home, Blog, Courses, Login/Workspace, Language, Theme.
- [ ] Highlight the active navigation item.
- [ ] Disable or visually de-emphasize clicking the current active navigation item.
- [ ] Keep the workspace entry in the main navigation, not inside the avatar menu.

### Public Mobile Navigation

- [ ] Use the shared top collapsed menu.
- [ ] Do not add a public-site bottom tab bar.
- [ ] Keep the menu compact and easy to close.
- [ ] Avoid layout shifts when opening or closing the menu.
- [ ] Keep language and theme controls accessible.

### Logged-In Avatar Menu

- [ ] Show the user avatar near the header user area.
- [ ] Avatar menu should contain personal information.
- [ ] Avatar menu should contain password modification.
- [ ] Avatar menu should contain logout.
- [ ] Do not put Workspace inside the avatar menu because it already exists in navigation.

## Homepage

### First Screen

- [ ] Show the owner name as the strongest first-viewport signal.
- [ ] Keep the terminal visible and usable.
- [ ] Keep the first screen uncluttered.
- [ ] Do not add blog/course previews to the first screen unless the product direction changes.
- [ ] Avoid long introduction paragraphs.

### Terminal

The terminal is a signature feature, not a decoration.

- [ ] Keep the terminal focusable and keyboard-friendly.
- [ ] Do not force focus on page load; allow pointer interaction to focus the input.
- [ ] Support practical commands such as `help`, `login`, `logout`, `blog`, `courses`, and `whoami`.
- [ ] Support parameterized commands where useful.
- [ ] Show clear command feedback.
- [ ] Keep terminal output concise.
- [ ] Keep terminal behavior usable on mobile.
- [ ] Avoid terminal effects that interfere with reading or input.
- [ ] Continue improving the terminal as an actual navigation and interaction layer.

### Scroll Animation

- [ ] Preserve the Apple-like scroll-controlled homepage animation.
- [ ] The page should feel controlled by scroll progress instead of simply revealing lower content.
- [ ] Move and scale the intro smoothly.
- [ ] Let the terminal fade or move away smoothly during the transition.
- [ ] Prevent title and intro text overlap on desktop.
- [ ] Keep mobile layout centered and readable during animation.
- [ ] Keep performance smooth on mainstream mobile devices.

### Profile Content

- [ ] Use technology stack as the main introduction content.
- [ ] Keep personal bio short.
- [ ] Avoid long autobiography-style copy.
- [ ] Make profile content editable from workspace in a future phase.
- [ ] Do not permanently hard-code profile content in frontend components.

## Blog

### Blog List

- [ ] Prefer a clean, simple list over high-density CSDN-style feeds.
- [ ] Avoid overly large cards.
- [ ] Show title, summary, category, tags, publish time, and key stats where useful.
- [ ] Keep list scanning easy on desktop and mobile.
- [ ] Avoid too much metadata noise.
- [ ] Keep typography compact but not cramped.

### Blog Detail

- [ ] Render Markdown with GitHub-like code block styling.
- [ ] Keep code blocks readable and copyable.
- [ ] Support tables, images, headings, lists, and links cleanly.
- [ ] Use a table of contents for longer posts.
- [ ] On desktop, place the table of contents on the right when there is enough width.
- [ ] On mobile, use a small floating right-side expand/collapse button for the table of contents.
- [ ] Keep the reading column comfortable.
- [ ] Avoid decorative elements that distract from reading.

### Blog Editor

- [ ] Provide a fixed action bar for major actions.
- [ ] Include save draft, publish, and preview actions when those features are available.
- [ ] Make upload progress visible with a simple progress bar.
- [ ] Keep editor controls discoverable but not noisy.
- [ ] Use clear warnings for unsaved changes.
- [ ] Keep Markdown preview close to published rendering.

## Courses

### Course List

- [ ] Use course cards.
- [ ] Keep cards clean and compact.
- [ ] Show course name, short description, code or metadata if useful.
- [ ] Avoid turning courses into a dense table on the public site.
- [ ] Keep mobile cards easy to tap.

### Course Detail

- [ ] Keep exactly three content sections: Documents, Courseware, Resources.
- [ ] On desktop, display the three sections in three columns when space allows.
- [ ] On mobile, stack the three sections vertically.
- [ ] Keep section empty states simple.
- [ ] Use clear file/link affordances.
- [ ] Show file type and file size when available.
- [ ] Open uploaded HTML courseware in a new tab.
- [ ] Open external resources in a new tab.

## Workspace

The workspace is a management system, not a marketing dashboard.

- [ ] Prefer dense, practical layouts.
- [ ] Reduce unnecessary whitespace.
- [ ] Use tables for management-heavy data.
- [ ] Use compact toolbars for search, refresh, create, and filters.
- [ ] Keep actions close to the data they affect.
- [ ] Use modals or focused panels for create/edit flows.
- [ ] Avoid large hero sections inside workspace.
- [ ] Avoid decorative animations inside workspace.
- [ ] Prioritize stable layout and fast scanning.
- [ ] Keep responsive mobile layouts usable, even if desktop is the primary workspace target.

### Workspace Desktop Navigation

- [ ] Group permitted routes under Teaching, Content, Records, and System.
- [ ] Hide a group when none of its routes are permitted.
- [ ] Keep the current route visibly active.

### Workspace Mobile Navigation

- [ ] Use a fixed, non-scrolling bottom bar for Overview, Courses, Files, Articles, and More when permitted.
- [ ] Put all remaining permitted workspace routes in the grouped More bottom drawer.
- [ ] Keep page content clear of the fixed bar.
- [ ] Verify the navigation and drawer at a 390 px viewport.

### Workspace Editing Flows

- [ ] Use fixed bottom action bars for longer editing pages.
- [ ] Keep save, publish, preview, and cancel actions visually clear.
- [ ] Show validation errors near the relevant field.
- [ ] Keep destructive actions separated from primary save actions.
- [ ] Use consistent form spacing and label placement.

## Feedback And State

### Success And Error Feedback

- [ ] Feedback should be more visible than lightweight toast-only hints when the action matters.
- [ ] Use clear success messages for create, update, publish, upload, and deploy-like operations.
- [ ] Use clear error messages for validation, network failure, service downtime, and backend errors.
- [ ] Keep backend error details user-safe.
- [ ] Preserve trace/debug details where useful for admin troubleshooting.
- [ ] Make retry actions available when loading fails.

### Delete And Destructive Actions

- [ ] All delete operations require confirmation.
- [ ] Confirmation text should name what will be deleted or affected.
- [ ] Use stronger visual treatment for irreversible actions.
- [ ] Prefer clear confirm/cancel labels.
- [ ] Do not allow accidental destructive clicks.

### Uploads

- [ ] Show simple progress bars for uploads.
- [ ] Keep upload progress easy to notice but not overwhelming.
- [ ] Show completion and failure states.
- [ ] Let users retry failed uploads where practical.
- [ ] Avoid sudden blank states while upload is still processing.

## Theme

### Dark Mode

- [ ] Use comfortable dark gray, not pure black.
- [ ] Apply dark mode globally, not only to the header.
- [ ] Update page background, surfaces, text, borders, terminal, blog, courses, and workspace elements.
- [ ] Keep contrast comfortable for long reading.
- [ ] Avoid overly saturated accent colors in dark mode.
- [ ] Remember the user's theme choice.
- [ ] Do not force-follow system theme unless the user explicitly chooses that behavior later.

### Theme Toggle

- [ ] Keep the toggle small and elegant.
- [ ] Icons should remain visible in both states.
- [ ] Avoid active colors covering the icon.
- [ ] Keep the interaction immediate and smooth.

## Motion

### Public Site

- [ ] Use subtle motion on public pages.
- [ ] Motion should support orientation and focus.
- [ ] Avoid motion that delays reading.
- [ ] Avoid motion that feels like decoration for its own sake.
- [ ] Keep scroll-driven motion smooth.
- [ ] Respect reduced-motion preferences if added later.

### Workspace

- [ ] Do not add decorative workspace motion.
- [ ] Use minimal transitions only where they clarify state changes.
- [ ] Prioritize responsiveness over visual flourish.

## Internationalization

- [ ] Interface language supports Chinese, English, and Thai.
- [ ] Language switching affects global UI strings.
- [ ] Blog content does not need multilingual versions.
- [ ] Course content does not need multilingual versions.
- [ ] Avoid putting emails or URLs directly into i18n messages when the i18n parser may treat special characters as syntax.

## Acceptance Checklist For UI Changes

Before finishing a meaningful UI change:

- [ ] Desktop layout does not overlap.
- [ ] Mobile layout does not overlap.
- [ ] Text fits inside buttons, cards, tabs, and controls.
- [ ] Public pages keep the simple homepage/blog/course direction.
- [ ] Workspace pages remain compact and management-oriented.
- [ ] Dark mode still applies globally.
- [ ] Language switching still works.
- [ ] Destructive actions require confirmation.
- [ ] Uploads show clear progress when applicable.
- [ ] Navigation active state is correct.
- [ ] Existing terminal interactions still work if the homepage is touched.
- [ ] Build and relevant tests pass.
