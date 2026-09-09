package com.coc.card.dto.pc;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * 角色卡手动排序请求（JSON 键名：pc_ids，数组顺序即新顺序）
 */
public record ReorderRequest(@NotEmpty(message = "pc_ids 不能为空") List<Long> pcIds) {
}
