package com.crud.lab_springboot.reposity;

import com.crud.lab_springboot.projo.Reservation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long>, JpaSpecificationExecutor<Reservation> {

    /**
     * 根据预约单号查询
     */
    Optional<Reservation> findByOrderNo(String orderNo);

    /**
     * 分页查询用户的预约记录
     */
    Page<Reservation> findByUserUserId(Integer userId, Pageable pageable);

    /**
     * 分页查询实验室的预约记录
     */
    Page<Reservation> findByLabId(Long labId, Pageable pageable);

    /**
     * 分页查询特定状态的预约记录
     */
    Page<Reservation> findByStatus(Integer status, Pageable pageable);

    /**
     * 分页查询用户的特定状态预约记录
     */
    Page<Reservation> findByUserUserIdAndStatus(Integer userId, Integer status, Pageable pageable);

    /**
     * 根据关键字搜索预约记录（匹配预约单号或预约用途）
     */
    @Query("SELECT r FROM Reservation r WHERE r.orderNo LIKE %:keyword% OR r.purpose LIKE %:keyword%")
    Page<Reservation> findByKeyword(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 统计用户的预约数量
     */
    long countByUserUserId(Integer userId);

    /**
     * 统计实验室的预约数量
     */
    long countByLabId(Long labId);

    /**
     * 检查某实验室在时间段内是否存在已占用预约（待审核或已通过，且未过期）
     * 重叠条件：已有预约的 start_time < 传入的 endTime 且 已有预约的 end_time > 传入的 startTime
     * 且已有预约的 end_time > now（已过期的预约不占用，实验室自动变空闲）
     */
    @Query("SELECT r FROM Reservation r WHERE r.lab.id = :labId AND r.status IN (0, 1) " +
           "AND r.startTime < :endTime AND r.endTime > :startTime AND r.endTime > :now")
    List<Reservation> findOverlappingByLab(
            @Param("labId") Long labId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("now") LocalDateTime now);

    /**
     * 检查某用户对某实验室在时间段内是否已有预约（待审核或已通过，且未过期）
     */
    @Query("SELECT r FROM Reservation r WHERE r.user.userId = :userId AND r.lab.id = :labId AND r.status IN (0, 1) " +
           "AND r.startTime < :endTime AND r.endTime > :startTime AND r.endTime > :now")
    List<Reservation> findOverlappingByUserAndLab(
            @Param("userId") Integer userId,
            @Param("labId") Long labId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("now") LocalDateTime now);
}
