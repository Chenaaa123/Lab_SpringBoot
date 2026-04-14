package com.crud.lab_springboot.projo;

/**
 * 用户角色枚举
 */
public enum Role {
    SYSTEM_ADMIN("系统管理员"),
    STUDENT("学生"),
    TEACHER("老师"),
    LAB_ADMIN("实验室管理员");

    private final String description;

    Role(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 根据描述获取角色
     */
    public static Role fromDescription(String description) {
        for (Role role : Role.values()) {
            if (role.description.equals(description)) {
                return role;
            }
        }
        throw new IllegalArgumentException("未知的角色: " + description);
    }
}

