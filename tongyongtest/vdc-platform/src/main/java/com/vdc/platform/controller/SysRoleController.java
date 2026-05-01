package com.vdc.platform.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vdc.platform.common.ApiResult;
import com.vdc.platform.dto.SysRoleRequest;
import com.vdc.platform.entity.OperationLog;
import com.vdc.platform.entity.SysRole;
import com.vdc.platform.security.model.SecurityUser;
import com.vdc.platform.service.IOperationLogService;
import com.vdc.platform.service.ISysRoleService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
public class SysRoleController {

    private final ISysRoleService sysRoleService;
    private final IOperationLogService operationLogService;

    private static final Set<String> BUILT_IN_ROLES = Set.of("SUPER_ADMIN", "SITE_ADMIN", "READONLY");

    @GetMapping
    @PreAuthorize("hasAuthority('role:read') or hasAuthority('admin')")
    public ApiResult<List<SysRole>> list() {
        return ApiResult.success(sysRoleService.list());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('role:read') or hasAuthority('admin')")
    public ApiResult<SysRole> getById(@PathVariable Long id) {
        return ApiResult.success(sysRoleService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('role:write') or hasAuthority('admin')")
    public ApiResult<Void> create(@Valid @RequestBody SysRoleRequest request, HttpServletRequest httpRequest) {
        SysRole role = new SysRole();
        role.setRoleCode(request.getRoleCode());
        role.setRoleName(request.getRoleName());
        role.setPermissions(request.getPermissions());
        role.setDataScope(request.getDataScope());
        sysRoleService.save(role);

        recordLog(getCurrentUser(), "CREATE_ROLE", "Created role: " + request.getRoleCode(), 1, httpRequest);
        return ApiResult.success();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('role:write') or hasAuthority('admin')")
    public ApiResult<Void> update(@PathVariable Long id, @Valid @RequestBody SysRoleRequest request, HttpServletRequest httpRequest) {
        SysRole existing = sysRoleService.getById(id);
        if (existing == null) {
            return ApiResult.error(404, "Role not found");
        }
        existing.setRoleCode(request.getRoleCode());
        existing.setRoleName(request.getRoleName());
        existing.setPermissions(request.getPermissions());
        existing.setDataScope(request.getDataScope());
        sysRoleService.updateById(existing);

        recordLog(getCurrentUser(), "UPDATE_ROLE", "Updated role: " + id, 1, httpRequest);
        return ApiResult.success();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('role:write') or hasAuthority('admin')")
    public ApiResult<Void> delete(@PathVariable Long id, HttpServletRequest httpRequest) {
        SysRole existing = sysRoleService.getById(id);
        if (existing != null && BUILT_IN_ROLES.contains(existing.getRoleCode())) {
            return ApiResult.error(403, "Built-in roles cannot be deleted");
        }
        sysRoleService.removeById(id);
        recordLog(getCurrentUser(), "DELETE_ROLE", "Deleted role: " + id, 1, httpRequest);
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
