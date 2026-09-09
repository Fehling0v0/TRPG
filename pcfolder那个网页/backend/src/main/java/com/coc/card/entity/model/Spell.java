package com.coc.card.entity.model;

/**
 * 法术（pc.spells 数组元素）
 */
public class Spell {

    /** 法术名称，如“灰色束缚” */
    private String name;

    /** 消耗，如“8mp 1d6san 1h” */
    private String cost;

    /** 效果描述 */
    private String effect;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCost() {
        return cost;
    }

    public void setCost(String cost) {
        this.cost = cost;
    }

    public String getEffect() {
        return effect;
    }

    public void setEffect(String effect) {
        this.effect = effect;
    }
}
