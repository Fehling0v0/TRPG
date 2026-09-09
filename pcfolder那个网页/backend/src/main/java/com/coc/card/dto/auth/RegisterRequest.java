package com.coc.card.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 注册请求（JSON 键名：username / password / invite_code）
 */
public record RegisterRequest(
        @NotBlank(message = "用户名不能为空")
        @Size(max = 50, message = "用户名最长 50 个字符")
        String username,

        @NotBlank(message = "密码不能为空")
        @Size(min = 6, max = 100, message = "密码长度需在 6-100 个字符之间")
        String password,

        @NotBlank(message = "邀请码不能为空")
        String inviteCode) {
}
