# 09 Product Direction Todo

Last updated: 2026-07-09

## Product Positioning

Simon Workspace is a personal homepage, public blog, public course-material site, and private workspace combined into one project.

Public visitors should mainly:

- Read blog posts.
- Browse public courses and course materials.
- Understand the site owner with a lightweight personal profile.

Authenticated users should mainly:

- Manage posts, comments, categories, tags, courses, files, site content, accounts, roles, and permissions.
- Use workspace features according to role permissions.

This project is for personal use, not a SaaS product. Do not introduce multi-tenant organization logic unless the product direction changes later.

## Global Product Principles

- [ ] Keep the public site simple: personal homepage, blog, courses, and login/register entry.
- [ ] Keep the workspace private and permission-driven.
- [ ] Use roles and permissions instead of hard-coded "owner only" checks.
- [ ] Do not add student login for now; course content is fully public.
- [ ] Treat UI i18n as interface translation only; blog/course content does not need multilingual versions.
- [ ] Store editable personal/site content in backend-managed configuration, not permanently hard-coded in frontend components.
- [ ] Prefer immediate cloud-file deletion when content is deleted and no other references exist.
- [ ] Use local storage only for development or fallback; production should use R2/OSS/COS.
- [ ] Keep Jenkins deployment focused on one application server.
- [ ] Support both manual Jenkins deployment and later push-triggered deployment.

## Phase A: Account Registration And Review

Goal: allow public registration, but require owner/admin review before the account can use protected workspace functions.

### Backend

- [x] Add account registration API.
- [x] Add username uniqueness validation.
- [x] Add email uniqueness validation if email is provided.
- [x] Add password strength validation with clear error messages.
- [x] Add account review status, such as `PENDING`, `APPROVED`, `REJECTED`.
- [x] Make newly registered accounts default to `PENDING`.
- [x] Prevent `PENDING` accounts from entering workspace APIs.
- [x] Prevent `REJECTED` accounts from logging in or using workspace APIs.
- [x] Keep login response clear when an account is waiting for review.
- [x] Add admin API for pending account list.
- [x] Add admin API for approving an account.
- [x] Add admin API for rejecting an account.
- [x] Add admin API for disabling an account after approval.
- [x] Add admin API for assigning roles during or after approval.
- [ ] Add audit fields for reviewer, review time, and review remark.
- [ ] Add tests for registration, duplicate fields, pending login, approve, reject, and permission boundaries.

### Frontend

- [x] Add public register page.
- [x] Add register entry near login.
- [x] Add registration success state explaining that review is required.
- [x] Add friendly pending/rejected login messages.
- [x] Add workspace account review page or review panel.
- [x] Show pending accounts with username, nickname, email, register time, and review actions.
- [x] Support approve with role assignment.
- [ ] Support reject with optional reason.
- [x] Add route guards so pending accounts cannot enter the workspace.
- [x] Add i18n strings for Chinese, English, and Thai.
- [x] Add mobile layout for registration and review pages.

### Acceptance

- [x] A visitor can register.
- [x] The new account cannot use workspace features before approval.
- [x] An approved account can log in and use only its assigned permissions.
- [x] A rejected account receives a clear message.
- [x] Owner/admin can review accounts from the workspace.

## Phase B: Editable Homepage And Site Profile

Goal: make the public homepage content editable from the workspace.

### Data And API

- [x] Extend site config with public profile fields.
- [x] Store owner display name.
- [x] Store owner introduction summary.
- [x] Store owner long bio.
- [x] Store owner role/title.
- [x] Store contact email.
- [x] Store technology stack list as structured data.
- [ ] Store profile avatar or portrait URL if needed.
- [ ] Store homepage terminal display settings if they should be configurable.
- [x] Add validation for required profile fields.
- [x] Add API tests for saving and reading site profile config.

### Workspace UI

- [x] Add profile fields in site settings.
- [x] Let owner edit short intro, long bio, role/title, email, and technology stack.
- [ ] Support adding, editing, sorting, and removing technology stack items.
- [ ] Show a compact live preview of public profile content.
- [ ] Keep the form consistent with the current minimal workspace style.
- [x] Add skeleton/loading state.
- [x] Add i18n strings.

### Public Homepage

- [x] Load profile content from backend site config.
- [x] Remove remaining hard-coded personal introduction text from the frontend.
- [x] Keep first screen simple: navigation, intro, terminal.
- [x] Preserve the Apple-like scroll animation.
- [x] Ensure PC intro/title does not overlap after animation.
- [x] Ensure mobile email/profile layout is centered and readable.
- [x] Keep blog and courses as primary public navigation entries.

### Acceptance

- [x] Editing site profile in workspace updates public homepage after refresh.
- [x] Homepage still works if some profile fields are empty.
- [x] Chinese/English/Thai UI strings switch correctly.

## Phase C: Blog Publishing Enhancements

