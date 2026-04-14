package com.crud.lab_springboot.controller;

import com.crud.lab_springboot.projo.Lab;
import com.crud.lab_springboot.projo.Repair;
import com.crud.lab_springboot.projo.Reservation;
import com.crud.lab_springboot.projo.ResponseMessage;
import com.crud.lab_springboot.projo.User;
import com.crud.lab_springboot.reposity.LabRepository;
import com.crud.lab_springboot.reposity.RepairRepository;
import com.crud.lab_springboot.reposity.ReservationRepository;
import com.crud.lab_springboot.reposity.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 报修接口：/lab/repairs
 * 使用完成后用户填报修表单生成报修记录
 */
@RestController
@RequestMapping("/lab/repairs")
public class RepairController {

    @Autowired
    private RepairRepository repairRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LabRepository labRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    /**
     * 分页查询报修记录 with role-based access control
     * role-based permissions:
     * - students/teachers: only see their own repair records
     * - lab admins: see repair records for labs they manage
     * - system admins: see all repair records
     * supports both labId and lab_id parameters for compatibility
     */
    @GetMapping
    public ResponseMessage<Map<String, Object>> listRepairs(
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            @RequestParam(value = "size", required = false, defaultValue = "10") int size,
            @RequestParam(value = "userId", required = false) Integer userId,
            @RequestParam(value = "role", required = false) String role,
            @RequestParam(value = "labId", required = false) Long labId,
            @RequestParam(value = "lab_id", required = false) Long labIdSnake,
            @RequestParam(value = "labIds", required = false) String labIds,
            @RequestParam(value = "status", required = false) Integer status
    ) {
        int p = Math.max(page - 1, 0);
        int s = Math.max(size, 1);
        PageRequest pageable = PageRequest.of(p, s, Sort.by(Sort.Direction.DESC, "createdAt"));

        // determine which lab ID to use (priority: labId > lab_id > labIds)
        Long effectiveLabId = null;
        if (labId != null) {
            effectiveLabId = labId;
        } else if (labIdSnake != null) {
            effectiveLabId = labIdSnake;
        } else if (labIds != null && !labIds.trim().isEmpty()) {
            String[] ids = labIds.split(",");
            if (ids.length > 0) {
                try {
                    effectiveLabId = Long.valueOf(ids[0].trim());
                } catch (NumberFormatException e) {
                    effectiveLabId = null;
                }
            }
        }

        // create final copies for lambda usage
        final Long finalEffectiveLabId = effectiveLabId;
        final String finalLabIds = labIds;
        final String finalRole = role;

        // build dynamic query with role-based filtering
        Page<Repair> pageResult = repairRepository.findAll((root, query, criteriaBuilder) -> {
            java.util.List<jakarta.persistence.criteria.Predicate> predicates = new java.util.ArrayList<>();
            
            // role-based access control
            if (finalRole != null && userId != null) {
                switch (finalRole) {
                    case "student":
                    case "teacher":
                        // students/teachers only see their own repair records
                        predicates.add(criteriaBuilder.equal(root.get("user").get("userId"), userId));
                        break;
                    case "lab_admin":
                        // lab admins see repair records for labs they manage
                        predicates.add(criteriaBuilder.equal(root.get("lab").get("manager").get("userId"), userId));
                        break;
                    case "system_admin":
                        // system admins see all repair records, no additional filtering needed
                        break;
                    default:
                        // default: only show user's own repair records
                        predicates.add(criteriaBuilder.equal(root.get("user").get("userId"), userId));
                }
            }
            
            // lab filtering - apply after role-based filtering to ensure proper scope
            if (finalEffectiveLabId != null) {
                predicates.add(criteriaBuilder.equal(root.get("lab").get("id"), finalEffectiveLabId));
            } else if (finalLabIds != null && !finalLabIds.trim().isEmpty() && !"lab_admin".equals(finalRole)) {
                // for non-lab_admin roles, support multiple lab IDs filtering
                String[] ids = finalLabIds.split(",");
                if (ids.length > 0) {
                    try {
                        java.util.List<Long> idList = new java.util.ArrayList<>();
                        for (String id : ids) {
                            idList.add(Long.valueOf(id.trim()));
                        }
                        predicates.add(root.get("lab").get("id").in(idList));
                    } catch (NumberFormatException e) {
                        // ignore invalid format
                    }
                }
            }
            
            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }
            
            return criteriaBuilder.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        }, pageable);

