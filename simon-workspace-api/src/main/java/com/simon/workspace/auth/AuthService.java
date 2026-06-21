package com.simon.workspace.auth;

import com.simon.workspace.auth.dto.LoginRequest;
import com.simon.workspace.auth.dto.LoginResponse;
import com.simon.workspace.auth.model.AuthUser;
import com.simon.workspace.auth.password.PasswordHashVerifier;
import com.simon.workspace.auth.session.TokenStore;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.Optional;

@Service
public class AuthService {

    private static final String LOGIN_SUCCESS = "SUCCESS";
    private static final String LOGIN_FAILED = "FAILED";

    private final JdbcTemplate jdbcTemplate;
    private final PasswordHashVerifier passwordHashVerifier;
    private final TokenStore tokenStore;

    public AuthService(JdbcTemplate jdbcTemplate, PasswordHashVerifier passwordHashVerifier, TokenStore tokenStore) {
        this.jdbcTemplate = jdbcTemplate;
        this.passwordHashVerifier = passwordHashVerifier;
        this.tokenStore = tokenStore;
    }

    @Transactional
    public LoginResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        String username = request.username().trim();
        Optional<AuthUser> user = findUser(username);

        if (user.isEmpty()) {
            recordLogin(null, username, httpRequest, LOGIN_FAILED, "USER_NOT_FOUND");
            throw new IllegalArgumentException("用户名或密码错误");
        }

        AuthUser authUser = user.get();
        if (!"ENABLED".equalsIgnoreCase(authUser.status())) {
            recordLogin(authUser.id(), username, httpRequest, LOGIN_FAILED, "USER_DISABLED");
            throw new IllegalArgumentException("账号不可用");
        }

        if (!passwordHashVerifier.matches(request.password(), authUser.passwordHash())) {
            recordLogin(authUser.id(), username, httpRequest, LOGIN_FAILED, "BAD_CREDENTIALS");
            throw new IllegalArgumentException("用户名或密码错误");
        }

        jdbcTemplate.update("UPDATE `user` SET last_login_time = NOW() WHERE id = ?", authUser.id());
        recordLogin(authUser.id(), username, httpRequest, LOGIN_SUCCESS, null);

        String token = tokenStore.create(authUser);
        return new LoginResponse(token, authUser.toCurrentUser());
    }

    public void logout(HttpServletRequest request) {
        tokenStore.remove(extractToken(request));
    }

    private Optional<AuthUser> findUser(String username) {
        return jdbcTemplate.query("""
                        SELECT id, username, password_hash, nickname, avatar_url, email, status
                        FROM `user`
                        WHERE username = ? AND deleted = 0
                        LIMIT 1
                        """,
                (rs, rowNum) -> mapUser(rs),
                username
        ).stream().findFirst();
    }

    private AuthUser mapUser(ResultSet rs) throws SQLException {
        return new AuthUser(
                rs.getLong("id"),
                rs.getString("username"),
                rs.getString("password_hash"),
                rs.getString("nickname"),
                rs.getString("avatar_url"),
                rs.getString("email"),
                rs.getString("status")
        );
    }

    private void recordLogin(Long userId, String username, HttpServletRequest request, String status, String failureReason) {
        jdbcTemplate.update("""
                        INSERT INTO login_log (user_id, username, login_ip, user_agent, status, failure_reason)
                        VALUES (?, ?, ?, ?, ?, ?)
                        """,
                userId,
                username,
                clientIp(request),
                userAgent(request),
                status,
                failureReason
        );
    }

    private String clientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(forwardedFor)) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String userAgent(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        if (userAgent == null || userAgent.length() <= 512) {
            return userAgent;
        }
        return userAgent.substring(0, 512);
    }

    private String extractToken(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }
        return null;
    }
}
