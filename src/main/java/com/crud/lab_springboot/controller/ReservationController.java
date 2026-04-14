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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 预约接口：/lab/reservations
 */
@RestController
@RequestMapping("/lab/reservations")
public class ReservationController {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LabRepository labRepository;

    @Autowired
    private RepairRepository repairRepository;

    /**
     * query reservation records with pagination and role-based access control
     * role-based permissions:
     * - students/teachers: only see their own reservations
     * - lab admins: see reservations for labs they manage
     * - system admins: see all reservations
     * support filtering by lab ID, status, and keyword search
     * supports both labId and lab_id parameters for compatibility
     */
    @GetMapping
    public ResponseMessage<Map<String, Object>> listReservations(
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            @RequestParam(value = "size", required = false, defaultValue = "10") int size,
            @RequestParam(value = "userId", required = false) Integer userId,
            @RequestParam(value = "role", required = false) String role,
            @RequestParam(value = "labId", required = false) Long labId,
            @RequestParam(value = "lab_id", required = false) Long labIdSnake,
            @RequestParam(value = "labIds", required = false) String labIds,
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "keyword", required = false) String keyword
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
            // parse labIds (comma-separated) - use first one for single selection
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
        Page<Reservation> pageResult = reservationRepository.findAll((root, query, criteriaBuilder) -> {
            java.util.List<jakarta.persistence.criteria.Predicate> predicates = new java.util.ArrayList<>();
            
            // role-based access control
            if (finalRole != null && userId != null) {
                switch (finalRole) {
                    case "student":
                    case "teacher":
                        // students/teachers only see their own reservations
                        predicates.add(criteriaBuilder.equal(root.get("user").get("userId"), userId));
                        break;
                    case "lab_admin":
                        // lab admins see reservations for labs they manage
                        predicates.add(criteriaBuilder.equal(root.get("lab").get("manager").get("userId"), userId));
                        break;
                    case "system_admin":
                        // system admins see all reservations, no additional filtering needed
                        break;
                    default:
                        // default: only show user's own reservations
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
            
            if (keyword != null && !keyword.trim().isEmpty()) {
                String searchKeyword = "%" + keyword.trim() + "%";
                predicates.add(criteriaBuilder.or(
                    criteriaBuilder.like(root.get("orderNo"), searchKeyword),
                    criteriaBuilder.like(root.get("purpose"), searchKeyword)
                ));
            }
            
            return criteriaBuilder.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        }, pageable);

        List<Map<String, Object>> records = pageResult.getContent().stream()
                .map(this::convertToReservationDto)
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
     * 查询预约详情
     */
    @GetMapping("/{id}")
    public ResponseMessage<Map<String, Object>> getReservation(@PathVariable("id") Long id) {
        Optional<Reservation> opt = reservationRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseMessage.error("预约记录不存在");
        }
        return ResponseMessage.success(convertToReservationDto(opt.get()));
    }

    /**
     * 创建预约 (Student)
     * 使用事务 + 实验室行级悲观锁，串行化同一实验室的并发创建请求，防止重复预约
     */
    @PostMapping
    @Transactional(rollbackFor = Exception.class)
    public ResponseMessage<Map<String, Object>> createReservation(@RequestBody Map<String, Object> request) {
        Object userIdObj = request.get("userId");
        Object labIdObj = request.get("labId");
        Object startTimeObj = request.get("startTime");
        Object endTimeObj = request.get("endTime");

        if (userIdObj == null) {
            return ResponseMessage.error("userId不能为空");
        }
        if (labIdObj == null) {
            return ResponseMessage.error("labId不能为空");
        }
        if (startTimeObj == null || endTimeObj == null) {
            return ResponseMessage.error("startTime和endTime不能为空");
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

        // 对实验室加悲观写锁，串行化同一实验室的并发创建，避免并发请求同时通过校验
        Optional<Lab> labOpt = labRepository.findByIdWithLock(labId);
        if (labOpt.isEmpty()) {
            return ResponseMessage.error("实验室不存在");
        }

        LocalDateTime startTime;
        LocalDateTime endTime;
        try {
            startTime = LocalDateTime.parse(String.valueOf(startTimeObj), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            endTime = LocalDateTime.parse(String.valueOf(endTimeObj), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        } catch (Exception e) {
            return ResponseMessage.error("时间格式应为: yyyy-MM-dd HH:mm:ss");
        }

        if (startTime.isAfter(endTime) || startTime.isEqual(endTime)) {
            return ResponseMessage.error("开始时间必须早于结束时间");
        }

        Lab lab = labOpt.get();
        // 实验室状态：0-停用，1-正常，2-维护中；仅状态为 1 时可预约
        if (lab.getStatus() == null || lab.getStatus() != 1) {
            return ResponseMessage.error("该实验室当前不可预约（停用或维护中）");
        }
        // 预约须在实验室开放时间内（同一天且时间段在 openTime～closeTime 内）
        if (lab.getOpenTime() != null && lab.getCloseTime() != null) {
            if (!startTime.toLocalDate().equals(endTime.toLocalDate())) {
                return ResponseMessage.error("预约开始与结束须为同一天");
            }
            if (startTime.toLocalTime().isBefore(lab.getOpenTime()) || endTime.toLocalTime().isAfter(lab.getCloseTime())) {
                return ResponseMessage.error("预约时间须在实验室开放时间内（" + lab.getOpenTime() + "～" + lab.getCloseTime() + "）");
            }
        }

        LocalDateTime now = LocalDateTime.now();
        // 先检查同一用户重复预约，给出更明确提示；再检查实验室是否被他人占用（已过期预约不占用）
        if (!reservationRepository.findOverlappingByUserAndLab(userId, labId, startTime, endTime, now).isEmpty()) {
            return ResponseMessage.error("您在该时间段已预约过该实验室，请勿重复预约");
        }
        if (!reservationRepository.findOverlappingByLab(labId, startTime, endTime, now).isEmpty()) {
            return ResponseMessage.error("该时间段该实验室已被预约，请选择其他时间或实验室");
        }

        Reservation reservation = new Reservation();
        reservation.setOrderNo(generateOrderNo());
        reservation.setUser(userOpt.get());
        reservation.setLab(lab);
        reservation.setStartTime(startTime);
        reservation.setEndTime(endTime);
        reservation.setPurpose(request.get("purpose") == null ? null : String.valueOf(request.get("purpose")).trim());
        reservation.setStatus(0); // 预约状态：待审核
        reservation.setUseStatus(0); // 使用状态：待使用

        reservation.setCreatedTime(now);
        reservation.setUpdatedTime(now);

        Reservation saved = reservationRepository.save(reservation);
        return ResponseMessage.success(convertToReservationDto(saved));
    }

    /**
     * 审核预约 (Admin/LabManager) - 通过/拒绝
     */
    @PutMapping("/{id}/audit")
    public ResponseMessage<Map<String, Object>> auditReservation(
            @PathVariable("id") Long id,
            @RequestBody Map<String, Object> request
    ) {
        Object statusObj = request.get("status");
        Object auditUserIdObj = request.get("auditUserId");

        if (statusObj == null) {
            return ResponseMessage.error("status不能为空");
        }

        Integer status;
        try {
            status = Integer.valueOf(String.valueOf(statusObj));
        } catch (NumberFormatException e) {
            return ResponseMessage.error("status格式不正确");
        }

        if (status != 1 && status != 2) {
            return ResponseMessage.error("status只能是1(通过)或2(拒绝)");
        }

        Optional<Reservation> opt = reservationRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseMessage.error("预约记录不存在");
        }

        Reservation reservation = opt.get();

        if (reservation.getStatus() != 0) {
            return ResponseMessage.error("只能审核待审核状态的预约");
        }

        reservation.setStatus(status);
        if (status == 1) {
            // 审核通过后，使用状态进入使用中（前端据此展示“使用完成”按钮）
            reservation.setUseStatus(1);
        } else if (status == 2) {
            // 审核拒绝：使用状态视为取消
            reservation.setUseStatus(4);
        }

        if (status == 2 && request.get("rejectReason") != null) {
            reservation.setRejectReason(String.valueOf(request.get("rejectReason")).trim());
        }

        if (auditUserIdObj != null) {
            Integer auditUserId = Integer.valueOf(String.valueOf(auditUserIdObj));
            userRepository.findById(auditUserId).ifPresent(reservation::setAuditUser);
        }

        reservation.setAuditTime(LocalDateTime.now());
        reservation.setUpdatedTime(LocalDateTime.now());

        Reservation saved = reservationRepository.save(reservation);
        return ResponseMessage.success(convertToReservationDto(saved));
    }

    /**
     * 取消预约 (Student - 只能取消自己待审核的预约)
     */
    @PutMapping("/{id}/cancel")
    public ResponseMessage<Void> cancelReservation(
            @PathVariable("id") Long id,
            @RequestBody(required = false) Map<String, Object> request
    ) {
        Integer userId = null;
        if (request != null && request.get("userId") != null) {
            try {
                userId = Integer.valueOf(String.valueOf(request.get("userId")));
            } catch (NumberFormatException e) {
                return ResponseMessage.error("userId格式不正确");
            }
        }

        Optional<Reservation> opt = reservationRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseMessage.error("预约记录不存在");
        }

        Reservation reservation = opt.get();

        if (userId != null && (reservation.getUser() == null || !userId.equals(reservation.getUser().getUserId()))) {
            return ResponseMessage.error("只能取消自己的预约");
        }

        // 待审核(status=0) 或 审核通过但未到使用时间段(status=1 且 now < startTime) 时可取消
        if (reservation.getStatus() == 0) {
            // 待审核，可取消
        } else if (reservation.getStatus() == 1 && reservation.getStartTime() != null && LocalDateTime.now().isBefore(reservation.getStartTime())) {
            // 审核通过且待使用，可取消
        } else {
            return ResponseMessage.error("只能取消待审核或待使用状态的预约");
        }

        reservation.setStatus(4); // 已取消
        reservation.setUseStatus(4); // 已取消
        reservation.setUpdatedTime(LocalDateTime.now());
        reservationRepository.save(reservation);

        return ResponseMessage.success();
    }

    /**
     * 使用完成 (Student - 已通过的预约，可提前结束或等时间到自动释放)
     * 用户点击「使用完成」后，预约变为已结束，实验室释放为空闲
     */
    @PutMapping("/{id}/finish")
    public ResponseMessage<Map<String, Object>> finishReservation(
            @PathVariable("id") Long id,
            @RequestBody(required = false) Map<String, Object> request
    ) {
        Integer userId = null;
        if (request != null && request.get("userId") != null) {
            try {
                userId = Integer.valueOf(String.valueOf(request.get("userId")));
            } catch (NumberFormatException e) {
                return ResponseMessage.error("userId格式不正确");
            }
        }

        Optional<Reservation> opt = reservationRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseMessage.error("预约记录不存在");
        }

        Reservation reservation = opt.get();

        if (userId != null && (reservation.getUser() == null || !userId.equals(reservation.getUser().getUserId()))) {
            return ResponseMessage.error("只能操作自己的预约");
        }

        // 预约状态必须为已通过，且使用状态为使用中（或由时间判断为使用中）
        if (reservation.getStatus() != 1) {
            return ResponseMessage.error("只能对已通过的预约进行操作");
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(reservation.getStartTime())) {
            return ResponseMessage.error("尚未到预约开始时间，无法提前结束");
        }

        // 使用完成：只改变使用状态，预约状态保持“已通过”
        reservation.setUseStatus(2); // 使用完成
        reservation.setUpdatedTime(now);
        reservationRepository.save(reservation);

        return ResponseMessage.success(convertToReservationDto(reservation));
    }

    /**
     * 故障报修 (Student - 使用完成后，预约状态为已通过，使用状态为已结束时，可进行故障报修)
     * 创建报修记录，预约状态和使用状态保持不变
     */
    @PostMapping("/{id}/repair")
    public ResponseMessage<Map<String, Object>> reportRepair(
            @PathVariable("id") Long id,
            @RequestBody Map<String, Object> request
    ) {
        Integer userId = null;
        if (request != null && request.get("userId") != null) {
            try {
                userId = Integer.valueOf(String.valueOf(request.get("userId")));
            } catch (NumberFormatException e) {
                return ResponseMessage.error("userId格式不正确");
            }
        }

        Object titleObj = request.get("title");
        Object descriptionObj = request.get("description");

        if (titleObj == null || String.valueOf(titleObj).trim().isEmpty()) {
            return ResponseMessage.error("报修标题不能为空");
        }

        Optional<Reservation> opt = reservationRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseMessage.error("预约记录不存在");
        }

        Reservation reservation = opt.get();

        if (userId != null && (reservation.getUser() == null || !userId.equals(reservation.getUser().getUserId()))) {
            return ResponseMessage.error("只能对自己的预约进行报修");
        }

        // 只有预约状态为"已通过"且使用状态为"使用完成"时才能报修（含时间到自动完成，即 endTime 已过）
        if (reservation.getStatus() != 1) {
            return ResponseMessage.error("只能对审核通过的预约进行报修");
        }

        boolean useFinished = Integer.valueOf(2).equals(reservation.getUseStatus())
                || (reservation.getEndTime() != null && reservation.getEndTime().isBefore(LocalDateTime.now()));
        if (!useFinished) {
            return ResponseMessage.error("只有使用完成后才能进行故障报修");
        }

        // 创建报修记录
        Repair repair = new Repair();
        repair.setUser(reservation.getUser());
        repair.setLab(reservation.getLab());
        repair.setReservation(reservation);
        repair.setTitle(String.valueOf(titleObj).trim());
        if (descriptionObj != null) {
            repair.setDescription(String.valueOf(descriptionObj).trim());
        }
        repair.setStatus(0); // 待处理

        LocalDateTime now = LocalDateTime.now();
        repair.setCreatedAt(now);
        repair.setUpdatedAt(now);

        Repair saved = repairRepository.save(repair);

        Map<String, Object> result = new HashMap<>();
        result.put("repairId", saved.getId());
        result.put("title", saved.getTitle());
        result.put("status", saved.getStatus());
        result.put("statusText", "待处理");
        result.put("createdAt", saved.getCreatedAt() != null ? saved.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : null);
        result.put("reservationId", reservation.getId());
        result.put("orderNo", reservation.getOrderNo());
        result.put("message", "报修提交成功");

        return ResponseMessage.success(result);
    }

