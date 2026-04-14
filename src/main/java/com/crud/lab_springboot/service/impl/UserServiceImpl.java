package com.crud.lab_springboot.service.impl;

import com.crud.lab_springboot.projo.Role;
import com.crud.lab_springboot.projo.User;
import com.crud.lab_springboot.projo.dto.LoginDto;
import com.crud.lab_springboot.projo.dto.RegisterDto;
import com.crud.lab_springboot.projo.dto.UserInfoDto;
import com.crud.lab_springboot.reposity.UserRepository;
import com.crud.lab_springboot.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserInfoDto register(RegisterDto registerDto) {
        // 验证账号是否已存在
        if (userRepository.existsByUserAccount(registerDto.getUserAccount())) {
            throw new RuntimeException("账号已存在");
        }

        // 验证角色是否有效
        try {
            Role.fromDescription(registerDto.getRole());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("无效的角色: " + registerDto.getRole());
        }

        // 创建新用户
        User user = new User();
        user.setUserAccount(registerDto.getUserAccount());
        // 简单保存明文密码（教学/实验环境，生产环境请务必加密）
        user.setPassword(registerDto.getPassword());
        user.setRole(registerDto.getRole());
        user.setUserName(registerDto.getUserAccount()); // 默认用户名为账号

        // 保存用户
        User savedUser = userRepository.save(user);

        // 返回用户信息（不包含密码）
        return convertToUserInfoDto(savedUser);
    }

    @Override
    public UserInfoDto login(LoginDto loginDto) {
        // 查找用户
        Optional<User> userOptional = userRepository.findByUserAccount(loginDto.getUserAccount());
        
        if (userOptional.isEmpty()) {
            throw new RuntimeException("账号或密码错误");
        }

        User user = userOptional.get();

        // 校验角色是否有效
        try {
            Role.fromDescription(loginDto.getRole());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("无效的角色: " + loginDto.getRole());
        }

        // 验证密码（明文比对）
        if (!loginDto.getPassword().equals(user.getPassword())) {
            throw new RuntimeException("账号或密码错误");
        }

        // 验证角色是否匹配
        if (loginDto.getRole() == null || !loginDto.getRole().equals(user.getRole())) {
            throw new RuntimeException("账号或角色错误");
        }

        // 返回用户信息（不包含密码）
        return convertToUserInfoDto(user);
    }

    /**
     * 将User实体转换为UserInfoDto
     */
    private UserInfoDto convertToUserInfoDto(User user) {
        UserInfoDto dto = new UserInfoDto();
        dto.setUserId(user.getUserId());
        dto.setUserAccount(user.getUserAccount());
        dto.setRole(user.getRole());
        dto.setUserName(user.getUserName());
        dto.setAvatar(user.getAvatar());
        return dto;
    }
}

