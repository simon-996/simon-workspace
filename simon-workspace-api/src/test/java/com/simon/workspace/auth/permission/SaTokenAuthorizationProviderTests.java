package com.simon.workspace.auth.permission;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SaTokenAuthorizationProviderTests {

    @SuppressWarnings("unchecked")
    @Test
    void ownerReceivesWildcardPermissionForSaTokenChecks() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        when(jdbcTemplate.query(contains("SELECT r.role_code"), any(RowMapper.class), eq(1L)))
                .thenReturn(List.of("OWNER"));
        SaTokenAuthorizationProvider provider = new SaTokenAuthorizationProvider(jdbcTemplate);

        assertThat(provider.getRoleList(1L, "login")).containsExactly("OWNER");
        assertThat(provider.getPermissionList(1L, "login")).containsExactly("*");
    }

    @SuppressWarnings("unchecked")
    @Test
    void nonOwnerReceivesExplicitPermissions() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        when(jdbcTemplate.query(contains("SELECT r.role_code"), any(RowMapper.class), eq(2L)))
                .thenReturn(List.of("EDITOR"));
        when(jdbcTemplate.query(contains("SELECT DISTINCT p.permission_code"), any(RowMapper.class), eq(2L)))
                .thenReturn(List.of("blog:manage", "file:manage"));
        SaTokenAuthorizationProvider provider = new SaTokenAuthorizationProvider(jdbcTemplate);

        assertThat(provider.getRoleList(2L, "login")).containsExactly("EDITOR");
        assertThat(provider.getPermissionList(2L, "login")).containsExactly("blog:manage", "file:manage");
    }
}