    /**
     * 生成预约单号
     */
    private String generateOrderNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = String.format("%04d", new Random().nextInt(10000));
        return "R" + timestamp + random;
    }

    /**
     * 转换为前端需要的DTO格式
     */
    private Map<String, Object> convertToReservationDto(Reservation r) {
        Map<String, Object> dto = new HashMap<>();
        dto.put("id", r.getId());
        dto.put("orderNo", r.getOrderNo());
        dto.put("startTime", r.getStartTime() != null ? r.getStartTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : null);
        dto.put("endTime", r.getEndTime() != null ? r.getEndTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : null);
        dto.put("purpose", r.getPurpose());
        dto.put("status", r.getStatus());
        dto.put("rejectReason", r.getRejectReason());
        dto.put("auditTime", r.getAuditTime() != null ? r.getAuditTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : null);
        dto.put("createdTime", r.getCreatedTime() != null ? r.getCreatedTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : null);

        // 用户信息
        if (r.getUser() != null) {
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("userId", r.getUser().getUserId());
            userInfo.put("userName", r.getUser().getUserName());
            dto.put("user", userInfo);
            dto.put("reserverName", r.getUser().getUserName()); // 预约人姓名
        }

        // 实验室信息
        if (r.getLab() != null) {
            Map<String, Object> labInfo = new HashMap<>();
            labInfo.put("id", r.getLab().getId());
            labInfo.put("name", r.getLab().getName());
            labInfo.put("code", r.getLab().getCode());
            dto.put("lab", labInfo);
            dto.put("labName", r.getLab().getName() != null ? r.getLab().getName() : r.getLab().getCode()); // 实验室名称
        }

        // 实验室管理员信息
        if (r.getLab() != null && r.getLab().getManager() != null) {
            dto.put("labManagerName", r.getLab().getManager().getUserName());
        }

        // 审核人信息
        if (r.getAuditUser() != null) {
            Map<String, Object> auditUserInfo = new HashMap<>();
            auditUserInfo.put("userId", r.getAuditUser().getUserId());
            auditUserInfo.put("userName", r.getAuditUser().getUserName());
            dto.put("auditUser", auditUserInfo);
        }

        // 状态文本
        String statusText = switch (r.getStatus()) {
            case 0 -> "待审核";
            case 1 -> "已通过";
            case 2 -> "已拒绝";
            case 4 -> "已取消";
            default -> "未知";
        };
        dto.put("statusText", statusText);

        // 使用状态：0-待审核, 3-待使用, 1-使用中, 2-使用完成, 4-已取消
        // 审核通过但未到使用时间段 → 待使用；到了使用时间段 → 使用中；时间到或点击使用完成 → 使用完成
        LocalDateTime now = LocalDateTime.now();
        int useStatusCode;
        if (r.getStatus() != null && r.getStatus() == 4) {
            useStatusCode = 4; // 已取消
        } else if (r.getStatus() != null && r.getStatus() == 2) {
            useStatusCode = 4; // 审核不通过视为已取消
        } else if (r.getStatus() != null && r.getStatus() == 0) {
            useStatusCode = 0; // 待审核
        } else if (r.getStatus() != null && r.getStatus() == 1) {
            if (r.getStartTime() != null && now.isBefore(r.getStartTime())) {
                useStatusCode = 3; // 审核通过但未到使用时间段 → 待使用
            } else if (r.getEndTime() != null && r.getEndTime().isBefore(now)) {
                useStatusCode = 2; // 已过结束时间 → 使用完成
            } else if (Integer.valueOf(2).equals(r.getUseStatus())) {
                useStatusCode = 2; // 用户已点击使用完成
            } else if (r.getStartTime() != null && r.getEndTime() != null && !now.isBefore(r.getStartTime()) && !now.isAfter(r.getEndTime())) {
                useStatusCode = 1; // 在使用时间段内 → 使用中
            } else {
                useStatusCode = 2; // 其他已通过情况视为使用完成
            }
        } else {
            useStatusCode = 0;
        }

        String useStatusText = switch (useStatusCode) {
            case 0 -> "待审核";
            case 1 -> "使用中";
            case 2 -> "使用完成";
            case 3 -> "待使用";
            case 4 -> "已取消";
            default -> "未知";
        };
        dto.put("useStatusCode", useStatusCode);
        dto.put("useStatus", useStatusText);

        return dto;
    }
}


