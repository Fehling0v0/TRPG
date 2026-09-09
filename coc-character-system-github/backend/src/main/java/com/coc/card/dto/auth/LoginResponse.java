package com.coc.card.dto.auth;

/**
 * 登录成功响应：JWT token + 用户信息
 */
public record LoginResponse(String token, String tokenType, UserInfo user) {
}
