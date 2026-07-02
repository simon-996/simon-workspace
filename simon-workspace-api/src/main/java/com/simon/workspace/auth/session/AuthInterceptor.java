package com.simon.workspace.auth.session;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.simon.workspace.auth.permission.PermissionAuthorization;
import com.simon.workspace.auth.permission.RequirePermission;
import com.simon.workspace.common.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final TokenStore tokenStore;
    private final ObjectMapper objectMapper;
    private final PermissionAuthorization permissionAuthorization;

    public AuthInterceptor(
            TokenStore tokenStore,
            ObjectMapper objectMapper,
            PermissionAuthorization permissionAuthorization
    ) {
        this.tokenStore = tokenStore;
        this.objectMapper = objectMapper;
        this.permissionAuthorization = permissionAuthorization;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (HttpMethod.OPTIONS.matches(request.getMethod()) || isPublicPath(request.getRequestURI())) {
            return true;
        }

        String token = extractToken(request);
        return tokenStore.find(token)
                .map(user -> {
                    String permission = requiredPermission(handler);
                    if (permission != null && !permissionAuthorization.canAccess(user, permission)) {
                        writeForbidden(response);
                        return false;
                    }
                    AuthContextHolder.set(user);
                    return true;
                })
                .orElseGet(() -> {
                    writeUnauthorized(response);
                    return false;
                });
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

    private String requiredPermission(Object handler) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return null;
        }

        RequirePermission methodPermission = handlerMethod.getMethodAnnotation(RequirePermission.class);
        if (methodPermission != null) {
            return methodPermission.value();
        }

        RequirePermission typePermission = handlerMethod.getBeanType().getAnnotation(RequirePermission.class);
        return typePermission == null ? null : typePermission.value();
    }

    private String extractToken(HttpServletRequest request) {
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorization != null && authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }
        return null;
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
