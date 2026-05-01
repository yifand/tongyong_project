package com.vdc.platform.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vdc.platform.entity.Site;
import com.vdc.platform.mapper.SiteMapper;
import com.vdc.platform.service.ISiteService;
import org.springframework.stereotype.Service;

@Service
public class SiteServiceImpl extends ServiceImpl<SiteMapper, Site> implements ISiteService {
}
