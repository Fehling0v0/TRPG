package com.coc.card.entity.model;

/**
 * 武器（pc.weapons 数组元素）
 */
public class Weapon {

    /** 武器名称，如“袖剑” */
    private String name;

    /** 类型，如“格斗”、“射击” */
    private String type;

    /** 伤害，如“1D8+1D4+DB” */
    private String damage;

    /** 射程，如“接触” */
    private String range;

    /** 备注 */
    private String note;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDamage() {
        return damage;
    }

    public void setDamage(String damage) {
        this.damage = damage;
    }

    public String getRange() {
        return range;
    }

    public void setRange(String range) {
        this.range = range;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
