package com.his.gateway.controller;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * 认证控制器
 * <p>提供登录接口，返回 JWT Token</p>
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @Value("${his.jwt.secret:his-rule-engine-secret-key-must-be-at-least-256-bits-long}")
    private String jwtSecret;

    @Value("${his.jwt.expiration:86400000}")
    private long jwtExpiration; // 默认24小时

    /**
     * 登录接口
     */
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody LoginRequest request) {
        // 简单验证：实际应查询数据库验证用户密码
        if ("admin".equals(request.getUsername()) && "admin".equals(request.getPassword())) {
            String token = generateToken("admin", "T001");
            log.info("用户登录成功: username={}", request.getUsername());
            return Map.of(
                "code", "0",
                "message", "登录成功",
                "data", Map.of(
                    "token", token,
                    "userId", "admin",
                    "tenantId", "T001"
                )
            );
        }

        log.warn("用户登录失败: username={}", request.getUsername());
        return Map.of(
            "code", "HIS-401",
            "message", "用户名或密码错误"
        );
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/me")
    public Map<String, Object> getCurrentUser(@RequestHeader("X-User-Id") String userId,
                                               @RequestHeader("X-Tenant-Id") String tenantId) {
        return Map.of(
            "code", "0",
            "data", Map.of(
                "userId", userId,
                "tenantId", tenantId
            )
        );
    }

    private String generateToken(String userId, String tenantId) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
                .subject(userId)
                .claim("tenantId", tenantId)
                .issuedAt(new java.util.Date())
                .expiration(new java.util.Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(key)
                .compact();
    }

    @Data
    public static class LoginRequest {
        private String username;
        private String password;
    }
}