package com.vdc.platform.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vdc.platform.entity.SystemConfig;
import com.vdc.platform.mapper.SystemConfigMapper;
import com.vdc.platform.service.ISystemConfigService;
import org.springframework.stereotype.Service;

@Service
public class SystemConfigServiceImpl extends ServiceImpl<SystemConfigMapper, SystemConfig> implements ISystemConfigService {

    @Override
    public SystemConfig getByKey(String configKey) {
        return lambdaQuery().eq(SystemConfig::getConfigKey, configKey).one();
    }
}
