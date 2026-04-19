package com.crud.lab_springboot.controller;

import com.crud.lab_springboot.projo.ResponseMessage;
import com.crud.lab_springboot.projo.User;
import com.crud.lab_springboot.projo.dto.LoginDto;
import com.crud.lab_springboot.projo.dto.RegisterDto;
import com.crud.lab_springboot.projo.dto.UserInfoDto;
import com.crud.lab_springboot.reposity.UserRepository;
import com.crud.lab_springboot.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Optional;

/**
 * 认证接口：/lab/auth
 */
@RestController
@RequestMapping("/lab/auth")
public class AuthController {

    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;

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

    /**
     * 忘记密码页重置（不需要验证码）：原密码 + 新密码 + 确认新密码
     * PUT /lab/auth/password/reset-by-old-password
     */
    @PutMapping("/password/reset-by-old-password")
    public ResponseMessage<Void> resetPasswordByOldPassword(@RequestBody Map<String, Object> request) {
        if (request == null) {
            return ResponseMessage.error("请求体不能为空");
        }
        Object accountObj = request.get("userAccount");
        Object oldPwdObj = request.get("oldPassword");
        Object newPwdObj = request.get("newPassword");
        Object confirmPwdObj = request.get("confirmNewPassword");

        if (accountObj == null || String.valueOf(accountObj).trim().isEmpty()) {
            return ResponseMessage.error("userAccount不能为空");
        }
        if (oldPwdObj == null || String.valueOf(oldPwdObj).trim().isEmpty()) {
            return ResponseMessage.error("oldPassword不能为空");
        }
        if (newPwdObj == null || String.valueOf(newPwdObj).trim().isEmpty()) {
            return ResponseMessage.error("newPassword不能为空");
        }
        if (confirmPwdObj == null || String.valueOf(confirmPwdObj).trim().isEmpty()) {
            return ResponseMessage.error("confirmNewPassword不能为空");
        }

        String userAccount = String.valueOf(accountObj).trim();
        String oldPassword = String.valueOf(oldPwdObj).trim();
        String newPassword = String.valueOf(newPwdObj).trim();
        String confirmNewPassword = String.valueOf(confirmPwdObj).trim();

        if (!newPassword.equals(confirmNewPassword)) {
            return ResponseMessage.error("新密码与确认新密码不一致");
        }
        if (newPassword.length() < 6) {
            return ResponseMessage.error("新密码长度至少6位");
        }
        if (newPassword.equals(oldPassword)) {
            return ResponseMessage.error("新密码不能与原密码相同");
        }

        Optional<User> userOpt = userRepository.findByUserAccount(userAccount);
        if (userOpt.isEmpty()) {
            return ResponseMessage.error("账号不存在");
        }
        User user = userOpt.get();
        if (!oldPassword.equals(user.getPassword())) {
            return ResponseMessage.error("原密码错误");
        }
        user.setPassword(newPassword);
        userRepository.save(user);
        return ResponseMessage.success();
    }
}


