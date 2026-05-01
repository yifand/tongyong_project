package com.vdc.platform.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vdc.platform.entity.EdgeBox;
import com.vdc.platform.mapper.EdgeBoxMapper;
import com.vdc.platform.service.IEdgeBoxService;
import org.springframework.stereotype.Service;

@Service
public class EdgeBoxServiceImpl extends ServiceImpl<EdgeBoxMapper, EdgeBox> implements IEdgeBoxService {
}
