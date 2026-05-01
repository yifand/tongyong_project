package com.vdc.platform.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vdc.platform.entity.Channel;
import com.vdc.platform.mapper.ChannelMapper;
import com.vdc.platform.service.IChannelService;
import org.springframework.stereotype.Service;

@Service
public class ChannelServiceImpl extends ServiceImpl<ChannelMapper, Channel> implements IChannelService {
}
