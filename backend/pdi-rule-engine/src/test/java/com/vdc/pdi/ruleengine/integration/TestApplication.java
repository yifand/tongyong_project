package com.vdc.pdi.ruleengine.integration;

import com.vdc.pdi.systemconfig.service.SystemConfigService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 测试应用入口
 */
@SpringBootApplication
@ComponentScan(basePackages = {
        "com.vdc.pdi.ruleengine",
        "com.vdc.pdi.common"
})
@EnableAsync
@EnableScheduling
public class TestApplication {

    @MockBean
    private SystemConfigService systemConfigService;

    public static void main(String[] args) {
        SpringApplication.run(TestApplication.class, args);
    }
}
