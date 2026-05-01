package com.vdc.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vdc.platform.entity.StateStream;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface StateStreamMapper extends BaseMapper<StateStream> {

    int batchInsert(@Param("list") List<StateStream> list);
}
