package com.vdc.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vdc.platform.entity.Alarm;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AlarmMapper extends BaseMapper<Alarm> {

    List<Alarm> selectAlarmPage(@Param("siteId") Long siteId,
                                @Param("alarmType") String alarmType,
                                @Param("processStatus") String processStatus);
}
