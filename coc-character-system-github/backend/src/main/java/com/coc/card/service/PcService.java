package com.coc.card.service;

import com.coc.card.dto.pc.PcListItem;
import com.coc.card.dto.pc.PcSaveRequest;
import com.coc.card.entity.Pc;
import com.coc.card.entity.User;
import com.coc.card.entity.model.SkillValue;
import com.coc.card.exception.BusinessException;
import com.coc.card.repository.PcRepository;
import com.coc.card.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 角色卡服务：列表 / 详情 / 创建 / 覆盖更新 / 删除 / 显示列配置 / Excel 导入
 */
@Service
public class PcService {

    private static final Logger log = LoggerFactory.getLogger(PcService.class);

    private final PcRepository pcRepository;
    private final UserRepository userRepository;
    private final ExcelPcParser excelPcParser;

    public PcService(PcRepository pcRepository, UserRepository userRepository, ExcelPcParser excelPcParser) {
        this.pcRepository = pcRepository;
        this.userRepository = userRepository;
        this.excelPcParser = excelPcParser;
    }

    @Transactional(readOnly = true)
    public List<PcListItem> list(Long userId) {
        return pcRepository.findByUserIdOrderBySortOrderAscIdAsc(userId).stream()
                .map(p -> new PcListItem(p.getId(), p.getPcNumber(), p.getName(), p.getGender(),
                        p.getAge(), p.getEra(), p.getOccupation(), p.getUpdatedAt()))
                .toList();
    }

    @Transactional(readOnly = true)
    public Pc get(Long userId, Long pcId) {
        return getOwned(userId, pcId);
    }

    @Transactional
    public Pc create(Long userId, PcSaveRequest request) {
        Pc pc = new Pc();
        pc.setUserId(userId);
        pc.setPcNumber(pcRepository.maxPcNumber(userId) + 1);
        pc.setSortOrder(pcRepository.maxSortOrder(userId) + 1);
        applyBasic(pc, request);
        applyJsonData(pc, request);
        recalcSkillTotals(pc);
        return pcRepository.save(pc);
    }

    @Transactional
    public Pc update(Long userId, Long pcId, PcSaveRequest request) {
        Pc pc = getOwned(userId, pcId);
        applyBasic(pc, request);
        applyJsonData(pc, request);
        recalcSkillTotals(pc);
        return pcRepository.save(pc);
    }

    @Transactional
    public void delete(Long userId, Long pcId) {
        pcRepository.delete(getOwned(userId, pcId));
    }

    /**
     * 手动排序：按前端传入的 pcId 顺序（数组下标即 sort_order）批量更新
     */
    @Transactional
    public void reorder(Long userId, List<Long> pcIds) {
        if (pcIds == null || pcIds.isEmpty()) {
            throw BusinessException.badRequest("排序列表不能为空");
        }
        if (pcIds.size() != new java.util.HashSet<>(pcIds).size()) {
            throw BusinessException.badRequest("排序列表中存在重复的角色卡");
        }
        List<Pc> pcs = new ArrayList<>(pcIds.size());
        for (Long pcId : pcIds) {
            pcs.add(getOwned(userId, pcId));
        }
        for (int i = 0; i < pcs.size(); i++) {
            pcs.get(i).setSortOrder(i);
        }
        pcRepository.saveAll(pcs);
    }

