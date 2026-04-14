package com.crud.lab_springboot.controller;

import com.crud.lab_springboot.projo.Lab;
import com.crud.lab_springboot.projo.Maintenance;
import com.crud.lab_springboot.projo.Repair;
import com.crud.lab_springboot.projo.ResponseMessage;
import com.crud.lab_springboot.reposity.LabRepository;
import com.crud.lab_springboot.reposity.MaintenanceRepository;
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
 * @return
 */
@RestController
@RequestMapping("/lab/maintenances")
public class MaintenanceController {

    @Autowired
    private MaintenanceRepository maintenanceRepository;
    @Autowired
    private LabRepository labRepository;
    @Autowired
    private com.crud.lab_springboot.reposity.RepairRepository repairRepository;

    /**
     * @return
     */
    @GetMapping
    public ResponseMessage<Map<String, Object>> listMaintenances(
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            @RequestParam(value = "size", required = false, defaultValue = "10") int size,
            @RequestParam(value = "userId", required = false) Integer userId,
            @RequestParam(value = "role", required = false) String role,
            @RequestParam(value = "labId", required = false) Long labId,
            @RequestParam(value = "lab_id", required = false) Long labIdSnake,
            @RequestParam(value = "labIds", required = false) String labIds,
            @RequestParam(value = "reporterUserId", required = false) Integer reporterUserId,
            @RequestParam(value = "reporterId", required = false) Integer reporterId
    ) {
        int p = Math.max(page - 1, 0);
        int s = Math.max(size, 1);
        PageRequest pageable = PageRequest.of(p, s, Sort.by(Sort.Direction.DESC, "createdTime"));

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

        Integer effectiveReporterFilter = reporterUserId != null ? reporterUserId : reporterId;

        // create final copies for lambda usage
        final Long finalEffectiveLabId = effectiveLabId;
        final String finalLabIds = labIds;
        final String finalRole = role;
        final Integer finalReporterFilter = effectiveReporterFilter;

        // build dynamic query with role-based filtering
        Page<Maintenance> pageResult = maintenanceRepository.findAll((root, query, criteriaBuilder) -> {
            java.util.List<jakarta.persistence.criteria.Predicate> predicates = new java.util.ArrayList<>();

            // role-based access control - simplified with direct repair relationship
            if (finalRole != null && userId != null) {
                switch (finalRole) {
                    case "student":
                    case "teacher":
                        // students/teachers only see maintenance records for their own repair records
                        predicates.add(criteriaBuilder.equal(root.get("repair").get("user").get("userId"), userId));
                        break;
                    case "lab_admin":
                        // lab admins see maintenance records for labs they manage
                        predicates.add(criteriaBuilder.equal(root.get("lab").get("manager").get("userId"), userId));
                        break;
                    case "system_admin":
                        // system admins see all maintenance records, no additional filtering needed
                        break;
                    default:
                        // default: only show user's own repair records
                        predicates.add(criteriaBuilder.equal(root.get("repair").get("user").get("userId"), userId));
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

            // 按报修人（关联报修单的 user_id）筛选；无报修单关联的检修记录不会命中
            if (finalReporterFilter != null) {
                predicates.add(criteriaBuilder.equal(root.get("repair").get("user").get("userId"), finalReporterFilter));
            }

            return criteriaBuilder.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        }, pageable);

        List<Map<String, Object>> records = pageResult.getContent().stream()
                .map(this::convertToMaintenanceDto)
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
     * 查询检修详情
     */
    @GetMapping("/{id}")
    public ResponseMessage<Map<String, Object>> getMaintenance(@PathVariable("id") Long id) {
        Optional<Maintenance> opt = maintenanceRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseMessage.error("检修记录不存在");
        }
        return ResponseMessage.success(convertToMaintenanceDto(opt.get()));
    }

    /**
     * @return
     */
    @PostMapping
    public ResponseMessage<Map<String, Object>> createMaintenance(@RequestBody Map<String, Object> request) {
        Object labIdObj = request.get("labId");       // 必填
        Object repairIdObj = request.get("repairId");   // 报修记录ID（可选）
        Object contentObj = request.get("content");
        Object maintenanceUnitObj = request.get("maintenanceUnit");
        Object maintenanceTimeObj = request.get("maintenanceTime");
        Object handlerObj = request.get("handler");
        Object handlerPhoneObj = request.get("handlerPhone");

        // 验证必填字段
        if (labIdObj == null || String.valueOf(labIdObj).trim().isEmpty()) {
            return ResponseMessage.error("实验室ID不能为空");
        }
        if (contentObj == null || String.valueOf(contentObj).trim().isEmpty()) {
            return ResponseMessage.error("检修内容不能为空");
        }
        if (handlerObj == null || String.valueOf(handlerObj).trim().isEmpty()) {
            return ResponseMessage.error("检修人不能为空");
        }
        if (handlerPhoneObj == null || String.valueOf(handlerPhoneObj).trim().isEmpty()) {
            return ResponseMessage.error("联系电话不能为空");
        }
        if (maintenanceUnitObj == null || String.valueOf(maintenanceUnitObj).trim().isEmpty()) {
            return ResponseMessage.error("检修单位不能为空");
        }
        if (maintenanceTimeObj == null || String.valueOf(maintenanceTimeObj).trim().isEmpty()) {
            return ResponseMessage.error("检修时间不能为空");
        }

        // 获取实验室信息
        Optional<Lab> labOpt = labRepository.findById(Long.valueOf(String.valueOf(labIdObj)));
        if (labOpt.isEmpty()) {
            return ResponseMessage.error("实验室不存在");
        }
        Lab lab = labOpt.get();

        Maintenance maintenance = new Maintenance();
        maintenance.setLab(lab);
        maintenance.setContent(String.valueOf(contentObj).trim());
        maintenance.setMaintenanceUnit(String.valueOf(maintenanceUnitObj).trim());
        maintenance.setHandler(String.valueOf(handlerObj).trim());
        maintenance.setHandlerPhone(String.valueOf(handlerPhoneObj).trim());

        // 处理报修记录关联（可选）
        if (repairIdObj != null && !String.valueOf(repairIdObj).trim().isEmpty()) {
            try {
                Long repairId = Long.valueOf(String.valueOf(repairIdObj));
                repairRepository.findById(repairId).ifPresent(maintenance::setRepair);
            } catch (NumberFormatException e) {
                // 忽略无效的repairId
            }
        }

        LocalDateTime maintenanceTime;
        if (maintenanceTimeObj == null || String.valueOf(maintenanceTimeObj).trim().isEmpty()) {
            maintenanceTime = LocalDateTime.now();
        } else {
            try {
                maintenanceTime = LocalDateTime.parse(String.valueOf(maintenanceTimeObj), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            } catch (Exception e) {
                return ResponseMessage.error("maintenanceTime format should be: yyyy-MM-dd HH:mm:ss");
            }
        }
        maintenance.setMaintenanceTime(maintenanceTime);

        LocalDateTime now = LocalDateTime.now();
        maintenance.setCreatedTime(now);
        maintenance.setStatus(0); // 处理中

        Maintenance saved = maintenanceRepository.save(maintenance);
        syncRepairStatusIfLinked(saved);

        return ResponseMessage.success(convertToMaintenanceDto(saved));
    }

    /**
     * 按 id 更新检修记录（部分字段可选，与文档 8.4 一致）
     */
    @PutMapping("/{id}")
    public ResponseMessage<Map<String, Object>> updateMaintenance(
            @PathVariable("id") Long id,
            @RequestBody Map<String, Object> request) {
        Optional<Maintenance> opt = maintenanceRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseMessage.error("检修记录不存在");
        }
        Maintenance m = opt.get();

        if (request.containsKey("labId") && request.get("labId") != null) {
            Optional<Lab> labOpt = labRepository.findById(Long.valueOf(String.valueOf(request.get("labId"))));
            if (labOpt.isEmpty()) {
                return ResponseMessage.error("实验室不存在");
            }
            m.setLab(labOpt.get());
        }
        if (request.containsKey("repairId")) {
            Object repairIdObj = request.get("repairId");
            if (repairIdObj == null || String.valueOf(repairIdObj).trim().isEmpty()) {
                m.setRepair(null);
            } else {
                try {
                    Long repairId = Long.valueOf(String.valueOf(repairIdObj).trim());
                    Optional<Repair> rOpt = repairRepository.findById(repairId);
                    if (rOpt.isEmpty()) {
                        return ResponseMessage.error("报修记录不存在");
                    }
                    m.setRepair(rOpt.get());
                } catch (NumberFormatException e) {
                    return ResponseMessage.error("repairId 格式不正确");
                }
            }
        }
        if (request.containsKey("content")) {
            String c = String.valueOf(request.get("content")).trim();
            if (c.isEmpty()) {
                return ResponseMessage.error("检修内容不能为空");
            }
            m.setContent(c);
        }
        if (request.containsKey("maintenanceUnit") && request.get("maintenanceUnit") != null) {
            m.setMaintenanceUnit(String.valueOf(request.get("maintenanceUnit")).trim());
        }
        if (request.containsKey("maintenanceTime") && request.get("maintenanceTime") != null) {
            String ts = String.valueOf(request.get("maintenanceTime")).trim();
            if (!ts.isEmpty()) {
                try {
                    m.setMaintenanceTime(LocalDateTime.parse(ts, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                } catch (Exception e) {
                    return ResponseMessage.error("maintenanceTime 格式应为: yyyy-MM-dd HH:mm:ss");
                }
            }
        }
        if (request.containsKey("handler") && request.get("handler") != null) {
            m.setHandler(String.valueOf(request.get("handler")).trim());
        }
        if (request.containsKey("handlerPhone") && request.get("handlerPhone") != null) {
            m.setHandlerPhone(String.valueOf(request.get("handlerPhone")).trim());
        }
        if (request.containsKey("status") && request.get("status") != null) {
            try {
                Integer status = Integer.valueOf(String.valueOf(request.get("status")));
                if (status < 0 || status > 1) {
                    return ResponseMessage.error("状态值无效，应为0（处理中）或1（已完成）");
                }
                m.setStatus(status);
            } catch (NumberFormatException e) {
                return ResponseMessage.error("status 格式错误");
            }
        }

        Maintenance saved = maintenanceRepository.save(m);
        syncRepairStatusIfLinked(saved);
        return ResponseMessage.success(convertToMaintenanceDto(saved));
    }

    /**
     * 更新检修记录状态（同步更新报修记录状态）
     */
    @PutMapping("/{id}/status")
    public ResponseMessage<Map<String, Object>> updateMaintenanceStatus(
            @PathVariable("id") Long id,
            @RequestBody Map<String, Object> request) {
        
        Object statusObj = request.get("status");
        if (statusObj == null) {
            return ResponseMessage.error("状态不能为空");
        }
        
        try {
            Integer status = Integer.valueOf(String.valueOf(statusObj));
            if (status < 0 || status > 1) {
                return ResponseMessage.error("状态值无效，应为0（处理中）或1（已完成）");
            }
            
            Optional<Maintenance> opt = maintenanceRepository.findById(id);
            if (opt.isEmpty()) {
                return ResponseMessage.error("检修记录不存在");
            }
            
            Maintenance maintenance = opt.get();
            maintenance.setStatus(status);
            Maintenance saved = maintenanceRepository.save(maintenance);
            syncRepairStatusIfLinked(saved);
            return ResponseMessage.success(convertToMaintenanceDto(saved));
        } catch (NumberFormatException e) {
            return ResponseMessage.error("状态格式错误");
        }
    }

    /**
     * 检修与报修关联时，按检修 status 同步报修单状态。
     */
    private void syncRepairStatusIfLinked(Maintenance maintenance) {
        if (maintenance.getRepair() == null) {
            return;
        }
        Repair repair = maintenance.getRepair();
        Integer st = maintenance.getStatus();
        if (st == null) {
            return;
        }
        if (st == 0) {
            repair.setStatus(1);
        } else if (st == 1) {
            repair.setStatus(2);
        }
        repair.setUpdatedAt(java.time.LocalDateTime.now());
        repairRepository.save(repair);
    }

    private Map<String, Object> convertToMaintenanceDto(Maintenance m) {
        Map<String, Object> dto = new HashMap<>();
        dto.put("id", m.getId());
        dto.put("content", m.getContent());
        dto.put("maintenanceUnit", m.getMaintenanceUnit());
        dto.put("maintenanceTime", m.getMaintenanceTime() != null ? m.getMaintenanceTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : null);
        dto.put("createdTime", m.getCreatedTime() != null ? m.getCreatedTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : null);
        dto.put("status", m.getStatus());
        dto.put("statusText", m.getStatus() == 0 ? "处理中" : "已完成");
        dto.put("handler", m.getHandler());
        dto.put("handlerPhone", m.getHandlerPhone());

        // 实验室信息
        if (m.getLab() != null) {
            Map<String, Object> labInfo = new HashMap<>();
            labInfo.put("id", m.getLab().getId());
            labInfo.put("name", m.getLab().getName());
            labInfo.put("code", m.getLab().getCode());
            dto.put("lab", labInfo);
            dto.put("labId", m.getLab().getId());
        }

        // 报修记录信息
        if (m.getRepair() != null) {
            Map<String, Object> repairInfo = new HashMap<>();
            repairInfo.put("id", m.getRepair().getId());
            repairInfo.put("title", m.getRepair().getTitle());
            repairInfo.put("description", m.getRepair().getDescription());
            repairInfo.put("status", m.getRepair().getStatus());
            repairInfo.put("createdAt", m.getRepair().getCreatedAt() != null ? m.getRepair().getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : null);
            dto.put("repair", repairInfo);
            dto.put("repairId", m.getRepair().getId());
            
            // 报修人信息
            if (m.getRepair().getUser() != null) {
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("userId", m.getRepair().getUser().getUserId());
                userInfo.put("userName", m.getRepair().getUser().getUserName());
                dto.put("reporter", userInfo);
                dto.put("reporterId", m.getRepair().getUser().getUserId());
            }
        }

        return dto;
    }
}
