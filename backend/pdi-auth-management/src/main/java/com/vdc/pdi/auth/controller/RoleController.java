package com.vdc.pdi.auth.controller;

import com.vdc.pdi.auth.domain.entity.Role;
import com.vdc.pdi.auth.dto.request.CreateRoleRequest;
import com.vdc.pdi.auth.dto.request.UpdateRoleRequest;
import com.vdc.pdi.auth.dto.response.RoleResponse;
import com.vdc.pdi.auth.service.RoleService;
import com.vdc.pdi.common.dto.ApiResponse;
import com.vdc.pdi.common.dto.PageResponse;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 角色控制器
 */
@RestController
@RequestMapping("/api/v1/auth/roles")
public class RoleController {

    @Autowired
    private RoleService roleService;

    /**
     * 分页查询角色
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('ROLE_MANAGER')")
    public ApiResponse<PageResponse<RoleResponse>> getRoles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "sortOrder") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {

        Sort sort = Sort.by(direction.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Role> rolePage = roleService.getRoles(pageable);

        // 转换为DTO
        Page<RoleResponse> responsePage = rolePage.map(this::convertToResponse);
        return ApiResponse.success(PageResponse.of(responsePage));
    }

    /**
     * 查询所有有效角色
     */
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ROLE_MANAGER') or hasRole('USER_MANAGER')")
    public ApiResponse<List<RoleResponse>> getAllActiveRoles() {
        List<Role> roles = roleService.getAllActiveRoles();
        List<RoleResponse> responses = roles.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        return ApiResponse.success(responses);
    }

    /**
     * 根据ID查询角色
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ROLE_MANAGER')")
    public ApiResponse<RoleResponse> getRoleById(@PathVariable Long id) {
        Role role = roleService.getRoleById(id);
        return ApiResponse.success(convertToResponse(role));
    }

    /**
     * 根据角色编码查询角色
     */
    @GetMapping("/code/{roleCode}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ROLE_MANAGER')")
    public ApiResponse<RoleResponse> getRoleByCode(@PathVariable String roleCode) {
        Role role = roleService.getRoleByCode(roleCode);
        return ApiResponse.success(convertToResponse(role));
    }

    /**
     * 创建角色
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<RoleResponse> createRole(@RequestBody CreateRoleRequest request) {
        // 转换为实体
        Role role = new Role();
        BeanUtils.copyProperties(request, role);

        Role createdRole = roleService.createRole(role);
        return ApiResponse.success(convertToResponse(createdRole));
    }

    /**
     * 更新角色
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<RoleResponse> updateRole(@PathVariable Long id,
                                                @RequestBody UpdateRoleRequest request) {
        // 转换为实体
        Role role = new Role();
        BeanUtils.copyProperties(request, role);

        Role updatedRole = roleService.updateRole(id, role);
        return ApiResponse.success(convertToResponse(updatedRole));
    }

    /**
     * 删除角色
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deleteRole(@PathVariable Long id) {
        roleService.deleteRole(id);
        return ApiResponse.success(null);
    }

    /**
     * 批量删除角色
     */
    @DeleteMapping("/batch")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> batchDeleteRoles(@RequestBody List<Long> ids) {
        roleService.batchDeleteRoles(ids);
        return ApiResponse.success(null);
    }

    /**
     * 更新角色状态
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> updateRoleStatus(@PathVariable Long id,
                                              @RequestParam Integer status) {
        roleService.updateRoleStatus(id, status);
        return ApiResponse.success(null);
    }

    /**
     * 获取用户的角色列表
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ROLE_MANAGER') or hasRole('USER_MANAGER')")
    public ApiResponse<List<RoleResponse>> getRolesByUserId(@PathVariable Long userId) {
        List<Role> roles = roleService.getRolesByUserId(userId);
        List<RoleResponse> responses = roles.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        return ApiResponse.success(responses);
    }

    /**
     * 转换为响应DTO
     */
    private RoleResponse convertToResponse(Role role) {
        RoleResponse response = new RoleResponse();
        BeanUtils.copyProperties(role, response);
        return response;
    }
}
