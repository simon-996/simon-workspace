# 08 Blog Phase

Last updated: 2026-07-06

## Phase Goal

Build the public blog as the first real content module of the personal homepage. The first version follows a CSDN-style technical blog: public reading, authenticated publishing and commenting, permission-gated category management, and free-form tags.

## Completed In First Version

### Backend

- [x] Added blog tables: `blog_category`, `blog_tag`, `blog_post`, `blog_post_tag`, `blog_comment`.
- [x] Added blog permissions: `blog:post:create`, `blog:post:update`, `blog:post:delete`, `blog:comment:create`, `blog:category:manage`, `blog:moderate`.
- [x] Added public APIs for category list, tag list, post list, post detail, and comments.
- [x] Added authenticated/permission-protected APIs for creating, updating, and deleting posts.
- [x] Added permission-protected category create/update/delete APIs.
- [x] Added permission-protected category management list API with post counts.
- [x] Categories with existing posts cannot be deleted.
- [x] Disabled categories cannot be used when publishing posts.
- [x] Tags are created freely from post input and de-duplicated by slug.
- [x] Markdown image references are parsed when saving posts.
- [x] Internal file links like `/api/files/{id}/download` are bound directly by `fileId`.
- [x] Public object-storage URLs are resolved through `file_resource.public_url`.
- [x] Post save calls `FileReferenceService.syncReferences(...)` so removed images become `ORPHANED` when unreferenced.
- [x] Post delete disconnects blog image references.
- [x] Added tests for Markdown asset extraction and blog image binding.

### Frontend

- [x] Added `/blog` public post list page.
- [x] Added `/blog/:id` public post detail page with Markdown preview and comments.
- [x] Added `/blog/new` Markdown publishing page protected by `blog:post:create`.
- [x] Integrated `md-editor-v3` for Markdown editing, preview, and image upload.
- [x] Added `.md` import into the editor and first-heading title extraction.
- [x] Blog editor uploads images through `/api/files` with `sourceType = BLOG_EDITOR` and `visibility = PUBLIC`.
- [x] Blog navigation now points to `/blog` instead of the old homepage anchor.
- [x] Terminal `blog` command now opens `/blog`.
- [x] Category creation is available in the editor for accounts with `blog:category:manage`.
- [x] Added `/workspace/blog` category management page for accounts with `blog:category:manage`.
- [x] Category management supports create, edit, enable/disable, and delete only when unused.
- [x] Added English, Chinese, and Thai operation strings for the category management page.

## Remaining Todo

### Blog Product

- [ ] Add draft list and edit existing draft/published posts from the UI.
- [x] Add category management page or workspace panel.
- [ ] Add tag search/autocomplete from existing tags.
- [ ] Add post cover image.
- [ ] Add post archive/unpublish flow instead of only delete.
- [ ] Add comment moderation: hide, restore, delete.
- [ ] Add comment replies UI.
- [ ] Add like/bookmark/view statistics UI.
- [ ] Add homepage blog preview section.

### Media And Ghost File Prevention

- [x] First prevention version: bind referenced images during post save and mark removed references as `ORPHANED`.
- [ ] Add scheduled cleanup for old `ORPHANED` files with a retention period.
- [ ] Add dry-run cleanup mode.
- [ ] Add workspace screen for orphaned file review, restore, and manual cleanup.
- [ ] Store inserted image file IDs in editor metadata where possible, instead of relying only on URL reverse lookup.

### Quality

- [ ] Add integration tests for blog controller permissions.
- [ ] Add UI tests for editor import/upload behavior.
- [ ] Add route-level code splitting for the Markdown editor bundle.
- [ ] Add full i18n strings for blog pages in English, Chinese, and Thai.
- [ ] Add mobile visual QA for list, detail, and editor pages.

## Verification

- [x] `mvn test` passed on 2026-07-06.
- [x] `npm run test` passed on 2026-07-06.
- [x] `npm run build` passed on 2026-07-06.

## Progress Log

```text
2026-07-06:
- Completed first backend blog model and API layer.
- Completed first public blog list/detail pages.
- Completed Markdown publishing page with md-editor-v3.
- Completed workspace category management with post counts, delete protection, and disabled-category publish guard.
- Completed first ghost-image prevention by syncing blog Markdown image references to file_reference.
- Deferred draft management, moderation, scheduled cleanup, and full i18n to later iterations.
```
