package com.vdc.pdi.behaviorarchive.service.impl;

import com.vdc.pdi.behaviorarchive.domain.entity.ArchiveTimeline;
import com.vdc.pdi.behaviorarchive.domain.entity.BehaviorArchive;
import com.vdc.pdi.behaviorarchive.domain.repository.ArchiveTimelineRepository;
import com.vdc.pdi.behaviorarchive.domain.repository.BehaviorArchiveRepository;
import com.vdc.pdi.behaviorarchive.dto.response.TimelineItemDTO;
import com.vdc.pdi.behaviorarchive.exception.ArchiveException;
import com.vdc.pdi.behaviorarchive.service.ArchiveExportService;
import com.vdc.pdi.behaviorarchive.service.ArchiveTimelineService;
import com.vdc.pdi.behaviorarchive.service.StorageService;
import com.vdc.pdi.common.enums.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 档案导出服务实现
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ArchiveExportServiceImpl implements ArchiveExportService {

    private final BehaviorArchiveRepository archiveRepository;
    private final ArchiveTimelineRepository timelineRepository;
    private final ArchiveTimelineService timelineService;
    private final StorageService storageService;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HHmmss");
    private static final int BUFFER_SIZE = 8192;

    @Override
    public ResponseEntity<StreamingResponseBody> downloadImagePackage(Long archiveId, Long userSiteId) {
        // 查询档案
        BehaviorArchive archive = archiveRepository.findById(archiveId)
                .orElseThrow(() -> new ArchiveException("档案不存在"));

        // 权限校验
        if (userSiteId != null && !userSiteId.equals(archive.getSiteId())) {
            throw new ArchiveException(ResultCode.PERMISSION_DENIED, "无权访问该档案");
        }

        // 获取时间线
        List<ArchiveTimeline> timelines = timelineRepository.findByArchiveIdOrderBySeqAsc(archiveId);
        if (timelines.isEmpty()) {
            throw new ArchiveException(ResultCode.DATA_NOT_FOUND, "该档案没有可下载的图片");
        }

        // 构建文件名
        String zipFileName = buildZipFileName(archiveId);

        // 构建StreamingResponseBody
        StreamingResponseBody stream = outputStream -> {
            try (ZipOutputStream zipOut = new ZipOutputStream(outputStream)) {
                byte[] buffer = new byte[BUFFER_SIZE];

                for (int i = 0; i < timelines.size(); i++) {
                    ArchiveTimeline timeline = timelines.get(i);
                    if (timeline.getImageUrl() == null || timeline.getImageUrl().isEmpty()) {
                        continue;
                    }

                    // 检查图片是否存在
                    if (!storageService.exists(timeline.getImageUrl())) {
                        log.warn("图片不存在: {}", timeline.getImageUrl());
                        continue;
                    }

                    // 构建ZIP条目文件名
                    String entryFileName = buildEntryFileName(i + 1, timeline);

                    // 添加ZIP条目
                    ZipEntry zipEntry = new ZipEntry(entryFileName);
                    zipOut.putNextEntry(zipEntry);

                    // 下载图片并写入ZIP
                    try (InputStream imageStream = storageService.getObject(timeline.getImageUrl())) {
                        int len;
                        while ((len = imageStream.read(buffer)) > 0) {
                            zipOut.write(buffer, 0, len);
                        }
                    } catch (Exception e) {
                        log.error("下载图片失败: {}", timeline.getImageUrl(), e);
                    }

                    zipOut.closeEntry();
                }

                zipOut.finish();
            } catch (Exception e) {
                log.error("生成ZIP文件失败, archiveId={}", archiveId, e);
                throw new ArchiveException(ResultCode.INTERNAL_ERROR, "图片包生成失败");
            }
        };

        // 构建响应头
        String encodedFileName = URLEncoder.encode(zipFileName, StandardCharsets.UTF_8)
                .replace("+", "%20");

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename*=UTF-8''" + encodedFileName)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(stream);
    }

    @Override
    public String buildZipFileName(Long archiveId) {
        BehaviorArchive archive = archiveRepository.findById(archiveId)
                .orElseThrow(() -> new ArchiveException("档案不存在"));

        String siteCode = "SITE" + archive.getSiteId();
        String dateStr = archive.getStartTime() != null
                ? archive.getStartTime().format(DATE_FORMATTER)
                : "unknown";
        String timeStr = archive.getStartTime() != null
                ? archive.getStartTime().format(TIME_FORMATTER)
                : "000000";

        return String.format("archive_%s_%s_%s.zip", siteCode, dateStr, timeStr);
    }

    /**
     * 构建ZIP条目文件名
     */
    private String buildEntryFileName(int index, ArchiveTimeline timeline) {
        String timeStr = timeline.getEventTime() != null
                ? timeline.getEventTime().format(TIME_FORMATTER)
                : "000000";
        String action = timeline.getAction() != null
                ? sanitizeFileName(timeline.getAction())
                : "unknown";

        return String.format("%02d_%s_%s.jpg", index, timeStr, action);
    }

    /**
     * 清理文件名中的非法字符
     */
    private String sanitizeFileName(String input) {
        if (input == null) {
            return "unknown";
        }
        return input.replaceAll("[^a-zA-Z0-9\\u4e00-\\u9fa5]", "_");
    }
}
