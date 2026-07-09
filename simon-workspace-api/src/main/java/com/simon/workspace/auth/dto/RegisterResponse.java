package com.simon.workspace.auth.dto;

public record RegisterResponse(
        String id,
        String username,
        String nickname,
        String email,
        String status
) {
}
