package com.his.gateway.config;

import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.NettyWriteResponseFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * 网关全局错误处理
 */
@Component
public class GlobalErrorAttributes implements ErrorAttributes, GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return NettyWriteResponseFilter.WRITE_RESPONSE_FILTER_ORDER - 1;
    }

    @Override
    public Map<String, Object> getErrorAttributes(ServerRequest request, boolean includeStackTrace) {
        Throwable error = getError(request);

        String message = "系统繁忙，请稍后重试";
        String code = "HIS-999";

        if (error instanceof ResponseStatusException responseStatusException) {
            HttpStatus status = HttpStatus.valueOf(responseStatusException.getStatusCode().value());
            message = responseStatusException.getReason();
            code = "HIS-" + status.value();
        } else if (error.getMessage() != null) {
            message = error.getMessage();
        }

        return Map.of(
                "code", code,
                "message", message,
                "timestamp", System.currentTimeMillis()
        );
    }

    private Throwable getError(ServerRequest request) {
        return request.exchange().getAttribute("javax.servlet.error.exception");
    }
}
