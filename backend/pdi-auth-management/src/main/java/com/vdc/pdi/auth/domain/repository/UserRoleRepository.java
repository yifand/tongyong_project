package com.vdc.pdi.auth.domain.repository;

import com.vdc.pdi.auth.domain.entity.Role;
import com.vdc.pdi.auth.domain.entity.User;
import com.vdc.pdi.auth.domain.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 用户角色关联Repository接口
 */
@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, Long> {

    /**
     * 根据用户ID查询用户角色关联
     */
    List<UserRole> findByUserId(Long userId);

    /**
     * 根据角色ID查询用户角色关联
     */
    List<UserRole> findByRoleId(Long roleId);

    /**
     * 根据用户ID和角色ID查询
     */
    UserRole findByUserIdAndRoleId(Long userId, Long roleId);

    /**
     * 删除用户的所有角色关联
     */
    @Modifying
    @Query("DELETE FROM UserRole ur WHERE ur.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);

    /**
     * 删除角色的所有用户关联
     */
    @Modifying
    @Query("DELETE FROM UserRole ur WHERE ur.role.id = :roleId")
    void deleteByRoleId(@Param("roleId") Long roleId);

    /**
     * 批量插入用户角色关联
     */
    @Modifying
    @Query(value = "INSERT INTO sys_user_role (user_id, role_id, create_time) VALUES (:userId, :roleId, NOW())", nativeQuery = true)
    void insertUserRole(@Param("userId") Long userId, @Param("roleId") Long roleId);

    /**
     * 根据用户ID查询角色列表
     */
    @Query("SELECT ur.role FROM UserRole ur WHERE ur.user.id = :userId")
    List<Role> findRolesByUserId(@Param("userId") Long userId);

    /**
     * 根据角色ID查询用户列表
     */
    @Query("SELECT ur.user FROM UserRole ur WHERE ur.role.id = :roleId")
    List<User> findUsersByRoleId(@Param("roleId") Long roleId);

    /**
     * 检查用户是否有指定角色
     */
    boolean existsByUserIdAndRoleId(Long userId, Long roleId);
}
