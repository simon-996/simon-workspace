package com.simon.workspace.auth;

import com.simon.workspace.auth.dto.CurrentUserResponse;
import com.simon.workspace.auth.dto.LoginRequest;
import com.simon.workspace.auth.dto.LoginResponse;
import com.simon.workspace.auth.dto.PasswordUpdateRequest;
import com.simon.workspace.auth.dto.ProfileUpdateRequest;
import com.simon.workspace.auth.dto.RegisterRequest;
import com.simon.workspace.auth.dto.RegisterResponse;
import com.simon.workspace.auth.session.AuthContextHolder;
import com.simon.workspace.common.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        return ApiResponse.ok(authService.login(request, httpRequest));
    }

    @PostMapping("/register")
    public ApiResponse<RegisterResponse> register(@Valid @RequestBody RegisterRequest request, HttpServletRequest httpRequest) {
        return ApiResponse.ok(authService.register(request, httpRequest));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(HttpServletRequest request) {
        authService.logout(request);
        return ApiResponse.ok(null);
    }

    @GetMapping("/me")
    public ApiResponse<CurrentUserResponse> me() {
        return ApiResponse.ok(CurrentUserResponse.from(AuthContextHolder.requireUser()));
    }

    @PutMapping("/me/profile")
    public ApiResponse<CurrentUserResponse> updateProfile(@Valid @RequestBody ProfileUpdateRequest request) {
        long userId = AuthContextHolder.requireUser().id();
        return ApiResponse.ok(CurrentUserResponse.from(authService.updateProfile(userId, request)));
    }

    @PutMapping("/me/password")
    public ApiResponse<Void> updatePassword(@Valid @RequestBody PasswordUpdateRequest request) {
        long userId = AuthContextHolder.requireUser().id();
        authService.updatePassword(userId, request);
        return ApiResponse.ok(null);
    }
}
