package com.vdc.pdi.auth.domain.repository;

import com.vdc.pdi.auth.domain.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 用户Repository接口
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    /**
     * 根据用户名查找用户
     */
    Optional<User> findByUsername(String username);

    /**
     * 根据用户名查找未删除的用户
     */
    @Query("SELECT u FROM User u WHERE u.username = :username AND u.deleted = false")
    Optional<User> findByUsernameAndNotDeleted(@Param("username") String username);

    /**
     * 检查用户名是否存在
     */
    boolean existsByUsername(String username);

    /**
     * 检查邮箱是否存在
     */
    boolean existsByEmail(String email);

    /**
     * 更新登录失败次数
     */
    @Modifying
    @Query("UPDATE User u SET u.loginFailCount = :count WHERE u.id = :userId")
    void updateLoginFailCount(@Param("userId") Long userId, @Param("count") Integer count);

    /**
     * 更新登录锁定时间
     */
    @Modifying
    @Query("UPDATE User u SET u.loginLockTime = :lockTime WHERE u.id = :userId")
    void updateLoginLockTime(@Param("userId") Long userId, @Param("lockTime") LocalDateTime lockTime);

    /**
     * 更新最后登录信息
     */
    @Modifying
    @Query("UPDATE User u SET u.lastLoginTime = :loginTime, u.lastLoginIp = :loginIp, u.loginFailCount = 0 WHERE u.id = :userId")
    void updateLastLoginInfo(@Param("userId") Long userId, @Param("loginTime") LocalDateTime loginTime, @Param("loginIp") String loginIp);

    /**
     * 分页查询未删除的用户
     */
    Page<User> findByDeletedFalse(Pageable pageable);

    /**
     * 根据部门ID查询用户
     */
    Page<User> findByDeptIdAndDeletedFalse(Long deptId, Pageable pageable);
}
