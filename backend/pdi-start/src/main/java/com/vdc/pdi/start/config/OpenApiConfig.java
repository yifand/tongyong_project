package com.vdc.pdi.start.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI (Swagger) 配置类
 * 配置API文档信息和JWT安全认证
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "PDI智能监测平台 API",
                description = "基于Java 17 + Spring Boot 3.x + PostgreSQL 16构建的PDI作业监测平台",
                version = "1.0.0",
                contact = @Contact(
                        name = "PDI开发团队",
                        email = "dev@vdc.com"
                ),
                license = @License(
                        name = "Apache 2.0",
                        url = "https://www.apache.org/licenses/LICENSE-2.0"
                )
        ),
        servers = {
                @Server(
                        description = "本地开发环境",
                        url = "http://localhost:8080"
                ),
                @Server(
                        description = "测试环境",
                        url = "http://test-api.vdc.com"
                ),
                @Server(
                        description = "生产环境",
                        url = "https://api.vdc.com"
                )
        }
)
@SecurityScheme(
        name = "bearerAuth",
        description = "JWT Token认证，请在下方输入Bearer Token",
        scheme = "bearer",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER
)
public class OpenApiConfig {
    // 配置通过注解完成，无需额外代码
}
