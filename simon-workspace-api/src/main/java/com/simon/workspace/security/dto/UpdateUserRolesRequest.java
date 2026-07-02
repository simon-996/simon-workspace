package com.simon.workspace.security.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record UpdateUserRolesRequest(
        @NotNull
        List<String> roleCodes
) {
}
