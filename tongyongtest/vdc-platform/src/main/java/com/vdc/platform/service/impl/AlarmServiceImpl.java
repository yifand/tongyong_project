package com.vdc.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vdc.platform.dto.AlarmPageQuery;
import com.vdc.platform.entity.Alarm;
import com.vdc.platform.mapper.AlarmMapper;
import com.vdc.platform.service.IAlarmService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AlarmServiceImpl extends ServiceImpl<AlarmMapper, Alarm> implements IAlarmService {

    @Override
    public List<Alarm> selectAlarmPage(Long siteId, String alarmType, String processStatus) {
        return baseMapper.selectAlarmPage(siteId, alarmType, processStatus);
    }

    @Override
    public IPage<Alarm> queryAlarmPage(AlarmPageQuery query) {
        LambdaQueryWrapper<Alarm> wrapper = new LambdaQueryWrapper<>();
        if (query.getAlarmType() != null && !query.getAlarmType().isEmpty()) {
            wrapper.eq(Alarm::getAlarmType, query.getAlarmType());
        }
        if (query.getSiteId() != null) {
            wrapper.eq(Alarm::getSiteId, query.getSiteId());
        }
        if (query.getChannelId() != null) {
            wrapper.eq(Alarm::getChannelId, query.getChannelId());
        }
        if (query.getProcessStatus() != null && !query.getProcessStatus().isEmpty()) {
            wrapper.eq(Alarm::getProcessStatus, query.getProcessStatus());
        }
        if (query.getStartTime() != null) {
            wrapper.ge(Alarm::getAlarmTime, query.getStartTime());
        }
        if (query.getEndTime() != null) {
            wrapper.le(Alarm::getAlarmTime, query.getEndTime());
        }
        wrapper.orderByDesc(Alarm::getAlarmTime);
        return baseMapper.selectPage(query, wrapper);
    }
}
