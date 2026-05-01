package com.vdc.platform.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vdc.platform.entity.SysRole;
import com.vdc.platform.mapper.SysRoleMapper;
import com.vdc.platform.service.ISysRoleService;
import org.springframework.stereotype.Service;

@Service
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements ISysRoleService {
}
