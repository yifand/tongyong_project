package com.vdc.platform.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vdc.platform.entity.StateStream;
import com.vdc.platform.mapper.StateStreamMapper;
import com.vdc.platform.service.IStateStreamService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StateStreamServiceImpl extends ServiceImpl<StateStreamMapper, StateStream> implements IStateStreamService {

    @Override
    public boolean batchInsert(List<StateStream> list) {
        if (list == null || list.isEmpty()) {
            return false;
        }
        return baseMapper.batchInsert(list) > 0;
    }
}
