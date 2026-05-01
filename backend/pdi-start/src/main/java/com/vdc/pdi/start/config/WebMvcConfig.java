package com.vdc.pdi.start.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC配置类
 * 配置CORS跨域支持
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${pdi.cors.allowed-origins:*}")
    private String allowedOrigins;

    @Value("${pdi.cors.allowed-methods:GET,POST,PUT,DELETE,OPTIONS,PATCH}")
    private String allowedMethods;

    @Value("${pdi.cors.allowed-headers:*}")
    private String allowedHeaders;

    @Value("${pdi.cors.allow-credentials:false}")
    private boolean allowCredentials;

    @Value("${pdi.cors.max-age:3600}")
    private long maxAge;

    /**
     * 配置CORS跨域映射
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns(allowedOrigins.split(","))
                .allowedMethods(allowedMethods.split(","))
                .allowedHeaders(allowedHeaders.split(","))
                .allowCredentials(allowCredentials)
                .maxAge(maxAge);
    }

}
