package com.simon.workspace.auth;

import com.simon.workspace.auth.dto.PasswordUpdateRequest;
import com.simon.workspace.auth.dto.ProfileUpdateRequest;
import com.simon.workspace.auth.model.AuthUser;
import com.simon.workspace.auth.model.CurrentUser;
import com.simon.workspace.auth.password.PasswordHashVerifier;
import com.simon.workspace.common.error.BusinessException;
import com.simon.workspace.common.error.ErrorCode;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthServiceProfileTests {

    private final JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
    private final PasswordHashVerifier passwordHashVerifier = new PasswordHashVerifier();
    private final AuthAccountService authAccountService = mock(AuthAccountService.class);
    private final AuthService service = new AuthService(jdbcTemplate, passwordHashVerifier, authAccountService);

    @Test
    void updatesCurrentUsersProfileAndReturnsRefreshedUser() {
        CurrentUser refreshed = new CurrentUser(
                1L,
                "simon",
                "Chen Ximeng",
                "https://cdn.example.com/avatar.png",
                "simon@example.com",
                List.of("OWNER"),
                List.of("*")
        );
        when(jdbcTemplate.update(
                contains("UPDATE `user`"),
                eq("Chen Ximeng"),
                eq("simon@example.com"),
                eq("https://cdn.example.com/avatar.png"),
                eq(1L)
        )).thenReturn(1);
        when(authAccountService.requireCurrentUser(1L)).thenReturn(refreshed);

        CurrentUser user = service.updateProfile(
                1L,
                new ProfileUpdateRequest(" Chen Ximeng ", " simon@example.com ", " https://cdn.example.com/avatar.png ")
        );

        assertThat(user).isEqualTo(refreshed);
        verify(jdbcTemplate).update(
                contains("UPDATE `user`"),
                eq("Chen Ximeng"),
                eq("simon@example.com"),
                eq("https://cdn.example.com/avatar.png"),
                eq(1L)
        );
    }

    @Test
    void updatesPasswordWithoutClearingTheCurrentSession() {
        String oldHash = "sha256:" + passwordHashVerifier.sha256("old-secret");
        AuthUser currentUser = new AuthUser(
                1L,
                "simon",
                oldHash,
                "Simon",
                null,
                null,
                "ENABLED",
                List.of("OWNER"),
                List.of("*")
        );
        when(authAccountService.requireAuthUser(1L)).thenReturn(currentUser);
        when(jdbcTemplate.update(
                contains("UPDATE `user` SET password_hash"),
                eq("sha256:" + passwordHashVerifier.sha256("new-secret")),
                eq(1L)
        )).thenReturn(1);

        service.updatePassword(1L, new PasswordUpdateRequest("old-secret", "new-secret"));

        verify(jdbcTemplate).update(
                contains("UPDATE `user` SET password_hash"),
                eq("sha256:" + passwordHashVerifier.sha256("new-secret")),
                eq(1L)
        );
    }

    @Test
    void rejectsPasswordChangeWhenCurrentPasswordIsWrong() {
        String oldHash = "sha256:" + passwordHashVerifier.sha256("old-secret");
        AuthUser currentUser = new AuthUser(
                1L,
                "simon",
                oldHash,
                "Simon",
                null,
                null,
                "ENABLED",
                List.of("OWNER"),
                List.of("*")
        );
        when(authAccountService.requireAuthUser(1L)).thenReturn(currentUser);

        assertThatThrownBy(() -> service.updatePassword(1L, new PasswordUpdateRequest("bad-secret", "new-secret")))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> assertThat(((BusinessException) exception).errorCode())
                        .isEqualTo(ErrorCode.AUTH_BAD_CREDENTIALS));

        verify(jdbcTemplate, never()).update(contains("UPDATE `user` SET password_hash"), any(), eq(1L));
    }
}
