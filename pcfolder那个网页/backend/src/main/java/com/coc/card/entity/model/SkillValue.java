package com.coc.card.entity.model;

/**
 * 技能数值（pc.skills 中每个技能名对应的值）
 * <p>
 * total = initial + growth + occupation + interest（业务层计算）
 */
public class SkillValue {

    /** 初始值 */
    private Integer initial = 0;

    /** 成长值 */
    private Integer growth = 0;

    /** 职业点数 */
    private Integer occupation = 0;

    /** 兴趣点数 */
    private Integer interest = 0;

    /** 合计（四项之和） */
    private Integer total = 0;

    public Integer getInitial() {
        return initial;
    }

    public void setInitial(Integer initial) {
        this.initial = initial;
    }

    public Integer getGrowth() {
        return growth;
    }

    public void setGrowth(Integer growth) {
        this.growth = growth;
    }

    public Integer getOccupation() {
        return occupation;
    }

    public void setOccupation(Integer occupation) {
        this.occupation = occupation;
    }

    public Integer getInterest() {
        return interest;
    }

    public void setInterest(Integer interest) {
        this.interest = interest;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }
}
