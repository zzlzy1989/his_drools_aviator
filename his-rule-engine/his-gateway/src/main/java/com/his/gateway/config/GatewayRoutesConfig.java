package com.his.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 网关路由配置
 */
@Configuration
public class GatewayRoutesConfig {

    /**
     * 配置路由规则
     */
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("rule-service", r -> r
                        .path("/api/v1/rules/**", "/api/v1/rule-groups/**", "/api/v2/flows/**")
                        .filters(f -> f.stripPrefix(0))
                        .uri("lb://his-rule-service"))

                .route("formula-service", r -> r
                        .path("/api/v1/formulas/**")
                        .filters(f -> f.stripPrefix(0))
                        .uri("lb://his-formula-service"))

                .route("settlement-service", r -> r
                        .path("/api/v1/settlements/**")
                        .filters(f -> f.stripPrefix(0))
                        .uri("lb://his-settlement-service"))

                .route("drug-service", r -> r
                        .path("/api/v1/drugs/**")
                        .filters(f -> f.stripPrefix(0))
                        .uri("lb://his-drug-service"))

                .route("quality-service", r -> r
                        .path("/api/v1/quality/**")
                        .filters(f -> f.stripPrefix(0))
                        .uri("lb://his-quality-service"))

                .route("drg-service", r -> r
                        .path("/api/v1/drg/**")
                        .filters(f -> f.stripPrefix(0))
                        .uri("lb://his-drg-service"))

                // ===== V2 服务路由 =====
                .route("monitor-service", r -> r
                        .path("/api/v1/monitor/**")
                        .filters(f -> f.stripPrefix(0))
                        .uri("lb://his-monitor-service"))

                .route("market-service", r -> r
                        .path("/api/v1/market/**")
                        .filters(f -> f.stripPrefix(0))
                        .uri("lb://his-market-service"))

                .route("settlement-sandbox", r -> r
                        .path("/api/v1/sandbox/**")
                        .filters(f -> f.stripPrefix(0))
                        .uri("lb://his-settlement-service"))

                .build();
    }
}
