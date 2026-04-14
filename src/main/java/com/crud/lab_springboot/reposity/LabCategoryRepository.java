package com.crud.lab_springboot.reposity;

import com.crud.lab_springboot.projo.LabCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LabCategoryRepository extends JpaRepository<LabCategory, Long> {
    
    /**
     * 根据实验室管理员id查找实验室分类
     * @param managerId 实验室管理员id
     * @return 实验室分类信息
     */
    @Query("SELECT l FROM LabCategory l WHERE l.admin.userId = :managerId")
    List<LabCategory> findByManagerId(@Param("managerId") Integer managerId);
}

