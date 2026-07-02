package com.simon.workspace.security.dto;

import java.util.List;

public record RoleResponse(
        String id,
        String roleCode,
        String roleName,
        String description,
        List<PermissionResponse> permissions
) {
    public RoleResponse {
        permissions = permissions == null ? List.of() : List.copyOf(permissions);
    }
}
