package com.vdc.platform.controller;

import com.vdc.platform.common.ApiResult;
import com.vdc.platform.common.MinioStorageService;
import com.vdc.platform.dto.AlarmPageQuery;
import com.vdc.platform.dto.AlarmProcessRequest;
import com.vdc.platform.entity.Alarm;
import com.vdc.platform.entity.OperationLog;
import com.vdc.platform.security.model.SecurityUser;
import com.vdc.platform.service.IAlarmService;
import com.vdc.platform.service.IOperationLogService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/alarms")
@RequiredArgsConstructor
public class AlarmController {

    private final IAlarmService alarmService;
    private final IOperationLogService operationLogService;
    private final MinioStorageService minioStorageService;

    @GetMapping
    @PreAuthorize("hasAuthority('alarm:read') or hasAuthority('admin')")
    public ApiResult<com.baomidou.mybatisplus.core.metadata.IPage<Alarm>> list(@ParameterObject AlarmPageQuery query) {
        return ApiResult.success(alarmService.queryAlarmPage(query));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('alarm:read') or hasAuthority('admin')")
    public ApiResult<Map<String, Object>> getById(@PathVariable Long id) {
        Alarm alarm = alarmService.getById(id);
        if (alarm == null) {
            return ApiResult.error(404, "Alarm not found");
        }
        Map<String, Object> result = new HashMap<>();
        result.put("alarm", alarm);
        String watermark = alarm.getWatermarkLogo();
        if (watermark == null || watermark.isBlank()) {
            watermark = "VDC";
        }
        result.put("targetImageUrl", minioStorageService.getWatermarkedPresignedUrl(alarm.getTargetImage(), watermark));
        result.put("sceneImageUrl", minioStorageService.getWatermarkedPresignedUrl(alarm.getSceneImage(), watermark));
        return ApiResult.success(result);
    }

    @PutMapping("/{id}/process")
    @PreAuthorize("hasAuthority('alarm:write') or hasAuthority('admin')")
    public ApiResult<Void> process(@PathVariable Long id, @Valid @RequestBody AlarmProcessRequest request,
                                   HttpServletRequest httpRequest) {
        Alarm alarm = alarmService.getById(id);
        if (alarm == null) {
            return ApiResult.error(404, "Alarm not found");
        }
        SecurityUser currentUser = getCurrentUser();
        alarm.setProcessStatus(request.getProcessStatus());
        alarm.setProcessedBy(currentUser.getUserId());
        alarm.setProcessedAt(LocalDateTime.now());
        if (request.getDescription() != null) {
            alarm.setDescription(request.getDescription());
        }
        alarmService.updateById(alarm);
        recordLog(currentUser, "PROCESS_ALARM", "Processed alarm: " + id, 1, httpRequest);
        return ApiResult.success();
    }

    @PostMapping("/export")
    @PreAuthorize("hasAuthority('alarm:read') or hasAuthority('admin')")
    public org.springframework.http.ResponseEntity<byte[]> export(@RequestBody AlarmPageQuery query,
                                                                   @RequestParam(defaultValue = "csv") String format,
                                                                   HttpServletRequest httpRequest) {
        query.setCurrent(1);
        query.setSize(10000);
        List<Alarm> list = alarmService.queryAlarmPage(query).getRecords();

        org.springframework.http.ResponseEntity<byte[]> result;
        if ("xlsx".equalsIgnoreCase(format)) {
            result = exportExcel(list);
        } else {
            result = exportCsv(list);
        }
        SecurityUser currentUser = getCurrentUser();
        recordLog(currentUser, "EXPORT", "Exported alarms: " + list.size() + " records (format=" + format + ")", 1, httpRequest);
        return result;
    }

    private org.springframework.http.ResponseEntity<byte[]> exportCsv(List<Alarm> list) {
        StringWriter writer = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT.builder()
                .setHeader("ID", "AlarmType", "SiteId", "ChannelId", "AlarmTime", "ProcessStatus", "Description")
                .build())) {
            for (Alarm alarm : list) {
                printer.printRecord(
                        alarm.getId(),
                        alarm.getAlarmType(),
                        alarm.getSiteId(),
                        alarm.getChannelId(),
                        alarm.getAlarmTime(),
                        alarm.getProcessStatus(),
                        alarm.getDescription()
                );
            }
        } catch (Exception e) {
            throw new RuntimeException("CSV export failed", e);
        }

