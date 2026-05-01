package com.vdc.platform.gateway.config;

import com.vdc.platform.gateway.filter.GatewayAuthFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayWebConfig {

    @Bean
    public FilterRegistrationBean<GatewayAuthFilter> gatewayAuthFilterRegistration(GatewayAuthFilter filter) {
        FilterRegistrationBean<GatewayAuthFilter> registration = new FilterRegistrationBean<>(filter);
        registration.addUrlPatterns("/gateway/*");
        registration.setOrder(1);
        return registration;
    }
}
