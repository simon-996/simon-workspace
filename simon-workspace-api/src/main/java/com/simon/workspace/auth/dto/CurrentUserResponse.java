package com.simon.workspace.auth.dto;

import com.simon.workspace.auth.model.CurrentUser;

public record CurrentUserResponse(
        String id,
        String username,
        String nickname,
        String avatarUrl,
        String email
) {
    public static CurrentUserResponse from(CurrentUser user) {
        return new CurrentUserResponse(
                String.valueOf(user.id()),
                user.username(),
                user.nickname(),
                user.avatarUrl(),
                user.email()
        );
    }
}
