package com.simon.workspace.auth.permission;

import cn.dev33.satoken.stp.StpInterface;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SaTokenAuthorizationProvider implements StpInterface {

    private static final String OWNER_ROLE = "OWNER";
    private static final String ALL_PERMISSIONS = "*";

    private final JdbcTemplate jdbcTemplate;

    public SaTokenAuthorizationProvider(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        Long userId = parseLoginId(loginId);
        if (userId == null) {
            return List.of();
        }

        List<String> roles = getRoleList(userId, loginType);
        if (roles.contains(OWNER_ROLE)) {
            return List.of(ALL_PERMISSIONS);
        }

        return jdbcTemplate.query("""
                        SELECT DISTINCT p.permission_code
                        FROM user_role ur
                        JOIN role r ON r.id = ur.role_id AND r.deleted = 0
                        JOIN role_permission rp ON rp.role_id = r.id AND rp.deleted = 0
                        JOIN permission p ON p.id = rp.permission_id AND p.deleted = 0
                        WHERE ur.user_id = ? AND ur.deleted = 0
                        ORDER BY p.permission_code ASC
                        """,
                (rs, rowNum) -> rs.getString("permission_code"),
                userId
        );
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        Long userId = parseLoginId(loginId);
        if (userId == null) {
            return List.of();
        }

        return jdbcTemplate.query("""
                        SELECT r.role_code
                        FROM user_role ur
                        JOIN role r ON r.id = ur.role_id AND r.deleted = 0
                        WHERE ur.user_id = ? AND ur.deleted = 0
                        ORDER BY r.role_code ASC
                        """,
                (rs, rowNum) -> rs.getString("role_code"),
                userId
        );
    }

    private Long parseLoginId(Object loginId) {
        if (loginId instanceof Number number) {
            return number.longValue();
        }
        try {
            return Long.parseLong(String.valueOf(loginId));
        } catch (NumberFormatException exception) {
            return null;
        }
    }
}
