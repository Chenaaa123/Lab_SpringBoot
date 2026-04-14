package com.crud.lab_springboot.projo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

/**
 * 实验室表 lab
 */
@Entity
@Table(name = "lab")
public class Lab {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * 实验室编号，对应表字段 lab_code
     */
    @Column(name = "lab_code", nullable = false, length = 50)
    private String code;

    /**
     * 实验室名称，可为空
     */
    @Column(name = "name", nullable = true, length = 100)
    private String name;

    /**
     * 所属分类，多对一
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", referencedColumnName = "id", nullable = false)
    private LabCategory category;

    /**
     * 实验室管理员（标准列 manager_id，与 lab_category 一致）。
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id", referencedColumnName = "user_id", nullable = false)
    @NotFound(action = NotFoundAction.IGNORE)
    @JsonProperty("manager_id")
    private User manager;

    /**
     * 与 {@link #manager} 同值，仅写入误拼列 manage_id；兼容历史上两列并存且均为 NOT NULL 的库。
     * 库中已删除 manage_id 后，可删掉本字段及 {@link #syncManageIdShadow()}。
     */
    @JsonIgnore
    @Column(name = "manage_id", nullable = false)
    private Integer manageIdShadow;

    @Column(name = "location", length = 100)
    private String location;

    /**
     * 实验室开放时间，如 08:00:00
     */
    @Column(name = "open_time", nullable = false)
    private java.time.LocalTime openTime;

    /**
     * 实验室关闭时间，如 22:00:00
     */
    @Column(name = "close_time", nullable = false)
    private java.time.LocalTime closeTime;

    /**
     * 主要设备清单 (JSON或逗号分隔)
     */
    @Column(name = "equipment", length = 500)
    private String equipment;

    /**
     * 状态：0-停用，1-正常，2-维护中
     */
    @Column(name = "status", nullable = false)
    private Integer status;
    @Column(name = "description", length = 255)
    private String description;

    /**
     * 实验室图片URL
     */
    @Column(name = "image_url", length = 255)
    private String imageUrl;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public LabCategory getCategory() {
        return category;
    }

    public void setCategory(LabCategory category) {
        this.category = category;
    }

    public User getManager() {
        return manager;
    }

    public void setManager(User manager) {
        this.manager = manager;
        syncManageIdShadow();
    }

    @PrePersist
    @PreUpdate
    private void syncManageIdShadow() {
        if (manager != null) {
            manageIdShadow = manager.getUserId();
        }
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public java.time.LocalTime getOpenTime() {
        return openTime;
    }

    public void setOpenTime(java.time.LocalTime openTime) {
        this.openTime = openTime;
    }

    public java.time.LocalTime getCloseTime() {
        return closeTime;
    }

    public void setCloseTime(java.time.LocalTime closeTime) {
        this.closeTime = closeTime;
    }

    public String getEquipment() {
        return equipment;
    }

    public void setEquipment(String equipment) {
        this.equipment = equipment;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}


