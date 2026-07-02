package com.simon.workspace.security.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ManagedUserResponse(
        String id,
        String username,
        String nickname,
        String email,
        String status,
        LocalDateTime lastLoginTime,
        List<String> roles,
        LocalDateTime createdTime,
        LocalDateTime updatedTime
) {
    public ManagedUserResponse {
        roles = roles == null ? List.of() : List.copyOf(roles);
    }
}
