package com.vdc.platform.common;

import io.minio.GetObjectArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class MinioStorageService {

    private final MinioClient minioClient;
    private final org.springframework.core.env.Environment env;

    private String getBucketName() {
        String bucket = env.getProperty("minio.bucket-name");
        return bucket != null ? bucket : "vdc-bucket";
    }

    public String uploadBase64Image(String base64Data, String prefix) {
        if (base64Data == null || base64Data.isBlank()) {
            return null;
        }
        String data = base64Data;
        if (data.contains(",")) {
            data = data.substring(data.indexOf(",") + 1);
        }
        try {
            byte[] bytes = Base64.getDecoder().decode(data);
            String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String objectName = prefix + "/" + date + "/" + UUID.randomUUID() + ".jpg";
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(getBucketName())
                            .object(objectName)
                            .stream(new ByteArrayInputStream(bytes), bytes.length, -1)
                            .contentType("image/jpeg")
                            .build());
            return objectName;
        } catch (Exception e) {
            log.error("MinIO upload failed", e);
            throw new RuntimeException("MinIO upload failed", e);
        }
    }

    public String getPresignedUrl(String objectName) {
        if (objectName == null || objectName.isBlank()) {
            return null;
        }
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .bucket(getBucketName())
                            .object(objectName)
                            .method(Method.GET)
                            .expiry(5, TimeUnit.MINUTES)
                            .build());
        } catch (Exception e) {
            log.error("MinIO presigned URL failed for {}", objectName, e);
            return null;
        }
    }

    public String getWatermarkedPresignedUrl(String objectName, String watermarkText) {
        if (objectName == null || objectName.isBlank()) {
            return null;
        }
        if (watermarkText == null || watermarkText.isBlank()) {
            return getPresignedUrl(objectName);
        }
        try {
            InputStream stream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(getBucketName())
                            .object(objectName)
                            .build());
            BufferedImage original = ImageIO.read(stream);
            stream.close();
            if (original == null) {
                return getPresignedUrl(objectName);
            }

            Graphics2D g2d = original.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int fontSize = Math.max(16, original.getWidth() / 25);
            Font font = new Font("Microsoft YaHei", Font.BOLD, fontSize);
            g2d.setFont(font);
            FontMetrics fm = g2d.getFontMetrics();
            int textWidth = fm.stringWidth(watermarkText);
            int textHeight = fm.getHeight();

            g2d.setColor(new Color(255, 255, 255, 128));
            int padding = 10;
            int x = original.getWidth() - textWidth - padding;
            int y = original.getHeight() - padding;
            g2d.fillRect(x - padding, y - textHeight + fm.getDescent(), textWidth + padding * 2, textHeight + padding);

            g2d.setColor(new Color(0, 0, 0, 180));
            g2d.drawString(watermarkText, x, y);
            g2d.dispose();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(original, "JPEG", baos);
            byte[] watermarkedBytes = baos.toByteArray();

            String tempObjectName = "temp/watermarked/" + UUID.randomUUID() + ".jpg";
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(getBucketName())
                            .object(tempObjectName)
                            .stream(new ByteArrayInputStream(watermarkedBytes), watermarkedBytes.length, -1)
                            .contentType("image/jpeg")
                            .build());

            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .bucket(getBucketName())
                            .object(tempObjectName)
                            .method(Method.GET)
                            .expiry(5, TimeUnit.MINUTES)
                            .build());
        } catch (Exception e) {
            log.error("Watermarked presigned URL failed for {}", objectName, e);
            return getPresignedUrl(objectName);
        }
    }
}
