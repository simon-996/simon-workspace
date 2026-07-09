package com.simon.workspace.security.dto;

import jakarta.validation.constraints.Size;

public record ReviewUserRequest(
        @Size(max = 255)
        String remark
) {
}
