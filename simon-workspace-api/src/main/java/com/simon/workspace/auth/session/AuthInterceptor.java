package com.simon.workspace.auth.session;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.stp.StpUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.simon.workspace.auth.AuthAccountService;
import com.simon.workspace.common.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final AuthAccountService authAccountService;
    private final ObjectMapper objectMapper;

    public AuthInterceptor(
            AuthAccountService authAccountService,
            ObjectMapper objectMapper
    ) {
        this.authAccountService = authAccountService;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (HttpMethod.OPTIONS.matches(request.getMethod()) || isPublicPath(request.getRequestURI())) {
            return true;
        }

        try {
            StpUtil.checkLogin();
            var currentUser = authAccountService.requireCurrentUser(StpUtil.getLoginIdAsLong());
            checkRequiredPermissions(handler);
            AuthContextHolder.set(currentUser);
            return true;
        } catch (NotLoginException exception) {
            writeUnauthorized(response);
            return false;
        } catch (NotPermissionException exception) {
            writeForbidden(response);
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        AuthContextHolder.clear();
    }

    private boolean isPublicPath(String path) {
        return path.equals("/api/health")
                || path.equals("/api/auth/login")
                || path.startsWith("/api/public/")
                || path.startsWith("/actuator/");
    }

    private void checkRequiredPermissions(Object handler) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return;
        }

        SaCheckPermission methodPermission = handlerMethod.getMethodAnnotation(SaCheckPermission.class);
        if (methodPermission != null) {
            checkPermissions(methodPermission.value());
            return;
        }

        SaCheckPermission typePermission = handlerMethod.getBeanType().getAnnotation(SaCheckPermission.class);
        if (typePermission != null) {
            checkPermissions(typePermission.value());
        }
    }

    private void checkPermissions(String[] permissions) {
        for (String permission : permissions) {
            StpUtil.checkPermission(permission);
        }
    }

    private void writeUnauthorized(HttpServletResponse response) {
        try {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            objectMapper.writeValue(response.getWriter(), ApiResponse.fail("未登录或登录已失效"));
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to write unauthorized response", exception);
        }
    }

    private void writeForbidden(HttpServletResponse response) {
        try {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            objectMapper.writeValue(response.getWriter(), ApiResponse.fail("无权限访问该功能"));
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to write forbidden response", exception);
        }
    }
}
