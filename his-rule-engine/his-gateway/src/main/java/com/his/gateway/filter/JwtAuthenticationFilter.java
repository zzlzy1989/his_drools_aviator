package com.his.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * JWT 认证过滤器
 *
 * <p>验证请求中的 JWT Token，有效则放行，无效则返回 401</p>
 */
@Slf4j
@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Value("${his.jwt.secret:his-rule-engine-secret-key-must-be-at-least-256-bits-long}")
    private String jwtSecret;

    @Value("${his.jwt.enabled:true}")
    private boolean jwtEnabled;

    private static final List<String> WHITE_LIST = List.of(
            "/api/v1/auth/**",
            "/api/health",
            "/api/v2/flows/**",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/actuator/**"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        if (!jwtEnabled) {
            return chain.filter(exchange);
        }

        if (isWhiteListed(path)) {
            return chain.filter(exchange);
        }

        String authHeader = request.getHeaders().getFirst(AUTHORIZATION_HEADER);

        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            log.warn("JWT Token缺失: path={}", path);
            return unauthorized(exchange, "JWT Token缺失");
        }

        String token = authHeader.substring(BEARER_PREFIX.length());

        try {
            Claims claims = validateToken(token);
            String userId = claims.getSubject();
            String tenantId = claims.get("tenantId", String.class);

            ServerHttpRequest mutatedRequest = request.mutate()
                    .header("X-User-Id", userId)
                    .header("X-Tenant-Id", tenantId != null ? tenantId : extractTenantFromRequest(request))
                    .build();

            exchange = exchange.mutate()
                    .request(mutatedRequest)
                    .build();

            log.debug("JWT认证成功: userId={}, path={}", userId, path);

            return chain.filter(exchange);

        } catch (Exception e) {
            log.warn("JWT认证失败: path={}, error={}", path, e.getMessage());
            return unauthorized(exchange, "JWT Token无效或已过期");
        }
    }

    /**
     * 验证 Token
     */
    private Claims validateToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));

        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 检查是否在白名单
     */
    private boolean isWhiteListed(String path) {
        return WHITE_LIST.stream().anyMatch(pattern -> {
            if (pattern.contains("**")) {
                String basePath = pattern.replace("**", "").replaceAll("/+$", "");
                return path.equals(basePath) || path.startsWith(basePath + "/");
            }
            return path.equals(pattern) || path.startsWith(pattern + "/");
        });
    }

    /**
     * 从请求头提取租户ID（备用）
     */
    private String extractTenantFromRequest(ServerHttpRequest request) {
        return request.getHeaders().getFirst("X-Tenant-Id");
    }

    /**
     * 返回 401 未授权
     */
    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add("Content-Type", "application/json");

        String body = String.format(
                "{\"code\":\"HIS-401\",\"message\":\"%s\",\"timestamp\":%d}",
                message, System.currentTimeMillis()
        );

        return response.writeWith(Mono.just(
                response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8))
        ));
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 100;
    }
}
