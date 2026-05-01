package com.vdc.platform.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vdc.platform.entity.OperationLog;
import com.vdc.platform.mapper.OperationLogMapper;
import com.vdc.platform.service.IOperationLogService;
import org.springframework.stereotype.Service;

@Service
public class OperationLogServiceImpl extends ServiceImpl<OperationLogMapper, OperationLog> implements IOperationLogService {
}
