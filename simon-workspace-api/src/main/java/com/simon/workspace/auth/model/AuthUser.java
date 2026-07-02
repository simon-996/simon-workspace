package com.simon.workspace.auth.model;

import java.util.List;

public record AuthUser(
        long id,
        String username,
        String passwordHash,
        String nickname,
        String avatarUrl,
        String email,
        String status,
        List<String> roles,
        List<String> permissions
) {
    public CurrentUser toCurrentUser() {
        return new CurrentUser(id, username, nickname, avatarUrl, email, roles, permissions);
    }
}
