package com.his.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.cors.reactive.CorsUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import reactor.core.publisher.Mono;

@Configuration
public class CorsConfig {

    private final CorsProperties corsProperties;

    public CorsConfig(CorsProperties corsProperties) {
        this.corsProperties = corsProperties;
    }

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
            response.getHeaders().add(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, corsProperties.getAllowedMethods());
            response.getHeaders().add(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, corsProperties.getAllowedHeaders());
            response.getHeaders().add(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, corsProperties.getAllowedHeaders());
            response.getHeaders().add(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, corsProperties.getAllowCredentials().toString());
            response.getHeaders().add(HttpHeaders.ACCESS_CONTROL_MAX_AGE, corsProperties.getMaxAge().toString());

            return chain.filter(exchange);
        };
    }

    private boolean isOriginAllowed(String origin) {
        if (origin == null || corsProperties.getAllowedOrigins() == null) {
            return false;
        }
        return corsProperties.getAllowedOrigins().contains(origin);
    }

    private Mono<Void> handleInvalidCors(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.FORBIDDEN);
        return Mono.empty();
    }
}
