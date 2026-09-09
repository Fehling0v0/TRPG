package com.coc.card.dto.pc;

import java.time.LocalDateTime;

/**
 * 角色卡列表项（列表接口只返回摘要字段）
 */
public record PcListItem(Long id,
                         Integer pcNumber,
                         String name,
                         String gender,
                         Integer age,
                         String era,
                         String occupation,
                         LocalDateTime updatedAt) {
}
