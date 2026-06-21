package com.simon.workspace.auth.session;

import com.simon.workspace.auth.model.CurrentUser;

public final class AuthContextHolder {

    private static final ThreadLocal<CurrentUser> CURRENT_USER = new ThreadLocal<>();

    private AuthContextHolder() {
    }

    public static void set(CurrentUser user) {
        CURRENT_USER.set(user);
    }

    public static CurrentUser get() {
        return CURRENT_USER.get();
    }

    public static CurrentUser requireUser() {
        CurrentUser user = get();
        if (user == null) {
            throw new IllegalStateException("当前用户不存在");
        }
        return user;
    }

    public static void clear() {
        CURRENT_USER.remove();
    }
}
