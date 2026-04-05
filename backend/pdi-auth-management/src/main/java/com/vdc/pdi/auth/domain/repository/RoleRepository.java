package com.vdc.pdi.auth.domain.repository;

import com.vdc.pdi.auth.domain.entity.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 角色Repository接口
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long>, JpaSpecificationExecutor<Role> {

    /**
     * 根据角色编码查找角色
     */
    Optional<Role> findByRoleCode(String roleCode);

    /**
     * 根据角色编码查找未删除的角色
     */
    @Query("SELECT r FROM Role r WHERE r.roleCode = :roleCode AND r.deleted = false")
    Optional<Role> findByRoleCodeAndNotDeleted(@Param("roleCode") String roleCode);

    /**
     * 检查角色编码是否存在
     */
    boolean existsByRoleCode(String roleCode);

    /**
     * 根据ID列表查询角色
     */
    List<Role> findByIdInAndDeletedFalse(List<Long> ids);

    /**
     * 分页查询未删除的角色
     */
    Page<Role> findByDeletedFalse(Pageable pageable);

    /**
     * 根据状态查询角色
     */
    List<Role> findByStatusAndDeletedFalse(Integer status);

    /**
     * 根据用户ID查询角色列表
     */
    @Query("SELECT r FROM Role r JOIN UserRole ur ON r.id = ur.role.id WHERE ur.user.id = :userId AND r.deleted = false")
    List<Role> findRolesByUserId(@Param("userId") Long userId);

    /**
     * 逻辑删除角色
     */
    @Modifying
    @Query("UPDATE Role r SET r.deleted = true WHERE r.id = :roleId")
    void logicDelete(@Param("roleId") Long roleId);
}
