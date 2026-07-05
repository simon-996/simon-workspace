# Header Terminal Theme and Error UX Design

**Goal:** Make account avatar display consistently, turn the homepage terminal into a useful command entry, add a global dark-mode switch, and harden user-facing error messages for network and backend failures.

## Account Avatar
- Avatar URLs should be resolved through one frontend helper so absolute URLs, backend relative `/api/...` URLs, and storage public URLs render consistently.
- The account center and header should share the same avatar source from `auth.user.avatarUrl` after profile/avatar updates.
- The header account control should show a compact circular avatar next to the account label. If no avatar exists, it falls back to the current initial.

## Terminal Commands
- The terminal prompt starts empty. The initial line stays as guidance but no command is pre-filled.
- Command parsing supports quoted arguments and commands with parameters.
- `login <username> <password>` calls the auth store and updates session state.
- `logout` calls the auth store and leaves protected pages when needed.
- `whoami` shows the current account state.
- Navigation commands stay permission-aware and keep the existing concise terminal style.

## Theme
- Add a persistent `light | dark` app theme setting stored in localStorage.
- Add a header toggle with a moon icon on the left and sun icon on the right.
- Wire Naive UI theme and app-level CSS variables to the selected theme so pages, cards, modals, and the homepage remain visually coherent.

## Error UX
- Normalize all request failures into `AppError`, including no response, timeout, backend business errors, non-standard HTTP errors, and unknown exceptions.
- Add one reusable helper for showing translated API errors through Naive UI messages.
- Make terminal/auth/profile commands use the same translated error path.
- Add tests for network failure, timeout, backend error response, and fallback message behavior.