        byte[] bytes = writer.toString().getBytes(StandardCharsets.UTF_8);
        return org.springframework.http.ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=alarms.csv")
                .contentType(org.springframework.http.MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(bytes);
    }

    private org.springframework.http.ResponseEntity<byte[]> exportExcel(List<Alarm> list) {
        try (org.apache.poi.ss.usermodel.Workbook wb = new org.apache.poi.xssf.usermodel.XSSFWorkbook();
             java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream()) {
            org.apache.poi.ss.usermodel.Sheet sheet = wb.createSheet("Alarm Report");
            org.apache.poi.ss.usermodel.Row header = sheet.createRow(0);
            String[] titles = {"ID", "报警类型", "站点ID", "通道ID", "报警时间", "处理状态", "描述"};
            for (int i = 0; i < titles.length; i++) {
                header.createCell(i).setCellValue(titles[i]);
            }
            int rowNum = 1;
            for (Alarm alarm : list) {
                org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(alarm.getId() != null ? alarm.getId() : 0L);
                row.createCell(1).setCellValue(alarm.getAlarmType());
                row.createCell(2).setCellValue(alarm.getSiteId() != null ? alarm.getSiteId() : 0L);
                row.createCell(3).setCellValue(alarm.getChannelId() != null ? alarm.getChannelId() : 0L);
                row.createCell(4).setCellValue(alarm.getAlarmTime() != null ? alarm.getAlarmTime().toString() : "");
                row.createCell(5).setCellValue(alarm.getProcessStatus());
                row.createCell(6).setCellValue(alarm.getDescription());
            }
            for (int i = 0; i < titles.length; i++) {
                sheet.autoSizeColumn(i);
            }
            wb.write(baos);
            byte[] bytes = baos.toByteArray();
            return org.springframework.http.ResponseEntity.ok()
                    .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=alarms.xlsx")
                    .contentType(org.springframework.http.MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(bytes);
        } catch (Exception e) {
            throw new RuntimeException("Excel export failed", e);
        }
    }

    @GetMapping("/{id}/images")
    @PreAuthorize("hasAuthority('alarm:read') or hasAuthority('admin')")
    public ApiResult<Map<String, String>> getImages(@PathVariable Long id) {
        Alarm alarm = alarmService.getById(id);
        if (alarm == null) {
            return ApiResult.error(404, "Alarm not found");
        }
        Map<String, String> result = new HashMap<>();
        String watermark = alarm.getWatermarkLogo();
        if (watermark == null || watermark.isBlank()) {
            watermark = "VDC";
        }
        result.put("targetImageUrl", minioStorageService.getWatermarkedPresignedUrl(alarm.getTargetImage(), watermark));
        result.put("sceneImageUrl", minioStorageService.getWatermarkedPresignedUrl(alarm.getSceneImage(), watermark));
        return ApiResult.success(result);
    }

    private SecurityUser getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return (SecurityUser) principal;
    }

    private void recordLog(SecurityUser user, String type, String content, int result, HttpServletRequest request) {
        OperationLog log = new OperationLog();
        log.setUserId(user.getUserId());
        log.setUsername(user.getUsername());
        log.setIpAddress(getClientIp(request));
        log.setOperationType(type);
        log.setOperationContent(content);
        log.setResult(result);
        log.setCreatedAt(LocalDateTime.now());
        operationLogService.save(log);
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }
        return ip.split(",")[0].trim();
    }
}
