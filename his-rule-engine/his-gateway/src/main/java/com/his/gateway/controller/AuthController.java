package com.his.gateway.controller;

import com.his.gateway.dto.LoginRequest;
import com.his.gateway.dto.LoginResponse;
import com.his.gateway.service.AuthService;
import com.his.gateway.service.impl.AuthServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public Mono<Map<String, Object>> login(@Valid @RequestBody LoginRequest request,
                                            ServerHttpRequest httpRequest) {
        String clientIp = extractClientIp(httpRequest);

        return Mono.fromCallable(() -> authService.login(request, clientIp))
                .subscribeOn(Schedulers.boundedElastic())
                .map(loginResponse -> Map.<String, Object>of(
                        "code", "0",
                        "message", "登录成功",
                        "data", Map.of(
                                "token", loginResponse.getToken(),
                                "userId", loginResponse.getUserId(),
                                "username", loginResponse.getUsername(),
                                "realName", loginResponse.getRealName() != null ? loginResponse.getRealName() : "",
                                "tenantId", loginResponse.getTenantId(),
                                "role", loginResponse.getRole() != null ? loginResponse.getRole() : ""
                        )
                ))
                .onErrorResume(AuthServiceImpl.LoginException.class, e ->
                        Mono.just(Map.<String, Object>of(
                                "code", e.getCode(),
                                "message", e.getMessage(),
                                "timestamp", System.currentTimeMillis()
                        ))
                )
                .onErrorResume(Exception.class, e -> {
                    log.error("登录异常: {}", e.getMessage(), e);
                    return Mono.just(Map.<String, Object>of(
                            "code", "HIS-099",
                            "message", "系统繁忙，请稍后重试",
                            "timestamp", System.currentTimeMillis()
                    ));
                });
    }

    @GetMapping("/me")
    public Mono<Map<String, Object>> getCurrentUser(@RequestHeader("X-User-Id") String userId,
                                                      @RequestHeader("X-Tenant-Id") String tenantId) {
        return Mono.just(Map.of(
                "code", "0",
                "data", Map.of(
                        "userId", userId,
                        "tenantId", tenantId
                )
        ));
    }

    private String extractClientIp(ServerHttpRequest request) {
        String ip = request.getHeaders().getFirst("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getHeaders().getFirst("X-Real-IP");
        }
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddress() != null
                    ? request.getRemoteAddress().getAddress().getHostAddress()
                    : "unknown";
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
