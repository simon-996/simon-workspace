# Error Handling and I18n Design

## Goal

Improve error feedback so API failures are predictable for code, understandable for users, and translatable in English, Chinese, and Thai.

## Current Problem

The API currently returns `code`, `message`, and `data`. Most failures collapse into generic messages, and several UI pages display raw `Error.message` through local `message.error(...)` calls. This makes login expiry, permission denial, validation errors, and server failures feel inconsistent.

## Design

The backend will keep the existing `ApiResponse<T>` shape compatible and add optional error fields:

- `errorCode`: stable machine-readable key, such as `AUTH_UNAUTHORIZED`.
- `traceId`: short request-level identifier for support/debugging.
- `params`: optional interpolation values for translated frontend messages.
- `fieldErrors`: validation errors for specific fields.

The frontend will prefer `errorCode` for translated messages. Backend `message` remains a fallback only.

## Error Families

- Authentication: `AUTH_UNAUTHORIZED`, `AUTH_BAD_CREDENTIALS`, `AUTH_ACCOUNT_DISABLED`.
- Authorization: `AUTH_FORBIDDEN`.
- Request validation: `VALIDATION_FAILED`, `BAD_REQUEST`.
- Business/domain: `BUSINESS_ERROR`, `RESOURCE_NOT_FOUND`, `CONFLICT`.
- Platform: `INTERNAL_ERROR`, `NETWORK_ERROR`, `REQUEST_TIMEOUT`.

## Frontend Behavior

- Toasts remain for lightweight action feedback.
- API errors become `AppError` objects through a shared helper.
- i18n keys live under `errors.<errorCode>`.
- Unknown or unsafe server details are replaced by a calm fallback message.
- Login and permission errors can later be promoted to route-level handling without changing API modules.

## First Implementation Slice

1. Add backend error response fields, `ErrorCode`, `BusinessException`, trace generation, and global exception handling.
2. Update auth failures and Sa-Token interceptor responses to use stable error codes.
3. Add frontend `AppError`, `unwrapApiResponse`, and `translateAppError`.
4. Add error i18n keys in English, Chinese, and Thai.
5. Replace API module unwrap logic and auth login handling with shared helpers.

