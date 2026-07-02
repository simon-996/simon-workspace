package com.simon.workspace.security;

import com.simon.workspace.security.dto.ManagedUserResponse;
import com.simon.workspace.security.dto.PermissionResponse;
import com.simon.workspace.security.dto.RoleResponse;
import com.simon.workspace.security.dto.UpdateUserRolesRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SecurityManagementService {

    private static final String OWNER_ROLE = "OWNER";

    private final JdbcTemplate jdbcTemplate;

    public SecurityManagementService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<ManagedUserResponse> listUsers(String keyword) {
        if (StringUtils.hasText(keyword)) {
            String like = "%" + keyword.trim() + "%";
            return jdbcTemplate.query("""
                            SELECT id, username, nickname, email, status, last_login_time, created_time, updated_time
                            FROM `user`
                            WHERE deleted = 0
                              AND (username LIKE ? OR nickname LIKE ? OR email LIKE ?)
                            ORDER BY updated_time DESC, id DESC
                            """,
                    (rs, rowNum) -> mapUser(rs),
                    like,
                    like,
                    like
            );
        }

        return jdbcTemplate.query("""
                        SELECT id, username, nickname, email, status, last_login_time, created_time, updated_time
                        FROM `user`
                        WHERE deleted = 0
                        ORDER BY updated_time DESC, id DESC
                        """,
                (rs, rowNum) -> mapUser(rs)
        );
    }

    public List<RoleResponse> listRoles() {
        return jdbcTemplate.query("""
                        SELECT id, role_code, role_name, description
                        FROM `role`
                        WHERE deleted = 0
                        ORDER BY role_code ASC
                        """,
                (rs, rowNum) -> mapRole(rs)
        );
    }

    @Transactional
    public ManagedUserResponse updateUserRoles(long userId, UpdateUserRolesRequest request) {
        ensureUserExists(userId);
        List<String> roleCodes = normalizeRoleCodes(request.roleCodes());

        if (!roleCodes.contains(OWNER_ROLE) && userHasRole(userId, OWNER_ROLE) && ownerUserCount() <= 1) {
            throw new IllegalArgumentException("至少保留一个 OWNER 账号");
        }

        Map<String, Long> roleIds = findRoleIds(roleCodes);
        if (roleIds.size() != roleCodes.size()) {
            throw new IllegalArgumentException("角色不存在");
        }

        jdbcTemplate.update("UPDATE user_role SET deleted = 1 WHERE user_id = ?", userId);
        roleCodes.forEach(roleCode -> upsertUserRole(userId, roleIds.get(roleCode)));

        return findUser(userId);
    }

    private ManagedUserResponse findUser(long userId) {
        return jdbcTemplate.query("""
                        SELECT id, username, nickname, email, status, last_login_time, created_time, updated_time
                        FROM `user`
                        WHERE id = ? AND deleted = 0
                        LIMIT 1
                        """,
                (rs, rowNum) -> mapUser(rs),
                userId
        ).stream().findFirst().orElseThrow(() -> new IllegalArgumentException("用户不存在"));
    }

    private void ensureUserExists(long userId) {
        Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM `user` WHERE id = ? AND deleted = 0",
                Long.class,
                userId
        );
        if (count == null || count == 0) {
            throw new IllegalArgumentException("用户不存在");
        }
    }

    private ManagedUserResponse mapUser(ResultSet rs) throws SQLException {
        long userId = rs.getLong("id");
        return new ManagedUserResponse(
                String.valueOf(userId),
                rs.getString("username"),
                rs.getString("nickname"),
                rs.getString("email"),
                rs.getString("status"),
                toLocalDateTime(rs.getTimestamp("last_login_time")),
                findUserRoles(userId),
                rs.getTimestamp("created_time").toLocalDateTime(),
                rs.getTimestamp("updated_time").toLocalDateTime()
        );
    }

    private RoleResponse mapRole(ResultSet rs) throws SQLException {
        long roleId = rs.getLong("id");
        return new RoleResponse(
                String.valueOf(roleId),
                rs.getString("role_code"),
                rs.getString("role_name"),
                rs.getString("description"),
                findRolePermissions(roleId)
        );
    }

    private List<String> findUserRoles(long userId) {
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

    private List<PermissionResponse> findRolePermissions(long roleId) {
        return jdbcTemplate.query("""
                        SELECT p.id, p.permission_code, p.permission_name, p.resource_type, p.description
                        FROM role_permission rp
                        JOIN permission p ON p.id = rp.permission_id AND p.deleted = 0
                        WHERE rp.role_id = ? AND rp.deleted = 0
                        ORDER BY p.resource_type ASC, p.permission_code ASC
                        """,
                (rs, rowNum) -> new PermissionResponse(
                        String.valueOf(rs.getLong("id")),
                        rs.getString("permission_code"),
                        rs.getString("permission_name"),
                        rs.getString("resource_type"),
                        rs.getString("description")
                ),
                roleId
        );
    }

    private List<String> normalizeRoleCodes(List<String> roleCodes) {
        if (roleCodes == null) {
            return List.of();
        }

        Set<String> normalized = new LinkedHashSet<>();
        for (String roleCode : roleCodes) {
            if (StringUtils.hasText(roleCode)) {
                normalized.add(roleCode.trim().toUpperCase(Locale.ROOT));
            }
        }
        return new ArrayList<>(normalized);
    }

    private boolean userHasRole(long userId, String roleCode) {
        Long count = jdbcTemplate.queryForObject(
                """
                        SELECT COUNT(1)
                        FROM user_role ur
                        JOIN role r ON r.id = ur.role_id AND r.deleted = 0
                        WHERE ur.user_id = ? AND ur.deleted = 0 AND r.role_code = ?
                        """,
                Long.class,
                userId,
                roleCode
        );
        return count != null && count > 0;
    }

    private long ownerUserCount() {
        Long count = jdbcTemplate.queryForObject(
                """
                        SELECT COUNT(DISTINCT ur.user_id)
                        FROM user_role ur
                        JOIN role r ON r.id = ur.role_id AND r.deleted = 0
                        JOIN `user` u ON u.id = ur.user_id AND u.deleted = 0
                        WHERE ur.deleted = 0 AND r.role_code = 'OWNER'
                        """,
                Long.class
        );
        return count == null ? 0 : count;
    }

    private Map<String, Long> findRoleIds(List<String> roleCodes) {
        if (roleCodes.isEmpty()) {
            return Map.of();
        }

        String placeholders = roleCodes.stream().map(roleCode -> "?").collect(Collectors.joining(", "));
        List<RoleIdRow> rows = jdbcTemplate.query(
                "SELECT id, role_code FROM `role` WHERE deleted = 0 AND role_code IN (" + placeholders + ")",
                (rs, rowNum) -> new RoleIdRow(rs.getString("role_code"), rs.getLong("id")),
                roleCodes.toArray()
        );
        return rows.stream().collect(Collectors.toMap(RoleIdRow::roleCode, RoleIdRow::id, (first, second) -> first));
    }

    private void upsertUserRole(long userId, long roleId) {
        jdbcTemplate.update("""
                        INSERT INTO user_role (user_id, role_id)
                        VALUES (?, ?)
                        ON DUPLICATE KEY UPDATE deleted = 0
                        """,
                userId,
                roleId
        );
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }

    private record RoleIdRow(String roleCode, long id) {
    }
}
