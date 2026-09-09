package com.coc.card.entity.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 模组经历（pc.experiences 数组元素）
 * <p>
 * 存储 JSON 键名为 snake_case（module_name / change_description），
 * 使用 {@link JsonProperty} 显式绑定，不依赖全局命名策略。
 */
public class Experience {

    /** 模组名称，如"毒汤" */
    @JsonProperty("module_name")
    private String moduleName;

    /** 玩家位置，如"HO1" */
    private String ho;

    /** 变更描述，如"SAN-6,HP-2,侦查+2" */
    @JsonProperty("change_description")
    private String changeDescription;

    /** 开团时间，格式 yyyy-MM-dd */
    @JsonProperty("start_date")
    private String startDate;

    /** 结团时间，格式 yyyy-MM-dd */
    @JsonProperty("end_date")
    private String endDate;

    /** 状态：卫星中/进行中/已结团/已散桌/暂停中 */
    private String status;

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public String getHo() {
        return ho;
    }

    public void setHo(String ho) {
        this.ho = ho;
    }

    public String getChangeDescription() {
        return changeDescription;
    }

    public void setChangeDescription(String changeDescription) {
        this.changeDescription = changeDescription;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
