package com.his.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.cors.reactive.CorsUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

/**
 * CORS 跨域配置
 */
@Configuration
public class CorsConfig {

    private static final List<String> ALLOWED_ORIGINS = Arrays.asList(
            "http://localhost:3000",
            "http://localhost:8080",
            "http://127.0.0.1:3000",
            "http://127.0.0.1:8080"
    );

    private static final List<String> ALLOWED_METHODS = Arrays.asList(
            HttpMethod.GET.name(),
            HttpMethod.POST.name(),
            HttpMethod.PUT.name(),
            HttpMethod.DELETE.name(),
            HttpMethod.OPTIONS.name(),
            HttpMethod.PATCH.name()
    );

    private static final List<String> ALLOWED_HEADERS = Arrays.asList(
            HttpHeaders.ORIGIN,
            HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD,
            HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS,
            HttpHeaders.CONTENT_TYPE,
            HttpHeaders.ACCEPT,
            "X-Tenant-Id",
            "Authorization"
    );

    private static final long MAX_AGE = 3600L;

    @Bean
    public WebFilter corsWebFilter() {
        return (ServerWebExchange exchange, WebFilterChain chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            if (!CorsUtils.isCorsRequest(request)) {
                return chain.filter(exchange);
            }

            String origin = request.getHeaders().getOrigin();

            if (!isOriginAllowed(origin)) {
                return handleInvalidCors(exchange);
            }

            ServerHttpResponse response = exchange.getResponse();
            response.getHeaders().add(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, origin);
            response.getHeaders().add(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, String.join(",", ALLOWED_METHODS));
            response.getHeaders().add(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, String.join(",", ALLOWED_HEADERS));
            response.getHeaders().add(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "X-Tenant-Id,Authorization");
            response.getHeaders().add(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
            response.getHeaders().add(HttpHeaders.ACCESS_CONTROL_MAX_AGE, String.valueOf(MAX_AGE));

            if (request.getMethod() == HttpMethod.OPTIONS) {
                response.setStatusCode(HttpStatus.OK);
                return Mono.empty();
            }

            return chain.filter(exchange);
        };
    }

    private boolean isOriginAllowed(String origin) {
        if (origin == null) {
            return false;
        }
        return ALLOWED_ORIGINS.contains(origin) || origin.startsWith("http://localhost:");
    }

    private Mono<Void> handleInvalidCors(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.FORBIDDEN);
        return Mono.empty();
    }
}
