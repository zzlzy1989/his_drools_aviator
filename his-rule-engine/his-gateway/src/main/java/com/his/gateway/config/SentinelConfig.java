package com.his.gateway.config;

import com.alibaba.csp.sentinel.adapter.gateway.sc.SentinelGatewayFilter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Sentinel 限流配置 (WebFlux 兼容)
 */
@Configuration
public class SentinelConfig {

    @Bean
    @ConditionalOnProperty(name = "sentinel.enabled", havingValue = "true", matchIfMissing = false)
    public SentinelGatewayFilter sentinelGatewayFilter() {
        return new SentinelGatewayFilter();
    }
}