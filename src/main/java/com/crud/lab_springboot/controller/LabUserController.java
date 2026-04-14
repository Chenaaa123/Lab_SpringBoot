package com.crud.lab_springboot.controller;

import com.crud.lab_springboot.projo.ResponseMessage;
import com.crud.lab_springboot.projo.User;
import com.crud.lab_springboot.projo.dto.UserInfoDto;
import com.crud.lab_springboot.reposity.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 用户相关接口：/lab/users
 */
@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/lab/users")
public class LabUserController {

    @Autowired
    private UserRepository userRepository;

    /**
     * 获取个人信息
     */
    @GetMapping("/profile")
    public ResponseMessage<UserInfoDto> getProfile(@RequestParam("userId") Integer userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return ResponseMessage.error("用户不存在");
        }
        return ResponseMessage.success(convertToUserInfoDto(userOpt.get()));
    }

    /**
     * 修改个人信息（昵称、头像等）
     */
    @PutMapping("/profile")
    public ResponseMessage<UserInfoDto> updateProfile(@RequestBody Map<String, Object> request) {
        Object idObj = request.get("userId");
        if (idObj == null) {
            return ResponseMessage.error("userId不能为空");
        }
        Integer userId;
        try {
            userId = Integer.valueOf(String.valueOf(idObj));
        } catch (NumberFormatException e) {
            return ResponseMessage.error("userId格式不正确");
        }

        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return ResponseMessage.error("用户不存在");
        }

        User user = userOpt.get();

        Object userNameObj = request.get("userName");
        if (userNameObj != null) {
            String userName = String.valueOf(userNameObj).trim();
            if (!userName.isEmpty()) {
                user.setUserName(userName);
            }
        }

        Object avatarObj = request.get("avatar");
        if (avatarObj != null) {
            String avatar = String.valueOf(avatarObj).trim();
            user.setAvatar(avatar.isEmpty() ? null : avatar);
        }

        User saved = userRepository.save(user);
        return ResponseMessage.success(convertToUserInfoDto(saved));
    }

    /**
     * 修改密码（需要原密码）
     */
    @PutMapping("/password")
    public ResponseMessage<Void> changePassword(@RequestBody Map<String, Object> request) {
        Object idObj = request.get("userId");
        Object oldPwdObj = request.get("oldPassword");
        Object newPwdObj = request.get("newPassword");

        if (idObj == null || oldPwdObj == null || newPwdObj == null) {
            return ResponseMessage.error("userId、oldPassword、newPassword不能为空");
        }

        Integer userId;
        try {
            userId = Integer.valueOf(String.valueOf(idObj));
        } catch (NumberFormatException e) {
            return ResponseMessage.error("userId格式不正确");
        }

        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return ResponseMessage.error("用户不存在");
        }

        User user = userOpt.get();
        String oldPassword = String.valueOf(oldPwdObj);
        String newPassword = String.valueOf(newPwdObj);

        if (!oldPassword.equals(user.getPassword())) {
            return ResponseMessage.error("原密码错误");
        }
        if (newPassword.trim().isEmpty()) {
            return ResponseMessage.error("新密码不能为空");
        }

        user.setPassword(newPassword);
        userRepository.save(user);
        return ResponseMessage.success();
    }

    /**
     * 分页查询用户列表 (Admin)
     */
    @GetMapping
    public ResponseMessage<Map<String, Object>> listUsers(
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            @RequestParam(value = "size", required = false, defaultValue = "10") int size,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "role", required = false) String role
    ) {
        int p = Math.max(page - 1, 0);
        int s = Math.max(size, 1);
        PageRequest pageable = PageRequest.of(p, s, Sort.by(Sort.Direction.ASC, "userId"));

        Page<User> pageResult;

        if (keyword != null && !keyword.trim().isEmpty()) {
            String searchKey = keyword.trim();
            if (role != null && !role.trim().isEmpty()) {
                pageResult = userRepository.findByRoleAndKeyword(role.trim(), searchKey, pageable);
            } else {
                pageResult = userRepository.findByKeyword(searchKey, pageable);
            }
        } else if (role != null && !role.trim().isEmpty()) {
            pageResult = userRepository.findByRole(role.trim(), pageable);
        } else {
            pageResult = userRepository.findAll(pageable);
        }

        List<UserInfoDto> records = pageResult.getContent().stream()
                .map(this::convertToUserInfoDto)
                .toList();

        Map<String, Object> data = new HashMap<>();
        data.put("records", records);
        data.put("total", pageResult.getTotalElements());
        data.put("current", page);
        data.put("size", s);
        data.put("pages", pageResult.getTotalPages());

        return ResponseMessage.success(data);
    }

    /**
     * 新增用户 (Admin)
     */
    @PostMapping
    public ResponseMessage<UserInfoDto> createUser(@RequestBody Map<String, Object> request) {
        Object accountObj = request.get("userAccount");
        Object nameObj = request.get("userName");
        Object roleObj = request.get("role");
        Object pwdObj = request.get("password");

        if (accountObj == null || String.valueOf(accountObj).trim().isEmpty()) {
            return ResponseMessage.error("userAccount不能为空");
        }
        if (nameObj == null || String.valueOf(nameObj).trim().isEmpty()) {
            return ResponseMessage.error("userName不能为空");
        }
        if (roleObj == null || String.valueOf(roleObj).trim().isEmpty()) {
            return ResponseMessage.error("role不能为空");
        }
        if (pwdObj == null || String.valueOf(pwdObj).trim().isEmpty()) {
            return ResponseMessage.error("password不能为空");
        }

        String userAccount = String.valueOf(accountObj).trim();
        String userName = String.valueOf(nameObj).trim();
        String role = String.valueOf(roleObj).trim();
        String password = String.valueOf(pwdObj).trim();

        if (userRepository.existsByUserAccount(userAccount)) {
            return ResponseMessage.error("用户账号已存在");
        }

        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserName(userName);
        user.setRole(role);
        user.setPassword(password);

        Object avatarObj = request.get("avatar");
        if (avatarObj != null) {
            String avatar = String.valueOf(avatarObj).trim();
            user.setAvatar(avatar.isEmpty() ? null : avatar);
        }

        User saved = userRepository.save(user);
        return ResponseMessage.success(convertToUserInfoDto(saved));
    }

    /**
     * 编辑用户 (Admin)
     */
    @PutMapping("/{userId}")
    public ResponseMessage<UserInfoDto> updateUser(
            @PathVariable("userId") Integer userId,
            @RequestBody Map<String, Object> request
    ) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return ResponseMessage.error("用户不存在");
        }

        User user = userOpt.get();

        Object accountObj = request.get("userAccount");
        if (accountObj != null) {
            String newAccount = String.valueOf(accountObj).trim();
            if (!newAccount.isEmpty() && !newAccount.equals(user.getUserAccount())) {
                if (userRepository.existsByUserAccount(newAccount)) {
                    return ResponseMessage.error("用户账号已被使用");
                }
                user.setUserAccount(newAccount);
            }
        }

        Object nameObj = request.get("userName");
        if (nameObj != null) {
            String newName = String.valueOf(nameObj).trim();
            if (!newName.isEmpty()) {
                user.setUserName(newName);
            }
        }

        Object roleObj = request.get("role");
        if (roleObj != null) {
            String newRole = String.valueOf(roleObj).trim();
            if (!newRole.isEmpty()) {
                user.setRole(newRole);
            }
        }

        Object avatarObj = request.get("avatar");
        if (avatarObj != null) {
            String avatar = String.valueOf(avatarObj).trim();
            user.setAvatar(avatar.isEmpty() ? null : avatar);
        }

        User saved = userRepository.save(user);
        return ResponseMessage.success(convertToUserInfoDto(saved));
    }

    /**
     * 删除用户 (Admin)
     */
    @DeleteMapping("/{userId}")
    public ResponseMessage<Void> deleteUser(@PathVariable("userId") Integer userId) {
        if (!userRepository.existsById(userId)) {
            return ResponseMessage.error("用户不存在");
        }
        userRepository.deleteById(userId);
        return ResponseMessage.success();
    }

    /**
     * 管理员修改用户密码 (Admin)
     */
    @PutMapping("/{userId}/password")
    public ResponseMessage<Void> updateUserPassword(
            @PathVariable("userId") Integer userId,
            @RequestBody Map<String, Object> request
    ) {
        Object pwdObj = request.get("password");
        if (pwdObj == null || String.valueOf(pwdObj).trim().isEmpty()) {
            return ResponseMessage.error("password不能为空");
        }

        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return ResponseMessage.error("用户不存在");
        }

        User user = userOpt.get();
        user.setPassword(String.valueOf(pwdObj).trim());
        userRepository.save(user);
        return ResponseMessage.success();
    }

    private UserInfoDto convertToUserInfoDto(User user) {
        UserInfoDto dto = new UserInfoDto();
        dto.setUserId(user.getUserId());
        dto.setUserAccount(user.getUserAccount());
        dto.setUserName(user.getUserName());
        dto.setRole(user.getRole());
        dto.setAvatar(user.getAvatar());
        return dto;
    }
}
