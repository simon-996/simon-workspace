package com.simon.workspace.security;

import com.simon.workspace.auth.permission.RequirePermission;
import com.simon.workspace.common.ApiResponse;
import com.simon.workspace.security.dto.ManagedUserResponse;
import com.simon.workspace.security.dto.RoleResponse;
import com.simon.workspace.security.dto.UpdateUserRolesRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/security")
@RequirePermission("user:manage")
public class SecurityManagementController {

    private final SecurityManagementService securityManagementService;

    public SecurityManagementController(SecurityManagementService securityManagementService) {
        this.securityManagementService = securityManagementService;
    }

    @GetMapping("/users")
    public ApiResponse<List<ManagedUserResponse>> users(@RequestParam(required = false) String keyword) {
        return ApiResponse.ok(securityManagementService.listUsers(keyword));
    }

    @GetMapping("/roles")
    public ApiResponse<List<RoleResponse>> roles() {
        return ApiResponse.ok(securityManagementService.listRoles());
    }

    @PutMapping("/users/{id}/roles")
    public ApiResponse<ManagedUserResponse> updateUserRoles(
            @PathVariable long id,
            @Valid @RequestBody UpdateUserRolesRequest request
    ) {
        return ApiResponse.ok(securityManagementService.updateUserRoles(id, request));
    }
}
