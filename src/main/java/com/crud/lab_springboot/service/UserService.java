package com.crud.lab_springboot.service;

import com.crud.lab_springboot.projo.dto.LoginDto;
import com.crud.lab_springboot.projo.dto.RegisterDto;
import com.crud.lab_springboot.projo.dto.UserInfoDto;

public interface UserService {
    /**
     * 用户注册
     * @param registerDto 注册信息
     * @return 用户信息
     */
    UserInfoDto register(RegisterDto registerDto);

    /**
     * 用户登录
     * @param loginDto 登录信息
     * @return 用户信息
     */
    UserInfoDto login(LoginDto loginDto);
}

