package com.crud.lab_springboot.controller;

import com.crud.lab_springboot.projo.ResponseMessage;
import com.crud.lab_springboot.projo.Announcement;
import com.crud.lab_springboot.projo.Lab;
import com.crud.lab_springboot.projo.User;
import com.crud.lab_springboot.reposity.AnnouncementRepository;
import com.crud.lab_springboot.reposity.LabRepository;
import com.crud.lab_springboot.reposity.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 公告接口：/lab/announcements
 */
@RestController
@RequestMapping("/lab/announcements")
public class AnnouncementController {

    @Autowired
    private AnnouncementRepository announcementRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LabRepository labRepository;

    /**
     * 查询公告列表（所有用户）
     * GET /lab/announcements?page=1&size=10
     */
    @GetMapping
    public ResponseMessage<Map<String, Object>> listAnnouncements(
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            @RequestParam(value = "size", required = false, defaultValue = "10") int size
    ) {
        int p = Math.max(page - 1, 0);
        int s = Math.max(size, 1);
        var pageable = PageRequest.of(p, s, Sort.by(Sort.Direction.DESC, "publishTime", "createdAt", "id"));

        var pageResult = announcementRepository.findAll(pageable)
                .map(this::toListItemDto);

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
     * 查询公告详情（所有用户）
     */
    @GetMapping("/{id}")
    public ResponseMessage<AnnouncementDetailDto> getAnnouncement(@PathVariable("id") Long id) {
        Optional<Announcement> opt = announcementRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseMessage.error("公告不存在");
        }
        return ResponseMessage.success(toDetailDto(opt.get()));
    }

    /**
     * 发布公告（Admin）
     * 约定：请求头携带 X-Role=Admin 或 X-Role=管理员
     */
    @PostMapping
    public ResponseMessage<AnnouncementDetailDto> createAnnouncement(
            @RequestHeader(value = "X-Role", required = false) String role,
            @RequestBody AnnouncementCreateDto body
    ) {
        if (!isAdmin(role)) {
            return ResponseMessage.error("无权限（需要Admin）");
        }
        if (body == null || body.title() == null || body.title().trim().isEmpty()) {
            return ResponseMessage.error("title不能为空");
        }
        if (body.content() == null || body.content().trim().isEmpty()) {
            return ResponseMessage.error("content不能为空");
        }

        Announcement a = new Announcement();
        a.setTitle(body.title().trim());
        a.setContent(body.content());
        a.setStatus(body.status() == null ? 1 : body.status());

        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        a.setCreatedAt(now);
        a.setUpdatedAt(now);
        if (a.getStatus() != null && a.getStatus() == 1) {
            a.setPublishTime(now);
        }

        if (body.publisherId() != null) {
            User publisher = userRepository.findById(body.publisherId()).orElse(null);
            a.setPublisher(publisher);
        }
        if (body.labId() != null) {
            Lab lab = labRepository.findById(body.labId()).orElse(null);
            a.setLab(lab);
        }

        Announcement saved = announcementRepository.save(a);
        return ResponseMessage.success(toDetailDto(saved));
    }

    /**
     * 编辑公告（Admin）
     * 约定：请求头携带 X-Role=Admin 或 X-Role=管理员
     */
    @PutMapping("/{id}")
    public ResponseMessage<AnnouncementDetailDto> updateAnnouncement(
            @RequestHeader(value = "X-Role", required = false) String role,
            @PathVariable("id") Long id,
            @RequestBody AnnouncementUpdateDto body
    ) {
        if (!isAdmin(role)) {
            return ResponseMessage.error("无权限（需要Admin）");
        }
        Optional<Announcement> opt = announcementRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseMessage.error("公告不存在");
        }

        Announcement a = opt.get();
        if (body != null) {
            if (body.title() != null && !body.title().trim().isEmpty()) {
                a.setTitle(body.title().trim());
            }
            if (body.content() != null && !body.content().trim().isEmpty()) {
                a.setContent(body.content());
            }
            if (body.status() != null) {
                a.setStatus(body.status());
                if (body.status() == 1 && a.getPublishTime() == null) {
                    a.setPublishTime(java.time.LocalDateTime.now());
                }
            }
            if (body.publisherId() != null) {
                User publisher = userRepository.findById(body.publisherId()).orElse(null);
                a.setPublisher(publisher);
            }
            if (body.labId() != null) {
                Lab lab = labRepository.findById(body.labId()).orElse(null);
                a.setLab(lab);
            }
        }

        a.setUpdatedAt(java.time.LocalDateTime.now());
        Announcement saved = announcementRepository.save(a);
        return ResponseMessage.success(toDetailDto(saved));
    }

    /**
     * 删除公告（Admin）
     * 约定：请求头携带 X-Role=Admin 或 X-Role=管理员
     */
    @DeleteMapping("/{id}")
    public ResponseMessage<Void> deleteAnnouncement(
            @RequestHeader(value = "X-Role", required = false) String role,
            @PathVariable("id") Long id
    ) {
        if (!isAdmin(role)) {
            return ResponseMessage.error("无权限（需要Admin）");
        }
        if (!announcementRepository.existsById(id)) {
            return ResponseMessage.error("公告不存在");
        }
        announcementRepository.deleteById(id);
        return ResponseMessage.success();
    }

    private boolean isAdmin(String role) {
        if (role == null) return false;
        String r = role.trim();
        return "Admin".equalsIgnoreCase(r) || "系统管理员".equals(r);
    }

    private AnnouncementListItemDto toListItemDto(Announcement a) {
        Integer publisherId = a.getPublisher() == null ? null : a.getPublisher().getUserId();
        Long labId = a.getLab() == null ? null : a.getLab().getId();
        return new AnnouncementListItemDto(
                a.getId(),
                a.getTitle(),
                a.getStatus(),
                publisherId,
                labId,
                a.getPublishTime(),
                a.getCreatedAt(),
                a.getUpdatedAt()
        );
    }

    private AnnouncementDetailDto toDetailDto(Announcement a) {
        Integer publisherId = a.getPublisher() == null ? null : a.getPublisher().getUserId();
        Long labId = a.getLab() == null ? null : a.getLab().getId();
        return new AnnouncementDetailDto(
                a.getId(),
                a.getTitle(),
                a.getContent(),
                a.getStatus(),
                publisherId,
                labId,
                a.getPublishTime(),
                a.getCreatedAt(),
                a.getUpdatedAt()
        );
    }

    public record AnnouncementCreateDto(
            String title,
            String content,
            Integer status,
            Integer publisherId,
            Long labId
    ) {}

    public record AnnouncementUpdateDto(
            String title,
            String content,
            Integer status,
            Integer publisherId,
            Long labId
    ) {}

    public record AnnouncementListItemDto(
            Long id,
            String title,
            Integer status,
            Integer publisherId,
            Long labId,
            java.time.LocalDateTime publishTime,
            java.time.LocalDateTime createdAt,
            java.time.LocalDateTime updatedAt
    ) {}

    public record AnnouncementDetailDto(
            Long id,
            String title,
            String content,
            Integer status,
            Integer publisherId,
            Long labId,
            java.time.LocalDateTime publishTime,
            java.time.LocalDateTime createdAt,
            java.time.LocalDateTime updatedAt
    ) {}
}

