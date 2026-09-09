package com.coc.card.entity;

import com.coc.card.entity.model.Experience;
import com.coc.card.entity.model.Item;
import com.coc.card.entity.model.SkillValue;
import com.coc.card.entity.model.Spell;
import com.coc.card.entity.model.Weapon;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 角色卡主表
 * <p>
 * 核心数据（属性 / 技能 / 武器 / 物品 / 背景 / 经历 / 法术 / 自定义字段）
 * 全部以 MySQL JSON 列存储，Java 侧通过 Hibernate 6 的 {@link JdbcTypeCode}
 * 直接映射为 Map / List / POJO，前端可以自由增删技能名和自定义字段。
 */
@Entity
@Table(name = "pc")
public class Pc {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 所属用户 ID（user.id） */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** 该用户下的角色卡编号，从 1 开始自动递增（业务层分配，库内有 (user_id, pc_number) 唯一约束） */
    @Column(name = "pc_number", nullable = false)
    private Integer pcNumber;

    /** 手动排序序号（越小越靠前，新卡追加到末尾；同序号时按 id 升序） */
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    /** 角色姓名 */
    @Column(nullable = false, length = 50)
    private String name;

    /** 性别 */
    @Column(length = 10)
    private String gender;

    /** 年龄 */
    private Integer age;

    /** 时代，如“现代”、“1920s” */
    @Column(length = 50)
    private String era;

    /** 职业 */
    @Column(length = 100)
    private String occupation;

    /** 创建时间 */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** 更新时间 */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 属性，键为属性缩写：
     * {"str":90,"con":65,"siz":65,"dex":65,"app":40,"int":90,"pow":85,"edu":50,"luck":75,"hp":14,"san":65,"db":"+1D4"}
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private Map<String, Object> attributes = new LinkedHashMap<>();

    /**
     * 技能表，键为技能名（支持前端任意添加自定义技能名）：
     * {"会计":{"initial":5,"growth":0,"occupation":20,"interest":10,"total":35}}
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private Map<String, SkillValue> skills = new LinkedHashMap<>();

    /**
     * 武器列表：
     * [{"name":"袖剑","type":"格斗","damage":"1D8+1D4+DB","range":"接触","note":""}]
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private List<Weapon> weapons = new ArrayList<>();

    /**
     * 物品列表：
     * [{"name":"储物戒","location":"左手","status":"显露","note":""}]
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private List<Item> items = new ArrayList<>();

    /**
     * 背景。两种形态均可：
     * 结构化：{"personal_description":"...","ideology":"...","important_people":"...",...}
     * 纯文本：{"text":"完整的背景故事文字"}
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private Map<String, Object> background = new LinkedHashMap<>();

    /**
     * 模组经历：
     * [{"module_name":"毒汤","ho":"HO1","change_description":"SAN-6,HP-2,侦查+2"}]
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private List<Experience> experiences = new ArrayList<>();

    /**
     * 法术列表：
     * [{"name":"灰色束缚","cost":"8mp 1d6san 1h","effect":"可以控制死去的人，直到腐烂为止"}]
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private List<Spell> spells = new ArrayList<>();

    /**
     * 用户完全自定义的键值对，字段名和值均由前端决定：
     * {"birthday":"1995-03-21","height":"175cm","weight":"70kg","hobby":"收集古董"}
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "custom_fields", columnDefinition = "json")
    private Map<String, Object> customFields = new LinkedHashMap<>();

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Integer getPcNumber() {
        return pcNumber;
    }

    public void setPcNumber(Integer pcNumber) {
        this.pcNumber = pcNumber;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getEra() {
        return era;
    }

    public void setEra(String era) {
        this.era = era;
    }

    public String getOccupation() {
        return occupation;
    }

    public void setOccupation(String occupation) {
        this.occupation = occupation;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    public Map<String, SkillValue> getSkills() {
        return skills;
    }

    public void setSkills(Map<String, SkillValue> skills) {
        this.skills = skills;
    }

    public List<Weapon> getWeapons() {
        return weapons;
    }

    public void setWeapons(List<Weapon> weapons) {
        this.weapons = weapons;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public Map<String, Object> getBackground() {
        return background;
    }

    public void setBackground(Map<String, Object> background) {
        this.background = background;
    }

    public List<Experience> getExperiences() {
        return experiences;
    }

    public void setExperiences(List<Experience> experiences) {
        this.experiences = experiences;
    }

    public List<Spell> getSpells() {
        return spells;
    }

    public void setSpells(List<Spell> spells) {
        this.spells = spells;
    }

    public Map<String, Object> getCustomFields() {
        return customFields;
    }

    public void setCustomFields(Map<String, Object> customFields) {
        this.customFields = customFields;
    }
}
