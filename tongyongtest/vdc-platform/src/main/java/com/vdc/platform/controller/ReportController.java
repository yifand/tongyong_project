package com.vdc.platform.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vdc.platform.common.ApiResult;
import com.vdc.platform.common.MinioStorageService;
import com.vdc.platform.dto.ReportExportRequest;
import com.vdc.platform.dto.ReportPageQuery;
import com.vdc.platform.entity.OperationLog;
import com.vdc.platform.entity.WorkSession;
import com.vdc.platform.security.model.SecurityUser;
import com.vdc.platform.service.IOperationLogService;
import com.vdc.platform.service.IWorkSessionService;
import jakarta.servlet.http.HttpServletRequest;
import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RestController
@RequestMapping("/api/v1/reports/pdi")
@RequiredArgsConstructor
public class ReportController {

    private final IWorkSessionService workSessionService;
    private final IOperationLogService operationLogService;
    private final MinioStorageService minioStorageService;

    @GetMapping
    @PreAuthorize("hasAuthority('report:read') or hasAuthority('admin')")
    public ApiResult<com.baomidou.mybatisplus.core.metadata.IPage<WorkSession>> list(@ParameterObject ReportPageQuery query) {
        LambdaQueryWrapper<WorkSession> wrapper = new LambdaQueryWrapper<>();
        if (query.getSiteId() != null) {
            wrapper.eq(WorkSession::getSiteId, query.getSiteId());
        }
        if (query.getChannelId() != null) {
            wrapper.eq(WorkSession::getChannelId, query.getChannelId());
        }
        if (query.getResult() != null && !query.getResult().isEmpty()) {
            wrapper.eq(WorkSession::getResult, query.getResult());
        }
        if (query.getStartTime() != null) {
            wrapper.ge(WorkSession::getStartTime, query.getStartTime());
        }
        if (query.getEndTime() != null) {
            wrapper.le(WorkSession::getEndTime, query.getEndTime());
        }
        wrapper.orderByDesc(WorkSession::getStartTime);
        return ApiResult.success(workSessionService.page(query, wrapper));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('report:read') or hasAuthority('admin')")
    public ApiResult<Map<String, Object>> getById(@PathVariable Long id) {
        WorkSession session = workSessionService.getById(id);
        if (session == null) {
            return ApiResult.error(404, "Work session not found");
        }
        Map<String, Object> result = new HashMap<>();
        result.put("session", session);
        result.put("snapshotHeadUrl", minioStorageService.getPresignedUrl(session.getSnapshotHead()));
        result.put("snapshotTailUrl", minioStorageService.getPresignedUrl(session.getSnapshotTail()));
        result.put("snapshotMidUrl", minioStorageService.getPresignedUrl(session.getSnapshotMid()));
        return ApiResult.success(result);
    }

    @PostMapping("/export")
    @PreAuthorize("hasAuthority('report:read') or hasAuthority('admin')")
    public ResponseEntity<byte[]> export(@RequestBody ReportExportRequest request, HttpServletRequest httpRequest) {
        LambdaQueryWrapper<WorkSession> wrapper = new LambdaQueryWrapper<>();
        if (request.getSiteIds() != null && !request.getSiteIds().isEmpty()) {
            wrapper.in(WorkSession::getSiteId, request.getSiteIds());
        }
        if (request.getChannelIds() != null && !request.getChannelIds().isEmpty()) {
            wrapper.in(WorkSession::getChannelId, request.getChannelIds());
        }
        if (request.getResult() != null && !request.getResult().isEmpty()) {
            wrapper.eq(WorkSession::getResult, request.getResult());
        }
        if (request.getStartTime() != null) {
            wrapper.ge(WorkSession::getStartTime, request.getStartTime());
        }
        if (request.getEndTime() != null) {
            wrapper.le(WorkSession::getEndTime, request.getEndTime());
        }
        wrapper.orderByDesc(WorkSession::getStartTime);
        List<WorkSession> list = workSessionService.list(wrapper);

        String format = request.getFormat() != null ? request.getFormat().toLowerCase() : "xlsx";
        boolean includeImages = request.getIncludeImages() != null && request.getIncludeImages();

        try {
            ResponseEntity<byte[]> result;
            switch (format) {
                case "pdf":
                    result = exportPdf(list);
                    break;
                case "zip":
                    result = exportZip(list, includeImages);
                    break;
                case "xlsx":
                default:
                    result = exportExcel(list);
                    break;
            }
            SecurityUser currentUser = getCurrentUser();
            recordLog(currentUser, "EXPORT", "Exported PDI reports: " + list.size() + " records (format=" + format + ")", 1, httpRequest);
            return result;
        } catch (Exception e) {
            throw new RuntimeException(format + " export failed", e);
        }
    }

    private ResponseEntity<byte[]> exportExcel(List<WorkSession> list) throws Exception {
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("PDI Report");
            Row header = sheet.createRow(0);
            String[] titles = {"Site ID", "Channel ID", "Vehicle Info", "Start Time", "End Time",
                    "Actual Duration", "Standard Duration", "Deviation (%)", "Result"};
            for (int i = 0; i < titles.length; i++) {
                header.createCell(i).setCellValue(titles[i]);
            }
            int rowNum = 1;
            for (WorkSession ws : list) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(ws.getSiteId() != null ? ws.getSiteId() : 0L);
                row.createCell(1).setCellValue(ws.getChannelId() != null ? ws.getChannelId() : 0L);
                row.createCell(2).setCellValue(ws.getVehicleInfo());
                row.createCell(3).setCellValue(ws.getStartTime() != null ? ws.getStartTime().toString() : "");
                row.createCell(4).setCellValue(ws.getEndTime() != null ? ws.getEndTime().toString() : "");
                row.createCell(5).setCellValue(ws.getActualDuration() != null ? ws.getActualDuration() : 0);
                row.createCell(6).setCellValue(ws.getStandardDuration() != null ? ws.getStandardDuration() : 0);
                row.createCell(7).setCellValue(ws.getDeviationPct() != null ? ws.getDeviationPct().doubleValue() : 0.0);
                row.createCell(8).setCellValue(ws.getResult());
            }
            wb.write(baos);
            byte[] bytes = baos.toByteArray();
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=pdi_report.xlsx")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(bytes);
        }
    }

    private ResponseEntity<byte[]> exportPdf(List<WorkSession> list) throws Exception {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4.rotate());
            PdfWriter.getInstance(document, baos);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            document.add(new Paragraph("PDI Report", titleFont));
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(9);
            table.setWidthPercentage(100);
            String[] headers = {"Site ID", "Channel ID", "Vehicle Info", "Start Time", "End Time",
                    "Actual Duration", "Standard Duration", "Deviation (%)", "Result"};
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10)));
                table.addCell(cell);
            }
            for (WorkSession ws : list) {
                table.addCell(String.valueOf(ws.getSiteId() != null ? ws.getSiteId() : 0L));
                table.addCell(String.valueOf(ws.getChannelId() != null ? ws.getChannelId() : 0L));
                table.addCell(ws.getVehicleInfo() != null ? ws.getVehicleInfo() : "");
                table.addCell(ws.getStartTime() != null ? ws.getStartTime().toString() : "");
                table.addCell(ws.getEndTime() != null ? ws.getEndTime().toString() : "");
                table.addCell(String.valueOf(ws.getActualDuration() != null ? ws.getActualDuration() : 0));
                table.addCell(String.valueOf(ws.getStandardDuration() != null ? ws.getStandardDuration() : 0));
                table.addCell(String.valueOf(ws.getDeviationPct() != null ? ws.getDeviationPct().doubleValue() : 0.0));
                table.addCell(ws.getResult() != null ? ws.getResult() : "");
            }
            document.add(table);
            document.close();

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=pdi_report.pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(baos.toByteArray());
        }
    }

    private ResponseEntity<byte[]> exportZip(List<WorkSession> list, boolean includeImages) throws Exception {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(baos)) {

            // Add Excel report
            byte[] excelBytes;
            try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream excelBaos = new ByteArrayOutputStream()) {
                Sheet sheet = wb.createSheet("PDI Report");
                Row header = sheet.createRow(0);
                String[] titles = {"Site ID", "Channel ID", "Vehicle Info", "Start Time", "End Time",
                        "Actual Duration", "Standard Duration", "Deviation (%)", "Result"};
                for (int i = 0; i < titles.length; i++) {
                    header.createCell(i).setCellValue(titles[i]);
                }
                int rowNum = 1;
                for (WorkSession ws : list) {
                    Row row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue(ws.getSiteId() != null ? ws.getSiteId() : 0L);
                    row.createCell(1).setCellValue(ws.getChannelId() != null ? ws.getChannelId() : 0L);
                    row.createCell(2).setCellValue(ws.getVehicleInfo());
                    row.createCell(3).setCellValue(ws.getStartTime() != null ? ws.getStartTime().toString() : "");
                    row.createCell(4).setCellValue(ws.getEndTime() != null ? ws.getEndTime().toString() : "");
                    row.createCell(5).setCellValue(ws.getActualDuration() != null ? ws.getActualDuration() : 0);
                    row.createCell(6).setCellValue(ws.getStandardDuration() != null ? ws.getStandardDuration() : 0);
                    row.createCell(7).setCellValue(ws.getDeviationPct() != null ? ws.getDeviationPct().doubleValue() : 0.0);
                    row.createCell(8).setCellValue(ws.getResult());
                }
                wb.write(excelBaos);
                excelBytes = excelBaos.toByteArray();
            }
            ZipEntry reportEntry = new ZipEntry("pdi_report.xlsx");
            zos.putNextEntry(reportEntry);
            zos.write(excelBytes);
            zos.closeEntry();

            // Add images if requested
            if (includeImages) {
                int imgIndex = 1;
                for (WorkSession ws : list) {
                    String[] snapshotKeys = {ws.getSnapshotHead(), ws.getSnapshotMid(), ws.getSnapshotTail()};
                    String[] snapshotNames = {"head", "mid", "tail"};
                    for (int i = 0; i < snapshotKeys.length; i++) {
                        String key = snapshotKeys[i];
                        if (key != null && !key.isEmpty()) {
                            String presignedUrl = minioStorageService.getPresignedUrl(key);
                            if (presignedUrl != null) {
                                try (InputStream is = new URL(presignedUrl).openStream()) {
                                    String ext = key.contains(".") ? key.substring(key.lastIndexOf('.')) : ".jpg";
                                    ZipEntry imgEntry = new ZipEntry("images/" + ws.getId() + "_" + snapshotNames[i] + ext);
                                    zos.putNextEntry(imgEntry);
                                    is.transferTo(zos);
                                    zos.closeEntry();
                                } catch (Exception ignored) {
                                    // Skip images that fail to download
                                }
                            }
                        }
                    }
                    imgIndex++;
                }
            }

            zos.finish();
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=pdi_report.zip")
                    .contentType(MediaType.parseMediaType("application/zip"))
                    .body(baos.toByteArray());
        }
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
        log.setCreatedAt(java.time.LocalDateTime.now());
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
