package com.simon.workspace.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProfileUpdateRequest(
        @NotBlank
        @Size(max = 64)
        String nickname,

        @Email
        @Size(max = 128)
        String email,

        @Size(max = 512)
        String avatarUrl
) {
}
