package com.his.rule.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.his.common.entity.AuditLog;
import com.his.common.mapper.AuditLogMapper;
import com.his.common.web.result.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 审计日志查询 Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogMapper auditLogMapper;

    private static final DateTimeFormatter DF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 分页查询审计日志
     */
    @GetMapping
    public Result<IPage<AuditLog>> pageList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String targetType,
            @RequestParam(required = false) String targetKey,
            @RequestParam(required = false) String operator,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        log.info("AuditLog query: page={}, pageSize={}, action={}, targetType={}, startDate={}, endDate={}",
                page, pageSize, action, targetType, startDate, endDate);

        Page<AuditLog> pageParam = new Page<>(page, pageSize);
        var wrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<AuditLog>()
                .eq(action != null && !action.isBlank(), AuditLog::getAction, action)
                .eq(targetType != null && !targetType.isBlank(), AuditLog::getTargetType, targetType)
                .like(targetKey != null && !targetKey.isBlank(), AuditLog::getTargetKey, targetKey)
                .eq(operator != null && !operator.isBlank(), AuditLog::getOperator, operator);

        // Only parse and apply date filters when values are provided
        if (startDate != null && !startDate.isBlank()) {
            wrapper.ge(AuditLog::getCreateTime, LocalDateTime.parse(startDate + " 00:00:00", DF));
        }
        if (endDate != null && !endDate.isBlank()) {
            wrapper.le(AuditLog::getCreateTime, LocalDateTime.parse(endDate + " 23:59:59", DF));
        }

        wrapper.orderByDesc(AuditLog::getCreateTime);

        log.debug("Executing audit log query...");
        IPage<AuditLog> pageResult = auditLogMapper.selectPage(pageParam, wrapper);
        log.debug("Query returned {} records", pageResult.getRecords().size());
        return Result.success(pageResult);
    }
}