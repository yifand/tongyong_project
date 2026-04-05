package com.vdc.pdi.behaviorarchive.service;

import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

/**
 * 档案导出服务接口
 */
public interface ArchiveExportService {

    /**
     * 下载图片包
     *
     * @param archiveId 档案ID
     * @param userSiteId 用户站点ID（用于权限校验）
     * @return 响应实体（包含ZIP流）
     */
    ResponseEntity<StreamingResponseBody> downloadImagePackage(Long archiveId, Long userSiteId);

    /**
     * 构建ZIP文件名
     *
     * @param archiveId 档案ID
     * @return ZIP文件名
     */
    String buildZipFileName(Long archiveId);
}
