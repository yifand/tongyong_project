package com.pdi.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * PDI智能监测平台 - 管理后台启动类
 *
 * @author PDI Team
 * @version 1.0.0
 * @since 2026-03-27
 */
@SpringBootApplication(scanBasePackages = "com.pdi")
public class PdiAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(PdiAdminApplication.class, args);
    }

}