        List<Map<String, Object>> records = pageResult.getContent().stream()
                .map(this::convertToRepairDto)
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
     * 查询报修详情
     */
    @GetMapping("/{id}")
    public ResponseMessage<Map<String, Object>> getRepair(@PathVariable("id") Long id) {
        Optional<Repair> opt = repairRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseMessage.error("报修记录不存在");
        }
        return ResponseMessage.success(convertToRepairDto(opt.get()));
    }

    /**
     * 创建报修（用户使用完成后填报修表单）
     */
    @PostMapping
    public ResponseMessage<Map<String, Object>> createRepair(@RequestBody Map<String, Object> request) {
        Object userIdObj = request.get("userId");
        Object labIdObj = request.get("labId");
        Object titleObj = request.get("title");

        if (userIdObj == null) {
            return ResponseMessage.error("userId不能为空");
        }
        if (labIdObj == null) {
            return ResponseMessage.error("labId不能为空");
        }
        if (titleObj == null || String.valueOf(titleObj).trim().isEmpty()) {
            return ResponseMessage.error("title不能为空");
        }

        Integer userId;
        Long labId;
        try {
            userId = Integer.valueOf(String.valueOf(userIdObj));
            labId = Long.valueOf(String.valueOf(labIdObj));
        } catch (NumberFormatException e) {
            return ResponseMessage.error("userId或labId格式不正确");
        }

        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return ResponseMessage.error("用户不存在");
        }

        Optional<Lab> labOpt = labRepository.findById(labId);
        if (labOpt.isEmpty()) {
            return ResponseMessage.error("实验室不存在");
        }

        Repair repair = new Repair();
        repair.setUser(userOpt.get());
        repair.setLab(labOpt.get());
        repair.setTitle(String.valueOf(titleObj).trim());
        repair.setDescription(request.get("description") == null ? null : String.valueOf(request.get("description")).trim());
        repair.setStatus(0); // 待处理

        Object reservationIdObj = request.get("reservationId");
        if (reservationIdObj != null) {
            try {
                Long reservationId = Long.valueOf(String.valueOf(reservationIdObj));
                reservationRepository.findById(reservationId).ifPresent(repair::setReservation);
            } catch (NumberFormatException ignored) {
            }
        }

        LocalDateTime now = LocalDateTime.now();
        repair.setCreatedAt(now);
        repair.setUpdatedAt(now);

        Repair saved = repairRepository.save(repair);
        return ResponseMessage.success(convertToRepairDto(saved));
    }

    private Map<String, Object> convertToRepairDto(Repair r) {
        Map<String, Object> dto = new HashMap<>();
        dto.put("id", r.getId());
        dto.put("title", r.getTitle());
        dto.put("description", r.getDescription());
        dto.put("status", r.getStatus());
        dto.put("createdAt", r.getCreatedAt() != null ? r.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : null);
        dto.put("updatedAt", r.getUpdatedAt() != null ? r.getUpdatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : null);

        String statusText = switch (r.getStatus() != null ? r.getStatus() : -1) {
            case 0 -> "待处理";
            case 1 -> "处理中";
            case 2 -> "已完成";
            case 3 -> "已关闭";
            default -> "未知";
        };
        dto.put("statusText", statusText);

        if (r.getUser() != null) {
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("userId", r.getUser().getUserId());
            userInfo.put("userName", r.getUser().getUserName());
            dto.put("user", userInfo);
        }
        if (r.getLab() != null) {
            Map<String, Object> labInfo = new HashMap<>();
            labInfo.put("id", r.getLab().getId());
            labInfo.put("name", r.getLab().getName());
            labInfo.put("code", r.getLab().getCode());
            dto.put("lab", labInfo);
        }
        if (r.getReservation() != null) {
            dto.put("reservationId", r.getReservation().getId());
            dto.put("orderNo", r.getReservation().getOrderNo());
        }

        return dto;
    }
}
