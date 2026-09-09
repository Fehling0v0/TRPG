package com.coc.card.dto.admin;

import java.time.LocalDateTime;

/**
 * 邀请码视图（管理员列表）
 */
public record InviteCodeView(Long id,
                             String code,
                             Boolean used,
                             Long createdBy,
                             Long usedBy,
                             LocalDateTime createdAt) {
}
