package com.vdc.platform.dto;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SysUserPageQuery extends Page<com.vdc.platform.entity.SysUser> {

    private String username;
    private Long siteId;
    private Integer status;
}
