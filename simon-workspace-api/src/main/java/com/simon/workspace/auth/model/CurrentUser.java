package com.simon.workspace.auth.model;

public record CurrentUser(
        long id,
        String username,
        String nickname,
        String avatarUrl,
        String email
) {
}
