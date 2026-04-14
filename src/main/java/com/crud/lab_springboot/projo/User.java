package com.crud.lab_springboot.projo;


import jakarta.persistence.*;

//类映射表
@Table(name = "tb_user")
@Entity
public class User {
    @Id     // id
    // 自增生成
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")       // id
    private Integer userId;
    @Column(name = "user_account")
    private String userAccount;
    @Column(name = "password")
    private String password;
    @Column(name = "role")
    private String role;
    @Column(name = "user_name")  // 用户名
    private String userName;

    /**
     * 头像：使用 LONGTEXT 存储，避免 Base64 或长 URL 过长导致数据库截断
     */
    @Lob
    @Column(name = "avatar", columnDefinition = "LONGTEXT")
    private String avatar;

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getUserAccount() {
        return userAccount;
    }

    public void setUserAccount(String userAccount) {
        this.userAccount = userAccount;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", userAccount='" + userAccount + '\'' +
                ", password='" + password + '\'' +
                ", role='" + role + '\'' +
                ", userName='" + userName + '\'' +
                ", avatar='" + avatar + '\'' +
                '}';
    }
}
