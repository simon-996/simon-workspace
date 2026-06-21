package com.simon.workspace.auth.dto;

import com.simon.workspace.auth.model.CurrentUser;

public record LoginResponse(
        String token,
        CurrentUserResponse user
) {
    public LoginResponse(String token, CurrentUser user) {
        this(token, CurrentUserResponse.from(user));
    }
}
