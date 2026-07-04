package com.simon.workspace.auth.dto;

import com.simon.workspace.auth.model.CurrentUser;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LoginResponseTests {

    @Test
    void exposesStableFrontendSessionContract() {
        CurrentUser user = new CurrentUser(1L, "simon", "Simon", null, null, List.of("OWNER"), List.of());

        LoginResponse response = new LoginResponse("token-value", "Bearer", 43_200L, user);

        assertThat(response.accessToken()).isEqualTo("token-value");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.expiresIn()).isEqualTo(43_200L);
        assertThat(response.user().username()).isEqualTo("simon");
    }
}
