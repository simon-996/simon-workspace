package com.simon.workspace.auth.dto;

import com.simon.workspace.auth.model.CurrentUser;

import java.util.List;

public record CurrentUserResponse(
        String id,
        String username,
        String nickname,
        String avatarUrl,
        String email,
        List<String> roles,
        List<String> permissions
) {
    public static CurrentUserResponse from(CurrentUser user) {
        return new CurrentUserResponse(
                String.valueOf(user.id()),
                user.username(),
                user.nickname(),
                user.avatarUrl(),
                user.email(),
                user.roles(),
                user.permissions()
        );
    }
}
