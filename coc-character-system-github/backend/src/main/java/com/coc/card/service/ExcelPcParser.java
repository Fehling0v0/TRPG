package com.coc.card.service;

import com.coc.card.entity.Pc;
import com.coc.card.entity.model.Experience;
import com.coc.card.entity.model.Item;
import com.coc.card.entity.model.SkillValue;
import com.coc.card.entity.model.Spell;
import com.coc.card.entity.model.Weapon;
import com.coc.card.exception.BusinessException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellReference;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.LinkedHashMap;

/**
 * COC 角色卡 Excel 解析器（Apache POI）。
 * <p>
 * 适配常见的中文 CoC7 车卡模板（如「弗朗西斯·伊文斯.xlsx」）：
 * 数据在「人物卡」sheet，公式单元格直接读取 Excel 缓存的计算结果，
 * 各区块按标签文字定位，缺失内容自动留空。
 */
@Component
public class ExcelPcParser {

    /**
     * 解析 Excel 为未保存的 Pc 实体（不设置 userId / pcNumber）
     */
    public Pc parse(InputStream inputStream) throws Exception {
        try (Workbook workbook = WorkbookFactory.create(inputStream)) {
            Sheet sheet = pickMainSheet(workbook);
            Pc pc = new Pc();
            parseBasicInfo(sheet, pc);
            parseAttributes(sheet, pc);
            parseSkills(sheet, pc);
            parseWeapons(sheet, pc);
            parseBackground(sheet, pc);
            parseItems(sheet, pc);
            parseExperiences(sheet, pc);
            parseSpells(sheet, pc);
            return pc;
        }
    }

