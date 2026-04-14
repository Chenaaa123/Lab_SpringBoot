package com.crud.lab_springboot.controller;

import com.crud.lab_springboot.projo.Lab;
import com.crud.lab_springboot.projo.LabCategory;
import com.crud.lab_springboot.projo.ResponseMessage;
import com.crud.lab_springboot.projo.User;
import com.crud.lab_springboot.reposity.LabCategoryRepository;
import com.crud.lab_springboot.reposity.LabRepository;
import com.crud.lab_springboot.reposity.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 实验室接口：/lab/labs
 */
@RestController
@RequestMapping("/lab/labs")
public class LabController {

    @Autowired
    private LabRepository labRepository;

    @Autowired
    private LabCategoryRepository labCategoryRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * 查询实验室列表 (支持分类、状态、编号筛选)
     * role-based access control:
     * - students/teachers: only see labs with status=1 (normal)
     * - lab admins: see labs they manage
     * - system admins: see all labs
     */
    @GetMapping
    public ResponseMessage<Map<String, Object>> listLabs(
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "role", required = false) String role,
            @RequestParam(value = "userId", required = false) Integer userId,
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            @RequestParam(value = "size", required = false, defaultValue = "10") int size
    ) {
        int p = Math.max(page - 1, 0);
        int s = Math.max(size, 1);
        PageRequest pageable = PageRequest.of(p, s, Sort.by(Sort.Direction.ASC, "id"));

        // build dynamic query conditions with role-based filtering
        Page<Lab> pageResult = labRepository.findAll((root, query, criteriaBuilder) -> {
            java.util.List<jakarta.persistence.criteria.Predicate> predicates = new java.util.ArrayList<>();

            // role-based filtering
            if (role != null && userId != null) {
                switch (role) {
                    case "student":
                    case "teacher":
                        // students/teachers only see normal labs (status=1)
                        predicates.add(criteriaBuilder.equal(root.get("status"), 1));
                        break;
                    case "lab_admin":
                        // lab admins only see labs they manage
                        predicates.add(criteriaBuilder.equal(root.get("manager").get("userId"), userId));
                        break;
                    case "system_admin":
                        // system admins see all labs, no additional filtering needed
                        break;
                    default:
                        // default: only show normal labs
                        predicates.add(criteriaBuilder.equal(root.get("status"), 1));
                }
            }

            // category filter
            if (categoryId != null) {
                predicates.add(criteriaBuilder.equal(root.get("category").get("id"), categoryId));
            }

            // status filter (only for system admins)
            if (status != null && ("system_admin".equals(role) || role == null)) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }

            // name/code filter
            if (name != null && !name.trim().isEmpty()) {
                String keyword = "%" + name.trim() + "%";
                predicates.add(criteriaBuilder.or(
                    criteriaBuilder.like(root.get("code"), keyword),
                    criteriaBuilder.like(root.get("name"), keyword)
                ));
            }
            
            return criteriaBuilder.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        }, pageable);

        Map<String, Object> result = new java.util.HashMap<>();
        result.put("records", pageResult.getContent());
        result.put("total", pageResult.getTotalElements());
        result.put("totalPages", pageResult.getTotalPages());
        result.put("currentPage", page + 1);
        result.put("pageSize", size);
        result.put("hasNext", pageResult.hasNext());
        result.put("hasPrevious", pageResult.hasPrevious());

        return ResponseMessage.success(result);
    }

    /**
     * 查询实验室详情
     */
    @GetMapping("/{id}")
    public ResponseMessage<Lab> getLab(@PathVariable("id") Long id) {
        Optional<Lab> opt = labRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseMessage.error("实验室不存在");
        }
        return ResponseMessage.success(opt.get());
    }

    /**
     * 根据编号查询实验室
     */
    @GetMapping("/code/{code}")
    public ResponseMessage<Lab> getLabByCode(@PathVariable("code") String code) {
        Optional<Lab> opt = labRepository.findByCode(code);
        if (opt.isEmpty()) {
            return ResponseMessage.error("实验室编号不存在");
        }
        return ResponseMessage.success(opt.get());
    }

    /**
     * 根据实验室管理员用户 ID（manager_id）查询实验室列表
     */
    @GetMapping("/manager/{managerId}")
    public ResponseMessage<List<Lab>> getLabsByManagerId(@PathVariable("managerId") Integer managerId) {
        List<Lab> labs = labRepository.findByManagerUserId(managerId);
        return ResponseMessage.success(labs);
    }

    /**
     * 新增实验室 (Admin)
     * 请求体:
     * {
     *   "code": "LAB001",
     *   "categoryId": 1,
     *   "managerUserId": 3,
     *   "location": "A-305",
     *   "equipment": "计算机40台,投影仪1台,空调2台",
     *   "openTime": "08:00:00",
     *   "closeTime": "22:00:00",
     *   "status": 1,
     *   "description": "描述",
     *   "imageUrl": "http://example.com/image.jpg"
     * }
     */
    @PostMapping
    public ResponseMessage<Lab> createLab(@RequestBody Map<String, Object> body) {
        Lab lab = new Lab();

        // 设置实验室编号（必填）
        String code = body.get("code") == null ? null : String.valueOf(body.get("code")).trim();
        if (code == null || code.isEmpty()) {
            return ResponseMessage.error("实验室编号不能为空");
        }
        lab.setCode(code);
        
        // 设置实验室名称（可选）
        if (body.get("name") != null) {
            String name = String.valueOf(body.get("name")).trim();
            lab.setName(name.isEmpty() ? null : name);
        }
        
        lab.setLocation(body.get("location") == null ? null : String.valueOf(body.get("location")).trim());

        if (body.get("status") != null) {
            lab.setStatus(Integer.valueOf(String.valueOf(body.get("status"))));
        } else {
            lab.setStatus(1); // 默认正常
        }
        if (body.get("description") != null) {
            lab.setDescription(String.valueOf(body.get("description")).trim());
        }
        if (body.get("equipment") != null) {
            lab.setEquipment(String.valueOf(body.get("equipment")).trim());
        }
        if (body.get("imageUrl") != null) {
            lab.setImageUrl(String.valueOf(body.get("imageUrl")).trim());
        }

        // 开放/关闭时间
        if (body.get("openTime") != null) {
            String openTimeStr = String.valueOf(body.get("openTime")).trim();
            if (!openTimeStr.isEmpty()) {
                try {
                    lab.setOpenTime(java.time.LocalTime.parse(openTimeStr));
                } catch (Exception e) {
                    return ResponseMessage.error("openTime 格式应为 HH:mm:ss");
                }
            }
        }
        if (body.get("closeTime") != null) {
            String closeTimeStr = String.valueOf(body.get("closeTime")).trim();
            if (!closeTimeStr.isEmpty()) {
                try {
                    lab.setCloseTime(java.time.LocalTime.parse(closeTimeStr));
                } catch (Exception e) {
                    return ResponseMessage.error("closeTime 格式应为 HH:mm:ss");
                }
            }
        }

        // 关联分类（必填）
        if (body.get("categoryId") != null) {
            Long categoryId = Long.valueOf(String.valueOf(body.get("categoryId")));
            LabCategory category = labCategoryRepository.findById(categoryId).orElse(null);
            lab.setCategory(category);
        } else {
            return ResponseMessage.error("categoryId 不能为空");
        }

        Integer managerUserId;
        try {
            managerUserId = resolveLabManagerUserId(body);
        } catch (IllegalArgumentException e) {
            return ResponseMessage.error(e.getMessage());
        }
        if (managerUserId == null || managerUserId <= 0) {
            return ResponseMessage.error("实验室管理员不能为空，可传 managerUserId 或 managerId（须为正整数）");
        }
        User manager = userRepository.findById(managerUserId).orElse(null);
        if (manager == null) {
            return ResponseMessage.error("managerUserId/managerId 无效，用户不存在");
        }
        lab.setManager(manager);

        Lab saved = labRepository.save(lab);
        return ResponseMessage.success(saved);
    }

    /**
     * 编辑实验室 (Admin)
     */
    @PutMapping("/{id}")
    public ResponseMessage<Lab> updateLab(@PathVariable("id") Long id, @RequestBody Map<String, Object> body) {
        Optional<Lab> opt = labRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseMessage.error("实验室不存在");
        }
        Lab lab = opt.get();

        if (body.get("code") != null) {
            String code = String.valueOf(body.get("code")).trim();
            if (!code.isEmpty()) lab.setCode(code);
        }
        if (body.get("name") != null) {
            String name = String.valueOf(body.get("name")).trim();
            lab.setName(name.isEmpty() ? null : name);
        }
        if (body.get("location") != null) {
            lab.setLocation(String.valueOf(body.get("location")).trim());
        }
        if (body.get("status") != null) {
            lab.setStatus(Integer.valueOf(String.valueOf(body.get("status"))));
        }
        if (body.get("description") != null) {
            lab.setDescription(String.valueOf(body.get("description")).trim());
        }
        if (body.get("equipment") != null) {
            lab.setEquipment(String.valueOf(body.get("equipment")).trim());
        }
        if (body.get("imageUrl") != null) {
            lab.setImageUrl(String.valueOf(body.get("imageUrl")).trim());
        }
        if (body.get("openTime") != null) {
            String openTimeStr = String.valueOf(body.get("openTime")).trim();
            if (!openTimeStr.isEmpty()) {
                try {
                    lab.setOpenTime(java.time.LocalTime.parse(openTimeStr));
                } catch (Exception e) {
                    return ResponseMessage.error("openTime 格式应为 HH:mm:ss");
                }
            }
        }
        if (body.get("closeTime") != null) {
            String closeTimeStr = String.valueOf(body.get("closeTime")).trim();
            if (!closeTimeStr.isEmpty()) {
                try {
                    lab.setCloseTime(java.time.LocalTime.parse(closeTimeStr));
                } catch (Exception e) {
                    return ResponseMessage.error("closeTime 格式应为 HH:mm:ss");
                }
            }
        }
        if (body.get("categoryId") != null) {
            Long categoryId = Long.valueOf(String.valueOf(body.get("categoryId")));
            LabCategory category = labCategoryRepository.findById(categoryId).orElse(null);
            lab.setCategory(category);
        }
        try {
            Integer managerUserId = resolveLabManagerUserId(body);
            if (managerUserId != null) {
                if (managerUserId <= 0) {
                    return ResponseMessage.error("managerUserId/managerId 须为正整数");
                }
                User manager = userRepository.findById(managerUserId).orElse(null);
                if (manager == null) {
                    return ResponseMessage.error("managerUserId/managerId 无效，用户不存在");
                }
                lab.setManager(manager);
            }
        } catch (IllegalArgumentException e) {
            return ResponseMessage.error(e.getMessage());
        }
        Lab saved = labRepository.save(lab);
        return ResponseMessage.success(saved);
    }

    /**
     * 删除实验室 (Admin)
     */
    @DeleteMapping("/{id}")
    public ResponseMessage<Void> deleteLab(@PathVariable("id") Long id) {
        if (!labRepository.existsById(id)) {
            return ResponseMessage.error("实验室不存在");
        }
        labRepository.deleteById(id);
        return ResponseMessage.success();
    }

    /**
     * 从请求体解析实验室绑定的管理员用户 ID（与分类模块一致：managerUserId → managerId）。
     */
    private static Integer resolveLabManagerUserId(Map<String, Object> body) {
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


