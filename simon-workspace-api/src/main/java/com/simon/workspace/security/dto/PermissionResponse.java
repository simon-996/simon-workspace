package com.simon.workspace.security.dto;

public record PermissionResponse(
        String id,
        String permissionCode,
        String permissionName,
        String resourceType,
        String description
) {
}
