package com.vdc.pdi.behaviorarchive.service;

import com.vdc.pdi.behaviorarchive.domain.entity.ArchiveTimeline;
import com.vdc.pdi.behaviorarchive.domain.entity.BehaviorArchive;
import com.vdc.pdi.behaviorarchive.domain.repository.ArchiveTimelineRepository;
import com.vdc.pdi.behaviorarchive.domain.repository.BehaviorArchiveRepository;
import com.vdc.pdi.behaviorarchive.exception.ArchiveException;
import com.vdc.pdi.behaviorarchive.service.impl.ArchiveExportServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

/**
 * 档案导出服务测试
 * 使用Mockito进行Service层单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("档案导出服务测试")
class ArchiveExportServiceTest {

    @Mock
    private BehaviorArchiveRepository archiveRepository;

    @Mock
    private ArchiveTimelineRepository timelineRepository;

    @Mock
    private ArchiveTimelineService timelineService;

    @Mock
    private StorageService storageService;

    @InjectMocks
    private ArchiveExportServiceImpl exportService;

    private BehaviorArchive createArchive(Long id, Long siteId, LocalDateTime startTime) {
        BehaviorArchive archive = new BehaviorArchive();
        archive.setId(id);
        archive.setPdiTaskId(id + 1000);
        archive.setChannelId(101L);
        archive.setSiteId(siteId);
        archive.setStartTime(startTime);
        archive.setEndTime(startTime != null ? startTime.plusMinutes(10) : null);
        archive.setEstimatedDuration(10);
        archive.setActualDuration(600);
        archive.setStatus(1);
        return archive;
    }

    private ArchiveTimeline createTimelineNode(Long id, Long archiveId, Integer seq,
                                                LocalDateTime eventTime, String action, String imageUrl) {
        ArchiveTimeline node = new ArchiveTimeline();
        node.setId(id);
        node.setArchiveId(archiveId);
        node.setSeq(seq);
        node.setEventTime(eventTime);
        node.setAction(action);
        node.setImageUrl(imageUrl);
        return node;
    }

    // ==================== buildZipFileName() 测试 ====================

    @Test
    @DisplayName("构建ZIP文件名 - 文件名格式正确")
    void buildZipFileName_Success() {
        // Given
        Long archiveId = 1001L;
        LocalDateTime startTime = LocalDateTime.of(2026, 3, 25, 10, 0, 0);
        BehaviorArchive archive = createArchive(archiveId, 1L, startTime);

        when(archiveRepository.findById(archiveId)).thenReturn(Optional.of(archive));

        // When
        String fileName = exportService.buildZipFileName(archiveId);

        // Then
        assertThat(fileName)
                .matches("archive_SITE\\d+_\\d{8}_\\d{6}\\.zip")
                .contains("SITE1")
                .contains("20260325")
                .contains("100000");
    }

    @Test
    @DisplayName("构建ZIP文件名 - 档案不存在")
    void buildZipFileName_ArchiveNotFound() {
        // Given
        Long archiveId = 999L;
        when(archiveRepository.findById(archiveId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> exportService.buildZipFileName(archiveId))
                .isInstanceOf(ArchiveException.class)
                .hasMessageContaining("档案不存在");
    }

    @Test
    @DisplayName("构建ZIP文件名 - 不同站点ID")
    void buildZipFileName_DifferentSiteId() {
        // Given
        Long archiveId = 1001L;
        BehaviorArchive archive = createArchive(archiveId, 2L,
                LocalDateTime.of(2026, 3, 25, 10, 0, 0));

        when(archiveRepository.findById(archiveId)).thenReturn(Optional.of(archive));

        // When
        String fileName = exportService.buildZipFileName(archiveId);

        // Then
        assertThat(fileName).contains("SITE2");
    }

    // ==================== downloadImagePackage() 测试 ====================

    @Test
    @DisplayName("下载图片包 - 档案无图片时抛出异常")
    void downloadImagePackage_NoImages() {
        // Given
        Long archiveId = 1001L;
        BehaviorArchive archive = createArchive(archiveId, 1L,
                LocalDateTime.of(2026, 3, 25, 10, 0, 0));

        when(archiveRepository.findById(archiveId)).thenReturn(Optional.of(archive));
        when(timelineRepository.findByArchiveIdOrderBySeqAsc(archiveId))
                .thenReturn(List.of());

        // When & Then
        assertThatThrownBy(() -> exportService.downloadImagePackage(archiveId, null))
                .isInstanceOf(ArchiveException.class)
                .hasMessageContaining("没有可下载的图片");
    }

    @Test
    @DisplayName("下载图片包 - 正常下载")
    void downloadImagePackage_Success() throws Exception {
        // Given
        Long archiveId = 1001L;
        LocalDateTime startTime = LocalDateTime.of(2026, 3, 25, 10, 0, 0);
        BehaviorArchive archive = createArchive(archiveId, 1L, startTime);

        ArchiveTimeline node1 = createTimelineNode(1L, archiveId, 1, startTime,
                "进入", "http://minio/enter.jpg");
        ArchiveTimeline node2 = createTimelineNode(2L, archiveId, 2,
                startTime.plusMinutes(5), "检查", "http://minio/check.jpg");

        when(archiveRepository.findById(archiveId)).thenReturn(Optional.of(archive));
        when(timelineRepository.findByArchiveIdOrderBySeqAsc(archiveId))
                .thenReturn(List.of(node1, node2));
        when(storageService.exists(any())).thenReturn(true);
        when(storageService.getObject(any()))
                .thenReturn(new java.io.ByteArrayInputStream("image data".getBytes()));

        // When
        ResponseEntity<StreamingResponseBody> response =
                exportService.downloadImagePackage(archiveId, null);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getHeaders().getFirst("Content-Disposition"))
                .contains("attachment");

        // 验证StreamingResponseBody可以正常执行
        StreamingResponseBody body = response.getBody();
        assertThat(body).isNotNull();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        body.writeTo(outputStream);
        byte[] result = outputStream.toByteArray();
        assertThat(result).isNotEmpty();
    }

    @Test
    @DisplayName("下载图片包 - 档案不存在")
    void downloadImagePackage_ArchiveNotFound() {
        // Given
        Long archiveId = 999L;
        when(archiveRepository.findById(archiveId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> exportService.downloadImagePackage(archiveId, null))
                .isInstanceOf(ArchiveException.class)
                .hasMessageContaining("档案不存在");
    }

    @Test
    @DisplayName("下载图片包 - 无权访问")
    void downloadImagePackage_NoPermission() {
        // Given
        Long archiveId = 1001L;
        BehaviorArchive archive = createArchive(archiveId, 2L,
                LocalDateTime.of(2026, 3, 25, 10, 0, 0));

        when(archiveRepository.findById(archiveId)).thenReturn(Optional.of(archive));

        // When & Then - 用户站点ID为1，但档案站点ID为2
        assertThatThrownBy(() -> exportService.downloadImagePackage(archiveId, 1L))
                .isInstanceOf(ArchiveException.class)
                .hasMessageContaining("无权访问该档案");
    }

    @Test
    @DisplayName("下载图片包 - 部分图片不存在")
    void downloadImagePackage_PartialImagesMissing() throws Exception {
        // Given
        Long archiveId = 1001L;
        LocalDateTime startTime = LocalDateTime.of(2026, 3, 25, 10, 0, 0);
        BehaviorArchive archive = createArchive(archiveId, 1L, startTime);

        ArchiveTimeline node1 = createTimelineNode(1L, archiveId, 1, startTime,
                "进入", "http://minio/enter.jpg");
        ArchiveTimeline node2 = createTimelineNode(2L, archiveId, 2,
                startTime.plusMinutes(5), "检查", "http://minio/check.jpg");

        when(archiveRepository.findById(archiveId)).thenReturn(Optional.of(archive));
        when(timelineRepository.findByArchiveIdOrderBySeqAsc(archiveId))
                .thenReturn(List.of(node1, node2));
        // 第一个图片存在，第二个不存在
        when(storageService.exists("http://minio/enter.jpg")).thenReturn(true);
        when(storageService.exists("http://minio/check.jpg")).thenReturn(false);
        when(storageService.getObject("http://minio/enter.jpg"))
                .thenReturn(new java.io.ByteArrayInputStream("image data".getBytes()));

        // When
        ResponseEntity<StreamingResponseBody> response =
                exportService.downloadImagePackage(archiveId, null);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();

        // 验证StreamingResponseBody可以正常执行
        StreamingResponseBody body = response.getBody();
        assertThat(body).isNotNull();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        body.writeTo(outputStream);
        byte[] result = outputStream.toByteArray();
        assertThat(result).isNotEmpty();
    }

    @Test
    @DisplayName("下载图片包 - 图片URL为空")
    void downloadImagePackage_EmptyImageUrl() throws Exception {
        // Given
        Long archiveId = 1001L;
        LocalDateTime startTime = LocalDateTime.of(2026, 3, 25, 10, 0, 0);
        BehaviorArchive archive = createArchive(archiveId, 1L, startTime);

        ArchiveTimeline node1 = createTimelineNode(1L, archiveId, 1, startTime,
                "进入", "http://minio/enter.jpg");
        // node2的imageUrl为空
        ArchiveTimeline node2 = createTimelineNode(2L, archiveId, 2,
                startTime.plusMinutes(5), "检查", null);

        when(archiveRepository.findById(archiveId)).thenReturn(Optional.of(archive));
        when(timelineRepository.findByArchiveIdOrderBySeqAsc(archiveId))
                .thenReturn(List.of(node1, node2));
        when(storageService.exists("http://minio/enter.jpg")).thenReturn(true);
        when(storageService.getObject("http://minio/enter.jpg"))
                .thenReturn(new java.io.ByteArrayInputStream("image data".getBytes()));

        // When
        ResponseEntity<StreamingResponseBody> response =
                exportService.downloadImagePackage(archiveId, null);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        response.getBody().writeTo(outputStream);
        assertThat(outputStream.toByteArray()).isNotEmpty();
    }
}