    @Transactional
    public List<String> updateDisplayFields(Long userId, List<String> fields) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(401, "用户不存在或登录状态已失效"));
        user.setDisplayFields(fields == null ? new ArrayList<>() : fields);
        return userRepository.save(user).getDisplayFields();
    }

    @Transactional
    public Long importExcel(Long userId, MultipartFile file) {
        String filename = file.getOriginalFilename();
        if (filename == null
                || (!filename.toLowerCase().endsWith(".xlsx") && !filename.toLowerCase().endsWith(".xls"))) {
            throw BusinessException.badRequest("请上传 .xlsx 或 .xls 格式的 Excel 文件");
        }
        Pc pc;
        try (InputStream in = file.getInputStream()) {
            pc = excelPcParser.parse(in);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Excel 角色卡解析失败", e);
            throw BusinessException.badRequest("Excel 解析失败：" + e.getMessage());
        }
        pc.setId(null);
        pc.setUserId(userId);
        pc.setPcNumber(pcRepository.maxPcNumber(userId) + 1);
        pc.setSortOrder(pcRepository.maxSortOrder(userId) + 1);
        if (pc.getName() == null || pc.getName().isBlank()) {
            pc.setName("导入的角色卡");
        }
        recalcSkillTotals(pc);
        return pcRepository.save(pc).getId();
    }

    /**
     * 搜索当前用户的角色卡中是否包含某个字段名。
     * 检查范围：
     * - 基础列：name, gender, age, era, occupation
     * - JSON 键名：attributes, skills, background, custom_fields 的 key
     * - 对象数组字段（weapons, items, experiences, spells）检查对象属性名
     * 返回包含该字段的角色卡 ID 列表。
     */
    @Transactional(readOnly = true)
    public List<Long> searchField(Long userId, String field) {
        if (field == null || field.isBlank()) {
            return List.of();
        }
        String f = field.trim().toLowerCase();

        // 基础列映射
        boolean isBasicCol = switch (f) {
            case "name", "gender", "age", "era", "occupation", "pc_number", "created_at", "updated_at" -> true;
            default -> false;
        };

        List<Long> result = new ArrayList<>();
        for (Pc pc : pcRepository.findByUserIdOrderByPcNumberAsc(userId)) {
            if (isBasicCol) {
                result.add(pc.getId());
                continue;
            }
            // 检查 JSON 字段键名
            if (containsKey(pc.getAttributes(), f)
                    || containsKey(pc.getSkills(), f)
                    || containsKey(pc.getBackground(), f)
                    || containsKey(pc.getCustomFields(), f)) {
                result.add(pc.getId());
                continue;
            }
            // 检查对象数组的属性名
            if (containsInList(pc.getWeapons(), f)
                    || containsInList(pc.getItems(), f)
                    || containsInList(pc.getExperiences(), f)
                    || containsInList(pc.getSpells(), f)) {
                result.add(pc.getId());
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private boolean containsKey(Map<String, ?> map, String key) {
        if (map == null) return false;
        for (String k : map.keySet()) {
            if (k != null && k.toLowerCase().equals(key)) return true;
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private boolean containsInList(List<?> list, String key) {
        if (list == null || list.isEmpty()) return false;
        for (Object item : list) {
            if (item instanceof Map) {
                Map<String, ?> m = (Map<String, ?>) item;
                for (String k : m.keySet()) {
                    if (k != null && k.toLowerCase().equals(key)) return true;
                }
            }
        }
        return false;
    }

    // ---------------- 内部方法 ----------------

    /**
     * 取出角色卡并校验归属；不属于当前用户时统一返回 404，避免泄露卡是否存在
     */
    private Pc getOwned(Long userId, Long pcId) {
        Pc pc = pcRepository.findById(pcId)
                .orElseThrow(() -> BusinessException.notFound("角色卡不存在"));
        if (!pc.getUserId().equals(userId)) {
            throw BusinessException.notFound("角色卡不存在");
        }
        return pc;
    }

    private void applyBasic(Pc pc, PcSaveRequest request) {
        if (request == null) {
            if (pc.getName() == null) {
                pc.setName("未命名角色卡");
            }
            return;
        }
        if (hasText(request.name())) {
            pc.setName(request.name().trim());
        } else if (pc.getName() == null) {
            pc.setName("未命名角色卡");
        }
        pc.setGender(blankToNull(request.gender()));
        pc.setAge(request.age());
        pc.setEra(blankToNull(request.era()));
        pc.setOccupation(blankToNull(request.occupation()));
    }

    private void applyJsonData(Pc pc, PcSaveRequest request) {
        if (request == null) {
            return;
        }
        pc.setAttributes(request.attributes() != null ? request.attributes() : new LinkedHashMap<>());
        pc.setSkills(request.skills() != null ? request.skills() : new LinkedHashMap<>());
        pc.setWeapons(request.weapons() != null ? request.weapons() : new ArrayList<>());
        pc.setItems(request.items() != null ? request.items() : new ArrayList<>());
        pc.setBackground(request.background() != null ? request.background() : new LinkedHashMap<>());
        pc.setExperiences(request.experiences() != null ? request.experiences() : new ArrayList<>());
        pc.setSpells(request.spells() != null ? request.spells() : new ArrayList<>());
        pc.setCustomFields(request.customFields() != null ? request.customFields() : new LinkedHashMap<>());
    }

    /**
     * 服务端兜底重算技能 total = initial + growth + occupation + interest
     */
    private void recalcSkillTotals(Pc pc) {
        if (pc.getSkills() == null) {
            return;
        }
        pc.getSkills().forEach((name, skill) -> {
            if (skill != null) {
                skill.setTotal(nz(skill.getInitial()) + nz(skill.getGrowth())
                        + nz(skill.getOccupation()) + nz(skill.getInterest()));
            }
        });
    }

    private static int nz(Integer value) {
        return value == null ? 0 : value;
    }

    private static boolean hasText(String s) {
        return s != null && !s.isBlank();
    }

    private static String blankToNull(String s) {
        return hasText(s) ? s.trim() : null;
    }
}
