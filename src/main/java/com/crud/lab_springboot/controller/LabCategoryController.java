package com.crud.lab_springboot.controller;

import com.crud.lab_springboot.projo.LabCategory;
import com.crud.lab_springboot.projo.ResponseMessage;
import com.crud.lab_springboot.projo.User;
import com.crud.lab_springboot.reposity.LabCategoryRepository;
import com.crud.lab_springboot.reposity.LabRepository;
import com.crud.lab_springboot.reposity.UserRepository;
import com.crud.lab_springboot.service.LabCategoryManagerCascadeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * 实验室分类接口：/lab/lab-categories
 */
@RestController
@RequestMapping("/lab/lab-categories")
public class LabCategoryController {

    @Autowired
    private LabCategoryRepository labCategoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LabRepository labRepository;

    @Autowired
    private LabCategoryManagerCascadeService labCategoryManagerCascadeService;

    /**
     * 查询分类列表 (所有用户)
     */
    @GetMapping
    public ResponseMessage<List<LabCategory>> listCategories() {
        List<LabCategory> list = labCategoryRepository.findAll();
        return ResponseMessage.success(list);
    }

    /**
     * 查询分类详情 (所有用户)
     */
    @GetMapping("/{id}")
    public ResponseMessage<LabCategory> getCategory(@PathVariable("id") Long id) {
        Optional<LabCategory> opt = labCategoryRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseMessage.error("分类不存在");
        }
        return ResponseMessage.success(opt.get());
    }

    /**
     * 新增分类 (Admin)
     * 请求体:
     * {
     *   "name": "计算机类",
     *   "description": "描述",
     *   "managerUserId": 2
     * 也可传 "managerId"（同时传时优先 managerUserId）
     * }
     */
    @PostMapping
    public ResponseMessage<LabCategory> createCategory(@RequestBody Map<String, Object> body) {
        String name = body.get("name") == null ? null : String.valueOf(body.get("name")).trim();
        String description = body.get("description") == null ? null : String.valueOf(body.get("description")).trim();
        if (name == null || name.isEmpty()) {
            return ResponseMessage.error("分类名称不能为空");
        }

        LabCategory c = new LabCategory();
        c.setName(name);
        c.setDescription(description);
        
        // 实验室管理员（实体字段 admin，对应表 manager_id）：managerUserId → managerId
        try {
            Integer managerUserId = resolveCategoryManagerUserId(body);
            if (managerUserId != null) {
                User manager = userRepository.findById(managerUserId).orElse(null);
                if (manager == null) {
                    return ResponseMessage.error("指定的实验室管理员用户不存在");
                }
                c.setAdmin(manager);
            }
        } catch (IllegalArgumentException e) {
            return ResponseMessage.error(e.getMessage());
        }
        
        var now = java.time.LocalDateTime.now();
        c.setCreatedAt(now);
        c.setUpdatedAt(now);

        LabCategory saved = labCategoryRepository.save(c);
        return ResponseMessage.success(saved);
    }

    /**
     * 编辑分类 (Admin)
     */
    @PutMapping("/{id}")
    @Transactional(rollbackFor = Exception.class)
    public ResponseMessage<LabCategory> updateCategory(@PathVariable("id") Long id, @RequestBody Map<String, Object> body) {
        Optional<LabCategory> opt = labCategoryRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseMessage.error("分类不存在");
        }
        LabCategory c = opt.get();
        Integer previousManagerUserId = c.getAdmin() == null ? null : c.getAdmin().getUserId();

        User categoryManagerToSync = null;
        // 先解析管理员，避免 @Transactional 下先改 name 后校验失败导致提交时误 flush
        try {
            Integer managerUserId = resolveCategoryManagerUserId(body);
            if (managerUserId != null) {
                User manager = userRepository.findById(managerUserId).orElse(null);
                if (manager == null) {
                    return ResponseMessage.error("指定的实验室管理员用户不存在");
                }
                categoryManagerToSync = manager;
            }
        } catch (IllegalArgumentException e) {
            return ResponseMessage.error(e.getMessage());
        }

        if (body.get("name") != null) {
            String name = String.valueOf(body.get("name")).trim();
            if (!name.isEmpty()) {
                c.setName(name);
            }
        }
        if (body.get("description") != null) {
            c.setDescription(String.valueOf(body.get("description")).trim());
        }
        if (categoryManagerToSync != null) {
            c.setAdmin(categoryManagerToSync);
        }

        c.setUpdatedAt(java.time.LocalDateTime.now());
        LabCategory saved = labCategoryRepository.save(c);
        if (categoryManagerToSync != null
                && !Objects.equals(previousManagerUserId, categoryManagerToSync.getUserId())) {
            labCategoryManagerCascadeService.cascadeManagerToLabsInCategory(id, categoryManagerToSync);
        }
        return ResponseMessage.success(saved);
    }

    /**
     * 删除分类 (Admin)
     */
    @DeleteMapping("/{id}")
    public ResponseMessage<Void> deleteCategory(@PathVariable("id") Long id) {
        if (!labCategoryRepository.existsById(id)) {
            return ResponseMessage.error("分类不存在");
        }
        // 检查该分类下是否有关联的实验室
        long labCount = labRepository.countByCategoryId(id);
        if (labCount > 0) {
            return ResponseMessage.error("该分类下存在" + labCount + "个实验室，请先删除关联的实验室或将其转移到其他分类");
        }
        labCategoryRepository.deleteById(id);
        return ResponseMessage.success();
    }

    /**
     * 分配/更换实验室管理员 (Admin)
     * 请求体:
     * {
     *   "managerUserId": 3
     * }
     */
    @PutMapping("/{id}/manager")
    @Transactional(rollbackFor = Exception.class)
    public ResponseMessage<LabCategory> assignManager(@PathVariable("id") Long id, @RequestBody Map<String, Object> body) {
        Optional<LabCategory> opt = labCategoryRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseMessage.error("分类不存在");
        }

        Integer adminUserId;
        try {
            adminUserId = resolveCategoryManagerUserId(body);
        } catch (IllegalArgumentException e) {
            return ResponseMessage.error(e.getMessage());
        }
        if (adminUserId == null) {
            return ResponseMessage.error("实验室管理员用户ID不能为空，可传 managerUserId 或 managerId");
        }

        Optional<User> userOpt = userRepository.findById(adminUserId);
        if (userOpt.isEmpty()) {
            return ResponseMessage.error("指定的管理员用户不存在");
        }

        LabCategory c = opt.get();
        Integer previousManagerUserId = c.getAdmin() == null ? null : c.getAdmin().getUserId();
        User manager = userOpt.get();
        c.setAdmin(manager);
        c.setUpdatedAt(java.time.LocalDateTime.now());
        LabCategory saved = labCategoryRepository.save(c);
        if (!Objects.equals(previousManagerUserId, manager.getUserId())) {
            labCategoryManagerCascadeService.cascadeManagerToLabsInCategory(id, manager);
        }
        return ResponseMessage.success(saved);
    }

    /**
     * 根据实验室管理员id获取管理员姓名 (所有用户)
     * GET /lab/lab-categories/manager/{managerId}/name
     * 逻辑：根据manager_id去用户表和用户id匹配，获取管理员姓名
     */
    @GetMapping("/manager/{managerId}/name")
    public ResponseMessage<Map<String, Object>> getManagerNameByManagerId(@PathVariable("managerId") Integer managerId) {
        // 直接根据managerId（用户ID）去用户表查询
        Optional<User> userOpt = userRepository.findById(managerId);
        if (userOpt.isEmpty()) {
            return ResponseMessage.error("未找到该管理员ID对应的用户信息");
        }
        
        User admin = userOpt.get();
        
        // 查找该管理员管理的所有实验室分类
        List<LabCategory> labCategories = labCategoryRepository.findByManagerId(managerId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("managerId", managerId);
        result.put("managerName", admin.getUserName());
        result.put("managerAccount", admin.getUserAccount());
        result.put("managerRole", admin.getRole());
        result.put("managedCategories", labCategories);
        
        // 如果找到了对应的实验室分类，添加分类信息
        if (!labCategories.isEmpty()) {
            LabCategory labCategory = labCategories.get(0);
            result.put("labCategoryId", labCategory.getId());
            result.put("labCategoryName", labCategory.getName());
        } else {
            result.put("labCategoryId", null);
            result.put("labCategoryName", null);
        }
        
        return ResponseMessage.success(result);
    }

    /**
     * 从请求体解析「实验室分类绑定的实验室管理员」用户 ID。
     * 优先级：managerUserId → managerId
     *
     * @return 未提供任何字段时返回 null；某字段有值但非合法整数时抛出 IllegalArgumentException
     */
    private static Integer resolveCategoryManagerUserId(Map<String, Object> body) {
        String[] keys = {"managerUserId", "managerId"};
        for (String key : keys) {
            Object v = body.get(key);
            if (v == null) {
                continue;
            }
            String s = String.valueOf(v).trim();
            if (s.isEmpty()) {
                continue;
            }
            try {
                return Integer.valueOf(s);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(key + "格式不正确");
            }
        }
        return null;
    }
}


