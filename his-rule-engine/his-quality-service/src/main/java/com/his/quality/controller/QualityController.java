package com.his.quality.controller;

import com.his.common.web.exception.BusinessException;
import com.his.common.web.result.ErrorCode;
import com.his.common.web.result.PageResult;
import com.his.common.web.result.Result;
import com.his.common.web.context.TenantContext;
import com.his.quality.dto.QualityCheckDTO;
import com.his.quality.dto.QualityCheckVO;
import com.his.quality.entity.QualityDefinition;
import com.his.quality.mapper.QualityDefinitionMapper;
import com.his.quality.service.QualityCheckService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * 质控 Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/quality")
@RequiredArgsConstructor
@Tag(name = "质控管理", description = "院感检查、质控指标检查")
public class QualityController {

    private final QualityCheckService qualityCheckService;
    private final QualityDefinitionMapper qualityDefinitionMapper;

    @GetMapping
    @Operation(summary = "分页查询质控规则")
    public Result<PageResult<QualityDefinition>> pageList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String itemName,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String status) {
        LambdaQueryWrapper<QualityDefinition> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(itemName), QualityDefinition::getItemName, itemName)
               .eq(StringUtils.hasText(category), QualityDefinition::getCategory, category)
               .eq(StringUtils.hasText(level), QualityDefinition::getLevel, level)
               .eq(StringUtils.hasText(status), QualityDefinition::getStatus, status)
               .orderByDesc(QualityDefinition::getCreateTime);
        IPage<QualityDefinition> result = qualityDefinitionMapper.selectPage(new Page<>(page, pageSize), wrapper);
        return Result.success(PageResult.of(result));
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询质控规则详情")
    public Result<QualityDefinition> getById(@PathVariable Long id) {
        return Result.success(qualityDefinitionMapper.selectById(id));
    }

    @PostMapping
    @Operation(summary = "新增质控规则")
    public Result<Void> create(@RequestBody QualityDefinition quality) {
        if (quality.getItemKey() == null || quality.getItemKey().isEmpty()) {
            quality.setItemKey("QC_" + System.currentTimeMillis());
        }
        if (quality.getStatus() == null) {
            quality.setStatus("active");
        }
        qualityDefinitionMapper.insert(quality);
        return Result.success();
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新质控规则")
    public Result<Void> update(@PathVariable Long id, @RequestBody QualityDefinition quality) {
        quality.setId(id);
        qualityDefinitionMapper.updateById(quality);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除质控规则")
    public Result<Void> delete(@PathVariable Long id) {
        QualityDefinition existing = qualityDefinitionMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(ErrorCode.QUALITY_RULE_NOT_FOUND, id);
        }
        qualityDefinitionMapper.deleteById(id);
        return Result.success();
    }
}
