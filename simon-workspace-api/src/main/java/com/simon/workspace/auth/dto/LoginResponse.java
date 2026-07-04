package com.simon.workspace.auth.dto;

import com.simon.workspace.auth.model.CurrentUser;

public record LoginResponse(
        String accessToken,
        String tokenType,
        Long expiresIn,
        CurrentUserResponse user
) {
    public LoginResponse(String accessToken, String tokenType, Long expiresIn, CurrentUser user) {
        this(accessToken, tokenType, expiresIn, CurrentUserResponse.from(user));
    }
}