    /**
     * 优先取「人物卡」sheet；否则取第一个名字含「人物」的可见 sheet；再退化为第一个 sheet
     */
    private Sheet pickMainSheet(Workbook workbook) {
        Sheet sheet = workbook.getSheet("人物卡");
        if (sheet != null) {
            return sheet;
        }
        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            String name = workbook.getSheetName(i);
            if (!workbook.isSheetHidden(i) && name != null && name.contains("人物")) {
                return workbook.getSheetAt(i);
            }
        }
        if (workbook.getNumberOfSheets() == 0) {
            throw BusinessException.badRequest("Excel 中没有任何工作表");
        }
        return workbook.getSheetAt(0);
    }

    // ---------------- 基础信息 ----------------
    // 模板布局：B 列标签 + E 列值；J 列副标签 + M 列副值
    // （行号为 0 基：Excel 第 3 行 → row(2)）

    private void parseBasicInfo(Sheet sheet, Pc pc) {
        pc.setName(str(sheet, 2, "E"));          // 姓名
        pc.setAge(num(sheet, 3, "E"));          // 年龄
        pc.setOccupation(str(sheet, 4, "E"));   // 职业
        // 性别：取 E6 第一个文字（模板占位「男/女」时取「男」无意义，实际填写后取第一个字）
        String gender = str(sheet, 5, "E");
        if (gender != null && !gender.isBlank()) {
            pc.setGender(String.valueOf(gender.charAt(0)));
        }
        pc.setEra(str(sheet, 3, "M"));          // 时代

        // 模板附带但不属于标准字段的信息存入 custom_fields
        putCustom(pc, "玩家", str(sheet, 2, "M"));
        putCustom(pc, "国籍", str(sheet, 5, "M"));
        putCustom(pc, "住地", str(sheet, 6, "E"));
        putCustom(pc, "故乡", str(sheet, 6, "M"));
    }

    // ---------------- 属性 ----------------
    // 标签列 S/Y/AE，数值列 U/AA/AG（第 3/5/7 行）

    private void parseAttributes(Sheet sheet, Pc pc) {
        putAttr(pc, "str", num(sheet, 2, "U"));
        putAttr(pc, "con", num(sheet, 4, "U"));
        putAttr(pc, "siz", num(sheet, 6, "U"));
        putAttr(pc, "dex", num(sheet, 2, "AA"));
        putAttr(pc, "app", num(sheet, 4, "AA"));
        putAttr(pc, "int", num(sheet, 6, "AA"));
        putAttr(pc, "pow", num(sheet, 2, "AG"));
        putAttr(pc, "edu", num(sheet, 4, "AG"));
        putAttr(pc, "luck", num(sheet, 6, "AG"));
        // HP（体力值）Excel 行10 E列，SAN（理智值）行10 N列
        putAttr(pc, "hp", num(sheet, 9, "E"));
        putAttr(pc, "san", num(sheet, 9, "N"));
        // DB（伤害加值）Excel 行52 AP列，值为文本如"+1D4"
        putAttrStr(pc, "db", str(sheet, 51, "AP"));
    }

    private void putAttr(Pc pc, String key, Integer value) {
        if (value != null) {
            pc.getAttributes().put(key, value);
        }
    }

    private void putAttrStr(Pc pc, String key, String value) {
        if (value != null && !value.isBlank()) {
            pc.getAttributes().put(key, value.trim());
        }
    }

    private void putCustom(Pc pc, String key, String value) {
        if (value != null && !value.isBlank()) {
            pc.getCustomFields().put(key, value.trim());
        }
    }

    // ---------------- 技能 ----------------
    // Excel 第 16-49 行（row 15-48），左右两栏：
    // 左栏：本职标记 D / 技能名 F / 子类型 H / 初始 J / 成长 L / 职业 N / 兴趣 P / 总值 R
    // 右栏：本职标记 Z / 技能名 AB / 子类型 AD / 初始 AF / 成长 AH / 职业 AJ / 兴趣 AL / 总值 AN
    // 「格斗：斗殴」「射击：手枪」等组合技能：技能名以「：」结尾，子类型在 H/AD 列

    private void parseSkills(Sheet sheet, Pc pc) {
        for (int r = 15; r <= 48; r++) {
            addSkill(pc,
                    str(sheet, r, "D"), str(sheet, r, "F"), str(sheet, r, "H"),
                    num(sheet, r, "J"), num(sheet, r, "L"), num(sheet, r, "N"), num(sheet, r, "P"));
            addSkill(pc,
                    str(sheet, r, "Z"), str(sheet, r, "AB"), str(sheet, r, "AD"),
                    num(sheet, r, "AF"), num(sheet, r, "AH"), num(sheet, r, "AJ"), num(sheet, r, "AL"));
        }
    }

    /**
     * @param marker   本职标记列内容（★/☯，也可能写了自定义子技能名，如「摄影★」）
     * @param name     技能名列
     * @param subtype  组合技能的子类型列（如「斗殴」「手枪」）
     */
    private void addSkill(Pc pc, String marker, String name, String subtype,
                          Integer initial, Integer growth, Integer occupation, Integer interest) {
        if (isBlank(name) || "0".equals(name) || "☐".equals(name)) {
            return;
        }
        boolean category = name.endsWith("：") || name.endsWith(":");
        if (category) {
            // 只有分类名（如「驾驶：」「生存：」）没有具体子技能的行，跳过
            if (isBlank(subtype) || subtype.matches("-?\\d+")) {
                return;
            }
            name = name + subtype.trim();
        }
        // 标记列里写了自定义子技能名（去掉 ★/☯ 符号），追加到技能名后，如「技艺①（摄影）」
        if (marker != null) {
            String sub = marker.replace("★", "").replace("☯", "").trim();
            if (!sub.isEmpty() && !sub.matches("-?\\d+") && !"0".equals(sub)) {
                name = name + "（" + sub + "）";
            }
        }

        SkillValue skill = new SkillValue();
        skill.setInitial(nz(initial));
        skill.setGrowth(nz(growth));
        skill.setOccupation(nz(occupation));
        skill.setInterest(nz(interest));
        // 同名技能（左右栏重复或自定义）保留先出现的，不覆盖
        pc.getSkills().putIfAbsent(name, skill);
    }

    // ---------------- 武器 ----------------
    // 表头（Excel 第 52 行）：B 武器名称 / G 类型 / M 使用技能 / W 伤害 / AA 射程

    private void parseWeapons(Sheet sheet, Pc pc) {
        int header = findRow(sheet, "B", "武器名称", 40, 75);
        if (header < 0) {
            return;
        }
        int last = Math.min(header + 10, sheet.getLastRowNum());
        for (int r = header + 1; r <= last; r++) {
            String name = str(sheet, r, "B");
            if (name == null) {
                continue;
            }
            if (name.contains("资产")) {
                break;
            }
            if (name.equals("无") || name.startsWith("←") || name.matches("-?\\d+")
                    || name.contains("闪避") || name.contains("体格")) {
                continue;
            }
            String skill = str(sheet, r, "M");
            String damage = str(sheet, r, "W");
            String range = str(sheet, r, "AA");
            String typeNote = str(sheet, r, "G");
            if (skill == null && damage == null && range == null) {
                continue;
            }
            Weapon weapon = new Weapon();
            weapon.setName(name);
            weapon.setType(skill);
            weapon.setDamage(damage);
            weapon.setRange(range);
            if (typeNote != null && !typeNote.equals(name)) {
                weapon.setNote(typeNote);
            }
            pc.getWeapons().add(weapon);
        }
    }

    // ---------------- 背景故事 ----------------
    // 第 60-77 行：W 列为标签（个人描述/思想与信念/重要之人/意义非凡之地/宝贵之物/特质/伤口和疤痕），
    // AA 列为内容；第 77 行 W 列的大段文字为完整背景故事

    private void parseBackground(Sheet sheet, Pc pc) {
        MapFiller bg = new MapFiller();
        for (int r = 59; r <= 77; r++) {
            String label = str(sheet, r, "W");
            if (label == null) {
                continue;
            }
            // 大段背景故事文本
            if (label.length() >= 30) {
                bg.put("text", label);
                continue;
            }
            String value = str(sheet, r, "AA");
            if (value == null) {
                continue;
            }
            if (label.contains("个人描述") || label.contains("外貌")) {
                bg.put("personal_description", value);
            } else if (label.contains("思想") || label.contains("信念")) {
                bg.put("ideology", value);
            } else if (label.contains("重要之人")) {
                bg.put("important_people", value);
            } else if (label.contains("意义") || label.contains("非凡之地")) {
                bg.put("important_place", value);
            } else if (label.contains("宝贵")) {
                bg.put("treasured_item", value);
            } else if (label.contains("特质")) {
                bg.put("traits", value);
            } else if (label.contains("伤口") || label.contains("疤痕")) {
                bg.put("wounds_scars", value);
            }
        }
        if (!bg.map.isEmpty()) {
            pc.setBackground(bg.map);
        }
    }

    // ---------------- 随身物品 ----------------
    // 表头（Excel 第 78 行）：B 状态 / D 部位 / F 物品名称 / N 背包格

    private void parseItems(Sheet sheet, Pc pc) {
        int header = findRow(sheet, "F", "物品名称", 70, 95);
        if (header < 0) {
            return;
        }
        int stop = findRow(sheet, "B", "调查员经历", header, header + 25);
        int last = stop > 0 ? stop - 1 : Math.min(header + 20, sheet.getLastRowNum());
        for (int r = header + 1; r <= last; r++) {
            String name = str(sheet, r, "F");
            String backpack = str(sheet, r, "N");
            if (name == null && backpack == null) {
                continue;
            }
            Item item = new Item();
            if (name != null) {
                item.setName(name);
                item.setNote(backpack);
            } else {
                // 只有背包格里写了东西
                item.setName(backpack);
            }
            String status = str(sheet, r, "B");
            if ("隐藏".equals(status) || "显露".equals(status)) {
                item.setStatus(status);
            }
            item.setLocation(str(sheet, r, "D"));
            pc.getItems().add(item);
        }
    }

    // ---------------- 模组经历 ----------------
    // 表头（Excel 第 96 行）：B 经历模组 / J 人物变化描述

    private void parseExperiences(Sheet sheet, Pc pc) {
        int header = findRow(sheet, "B", "经历模组", 90, 115);
        if (header < 0) {
            return;
        }
        for (int r = header + 1; r <= header + 8; r++) {
            String module = str(sheet, r, "B");
            if (module == null) {
                continue;
            }
            // 模板自带的示例行（「例：【毒汤】」等）跳过
            if (module.startsWith("例")) {
                continue;
            }
            String change = str(sheet, r, "J");
            if (change != null && change.startsWith("例")) {
                change = null;
            }
            Experience experience = new Experience();
            experience.setModuleName(module.replace("【", "").replace("】", "").trim());
            experience.setChangeDescription(change);
            pc.getExperiences().add(experience);
        }
    }

    // ---------------- 法术 ----------------
    // 表头（Excel 第 113 行）：Y 法术名称 / AC 使用代价 / AH 作用；W 列「例：x」为示例行标记

    private void parseSpells(Sheet sheet, Pc pc) {
        int header = findRow(sheet, "Y", "法术名称", 105, 130);
        if (header < 0) {
            return;
        }
        for (int r = header + 1; r <= header + 8; r++) {
            String name = str(sheet, r, "Y");
            if (name == null) {
                continue;
            }
            String exampleMark = str(sheet, r, "W");
            if (exampleMark != null && exampleMark.startsWith("例")) {
                continue;
            }
            Spell spell = new Spell();
            spell.setName(name);
            spell.setCost(str(sheet, r, "AC"));
            spell.setEffect(str(sheet, r, "AH"));
            pc.getSpells().add(spell);
        }
    }

    // ---------------- POI 单元格工具方法 ----------------

    /** 简单的可空 Map 填充器 */
    private static class MapFiller {
        private final LinkedHashMap<String, Object> map = new LinkedHashMap<>();

        void put(String key, String value) {
            if (value != null && !value.isBlank()) {
                map.put(key, value.trim());
            }
        }
    }

    /**
     * 在指定列、指定行范围内查找第一个包含关键字的单元格，返回行号（0 基），找不到返回 -1
     */
    private int findRow(Sheet sheet, String col, String keyword, int fromRow, int toRow) {
        int to = Math.min(toRow, sheet.getLastRowNum());
        for (int r = fromRow; r <= to; r++) {
            String value = str(sheet, r, col);
            if (value != null && value.contains(keyword)) {
                return r;
            }
        }
        return -1;
    }

    private String str(Sheet sheet, int rowIdx, String col) {
        Row row = sheet.getRow(rowIdx);
        if (row == null) {
            return null;
        }
        Cell cell = row.getCell(CellReference.convertColStringToIndex(col),
                Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        return cell == null ? null : str(cell);
    }

    /**
     * 读取单元格文本；公式单元格读取 Excel 缓存的计算结果（车卡模板公式多、跨表引用多，不做公式重算）
     */
    private String str(Cell cell) {
        try {
            CellType type = cell.getCellType();
            if (type == CellType.FORMULA) {
                type = cell.getCachedFormulaResultType();
            }
            return switch (type) {
                case STRING -> {
                    String value = cell.getStringCellValue();
                    if (value == null) {
                        yield null;
                    }
                    value = value.trim();
                    yield value.isEmpty() ? null : value;
                }
                case NUMERIC -> {
                    double d = cell.getNumericCellValue();
                    yield d == Math.floor(d) ? String.valueOf((long) d) : String.valueOf(d);
                }
                case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
                default -> null;
            };
        } catch (Exception e) {
            return null;
        }
    }

    private Integer num(Sheet sheet, int rowIdx, String col) {
        Row row = sheet.getRow(rowIdx);
        if (row == null) {
            return null;
        }
        Cell cell = row.getCell(CellReference.convertColStringToIndex(col),
                Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) {
            return null;
        }
        try {
            CellType type = cell.getCellType();
            if (type == CellType.FORMULA) {
                type = cell.getCachedFormulaResultType();
            }
            if (type == CellType.NUMERIC) {
                return (int) Math.round(cell.getNumericCellValue());
            }
            String text = str(cell);
            if (text != null && text.matches("-?\\d+")) {
                return Integer.valueOf(text);
            }
        } catch (Exception ignored) {
            // 读取失败按空处理
        }
        return null;
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private static int nz(Integer value) {
        return value == null ? 0 : value;
    }
}
