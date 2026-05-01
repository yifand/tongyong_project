package com.vdc.platform.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.vdc.platform.dto.AlarmPageQuery;
import com.vdc.platform.entity.Alarm;

import java.util.List;

public interface IAlarmService extends IService<Alarm> {

    List<Alarm> selectAlarmPage(Long siteId, String alarmType, String processStatus);

    IPage<Alarm> queryAlarmPage(AlarmPageQuery query);
}
