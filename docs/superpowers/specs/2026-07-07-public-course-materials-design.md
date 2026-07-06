# Public Course Materials Design

## Goal

Build a course feature where visitors can open published courses without signing in and see three directories: documents, courseware, and resources. Workspace users with course permissions manage which courses are public and maintain uploaded files or external links.

## Public Experience

The public side follows the same model as the blog: content can be viewed without authentication, while visibility is controlled from the workspace.

- `/courses` shows active public courses.
- `/courses/:id` shows one course with three sections:
  - Documents
  - Courseware
  - Resources
- Each item is either a file or an external link.
- Markdown files open in an in-site reader.
- HTML courseware opens through a public view endpoint in a new tab.
- Other files use a public download endpoint.
- External links open in a new tab.

## Workspace Management

The existing course management screen remains the owner-facing entry point. Each course gets public settings and a material manager:

- Public visibility switch
- Public sort order
- Three material tabs: documents, courseware, resources
- Add file material by uploading a public file
- Add link material by entering a URL
- Edit title, description, type, sort order, and status
- Delete material with file references cleaned up

## Backend Model

The existing `course` table gains:

- `public_visible`
- `public_sort_order`

A new `course_material` table stores all three public directories:

- `course_id`
- `section`: `DOCUMENT`, `COURSEWARE`, `RESOURCE`
- `material_type`: `FILE`, `LINK`
- `file_id`
- `external_url`
- `title`
- `description`
- `sort_order`
- `status`

File materials reference `file_resource` and are synchronized through the existing file reference table using resource type `COURSE_MATERIAL`.

## Permissions

Public read APIs require no login. Workspace management uses the existing `course:manage` permission for the first version.

## Non-Goals

This version does not include chapters, student accounts, learning progress, assignments, comments, or analytics.
