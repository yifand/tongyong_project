package com.vdc.platform.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vdc.platform.common.ApiResult;
import com.vdc.platform.dto.SysUserPageQuery;
import com.vdc.platform.dto.SysUserRequest;
import com.vdc.platform.entity.OperationLog;
import com.vdc.platform.entity.SysUser;
import com.vdc.platform.security.model.SecurityUser;
import com.vdc.platform.service.IOperationLogService;
import com.vdc.platform.service.ISysUserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class SysUserController {

    private final ISysUserService sysUserService;
    private final IOperationLogService operationLogService;
    private final PasswordEncoder passwordEncoder;

    @GetMapping
    @PreAuthorize("hasAuthority('user:read') or hasAuthority('admin')")
    public ApiResult<com.baomidou.mybatisplus.extension.plugins.pagination.Page<SysUser>> list(SysUserPageQuery query) {
        SecurityUser currentUser = getCurrentUser();
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        if (query.getUsername() != null && !query.getUsername().isEmpty()) {
            wrapper.like(SysUser::getUsername, query.getUsername());
        }
        if (query.getSiteId() != null) {
            wrapper.eq(SysUser::getSiteId, query.getSiteId());
        } else if (!"SUPER_ADMIN".equals(currentUser.getRoleCode()) && currentUser.getSiteId() != null) {
            wrapper.eq(SysUser::getSiteId, currentUser.getSiteId());
        }
        if (query.getStatus() != null) {
            wrapper.eq(SysUser::getStatus, query.getStatus());
        }
        return ApiResult.success(sysUserService.page(query, wrapper));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('user:read') or hasAuthority('admin')")
    public ApiResult<SysUser> getById(@PathVariable Long id) {
        return ApiResult.success(sysUserService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('user:write') or hasAuthority('admin')")
    public ApiResult<Void> create(@Valid @RequestBody SysUserRequest request, HttpServletRequest httpRequest) {
        SecurityUser currentUser = getCurrentUser();
        if (!"SUPER_ADMIN".equals(currentUser.getRoleCode()) && request.getSiteId() == null) {
            return ApiResult.error(403, "Only SUPER_ADMIN can create cross-site users");
        }
        if (!"SUPER_ADMIN".equals(currentUser.getRoleCode()) && request.getSiteId() != null
                && !request.getSiteId().equals(currentUser.getSiteId())) {
            return ApiResult.error(403, "Cannot create user for another site");
        }

        SysUser user = new SysUser();
        user.setUsername(request.getUsername());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRealName(request.getRealName());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setRoleId(request.getRoleId());
        user.setSiteId(request.getSiteId());
        user.setStatus(request.getStatus());
        sysUserService.save(user);

        recordLog(currentUser, "CREATE_USER", "Created user: " + request.getUsername(), 1, httpRequest);
        return ApiResult.success();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('user:write') or hasAuthority('admin')")
    public ApiResult<Void> update(@PathVariable Long id, @Valid @RequestBody SysUserRequest request, HttpServletRequest httpRequest) {
        SecurityUser currentUser = getCurrentUser();
        SysUser existing = sysUserService.getById(id);
        if (existing == null) {
            return ApiResult.error(404, "User not found");
        }
        if (!"SUPER_ADMIN".equals(currentUser.getRoleCode()) && request.getSiteId() == null) {
            return ApiResult.error(403, "Only SUPER_ADMIN can set site to null");
        }
        if (!"SUPER_ADMIN".equals(currentUser.getRoleCode()) && request.getSiteId() != null
                && !request.getSiteId().equals(currentUser.getSiteId())) {
            return ApiResult.error(403, "Cannot assign user to another site");
        }

        existing.setUsername(request.getUsername());
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            existing.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }
        existing.setRealName(request.getRealName());
        existing.setPhone(request.getPhone());
        existing.setEmail(request.getEmail());
        existing.setRoleId(request.getRoleId());
        existing.setSiteId(request.getSiteId());
        existing.setStatus(request.getStatus());
        sysUserService.updateById(existing);

        recordLog(currentUser, "UPDATE_USER", "Updated user: " + id, 1, httpRequest);
        return ApiResult.success();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('user:write') or hasAuthority('admin')")
    public ApiResult<Void> delete(@PathVariable Long id, HttpServletRequest httpRequest) {
        SecurityUser currentUser = getCurrentUser();
        sysUserService.removeById(id);
        recordLog(currentUser, "DELETE_USER", "Deleted user: " + id, 1, httpRequest);
        return ApiResult.success();
    }

    private SecurityUser getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return (SecurityUser) principal;
    }

    private void recordLog(SecurityUser user, String type, String content, int result, HttpServletRequest request) {
        OperationLog log = new OperationLog();
        log.setUserId(user.getUserId());
        log.setUsername(user.getUsername());
        log.setIpAddress(getClientIp(request));
        log.setOperationType(type);
        log.setOperationContent(content);
        log.setResult(result);
        log.setCreatedAt(LocalDateTime.now());
        operationLogService.save(log);
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }
        return ip.split(",")[0].trim();
    }
}
