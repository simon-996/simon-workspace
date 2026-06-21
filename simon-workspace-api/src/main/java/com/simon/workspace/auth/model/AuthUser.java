package com.simon.workspace.auth.model;

public record AuthUser(
        long id,
        String username,
        String passwordHash,
        String nickname,
        String avatarUrl,
        String email,
        String status
) {
    public CurrentUser toCurrentUser() {
        return new CurrentUser(id, username, nickname, avatarUrl, email);
    }
}