Goal: make the blog suitable for technical posts, teaching notes, project records, and personal reflections.

### Markdown Reading Quality

- [x] Improve Markdown detail rendering typography.
- [x] Add code block copy button.
- [x] Add code block language label.
- [x] Add table overflow handling on mobile.
- [x] Add image responsive layout.
- [x] Add heading anchors.
- [x] Add generated table of contents for longer posts.
- [x] Add safe external-link handling.
- [ ] Ensure Markdown preview and published rendering are visually consistent.
- [ ] Add mobile visual QA for blog detail pages.

### Drafts And Editing

- [x] Add draft list in workspace or blog editor.
- [x] Support saving a post as draft.
- [x] Support editing existing drafts.
- [x] Support editing published posts.
- [ ] Support preview before publishing.
- [x] Add status display for draft/published posts.
- [ ] Add archived status display after archive/unpublish flow exists.
- [x] Add unsaved-change warning in the editor.
- [ ] Add autosave or manual save feedback if needed.

### Post Metadata

- [ ] Add pinned post field.
- [ ] Add featured/recommended post field.
- [ ] Add view count.
- [ ] Add like count if needed.
- [ ] Add bookmark count if needed.
- [ ] Add cover image field.
- [ ] Add archive/unpublish flow.
- [ ] Add tag autocomplete from existing tags.
- [ ] Add category/tag filters on public list page.
- [ ] Add workspace controls for pinned/featured posts.

### Public List And Detail

- [ ] Show pinned posts first where appropriate.
- [ ] Show featured marker.
- [ ] Show view count.
- [ ] Show category and tags.
- [ ] Keep list text density compact.
- [ ] Avoid oversized headings on blog pages.
- [ ] Add empty state for no posts.

### Acceptance

- [ ] Blog supports draft, publish, edit, archive/unpublish, pinned, featured, and view count.
- [ ] Markdown detail pages render code, tables, images, and headings cleanly on PC and mobile.
- [ ] Existing blog posts remain readable after migration.

## Phase D: Blog Comments And AI Moderation

Goal: allow public discussion while reducing harmful or low-quality comments through AI moderation and manual review.

### Comment Model

- [ ] Add comment status such as `PENDING`, `APPROVED`, `REJECTED`, `HIDDEN`.
- [ ] Support visitor comments if desired by final policy.
- [ ] Support logged-in user comments.
- [ ] Store commenter display name.
- [ ] Store commenter email if anonymous comments are allowed.
- [ ] Store commenter IP/user agent for moderation auditing if needed.
- [ ] Support comment reply relationships.
- [ ] Add moderation result fields: AI decision, reason, score, reviewed time.
- [ ] Add manual reviewer fields.

### AI Moderation

- [ ] Define moderation categories: spam, abuse, sensitive content, advertising, unsafe links, normal.
- [ ] Add AI moderation service abstraction.
- [ ] Add prompt/config for comment moderation.
- [ ] Add timeout and fallback behavior.
- [ ] If AI passes a comment, publish automatically.
- [ ] If AI rejects a comment, keep it hidden and visible in moderation queue.
- [ ] If AI is uncertain or unavailable, set comment to `PENDING`.
- [ ] Log moderation failures without exposing internals to users.
- [ ] Add tests for approved/rejected/pending moderation paths.

### Frontend

- [ ] Add comment form on blog detail page.
- [ ] Show clear success message after submitting a comment.
- [ ] Explain when a comment is waiting for review.
- [ ] Render approved comments only on public page.
- [ ] Add replies UI.
- [ ] Add workspace moderation queue.
- [ ] Allow approve, reject, hide, restore, and delete.
- [ ] Show AI reason and score to admins.
- [ ] Add i18n strings.

### Acceptance

- [ ] Public users can submit comments.
- [ ] Harmful comments are not shown before approval.
- [ ] Admin can review AI decisions.
- [ ] AI outage does not break comment submission.

## Phase E: Public Course Improvements

Goal: keep courses fully public and easy to maintain.

### Current Direction

- [ ] Course list remains public.
- [ ] Course detail remains public.
- [ ] Course content has exactly three sections: documents, courseware, resources.
- [ ] Do not add chapters.
- [ ] Do not add student login.
- [ ] Do not add course material version management.

### Upload And Display

- [ ] Keep documents as uploaded files or links.
- [ ] Keep courseware as uploaded HTML files or links.
- [ ] Keep resources as uploaded files, download links, or official website links.
- [ ] Improve HTML courseware opening behavior.
- [ ] Improve Markdown document rendering if course docs use Markdown.
- [ ] Add clearer file type icons.
- [ ] Add file size display.
- [ ] Add updated time display.
- [ ] Add empty states for each section.
- [ ] Add mobile layout QA for course detail.

### Workspace

