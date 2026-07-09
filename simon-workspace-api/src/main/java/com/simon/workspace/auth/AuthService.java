package com.simon.workspace.auth;

import cn.dev33.satoken.stp.StpUtil;
import com.simon.workspace.auth.dto.RegisterRequest;
import com.simon.workspace.auth.dto.RegisterResponse;
import com.simon.workspace.auth.dto.PasswordUpdateRequest;
import com.simon.workspace.auth.dto.ProfileUpdateRequest;
import com.simon.workspace.auth.dto.LoginRequest;
import com.simon.workspace.auth.dto.LoginResponse;
import com.simon.workspace.auth.model.AuthUser;
import com.simon.workspace.auth.model.CurrentUser;
import com.simon.workspace.auth.password.PasswordHashVerifier;
import com.simon.workspace.common.error.BusinessException;
import com.simon.workspace.common.error.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Optional;

@Service
public class AuthService {

    private static final String LOGIN_SUCCESS = "SUCCESS";
    private static final String LOGIN_FAILED = "FAILED";
    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_ENABLED = "ENABLED";
    private static final String STATUS_REJECTED = "REJECTED";

    private final JdbcTemplate jdbcTemplate;
    private final PasswordHashVerifier passwordHashVerifier;
    private final AuthAccountService authAccountService;

    public AuthService(
            JdbcTemplate jdbcTemplate,
            PasswordHashVerifier passwordHashVerifier,
            AuthAccountService authAccountService
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.passwordHashVerifier = passwordHashVerifier;
        this.authAccountService = authAccountService;
    }

    @Transactional
    public RegisterResponse register(RegisterRequest request, HttpServletRequest httpRequest) {
        String username = required(request.username(), "用户名不能为空");
        String nickname = required(request.nickname(), "昵称不能为空");
        String password = required(request.password(), "密码不能为空");
        String email = blankToNull(request.email());

        if (password.length() < 8) {
            throw new IllegalArgumentException("密码至少需要 8 位");
        }
        if (existsByUsername(username)) {
            throw new BusinessException(ErrorCode.CONFLICT);
        }
        if (email != null && existsByEmail(email)) {
            throw new BusinessException(ErrorCode.CONFLICT);
        }

        String passwordHash = "sha256:" + passwordHashVerifier.sha256(password);
        String registerIp = clientIp(httpRequest);
        String userAgent = userAgent(httpRequest);
        jdbcTemplate.update("""
                        INSERT INTO `user` (username, password_hash, nickname, email, status, review_remark)
                        VALUES (?, ?, ?, ?, 'PENDING', ?)
                        """,
                username,
                passwordHash,
                nickname,
                email,
                registerIp + " | " + (userAgent == null ? "" : userAgent)
        );
        Long id = jdbcTemplate.queryForObject("SELECT id FROM `user` WHERE username = ? AND deleted = 0", Long.class, username);
        return new RegisterResponse(String.valueOf(id), username, nickname, email, STATUS_PENDING);
    }

    @Transactional
    public LoginResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        String username = request.username().trim();
        Optional<AuthUser> user = authAccountService.findUserByUsername(username);

        if (user.isEmpty()) {
            recordLogin(null, username, httpRequest, LOGIN_FAILED, "USER_NOT_FOUND");
            throw new BusinessException(ErrorCode.AUTH_BAD_CREDENTIALS);
        }

        AuthUser authUser = user.get();
        if (!STATUS_ENABLED.equalsIgnoreCase(authUser.status())) {
            String status = authUser.status();
            recordLogin(authUser.id(), username, httpRequest, LOGIN_FAILED, "USER_" + status);
            if (STATUS_PENDING.equalsIgnoreCase(status)) {
                throw new BusinessException(ErrorCode.AUTH_ACCOUNT_PENDING);
            }
            if (STATUS_REJECTED.equalsIgnoreCase(status)) {
                throw new BusinessException(ErrorCode.AUTH_ACCOUNT_REJECTED);
            }
            throw new BusinessException(ErrorCode.AUTH_ACCOUNT_DISABLED);
        }

        if (!passwordHashVerifier.matches(request.password(), authUser.passwordHash())) {
            recordLogin(authUser.id(), username, httpRequest, LOGIN_FAILED, "BAD_CREDENTIALS");
            throw new BusinessException(ErrorCode.AUTH_BAD_CREDENTIALS);
        }

        jdbcTemplate.update("UPDATE `user` SET last_login_time = NOW() WHERE id = ?", authUser.id());
        recordLogin(authUser.id(), username, httpRequest, LOGIN_SUCCESS, null);

        StpUtil.login(authUser.id());
        return new LoginResponse(
                StpUtil.getTokenValue(),
                "Bearer",
                StpUtil.getTokenTimeout(),
                authUser.toCurrentUser()
        );
    }

    public void logout(HttpServletRequest request) {
        if (StpUtil.isLogin()) {
            StpUtil.logout();
        }
    }

    @Transactional
    public CurrentUser updateProfile(long userId, ProfileUpdateRequest request) {
        String nickname = required(request.nickname(), "昵称不能为空");
        int affected = jdbcTemplate.update("""
                        UPDATE `user`
                        SET nickname = ?, email = ?, avatar_url = ?
                        WHERE id = ? AND deleted = 0
                        """,
                nickname,
                blankToNull(request.email()),
                blankToNull(request.avatarUrl()),
                userId
        );
        if (affected == 0) {
            throw new IllegalStateException("当前用户不存在");
        }
        return authAccountService.requireCurrentUser(userId);
    }

    @Transactional
    public void updatePassword(long userId, PasswordUpdateRequest request) {
        AuthUser currentUser = authAccountService.requireAuthUser(userId);
        if (!passwordHashVerifier.matches(request.currentPassword(), currentUser.passwordHash())) {
            throw new BusinessException(ErrorCode.AUTH_BAD_CREDENTIALS);
        }

        String newPassword = required(request.newPassword(), "新密码不能为空");
        String newPasswordHash = "sha256:" + passwordHashVerifier.sha256(newPassword);
        int affected = jdbcTemplate.update("""
                        UPDATE `user` SET password_hash = ?
                        WHERE id = ? AND deleted = 0
                        """,
                newPasswordHash,
                userId
        );
        if (affected == 0) {
            throw new IllegalStateException("当前用户不存在");
        }
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

    private String required(String value, String message) {
        if (!StringUtils.hasText(value)) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }

    private String blankToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private boolean existsByUsername(String username) {
        Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM `user` WHERE username = ? AND deleted = 0",
                Long.class,
                username
        );
        return count != null && count > 0;
    }

    private boolean existsByEmail(String email) {
        Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM `user` WHERE email = ? AND deleted = 0",
                Long.class,
                email
        );
        return count != null && count > 0;
    }

}
