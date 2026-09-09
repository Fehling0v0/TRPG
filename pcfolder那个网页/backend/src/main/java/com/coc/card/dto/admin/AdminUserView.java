package com.coc.card.dto.admin;

/**
 * 用户视图（管理员列表），附带该用户拥有的角色卡数量
 */
public record AdminUserView(Long id,
                            String username,
                            String role,
                            long pcCount) {
}
