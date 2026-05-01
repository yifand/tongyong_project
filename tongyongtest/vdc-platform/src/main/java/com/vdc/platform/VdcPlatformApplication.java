package com.vdc.platform;

import com.vdc.platform.config.MinioConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(MinioConfig.class)
public class VdcPlatformApplication {
    public static void main(String[] args) {
        SpringApplication.run(VdcPlatformApplication.class, args);
    }
}