- [ ] Keep course visibility controlled from workspace.
- [ ] Allow course material add/edit/delete from workspace.
- [ ] Consider drag-and-drop sorting for materials.
- [ ] Consider quick link validation for resource URLs.
- [ ] Keep category/section labels simple.

### Acceptance

- [ ] Anonymous visitors can read every public course.
- [ ] Workspace managers can maintain all three material sections.
- [ ] Upload-only HTML courseware can open through a public link.

## Phase F: File Storage And Deletion Policy

Goal: avoid ghost files by deleting cloud objects immediately when the application no longer needs them.

### Storage Direction

- [ ] Use local storage only for development or fallback.
- [ ] Use Cloudflare R2, Aliyun OSS, or Tencent COS in production.
- [ ] Keep storage provider configurable in workspace and environment variables.
- [ ] Do not restrict upload type or size at the application product level.
- [ ] Document infrastructure-level upload limits for Nginx, backend multipart, and cloud provider.

### Reference Tracking

- [ ] Track file references from blog posts.
- [ ] Track file references from course materials.
- [ ] Track file references from avatars.
- [ ] Track file references from future homepage/profile media.
- [ ] Ensure deleting a blog post disconnects referenced images.
- [ ] Ensure removing an image from Markdown disconnects that reference.
- [ ] Ensure deleting a course material disconnects the referenced file.
- [ ] Ensure replacing an avatar disconnects the old avatar reference if unused.

### Immediate Deletion

- [ ] Implement safe delete when reference count becomes zero.
- [ ] Delete remote cloud object when file is no longer referenced.
- [ ] Mark file record as deleted after successful remote deletion.
- [ ] If remote deletion fails, keep retryable failure status.
- [ ] Add manual retry action in workspace.
- [ ] Add logs for remote deletion failures.
- [ ] Add tests for no-reference deletion and still-referenced protection.

### Safety

- [ ] Never delete a file still referenced by any active content.
- [ ] Avoid deleting files when provider configuration is missing or mismatched.
- [ ] Keep enough metadata to locate the remote object by provider and object key.
- [ ] Consider a short soft-delete grace period only if immediate deletion causes real operational risk.

### Acceptance

- [ ] Deleting a blog image removes the cloud object when no longer referenced.
- [ ] Deleting a course file removes the cloud object when no longer referenced.
- [ ] Shared files are not deleted while still referenced.

## Phase G: Deployment And Operations

Goal: make one-server Jenkins deployment reliable enough for daily personal use.

### Jenkins

- [ ] Keep manual parameterized deployment for `all`, `api`, and `web`.
- [ ] Add Git push trigger after manual deployment is stable.
- [ ] Keep `DEPLOY_TARGET=api` from restarting the web container.
- [ ] Keep `DEPLOY_TARGET=web` from restarting the API container.
- [ ] Archive build artifacts.
- [ ] Keep SSH credentials in Jenkins credentials store.
- [ ] Do not put production `.env` into git.

### Application Server

- [ ] Deploy to `/apps/simon-workspace`.
- [ ] Keep production `.env` at `/apps/simon-workspace/deploy/.env`.
- [ ] Verify Docker and Docker Compose versions.
- [ ] Open or reverse-proxy ports `9526` and `9527`.
- [ ] Configure domains: `www.simon996.com` and `api.simon996.com`.
- [ ] Configure HTTPS certificates.
- [ ] Confirm app server can reach external MySQL.
- [ ] Confirm app server can reach external Redis.
- [ ] Confirm app server can reach configured object storage.

### Runtime

- [ ] Add container restart policy.
- [ ] Add backend health check endpoint usage in deployment verification.
- [ ] Add basic log inspection command to deployment docs.
- [ ] Add rollback notes for previous deployment bundles or previous git revision.
- [ ] Add backup notes for database and storage provider.
- [ ] Add upload size notes for Nginx and backend.

### Acceptance

- [ ] Jenkins can deploy all services with one click.
- [ ] Jenkins can deploy only API with one click.
- [ ] Jenkins can deploy only Web with one click.
- [ ] Production `.env` survives every Jenkins deployment.

## Deferred Or Explicitly Not Planned

- [ ] Multi-tenant SaaS mode is not planned.
- [ ] Student login is not planned for now.
- [ ] Course-specific visibility by class/student is not planned for now.
- [ ] Course material version management is not planned.
- [ ] Scheduled blog publishing is not planned.
- [ ] Multilingual blog/course content is not planned; only interface i18n is needed.

## Suggested Execution Order

1. [ ] Registration and account review.
2. [ ] Editable homepage/site profile.
3. [ ] Blog Markdown reading quality.
4. [ ] Blog drafts, editing, pinned, featured, view count.
5. [ ] Blog comments and AI moderation.
6. [ ] Immediate cloud-file deletion when references are removed.
7. [ ] Course material display polish.
8. [ ] Jenkins push-triggered deployment and operations polish.
