package com.coc.card.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户表
 */
@Entity
@Table(name = "user")
public class User {

    /**
     * 角色枚举：管理员 / 普通用户
     */
    public enum Role {
        ADMIN,
        USER
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 登录账号（唯一） */
    @Column(nullable = false, unique = true, length = 50)
    private String username;

    /** BCrypt 加密后的密码 */
    @Column(nullable = false, length = 100)
    private String password;

    /** 角色：ADMIN / USER */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role = Role.USER;

    /**
     * 角色卡列表自定义显示列
     * 存储形式：["name","gender","age","occupation"]
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "display_fields", columnDefinition = "json")
    private List<String> displayFields = new ArrayList<>(List.of("name", "gender", "age", "occupation"));

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public List<String> getDisplayFields() {
        return displayFields;
    }

    public void setDisplayFields(List<String> displayFields) {
        this.displayFields = displayFields;
    }
}
