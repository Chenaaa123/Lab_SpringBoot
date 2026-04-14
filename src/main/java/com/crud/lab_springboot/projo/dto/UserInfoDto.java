package com.crud.lab_springboot.projo.dto;

/**
 * 用户信息DTO（用于返回，不包含密码）
 */
public class UserInfoDto {
    private Integer userId;
    private String userAccount;
    private String role;
    private String userName;
    private String avatar;

    public UserInfoDto() {
    }

    public UserInfoDto(Integer userId, String userAccount, String role, String userName, String avatar) {
        this.userId = userId;
        this.userAccount = userAccount;
        this.role = role;
        this.userName = userName;
        this.avatar = avatar;
    }

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
}

