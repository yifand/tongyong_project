package com.vdc.platform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.vdc.platform.entity.SystemConfig;

public interface ISystemConfigService extends IService<SystemConfig> {

    SystemConfig getByKey(String configKey);
}
