package com.simon.workspace.auth;

import com.simon.workspace.auth.model.AuthUser;
import com.simon.workspace.auth.model.CurrentUser;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Service
public class AuthAccountService {

    private final JdbcTemplate jdbcTemplate;

    public AuthAccountService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<AuthUser> findUserByUsername(String username) {
        return findUser("""
                        SELECT id, username, password_hash, nickname, avatar_url, email, status
                        FROM `user`
                        WHERE username = ? AND deleted = 0
                        LIMIT 1
                        """,
                username
        );
    }

    public Optional<CurrentUser> findCurrentUser(long userId) {
        return findAuthUserById(userId).map(AuthUser::toCurrentUser);
    }

    public CurrentUser requireCurrentUser(long userId) {
        return findCurrentUser(userId)
                .orElseThrow(() -> new IllegalStateException("当前用户不存在"));
    }

    public Optional<AuthUser> findAuthUserById(long userId) {
        return findUser("""
                        SELECT id, username, password_hash, nickname, avatar_url, email, status
                        FROM `user`
                        WHERE id = ? AND deleted = 0
                        LIMIT 1
                        """,
                userId
        );
    }

    public AuthUser requireAuthUser(long userId) {
        return findAuthUserById(userId)
                .orElseThrow(() -> new IllegalStateException("当前用户不存在"));
    }

    public List<String> findRoles(long userId) {
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

    public List<String> findPermissions(long userId) {
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

    private Optional<AuthUser> findUser(String sql, Object parameter) {
        Optional<AuthUser> user = jdbcTemplate.query(sql, (rs, rowNum) -> mapUser(rs), parameter)
                .stream()
                .findFirst();

        return user.map(authUser -> new AuthUser(
                authUser.id(),
                authUser.username(),
                authUser.passwordHash(),
                authUser.nickname(),
                authUser.avatarUrl(),
                authUser.email(),
                authUser.status(),
                findRoles(authUser.id()),
                findPermissions(authUser.id())
        ));
    }

    private AuthUser mapUser(ResultSet rs) throws SQLException {
        return new AuthUser(
                rs.getLong("id"),
                rs.getString("username"),
                rs.getString("password_hash"),
                rs.getString("nickname"),
                rs.getString("avatar_url"),
                rs.getString("email"),
                rs.getString("status"),
                List.of(),
                List.of()
        );
    }
}
