package com.coc.card.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 修改密码请求（JSON 键名：old_password / new_password）
 */
public record ChangePasswordRequest(
        @NotBlank(message = "原密码不能为空")
        String oldPassword,

        @NotBlank(message = "新密码不能为空")
        @Size(min = 6, max = 100, message = "密码长度需在 6-100 个字符之间")
        String newPassword) {
}
