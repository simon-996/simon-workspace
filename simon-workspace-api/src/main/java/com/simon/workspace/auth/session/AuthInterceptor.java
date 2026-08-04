package com.simon.workspace.auth.session;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.stp.StpUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.simon.workspace.auth.AuthAccountService;
import com.simon.workspace.common.ApiResponse;
import com.simon.workspace.common.error.ErrorCode;
import com.simon.workspace.common.error.ErrorTraceId;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.regex.Pattern;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private static final Set<String> PUBLIC_BLOG_READ_PATHS = Set.of(
            "/api/blog/categories",
            "/api/blog/tags",
            "/api/blog/posts"
    );
    private static final Pattern PUBLIC_BLOG_POST_PATH = Pattern.compile(
            "/api/blog/posts/\\d+(?:/comments)?"
    );

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
        if (HttpMethod.OPTIONS.matches(request.getMethod()) || isPublicRequest(request)) {
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
                || path.equals("/api/auth/register")
                || path.startsWith("/api/files/public/")
                || path.startsWith("/api/public/")
                || path.startsWith("/actuator/");
    }

    private boolean isPublicRequest(HttpServletRequest request) {
        String path = request.getRequestURI();
        if (isPublicPath(path)) {
            return true;
        }
        return HttpMethod.GET.matches(request.getMethod())
                && (PUBLIC_BLOG_READ_PATHS.contains(path) || PUBLIC_BLOG_POST_PATH.matcher(path).matches());
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
            objectMapper.writeValue(response.getWriter(), ApiResponse.fail(ErrorCode.AUTH_UNAUTHORIZED, ErrorTraceId.next()));
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to write unauthorized response", exception);
        }
    }

    private void writeForbidden(HttpServletResponse response) {
        try {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            objectMapper.writeValue(response.getWriter(), ApiResponse.fail(ErrorCode.AUTH_FORBIDDEN, ErrorTraceId.next()));
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to write forbidden response", exception);
        }
    }
}
