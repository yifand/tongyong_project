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
    Optional<Role> findByCode(String code);

    /**
     * @deprecated 使用 {@link #findByCode(String)} 替代
     */
    @Deprecated
    default Optional<Role> findByRoleCode(String roleCode) {
        return findByCode(roleCode);
    }

    /**
     * 根据角色编码查找未删除的角色
     */
    @Query("SELECT r FROM Role r WHERE r.code = :code AND r.deletedAt IS NULL")
    Optional<Role> findByCodeAndNotDeleted(@Param("code") String code);

    /**
     * @deprecated 使用 {@link #findByCodeAndNotDeleted(String)} 替代
     */
    @Deprecated
    default Optional<Role> findByRoleCodeAndNotDeleted(@Param("roleCode") String roleCode) {
        return findByCodeAndNotDeleted(roleCode);
    }

    /**
     * 检查角色编码是否存在
     */
    boolean existsByCode(String code);

    /**
     * @deprecated 使用 {@link #existsByCode(String)} 替代
     */
    @Deprecated
    default boolean existsByRoleCode(String roleCode) {
        return existsByCode(roleCode);
    }

    /**
     * 根据ID列表查询角色
     */
    List<Role> findByIdInAndDeletedAtIsNull(List<Long> ids);

    /**
     * @deprecated 使用 {@link #findByIdInAndDeletedAtIsNull(List)} 替代
     */
    @Deprecated
    default List<Role> findByIdInAndDeletedFalse(List<Long> ids) {
        return findByIdInAndDeletedAtIsNull(ids);
    }

    /**
     * 分页查询未删除的角色
     */
    Page<Role> findByDeletedAtIsNull(Pageable pageable);

    /**
     * @deprecated 使用 {@link #findByDeletedAtIsNull(Pageable)} 替代
     */
    @Deprecated
    default Page<Role> findByDeletedFalse(Pageable pageable) {
        return findByDeletedAtIsNull(pageable);
    }

    /**
     * 根据状态查询角色
     */
    List<Role> findByStatusAndDeletedAtIsNull(Integer status);

    /**
     * @deprecated 使用 {@link #findByStatusAndDeletedAtIsNull(Integer)} 替代
     */
    @Deprecated
    default List<Role> findByStatusAndDeletedFalse(Integer status) {
        return findByStatusAndDeletedAtIsNull(status);
    }

    /**
     * 根据用户ID查询角色列表
     */
    @Query("SELECT r FROM Role r JOIN UserRole ur ON r.id = ur.role.id WHERE ur.user.id = :userId AND r.deletedAt IS NULL")
    List<Role> findRolesByUserId(@Param("userId") Long userId);

    /**
     * 逻辑删除角色
     */
    @Modifying
    @Query("UPDATE Role r SET r.deletedAt = CURRENT_TIMESTAMP WHERE r.id = :roleId")
    void logicDelete(@Param("roleId") Long roleId);
}
