package com.vdc.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vdc.platform.entity.EdgeBox;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface EdgeBoxMapper extends BaseMapper<EdgeBox> {

    @Select("SELECT site_id, status, COUNT(*) as cnt FROM edge_box GROUP BY site_id, status")
    List<Map<String, Object>> selectBoxStats();

    @Select("SELECT c.site_id, ch.status, COUNT(*) as cnt FROM channel ch " +
            "JOIN edge_box c ON ch.box_id = c.id " +
            "GROUP BY c.site_id, ch.status")
    List<Map<String, Object>> selectChannelStats();
}

