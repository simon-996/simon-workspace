package com.simon.workspace.auth.model;

import java.util.List;

public record CurrentUser(
        long id,
        String username,
        String nickname,
        String avatarUrl,
        String email,
        List<String> roles,
        List<String> permissions
) {
    public CurrentUser {
        roles = roles == null ? List.of() : List.copyOf(roles);
        permissions = permissions == null ? List.of() : List.copyOf(permissions);
    }

    public boolean hasRole(String role) {
        return roles.contains(role);
    }

    public boolean hasPermission(String permission) {
        return permissions.contains(permission);
    }
}
