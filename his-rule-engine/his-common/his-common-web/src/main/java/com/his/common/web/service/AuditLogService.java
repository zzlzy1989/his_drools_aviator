package com.his.common.web.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.his.common.entity.AuditLog;
import com.his.common.mapper.AuditLogMapper;
import com.his.common.web.context.TenantContext;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Map;

/**
 * 审计日志服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogMapper auditLogMapper;
    private final ObjectMapper objectMapper;

    /**
     * 记录审计日志
     */
    @Async
    public void log(String action, String targetType, String targetId, String targetKey, Object detail) {
        try {
            AuditLog auditLog = new AuditLog();
            auditLog.setTenantId(TenantContext.getTenantId());
            auditLog.setAction(action);
            auditLog.setTargetType(targetType);
            auditLog.setTargetId(targetId);
            auditLog.setTargetKey(targetKey);
            auditLog.setOperator(TenantContext.getTenantId());

            if (detail != null) {
                auditLog.setDetail(objectMapper.writeValueAsString(detail));
            }

            HttpServletRequest request = getCurrentRequest();
            if (request != null) {
                auditLog.setIpAddress(getClientIp(request));
                auditLog.setUserAgent(request.getHeader("User-Agent"));
            }

            auditLogMapper.insert(auditLog);

            log.debug("审计日志记录: action={}, targetType={}, targetId={}",
                    action, targetType, targetId);
        } catch (Exception e) {
            log.error("审计日志记录失败", e);
        }
    }

    /**
     * 记录创建操作
     */
    public void logCreate(String targetType, String targetId, String targetKey, Object detail) {
        log("CREATE", targetType, targetId, targetKey, detail);
    }

    /**
     * 记录更新操作
     */
    public void logUpdate(String targetType, String targetId, String targetKey, Object detail) {
        log("UPDATE", targetType, targetId, targetKey, detail);
    }

    /**
     * 记录删除操作
     */
    public void logDelete(String targetType, String targetId, String targetKey) {
        log("DELETE", targetType, targetId, targetKey, null);
    }

    /**
     * 记录发布操作
     */
    public void logPublish(String targetType, String targetId, String targetKey) {
        log("PUBLISH", targetType, targetId, targetKey, null);
    }

    /**
     * 记录校验操作
     */
    public void logValidate(String targetType, String targetId, String targetKey, Object detail) {
        log("VALIDATE", targetType, targetId, targetKey, detail);
    }

    /**
     * 获取当前请求
     */
    private HttpServletRequest getCurrentRequest() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            return attrs != null ? attrs.getRequest() : null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取客户端IP
     */
    private String getClientIp(HttpServletRequest request) {
        String[] headers = {
                "X-Forwarded-For",
                "X-Real-IP",
                "Proxy-Client-IP",
                "WL-Proxy-Client-IP"
        };

        for (String header : headers) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                return ip.split(",")[0].trim();
            }
        }

        return request.getRemoteAddr();
    }
}
