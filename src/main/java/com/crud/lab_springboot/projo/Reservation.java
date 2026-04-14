package com.crud.lab_springboot.projo;

import jakarta.persistence.*;

/**
 * 预约记录表 reservation
 * 预约状态（审核流转）: 0-待审核, 1-已通过, 2-已拒绝, 4-已取消
 * 使用状态（使用流转）: 0-待使用, 1-使用中, 2-已结束, 4-已取消
 */
@Entity
@Table(name = "reservation")
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * 预约单号，唯一
     */
    @Column(name = "order_no", nullable = false, length = 32)
    private String orderNo;

    /**
     * 学生用户，多对一
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    private User user;

    /**
     * 实验室，多对一
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lab_id", referencedColumnName = "id")
    private Lab lab;

    /**
     * 预约开始时间
     */
    @Column(name = "start_time", nullable = false)
    private java.time.LocalDateTime startTime;

    /**
     * 预约结束时间
     */
    @Column(name = "end_time", nullable = false)
    private java.time.LocalDateTime endTime;

    /**
     * 预约用途
     */
    @Column(name = "purpose", length = 255)
    private String purpose;

    /**
     * 预约状态（审核流转）: 0-待审核, 1-已通过, 2-已拒绝, 4-已取消
     */
    @Column(name = "status", nullable = false)
    private Integer status;

    /**
     * 使用状态（使用流转）: 0-待使用, 1-使用中, 2-已结束, 4-已取消
     */
    @Column(name = "use_status", nullable = false)
    private Integer useStatus;

    /**
     * 拒绝原因
     */
    @Column(name = "reject_reason", length = 255)
    private String rejectReason;

    /**
     * 审核人（管理员）
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "audit_user_id", referencedColumnName = "user_id")
    private User auditUser;

    /**
     * 审核时间
     */
    @Column(name = "audit_time")
    private java.time.LocalDateTime auditTime;

    @Column(name = "created_time")
    private java.time.LocalDateTime createdTime;

    @Column(name = "updated_time")
    private java.time.LocalDateTime updatedTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Lab getLab() {
        return lab;
    }

    public void setLab(Lab lab) {
        this.lab = lab;
    }

    public java.time.LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(java.time.LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public java.time.LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(java.time.LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getUseStatus() {
        return useStatus;
    }

    public void setUseStatus(Integer useStatus) {
        this.useStatus = useStatus;
    }

    public String getRejectReason() {
        return rejectReason;
    }

    public void setRejectReason(String rejectReason) {
        this.rejectReason = rejectReason;
    }

    public User getAuditUser() {
        return auditUser;
    }

    public void setAuditUser(User auditUser) {
        this.auditUser = auditUser;
    }

    public java.time.LocalDateTime getAuditTime() {
        return auditTime;
    }

    public void setAuditTime(java.time.LocalDateTime auditTime) {
        this.auditTime = auditTime;
    }

    public java.time.LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(java.time.LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }

    public java.time.LocalDateTime getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(java.time.LocalDateTime updatedTime) {
        this.updatedTime = updatedTime;
    }
}


