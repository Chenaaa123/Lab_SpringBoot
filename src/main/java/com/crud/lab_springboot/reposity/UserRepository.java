package com.crud.lab_springboot.reposity;

import com.crud.lab_springboot.projo.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    /**
     * 根据账号查找用户
     */
    Optional<User> findByUserAccount(String userAccount);

    /**
     * 检查账号是否存在
     */
    boolean existsByUserAccount(String userAccount);

    /**
     * 根据角色分页查询用户
     */
    Page<User> findByRole(String role, Pageable pageable);

    /**
     * 根据关键字搜索用户（匹配用户名或账号）
     */
    @Query("SELECT u FROM User u WHERE u.userName LIKE %:keyword% OR u.userAccount LIKE %:keyword%")
    Page<User> findByKeyword(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 根据角色和关键字搜索用户
     */
    @Query("SELECT u FROM User u WHERE u.role = :role AND (u.userName LIKE %:keyword% OR u.userAccount LIKE %:keyword%)")
    Page<User> findByRoleAndKeyword(@Param("role") String role, @Param("keyword") String keyword, Pageable pageable);
}

