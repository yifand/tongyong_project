package com.pdi.service.device;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pdi.common.enums.StatusEnum;
import com.pdi.common.exception.BusinessException;
import com.pdi.common.result.PageResult;
import com.pdi.common.result.ResultCode;
import com.pdi.dao.entity.Site;
import com.pdi.dao.mapper.SiteMapper;
import com.pdi.service.device.dto.SiteDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 监测点位服务实现
 *
 * @author PDI Team
 * @version 1.0.0
 * @since 2026-03-27
 */
@Slf4j
@Service
public class SiteServiceImpl extends ServiceImpl<SiteMapper, Site> implements SiteService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SiteDTO createSite(SiteDTO dto) {
        // 检查编码唯一性
        if (lambdaQuery().eq(Site::getSiteCode, dto.getSiteCode()).exists()) {
            throw new BusinessException("点位编码已存在");
        }

        Site site = new Site();
        BeanUtils.copyProperties(dto, site);
        site.setCreatedAt(LocalDateTime.now());
        site.setUpdatedAt(LocalDateTime.now());

        save(site);

        SiteDTO result = new SiteDTO();
        BeanUtils.copyProperties(site, result);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSite(Long siteId, SiteDTO dto) {
        Site site = getById(siteId);
        if (site == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "点位不存在");
        }

        // 检查编码唯一性(排除自身)
        if (StringUtils.hasText(dto.getSiteCode()) && 
            !dto.getSiteCode().equals(site.getSiteCode())) {
            if (lambdaQuery().eq(Site::getSiteCode, dto.getSiteCode()).exists()) {
                throw new BusinessException("点位编码已存在");
            }
        }

        BeanUtils.copyProperties(dto, site, "id", "createdAt", "createdBy");
        site.setId(siteId);
        site.setUpdatedAt(LocalDateTime.now());

        updateById(site);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSite(Long siteId) {
        Site site = getById(siteId);
        if (site == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "点位不存在");
        }

        removeById(siteId);
        log.info("删除点位: siteId={}", siteId);
    }

    @Override
    public SiteDTO getSite(Long siteId) {
        Site site = getById(siteId);
        if (site == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "点位不存在");
        }

        SiteDTO dto = new SiteDTO();
        BeanUtils.copyProperties(site, dto);
        return dto;
    }

    @Override
    public List<SiteDTO> listAllSites() {
        List<Site> sites = lambdaQuery()
                .eq(Site::getStatus, StatusEnum.ENABLED.getCode())
                .orderByAsc(Site::getSiteCode)
                .list();

        return sites.stream().map(site -> {
            SiteDTO dto = new SiteDTO();
            BeanUtils.copyProperties(site, dto);
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public PageResult<SiteDTO> listSites(Long page, Long size) {
        Page<Site> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Site> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Site::getCreatedAt);

        Page<Site> pageResult = page(pageParam, wrapper);

        List<SiteDTO> list = pageResult.getRecords().stream().map(site -> {
            SiteDTO dto = new SiteDTO();
            BeanUtils.copyProperties(site, dto);
            return dto;
        }).collect(Collectors.toList());

        return PageResult.of(list, pageResult.getTotal(), page, size);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSiteStatus(Long siteId, Integer status) {
        Site site = getById(siteId);
        if (site == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "点位不存在");
        }

        site.setStatus(status);
        site.setUpdatedAt(LocalDateTime.now());
        updateById(site);

        log.info("更新点位状态: siteId={}, status={}", siteId, status);
    }

}
