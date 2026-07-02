package com.simon.workspace.auth.permission;

import com.simon.workspace.auth.model.CurrentUser;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PermissionAuthorizationTests {

    private final PermissionAuthorization authorization = new PermissionAuthorization();

    @Test
    void ownerRoleCanAccessEveryPermission() {
        CurrentUser user = new CurrentUser(1L, "simon", "Simon", null, null, List.of("OWNER"), List.of());

        assertThat(authorization.canAccess(user, "course:manage")).isTrue();
        assertThat(authorization.canAccess(user, "site:config")).isTrue();
    }

    @Test
    void userCanAccessExplicitPermission() {
        CurrentUser user = new CurrentUser(2L, "editor", "Editor", null, null, List.of("EDITOR"), List.of("blog:manage"));

        assertThat(authorization.canAccess(user, "blog:manage")).isTrue();
    }

    @Test
    void userCannotAccessMissingPermission() {
        CurrentUser user = new CurrentUser(3L, "viewer", "Viewer", null, null, List.of("VIEWER"), List.of("blog:read"));

        assertThat(authorization.canAccess(user, "course:manage")).isFalse();
    }
}
