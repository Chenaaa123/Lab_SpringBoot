package com.crud.lab_springboot.reposity;

import com.crud.lab_springboot.projo.Maintenance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MaintenanceRepository extends JpaRepository<Maintenance, Long>, JpaSpecificationExecutor<Maintenance> {

    Page<Maintenance> findByLab_Id(Long labId, Pageable pageable);

    /**
     * 根据实验室管理员ID查询检修记录（实验室管理员查看所管辖实验室的）
     */
    @Query("SELECT m FROM Maintenance m WHERE m.lab.manager.userId = :managerId")
    Page<Maintenance> findByLabManagerId(@Param("managerId") Integer managerId, Pageable pageable);
}
