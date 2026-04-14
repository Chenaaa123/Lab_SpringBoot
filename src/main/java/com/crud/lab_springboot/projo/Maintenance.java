package com.crud.lab_springboot.projo;

import jakarta.persistence.*;

/**
 * 检修记录表 maintenance
 */
@Entity
@Table(name = "maintenance")
public class Maintenance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * 实验室，多对一
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lab_id", referencedColumnName = "id", nullable = false)
    private Lab lab;

    /**
     * 关联的报修记录，多对一
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repair_id", referencedColumnName = "id")
    private Repair repair;

    /**
     * 检修内容
     */
    @Lob
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    /**
     * 检修单位
     */
    @Column(name = "maintenance_unit", length = 100)
    private String maintenanceUnit;

    /**
     * 检修时间
     */
    @Column(name = "maintenance_time", nullable = false)
    private java.time.LocalDateTime maintenanceTime;

    @Column(name = "created_time", nullable = false)
    private java.time.LocalDateTime createdTime;

    @Column(name = "status", nullable = false)
    private Integer status;

    /**
     * 检修人
     */
    @Column(name = "handler", length = 50)
    private String handler;

    /**
     * 联系电话
     */
    @Column(name = "handler_phone", length = 20)
    private String handlerPhone;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Lab getLab() {
        return lab;
    }

    public void setLab(Lab lab) {
        this.lab = lab;
    }

    public Repair getRepair() {
        return repair;
    }

    public void setRepair(Repair repair) {
        this.repair = repair;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    
    public String getMaintenanceUnit() {
        return maintenanceUnit;
    }

    public void setMaintenanceUnit(String maintenanceUnit) {
        this.maintenanceUnit = maintenanceUnit;
    }

    public java.time.LocalDateTime getMaintenanceTime() {
        return maintenanceTime;
    }

    public void setMaintenanceTime(java.time.LocalDateTime maintenanceTime) {
        this.maintenanceTime = maintenanceTime;
    }

    public java.time.LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(java.time.LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getHandler() {
        return handler;
    }

    public void setHandler(String handler) {
        this.handler = handler;
    }

    public String getHandlerPhone() {
        return handlerPhone;
    }

    public void setHandlerPhone(String handlerPhone) {
        this.handlerPhone = handlerPhone;
    }
}


