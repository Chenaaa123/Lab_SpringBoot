package com.crud.lab_springboot.controller;

import com.crud.lab_springboot.projo.ResponseMessage;
import com.crud.lab_springboot.projo.dto.LoginDto;
import com.crud.lab_springboot.projo.dto.RegisterDto;
import com.crud.lab_springboot.projo.dto.UserInfoDto;
import com.crud.lab_springboot.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证接口：/lab/auth
 */
@RestController
@RequestMapping("/lab/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    /**
     * 登录
     */
    @PostMapping("/login")
    public ResponseMessage<UserInfoDto> login(@RequestBody LoginDto loginDto) {
        try {
            if (loginDto.getUserAccount() == null || loginDto.getUserAccount().trim().isEmpty()) {
                return ResponseMessage.error("账号不能为空");
            }
            if (loginDto.getPassword() == null || loginDto.getPassword().trim().isEmpty()) {
                return ResponseMessage.error("密码不能为空");
            }
            if (loginDto.getRole() == null || loginDto.getRole().trim().isEmpty()) {
                return ResponseMessage.error("角色不能为空");
            }

            UserInfoDto userInfoDto = userService.login(loginDto);
            return ResponseMessage.success(userInfoDto);
        } catch (RuntimeException e) {
            return ResponseMessage.error(e.getMessage());
        } catch (Exception e) {
            return ResponseMessage.error("登录失败: " + e.getMessage());
        }
    }

    /**
     * 学生注册
     */
    @PostMapping("/register")
    public ResponseMessage<UserInfoDto> register(@RequestBody RegisterDto registerDto) {
        try {
            if (registerDto.getUserAccount() == null || registerDto.getUserAccount().trim().isEmpty()) {
                return ResponseMessage.error("账号不能为空");
            }
            if (registerDto.getPassword() == null || registerDto.getPassword().trim().isEmpty()) {
                return ResponseMessage.error("密码不能为空");
            }
            if (registerDto.getRole() == null || registerDto.getRole().trim().isEmpty()) {
                return ResponseMessage.error("角色不能为空");
            }

            UserInfoDto userInfoDto = userService.register(registerDto);
            return ResponseMessage.success(userInfoDto);
        } catch (RuntimeException e) {
            return ResponseMessage.error(e.getMessage());
        } catch (Exception e) {
            return ResponseMessage.error("注册失败: " + e.getMessage());
        }
    }

    /**
     * 退出登录（当前简单返回成功，具体清理登录状态由前端或后续安全方案处理）
     */
    @PostMapping("/logout")
    public ResponseMessage<Void> logout() {
        return ResponseMessage.success();
    }
}


