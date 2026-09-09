package com.coc.card.dto.pc;

import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * 更新角色卡列表自定义显示列配置
 */
public record DisplayFieldsRequest(@NotNull(message = "display_fields 不能为空") List<String> displayFields) {
}
