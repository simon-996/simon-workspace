package com.simon.workspace.auth;

import com.simon.workspace.auth.dto.RegisterRequest;
import com.simon.workspace.auth.password.PasswordHashVerifier;
import com.simon.workspace.common.error.BusinessException;
import com.simon.workspace.common.error.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthRegistrationServiceTests {

    private final JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
    private final PasswordHashVerifier passwordHashVerifier = new PasswordHashVerifier();
    private final AuthAccountService authAccountService = mock(AuthAccountService.class);
    private final AuthService service = new AuthService(jdbcTemplate, passwordHashVerifier, authAccountService);

    @Test
    void registersPendingAccountForReview() {
        when(jdbcTemplate.queryForObject(contains("COUNT(1) FROM `user` WHERE username"), eq(Long.class), eq("newuser")))
                .thenReturn(0L);
        when(jdbcTemplate.queryForObject(contains("COUNT(1) FROM `user` WHERE email"), eq(Long.class), eq("new@example.com")))
                .thenReturn(0L);
        when(jdbcTemplate.queryForObject(contains("SELECT id FROM `user` WHERE username"), eq(Long.class), eq("newuser")))
                .thenReturn(12L);

        var response = service.register(
                new RegisterRequest(" newuser ", "secret123", "New User", " new@example.com "),
                mock(HttpServletRequest.class)
        );

        assertThat(response.id()).isEqualTo("12");
        assertThat(response.username()).isEqualTo("newuser");
        assertThat(response.status()).isEqualTo("PENDING");
        verify(jdbcTemplate).update(
                contains("INSERT INTO `user`"),
                eq("newuser"),
                eq("sha256:" + passwordHashVerifier.sha256("secret123")),
                eq("New User"),
                eq("new@example.com"),
                any()
        );
    }

    @Test
    void rejectsDuplicateUsername() {
        when(jdbcTemplate.queryForObject(contains("COUNT(1) FROM `user` WHERE username"), eq(Long.class), eq("simon")))
                .thenReturn(1L);

        assertThatThrownBy(() -> service.register(
                new RegisterRequest("simon", "secret123", "Simon", null),
                mock(HttpServletRequest.class)
        )).isInstanceOf(BusinessException.class)
                .satisfies(exception -> assertThat(((BusinessException) exception).errorCode())
                        .isEqualTo(ErrorCode.CONFLICT));
    }
}
