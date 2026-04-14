package com.crud.lab_springboot.projo.dto;

/**
 * 登录请求DTO
 */
public class LoginDto {
    private String userAccount;  // 账号
    private String password;     // 密码
    private String role;         // 角色：系统管理员、学生、老师、实验室管理员

    public LoginDto() {
    }

    public LoginDto(String userAccount, String password, String role) {
        this.userAccount = userAccount;
        this.password = password;
        this.role = role;
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
}

