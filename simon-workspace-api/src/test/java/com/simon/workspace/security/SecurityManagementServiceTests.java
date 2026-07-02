package com.simon.workspace.security;

import com.simon.workspace.security.dto.UpdateUserRolesRequest;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SecurityManagementServiceTests {

    private final JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
    private final SecurityManagementService service = new SecurityManagementService(jdbcTemplate);

    @Test
    void cannotRemoveTheOnlyOwnerRole() {
        when(jdbcTemplate.queryForObject(
                eq("SELECT COUNT(1) FROM `user` WHERE id = ? AND deleted = 0"),
                eq(Long.class),
                eq(1L)
        )).thenReturn(1L);
        when(jdbcTemplate.queryForObject(
                eq("""
                        SELECT COUNT(1)
                        FROM user_role ur
                        JOIN role r ON r.id = ur.role_id AND r.deleted = 0
                        WHERE ur.user_id = ? AND ur.deleted = 0 AND r.role_code = ?
                        """),
                eq(Long.class),
                eq(1L),
                eq("OWNER")
        )).thenReturn(1L);
        when(jdbcTemplate.queryForObject(
                eq("""
                        SELECT COUNT(DISTINCT ur.user_id)
                        FROM user_role ur
                        JOIN role r ON r.id = ur.role_id AND r.deleted = 0
                        JOIN `user` u ON u.id = ur.user_id AND u.deleted = 0
                        WHERE ur.deleted = 0 AND r.role_code = 'OWNER'
                        """),
                eq(Long.class)
        )).thenReturn(1L);

        assertThatThrownBy(() -> service.updateUserRoles(1L, new UpdateUserRolesRequest(List.of("ADMIN"))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("至少保留一个 OWNER 账号");

        verify(jdbcTemplate, never()).update(eq("UPDATE user_role SET deleted = 1 WHERE user_id = ?"), eq(1L));
    }
}
