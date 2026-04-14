package com.crud.lab_springboot.reposity;

import com.crud.lab_springboot.projo.Repair;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface RepairRepository extends JpaRepository<Repair, Long>, JpaSpecificationExecutor<Repair> {

    Page<Repair> findByUserUserId(Integer userId, Pageable pageable);

    Page<Repair> findByLab_Id(Long labId, Pageable pageable);

    Page<Repair> findByStatus(Integer status, Pageable pageable);
}
