package com.pdi.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pdi.dao.entity.SysPermission;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 系统权限Mapper接口
 *
 * @author PDI Team
 * @version 1.0.0
 * @since 2026-03-27
 */
public interface SysPermissionMapper extends BaseMapper<SysPermission> {

    /**
     * 根据角色ID查询权限列表
     *
     * @param roleId 角色ID
     * @return 权限列表
     */
    @Select("SELECT p.* FROM sys_permission p " +
            "INNER JOIN sys_role_permission rp ON p.id = rp.permission_id " +
            "WHERE rp.role_id = #{roleId} AND p.status = 1")
    List<SysPermission> selectByRoleId(@Param("roleId") Long roleId);
}
