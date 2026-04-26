package com.his.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 租户上下文过滤器
 *
 * <p>将 X-Tenant-Id 请求头透传到下游服务</p>
 */
@Component
public class TenantContextFilter implements GlobalFilter, Ordered {

    private static final String TENANT_HEADER = "X-Tenant-Id";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String tenantId = request.getHeaders().getFirst(TENANT_HEADER);

        if (tenantId != null && !tenantId.isBlank()) {
            ServerHttpRequest mutatedRequest = request.mutate()
                    .header(TENANT_HEADER, tenantId)
                    .build();
            exchange = exchange.mutate()
                    .request(mutatedRequest)
                    .build();
        }

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
