package com.coc.card.controller;

import com.coc.card.common.ApiResponse;
import com.coc.card.dto.admin.AdminUserView;
import com.coc.card.dto.admin.InviteCodeView;
import com.coc.card.security.SecurityUtil;
import com.coc.card.service.AdminService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 管理员接口（仅 ADMIN 角色可访问，权限在 SecurityConfig 中统一拦截）
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    // ---------------- 邀请码 ----------------

    @GetMapping("/invite-codes")
    public ApiResponse<List<InviteCodeView>> listInviteCodes() {
        return ApiResponse.ok(adminService.listInviteCodes());
    }

    @PostMapping("/invite-codes")
    public ApiResponse<InviteCodeView> createInviteCode() {
        return ApiResponse.ok(adminService.createInviteCode(SecurityUtil.currentUserId()));
    }

    @DeleteMapping("/invite-codes/{id}")
    public ApiResponse<Void> deleteInviteCode(@PathVariable Long id) {
        adminService.deleteInviteCode(id);
        return ApiResponse.ok();
    }

    // ---------------- 用户 ----------------

    @GetMapping("/users")
    public ApiResponse<List<AdminUserView>> listUsers() {
        return ApiResponse.ok(adminService.listUsers());
    }

    @DeleteMapping("/users/{userId}")
    public ApiResponse<Void> deleteUser(@PathVariable Long userId) {
        adminService.deleteUser(userId);
        return ApiResponse.ok();
    }

    @PutMapping("/users/{userId}/reset-password")
    public ApiResponse<Map<String, String>> resetPassword(@PathVariable Long userId) {
        String newPassword = adminService.resetPassword(userId);
        return ApiResponse.ok(Map.of("new_password", newPassword));
    }
}
