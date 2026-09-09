package com.coc.card.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * 邀请码表（注册制：仅管理员可生成邀请码）
 */
@Entity
@Table(name = "invite_code")
public class InviteCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 邀请码字符串（唯一） */
    @Column(nullable = false, unique = true, length = 32)
    private String code;

    /** 生成该邀请码的管理员 ID（user.id） */
    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    /** 是否已被使用 */
    @Column(nullable = false)
    private Boolean used = false;

    /** 使用该邀请码注册的用户 ID（user.id），未使用时为 null */
    @Column(name = "used_by")
    private Long usedBy;

    /** 生成时间 */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.used == null) {
            this.used = false;
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public Boolean getUsed() {
        return used;
    }

    public void setUsed(Boolean used) {
        this.used = used;
    }

    public Long getUsedBy() {
        return usedBy;
    }

    public void setUsedBy(Long usedBy) {
        this.usedBy = usedBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
