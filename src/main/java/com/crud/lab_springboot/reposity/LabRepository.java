package com.crud.lab_springboot.reposity;

import com.crud.lab_springboot.projo.Lab;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LabRepository extends JpaRepository<Lab, Long>, JpaSpecificationExecutor<Lab> {

    /**
     * 根据实验室编号查询实验室
     * @param code 实验室编号
     * @return 实验室信息
     */
    Optional<Lab> findByCode(String code);

    /**
     * 根据实验室管理员用户 ID（manager_id）查询实验室列表
     */
    List<Lab> findByManagerUserId(Integer managerId);

    /**
     * 根据分类ID查询实验室数量
     */
    long countByCategoryId(Long categoryId);

    /**
     * 某分类下的全部实验室（用于分类管理员变更时同步 lab.manager_id）
     */
    List<Lab> findByCategory_Id(Long categoryId);

    /**
     * 根据ID查询实验室并加悲观写锁，用于创建预约时的并发控制
     * 同一实验室的并发创建请求将串行化，避免重复预约
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT l FROM Lab l WHERE l.id = :id")
    Optional<Lab> findByIdWithLock(@Param("id") Long id);
}

