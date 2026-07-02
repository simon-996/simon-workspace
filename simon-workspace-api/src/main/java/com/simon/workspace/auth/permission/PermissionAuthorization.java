package com.simon.workspace.auth.permission;

import com.simon.workspace.auth.model.CurrentUser;
import org.springframework.stereotype.Component;

@Component
public class PermissionAuthorization {

    public boolean canAccess(CurrentUser user, String permission) {
        if (user == null) {
            return false;
        }
        return user.hasRole("OWNER") || user.hasPermission(permission);
    }
}
