package com.coc.card.controller;

import com.coc.card.common.ApiResponse;
import com.coc.card.dto.pc.DisplayFieldsRequest;
import com.coc.card.dto.pc.PcListItem;
import com.coc.card.dto.pc.PcSaveRequest;
import com.coc.card.dto.pc.ReorderRequest;
import com.coc.card.entity.Pc;
import com.coc.card.security.SecurityUtil;
import com.coc.card.service.PcService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 角色卡接口（需登录，只能操作自己的角色卡）
 */
@RestController
@RequestMapping("/api/pc")
public class PcController {

    private final PcService pcService;

    public PcController(PcService pcService) {
        this.pcService = pcService;
    }

    /** 角色卡列表（只返回摘要字段） */
    @GetMapping
    public ApiResponse<List<PcListItem>> list() {
        return ApiResponse.ok(pcService.list(SecurityUtil.currentUserId()));
    }

    /** 角色卡完整数据 */
    @GetMapping("/{pcId}")
    public ApiResponse<Pc> get(@PathVariable Long pcId) {
        return ApiResponse.ok(pcService.get(SecurityUtil.currentUserId(), pcId));
    }

    /** 创建空白角色卡（可附带 name 等初始基础字段） */
    @PostMapping
    public ApiResponse<Pc> create(@RequestBody(required = false) PcSaveRequest request) {
        return ApiResponse.ok(pcService.create(SecurityUtil.currentUserId(), request));
    }

    /** 覆盖更新角色卡（前端传入完整 JSON） */
    @PutMapping("/{pcId}")
    public ApiResponse<Pc> update(@PathVariable Long pcId,
                                  @RequestBody(required = false) PcSaveRequest request) {
        return ApiResponse.ok(pcService.update(SecurityUtil.currentUserId(), pcId, request));
    }

    @DeleteMapping("/{pcId}")
    public ApiResponse<Void> delete(@PathVariable Long pcId) {
        pcService.delete(SecurityUtil.currentUserId(), pcId);
        return ApiResponse.ok();
    }

    /** 更新当前用户的角色卡列表自定义显示列 */
    @PutMapping("/display-fields")
    public ApiResponse<List<String>> updateDisplayFields(@Valid @RequestBody DisplayFieldsRequest request) {
        return ApiResponse.ok(pcService.updateDisplayFields(SecurityUtil.currentUserId(), request.displayFields()));
    }

    /** 手动排序：按传入的 pcId 顺序更新角色卡排序 */
    @PutMapping("/reorder")
    public ApiResponse<Void> reorder(@Valid @RequestBody ReorderRequest request) {
        pcService.reorder(SecurityUtil.currentUserId(), request.pcIds());
        return ApiResponse.ok();
    }

    /** 上传 Excel 角色卡，解析后自动创建，返回新卡 ID */
    @PostMapping("/upload")
    public ApiResponse<Map<String, Long>> upload(@RequestParam("file") MultipartFile file) {
        Long pcId = pcService.importExcel(SecurityUtil.currentUserId(), file);
        return ApiResponse.ok(Map.of("pc_id", pcId));
    }

    /**
     * 搜索当前用户的角色卡中是否包含某个字段。
     * 检查范围：基础列 + 所有 JSON 字段内的键名（attributes / skills / weapons / items /
     * background / experiences / spells / custom_fields）。
     * 返回包含该字段的角色卡 ID 列表。
     */
    @GetMapping("/search-field")
    public ApiResponse<List<Long>> searchField(@RequestParam String field) {
        return ApiResponse.ok(pcService.searchField(SecurityUtil.currentUserId(), field));
    }
}
