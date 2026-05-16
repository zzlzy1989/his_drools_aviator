package com.his.formula.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.common.web.result.PageResult;
import com.his.common.web.result.Result;
import com.his.formula.dto.*;
import com.his.formula.service.FormulaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 公式管理 Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/formulas")
@RequiredArgsConstructor
@Tag(name = "公式管理", description = "公式 CRUD 和发布")
public class FormulaController {

    private final FormulaService formulaService;

    @GetMapping
    @Operation(summary = "分页查询公式")
    public Result<PageResult<FormulaVO>> pageList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize,
            FormulaQueryDTO queryDTO) {
        IPage<FormulaVO> pageResult = formulaService.pageList(page, pageSize, queryDTO);
        return Result.success(PageResult.of(pageResult));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取公式详情")
    public Result<FormulaVO> getById(@PathVariable Long id) {
        return Result.success(formulaService.getById(id));
    }

    @GetMapping("/key/{formulaKey}")
    @Operation(summary = "根据KEY获取公式")
    public Result<FormulaVO> getByKey(@PathVariable String formulaKey) {
        return Result.success(formulaService.getByKey(formulaKey));
    }

    @PostMapping
    @Operation(summary = "创建公式")
    public Result<FormulaVO> create(@RequestBody @Valid FormulaCreateDTO dto) {
        return Result.success(formulaService.create(dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新公式")
    public Result<FormulaVO> update(@PathVariable Long id,
                                      @RequestBody @Valid FormulaUpdateDTO dto) {
        return Result.success(formulaService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除公式")
    public Result<Void> delete(@PathVariable Long id) {
        formulaService.delete(id);
        return Result.success(null);
    }

    @PostMapping("/{id}/publish")
    @Operation(summary = "发布公式")
    public Result<FormulaVO> publish(@PathVariable Long id) {
        return Result.success(formulaService.publish(id));
    }

    @PostMapping("/{id}/validate")
    @Operation(summary = "校验公式")
    public Result<String> validate(@PathVariable Long id) {
        String message = formulaService.validate(id);
        return Result.success(message);
    }

    @PostMapping("/{id}/snapshot")
    @Operation(summary = "保存快照")
    public Result<Void> saveSnapshot(@PathVariable Long id) {
        formulaService.saveSnapshot(id);
        return Result.success(null);
    }

    @PostMapping("/{id}/rollback")
    @Operation(summary = "回滚到指定版本")
    public Result<FormulaVO> rollback(@PathVariable Long id,
                                       @RequestParam Integer version) {
        return Result.success(formulaService.rollback(id, version));
    }

    @PostMapping("/{id}/test")
    @Operation(summary = "测试公式")
    public Result<Object> test(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        Object result = formulaService.test(id, params);
        return Result.success(result);
    }

    @GetMapping("/{id}/versions")
    @Operation(summary = "获取版本历史")
    public Result<List<FormulaHistoryVO>> getVersionHistory(@PathVariable Long id) {
        return Result.success(formulaService.getVersionHistory(id));
    }
}
