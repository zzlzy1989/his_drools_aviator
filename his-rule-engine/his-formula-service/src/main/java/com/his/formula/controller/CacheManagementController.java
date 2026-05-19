package com.his.formula.controller;

import com.his.formula.service.CacheManagementService;
import com.his.common.web.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 缓存管理 Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/cache")
@RequiredArgsConstructor
@Tag(name = "缓存管理", description = "缓存统计和手动刷新")
public class CacheManagementController {

    private final CacheManagementService cacheManagementService;

    @GetMapping("/stats")
    @Operation(summary = "获取缓存统计")
    public Result<Map<String, Object>> getCacheStats() {
        return Result.success(cacheManagementService.getCacheStats());
    }

    @GetMapping("/keys")
    @Operation(summary = "获取缓存key列表")
    public Result<Map<String, Object>> getCacheKeys(
            @RequestParam(defaultValue = "aviator") String cacheName) {
        return Result.success(cacheManagementService.getCacheKeys(cacheName));
    }

    @PostMapping("/invalidate")
    @Operation(summary = "刷新指定缓存")
    public Result<Map<String, Object>> invalidateCache(
            @RequestParam String cacheName,
            @RequestParam(required = false) String key) {
        return Result.success(cacheManagementService.invalidateCache(cacheName, key));
    }

    @PostMapping("/refresh")
    @Operation(summary = "刷新所有缓存")
    public Result<Map<String, Object>> refreshAll() {
        var result = cacheManagementService.invalidateCache("aviator", null);
        var ruleResult = cacheManagementService.invalidateCache("rule", null);
        return Result.success(Map.of(
                "aviator", result,
                "rule", ruleResult,
                "timestamp", System.currentTimeMillis()
        ));
    }
}