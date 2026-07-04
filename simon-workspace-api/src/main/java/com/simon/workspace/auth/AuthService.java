package com.simon.workspace.auth;

import cn.dev33.satoken.stp.StpUtil;
import com.simon.workspace.auth.dto.LoginRequest;
import com.simon.workspace.auth.dto.LoginResponse;
import com.simon.workspace.auth.model.AuthUser;
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
    public LoginResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        String username = request.username().trim();
        Optional<AuthUser> user = authAccountService.findUserByUsername(username);

        if (user.isEmpty()) {
            recordLogin(null, username, httpRequest, LOGIN_FAILED, "USER_NOT_FOUND");
            throw new BusinessException(ErrorCode.AUTH_BAD_CREDENTIALS);
        }

        AuthUser authUser = user.get();
        if (!"ENABLED".equalsIgnoreCase(authUser.status())) {
            recordLogin(authUser.id(), username, httpRequest, LOGIN_FAILED, "USER_DISABLED");
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

}
