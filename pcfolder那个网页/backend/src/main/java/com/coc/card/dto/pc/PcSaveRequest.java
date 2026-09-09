package com.coc.card.dto.pc;

import com.coc.card.entity.model.Experience;
import com.coc.card.entity.model.Item;
import com.coc.card.entity.model.SkillValue;
import com.coc.card.entity.model.Spell;
import com.coc.card.entity.model.Weapon;

import java.util.List;
import java.util.Map;

/**
 * 角色卡保存请求（创建 / 更新共用）。
 * 前端传入完整 JSON 数据；除 name 外均可为空，JSON 键名由全局 SNAKE_CASE 策略映射。
 */
public record PcSaveRequest(String name,
                            String gender,
                            Integer age,
                            String era,
                            String occupation,
                            Map<String, Object> attributes,
                            Map<String, SkillValue> skills,
                            List<Weapon> weapons,
                            List<Item> items,
                            Map<String, Object> background,
                            List<Experience> experiences,
                            List<Spell> spells,
                            Map<String, Object> customFields) {
}
