package com.coc.card.entity.model;

/**
 * 物品 / 装备（pc.items 数组元素）
 */
public class Item {

    /** 物品名称，如“储物戒” */
    private String name;

    /** 位置，如“左手”、“背包” */
    private String location;

    /** 状态，如“显露”、“隐藏” */
    private String status;

    /** 备注，如“疗伤用” */
    private String note;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
