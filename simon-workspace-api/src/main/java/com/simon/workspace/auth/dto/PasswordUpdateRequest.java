package com.simon.workspace.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PasswordUpdateRequest(
        @NotBlank
        String currentPassword,

        @NotBlank
        @Size(min = 6, max = 128)
        String newPassword
) {
}
