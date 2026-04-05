package com.vdc.pdi.auth.controller;

import com.vdc.pdi.auth.dto.request.CreateUserRequest;
import com.vdc.pdi.auth.dto.request.UpdateUserRequest;
import com.vdc.pdi.auth.dto.response.UserResponse;
import com.vdc.pdi.auth.service.UserService;
import com.vdc.pdi.common.dto.ApiResponse;
import com.vdc.pdi.common.dto.PageResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户控制器
 */
@RestController
@RequestMapping("/api/v1/auth/users")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    /**
     * 分页查询用户
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER_MANAGER')")
    public ApiResponse<PageResponse<UserResponse>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        
        Sort sort = Sort.by(direction.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<UserResponse> userPage = userService.getUsers(pageable);
        
        return ApiResponse.success(PageResponse.of(userPage));
    }

    /**
     * 根据ID查询用户
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER_MANAGER') or @securityService.isCurrentUser(#id)")
    public ApiResponse<UserResponse> getUserById(@PathVariable Long id) {
        UserResponse user = userService.getUserById(id);
        return ApiResponse.success(user);
    }

    /**
     * 创建用户
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER_MANAGER')")
    public ApiResponse<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        UserResponse user = userService.createUser(request);
        return ApiResponse.success(user);
    }

    /**
     * 更新用户
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER_MANAGER')")
    public ApiResponse<UserResponse> updateUser(@PathVariable Long id,
                                                @Valid @RequestBody UpdateUserRequest request) {
        UserResponse user = userService.updateUser(id, request);
        return ApiResponse.success(user);
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ApiResponse.success(null);
    }

    /**
     * 批量删除用户
     */
    @DeleteMapping("/batch")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> batchDeleteUsers(@RequestBody List<Long> ids) {
        userService.batchDeleteUsers(ids);
        return ApiResponse.success(null);
    }

    /**
     * 更新用户状态
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> updateUserStatus(@PathVariable Long id,
                                              @RequestParam Integer status) {
        userService.updateUserStatus(id, status);
        return ApiResponse.success(null);
    }

    /**
     * 重置密码
     */
    @PutMapping("/{id}/password/reset")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> resetPassword(@PathVariable Long id,
                                           @RequestParam String newPassword) {
        userService.resetPassword(id, newPassword);
        return ApiResponse.success(null);
    }

    /**
     * 分配用户角色
     */
    @PutMapping("/{id}/roles")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER_MANAGER')")
    public ApiResponse<Void> assignRoles(@PathVariable Long id,
                                         @RequestBody List<Long> roleIds) {
        userService.assignRoles(id, roleIds);
        return ApiResponse.success(null);
    }

    /**
     * 获取用户角色
     */
    @GetMapping("/{id}/roles")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER_MANAGER')")
    public ApiResponse<List<Long>> getUserRoles(@PathVariable Long id) {
        List<Long> roleIds = userService.getUserRoleIds(id);
        return ApiResponse.success(roleIds);
    }
}
