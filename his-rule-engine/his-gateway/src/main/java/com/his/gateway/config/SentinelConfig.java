package com.his.gateway.config;

import com.alibaba.csp.sentinel.adapter.gateway.common.SentinelGatewayConstants;
import com.alibaba.csp.sentinel.adapter.gateway.sc.SentinelGatewayFilter;
import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.GatewayCallbackManager;
import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.UrlBlockHandler;
import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.WebCustomizer;
import com.alibaba.csp.sentinel.adapter.gateway.sc.exception.SentinelGatewayBlockExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.result.view.ViewResolver;

import java.util.HashMap;
import java.util.Map;

/**
 * Sentinel 限流配置
 */
@Configuration
public class SentinelConfig {

    private final ViewResolver viewResolver;
    private final ObjectMapper objectMapper;

    public SentinelConfig(ViewResolver viewResolver, ObjectMapper objectMapper) {
        this.viewResolver = viewResolver;
        this.objectMapper = objectMapper;
    }

    @Bean
    public SentinelGatewayBlockExceptionHandler sentinelGatewayBlockExceptionHandler() {
        return new SentinelGatewayBlockExceptionHandler(viewResolver, objectMapper);
    }

    @Bean
    public SentinelGatewayFilter sentinelGatewayFilter() {
        return new SentinelGatewayFilter();
    }

    @Bean
    public UrlBlockHandler urlBlockHandler() {
        return (exchange, t) -> {
            Map<String, Object> result = new HashMap<>();
            result.put("code", "HIS-429");
            result.put("message", "请求过于频繁，请稍后重试");
            result.put("timestamp", System.currentTimeMillis());

            exchange.getResponse().setStatusCode(HttpServletResponse.SC_TOO_MANY_REQUESTS);
            exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
            exchange.getResponse().getHeaders().set("X-Tenant-Id", exchange.getRequest().getHeaders().getFirst("X-Tenant-Id"));

            byte[] bytes = objectMapper.writeValueAsBytes(result);
            exchange.getResponse().getBody().write(bytes);
        };
    }
}
