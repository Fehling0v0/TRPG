package com.coc.card.controller;

import com.coc.card.common.ApiResponse;
import com.coc.card.dto.auth.ChangePasswordRequest;
import com.coc.card.dto.auth.LoginRequest;
import com.coc.card.dto.auth.LoginResponse;
import com.coc.card.dto.auth.RegisterRequest;
import com.coc.card.dto.auth.UserInfo;
import com.coc.card.security.SecurityUtil;
import com.coc.card.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户认证：注册 / 登录 / 当前用户
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ApiResponse<UserInfo> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok(authService.login(request));
    }

    @GetMapping("/current")
    public ApiResponse<UserInfo> current() {
        return ApiResponse.ok(authService.currentUser(SecurityUtil.currentUserId()));
    }

    @PutMapping("/password")
    public ApiResponse<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(SecurityUtil.currentUserId(), request.oldPassword(), request.newPassword());
        return ApiResponse.ok();
    }
}
