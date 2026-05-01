package com.vdc.platform.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vdc.platform.entity.RuleConfig;
import com.vdc.platform.mapper.RuleConfigMapper;
import com.vdc.platform.service.IRuleConfigService;
import org.springframework.stereotype.Service;

@Service
public class RuleConfigServiceImpl extends ServiceImpl<RuleConfigMapper, RuleConfig> implements IRuleConfigService {
}
