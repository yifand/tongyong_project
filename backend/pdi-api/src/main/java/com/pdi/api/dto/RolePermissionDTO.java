package com.pdi.api.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 角色权限DTO
 *
 * @author PDI Team
 * @version 1.0.0
 * @since 2026-03-27
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RolePermissionDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 权限ID列表
     */
    @NotNull(message = "权限ID列表不能为空")
    private List<Long> permIds;
}
