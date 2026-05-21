package com.his.gateway.service.impl;

import com.his.gateway.dto.LoginRequest;
import com.his.gateway.dto.LoginResponse;
import com.his.gateway.entity.AuthUser;
import com.his.gateway.service.AuthService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final JdbcTemplate jdbcTemplate;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);

    @Value("${his.jwt.secret:his-rule-engine-secret-key-must-be-at-least-256-bits-long}")
    private String jwtSecret;

    @Value("${his.jwt.expiration:86400000}")
    private long jwtExpiration;

    @Value("${his.auth.max-login-fail-count:5}")
    private int maxLoginFailCount;

    @Value("${his.auth.lock-duration-minutes:30}")
    private int lockDurationMinutes;

    private final RowMapper<AuthUser> userRowMapper = (rs, rowNum) -> {
        AuthUser user = new AuthUser();
        user.setId(rs.getLong("id"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        user.setRealName(rs.getString("real_name"));
        user.setEmail(rs.getString("email"));
        user.setPhone(rs.getString("phone"));
        user.setAvatar(rs.getString("avatar"));
        user.setRole(rs.getString("role"));
        user.setTenantId(rs.getString("tenant_id"));
        user.setStatus(rs.getString("status"));
        user.setLoginFailCount(rs.getInt("login_fail_count"));
        user.setLastLoginTime(rs.getTimestamp("last_login_time") != null
                ? rs.getTimestamp("last_login_time").toLocalDateTime() : null);
        user.setLastLoginIp(rs.getString("last_login_ip"));
        user.setLockedUntil(rs.getTimestamp("locked_until") != null
                ? rs.getTimestamp("locked_until").toLocalDateTime() : null);
        user.setDeleted(rs.getInt("deleted"));
        return user;
    };

    @Override
    public LoginResponse login(LoginRequest request, String clientIp) {
        String username = sanitizeInput(request.getUsername());

        AuthUser user = findByUsername(username);
        if (user == null) {
            log.warn("登录失败-用户不存在: username={}", username);
            throw new LoginException("HIS-A05", "用户名或密码错误");
        }

        if ("disabled".equals(user.getStatus())) {
            log.warn("登录失败-账户已禁用: username={}", username);
            throw new LoginException("HIS-A07", "账户已被禁用");
        }

        if ("locked".equals(user.getStatus()) && user.getLockedUntil() != null) {
            if (LocalDateTime.now().isBefore(user.getLockedUntil())) {
                log.warn("登录失败-账户已锁定: username={}, lockedUntil={}", username, user.getLockedUntil());
                throw new LoginException("HIS-A06", "账户已被锁定，请稍后重试");
            } else {
                resetLockStatus(username);
            }
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            int failCount = user.getLoginFailCount() + 1;
            handleLoginFailure(username, failCount);
            log.warn("登录失败-密码错误: username={}, failCount={}", username, failCount);
            throw new LoginException("HIS-A05", "用户名或密码错误");
        }

        resetLoginFailCount(username);
        updateLastLoginInfo(username, clientIp);

        String token = generateToken(user.getUsername(), user.getTenantId(), user.getRole());

        log.info("用户登录成功: username={}, tenantId={}, role={}", username, user.getTenantId(), user.getRole());

        return LoginResponse.builder()
                .token(token)
                .userId(user.getUsername())
                .username(user.getUsername())
                .realName(user.getRealName())
                .tenantId(user.getTenantId())
                .role(user.getRole())
                .build();
    }

    private AuthUser findByUsername(String username) {
        String sql = "SELECT id, username, password, real_name, email, phone, avatar, " +
                "role, tenant_id, status, login_fail_count, last_login_time, " +
                "last_login_ip, locked_until, deleted " +
                "FROM his_auth_user WHERE username = ? AND deleted = 0";
        List<AuthUser> users = jdbcTemplate.query(sql, userRowMapper, username);
        return users.isEmpty() ? null : users.get(0);
    }

    private void handleLoginFailure(String username, int failCount) {
        if (failCount >= maxLoginFailCount) {
            String sql = "UPDATE his_auth_user SET login_fail_count = ?, " +
                    "status = 'locked', locked_until = DATE_ADD(NOW(), INTERVAL ? MINUTE) " +
                    "WHERE username = ? AND deleted = 0";
            jdbcTemplate.update(sql, failCount, lockDurationMinutes, username);
            log.warn("账户已锁定: username={}, failCount={}, lockMinutes={}", username, failCount, lockDurationMinutes);
        } else {
            String sql = "UPDATE his_auth_user SET login_fail_count = ? WHERE username = ? AND deleted = 0";
            jdbcTemplate.update(sql, failCount, username);
        }
    }

    private void resetLoginFailCount(String username) {
        String sql = "UPDATE his_auth_user SET login_fail_count = 0 WHERE username = ? AND deleted = 0";
        jdbcTemplate.update(sql, username);
    }

    private void resetLockStatus(String username) {
        String sql = "UPDATE his_auth_user SET login_fail_count = 0, status = 'active', locked_until = NULL " +
                "WHERE username = ? AND deleted = 0";
        jdbcTemplate.update(sql, username);
    }

    private void updateLastLoginInfo(String username, String clientIp) {
        String sql = "UPDATE his_auth_user SET last_login_time = NOW(), last_login_ip = ? " +
                "WHERE username = ? AND deleted = 0";
        jdbcTemplate.update(sql, clientIp, username);
    }

    private String generateToken(String userId, String tenantId, String role) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
                .subject(userId)
                .claim("tenantId", tenantId)
                .claim("role", role)
                .issuedAt(new java.util.Date())
                .expiration(new java.util.Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(key)
                .compact();
    }

    private String sanitizeInput(String input) {
        if (input == null) return "";
        return input.replaceAll("[<>\"'&]", "").trim();
    }

    public static class LoginException extends RuntimeException {
        private final String code;

        public LoginException(String code, String message) {
            super(message);
            this.code = code;
        }

        public String getCode() {
            return code;
        }
    }
}
