package com.coc.card.dto.auth;

import java.util.List;

/**
 * 用户信息（登录/注册/当前用户接口返回）
 */
public record UserInfo(Long id, String username, String role, List<String> displayFields) {
}
