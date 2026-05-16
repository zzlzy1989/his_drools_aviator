package com.his.settlement.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.common.web.result.PageResult;
import com.his.common.web.result.Result;
import com.his.settlement.dto.SettlementDTO;
import com.his.settlement.dto.SettlementUpdateDTO;
import com.his.settlement.dto.SettlementVO;
import com.his.settlement.service.FormulaLoaderService;
import com.his.settlement.service.SettlementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 结算 Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/settlements")
@RequiredArgsConstructor
@Tag(name = "结算管理", description = "医保结算管理")
public class SettlementController {

    private final SettlementService settlementService;
    private final FormulaLoaderService formulaLoaderService;

    @PostMapping
    @Operation(summary = "发起结算")
    public Result<SettlementVO> settle(@RequestBody @Valid SettlementDTO dto) {
        return Result.success(settlementService.settle(dto));
    }

    @PostMapping("/cache/refresh")
    @Operation(summary = "刷新公式缓存")
    public Result<Void> refreshCache(
            @RequestParam String tenantId,
            @RequestParam String formulaKey) {
        formulaLoaderService.refreshCache(tenantId, formulaKey);
        return Result.success();
    }

    @PostMapping("/cache/clear")
    @Operation(summary = "清空公式缓存")
    public Result<Void> clearCache() {
        formulaLoaderService.clearCache();
        return Result.success();
    }

    @PostMapping("/refresh")
    @Operation(summary = "强制刷新缓存（热更新）")
    public Result<Void> forceRefresh(
            @RequestParam(required = false) String tenantId,
            @RequestParam(required = false) String formulaKey) {
        if (formulaKey != null && tenantId != null) {
            formulaLoaderService.refreshCache(tenantId, formulaKey);
            log.info("指定刷新: tenantId={}, formulaKey={}", tenantId, formulaKey);
        } else {
            formulaLoaderService.clearCache();
            log.info("全量刷新缓存");
        }
        return Result.success();
    }

    @GetMapping
    @Operation(summary = "分页查询结算记录")
    public Result<PageResult<SettlementVO>> pageList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String settlementNo,
            @RequestParam(required = false) String patientId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) Double minAmount,
            @RequestParam(required = false) Double maxAmount) {
        IPage<SettlementVO> pageResult = settlementService.pageList(page, pageSize, settlementNo, patientId, status, startDate, endDate, minAmount, maxAmount);
        return Result.success(PageResult.of(pageResult));
    }

    @GetMapping("/{settlementNo}")
    @Operation(summary = "根据结算单号查询")
    public Result<SettlementVO> getBySettlementNo(@PathVariable String settlementNo) {
        return Result.success(settlementService.getBySettlementNo(settlementNo));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新结算记录")
    public Result<Void> update(@PathVariable Long id, @RequestBody SettlementUpdateDTO dto) {
        settlementService.update(id, dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除结算记录")
    public Result<Void> delete(@PathVariable Long id) {
        settlementService.delete(id);
        return Result.success();
    }
}
