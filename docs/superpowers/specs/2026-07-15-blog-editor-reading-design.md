# Blog Editor and Reading Design

## Scope

Improve the existing blog editor tag interaction and the blog detail reading layout without changing API contracts or database schema.

## Tag interaction

The existing `NSelect` combines `tag` and `remote`. Naive UI 2.44 only creates ad-hoc options when `tag` is enabled and `remote` is disabled, so the current configuration prevents new tags even though the backend already upserts and deduplicates tag names.

Use one select for both reuse and creation:

- Keep `multiple`, `tag`, and `filterable`.
- Remove the `remote` prop so typed values can become options on Enter.
- Continue calling `fetchBlogTags` from `onSearch`, with a short debounce and stale-response protection.
- Merge remote options with selected values case-insensitively.
- Normalize whitespace and cap the submitted list at the backend limit of eight tags.
- Use responsive tag collapsing so selected tags do not consume the whole metadata row.
- Make the placeholder explain both actions: search an existing tag or press Enter to create one.

The backend remains unchanged because `BlogPostService.syncTags` already inserts missing tags, restores soft-deleted tags, and reuses existing slugs.

## Reading layout

The outer article remains a two-column desktop grid with a sticky table of contents. The Markdown preview keeps a readable maximum measure but starts at the left edge rather than using auto margins. Explicit `width`, `min-width`, `box-sizing`, and flex rules prevent wide tables from forcing the preview to approximately 600px on a 390px viewport.

On small screens:

- The article frame uses no redundant inner padding.
- The preview uses the full available width with compact horizontal padding.
- Normal prose wraps anywhere when required.
- Tables and code blocks retain their own horizontal scrolling.
- The page title can use the full header width instead of being limited to `14ch`.

## Mobile table of contents

The desktop sticky table of contents stays unchanged. Below 920px it is hidden and replaced by a fixed 46px icon button at the lower-right safe area. Activating it opens a right-side Naive UI drawer, preserving heading indentation and active-heading highlighting. Selecting a heading closes the drawer and scrolls to the target.

## Verification

- Unit-test tag normalization, case-insensitive deduplication, creation options, and the eight-tag limit.
- Integration-test the editor component contract and detail responsive styles.
- Run the complete Vitest suite and production build.
- Verify `/blog/4` at 1440x900 and 390x844, including pixel overflow checks and mobile drawer interaction.
