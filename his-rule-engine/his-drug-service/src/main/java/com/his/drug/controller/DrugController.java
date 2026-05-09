package com.his.drug.controller;

import com.his.common.web.result.PageResult;
import com.his.common.web.result.Result;
import com.his.common.web.context.TenantContext;
import com.his.drug.dto.PrescriptionDTO;
import com.his.drug.dto.PrescriptionReviewVO;
import com.his.drug.entity.DrugCatalog;
import com.his.drug.service.DrugCheckService;
import com.his.drug.mapper.DrugCatalogMapper;
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
 * 药品 Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/drugs")
@RequiredArgsConstructor
@Tag(name = "用药审核", description = "处方审核、配伍禁忌检查")
public class DrugController {

    private final DrugCheckService drugCheckService;
    private final DrugCatalogMapper drugCatalogMapper;

    @GetMapping
    @Operation(summary = "分页查询药品目录")
    public Result<PageResult<DrugCatalog>> pageList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String drugName,
            @RequestParam(required = false) String drugType,
            @RequestParam(required = false) String status) {
        LambdaQueryWrapper<DrugCatalog> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(drugName), DrugCatalog::getDrugName, drugName)
               .eq(StringUtils.hasText(drugType), DrugCatalog::getDrugType, drugType)
               .eq(StringUtils.hasText(status), DrugCatalog::getIsEnabled, status)
               .orderByDesc(DrugCatalog::getCreateTime);
        IPage<DrugCatalog> result = drugCatalogMapper.selectPage(new Page<>(page, pageSize), wrapper);
        return Result.success(PageResult.of(result));
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询药品详情")
    public Result<DrugCatalog> getById(@PathVariable Long id) {
        return Result.success(drugCatalogMapper.selectById(id));
    }

    @PostMapping
    @Operation(summary = "新增药品")
    public Result<Void> create(@RequestBody DrugCatalog drug) {
        if (drug.getDrugCode() == null || drug.getDrugCode().isEmpty()) {
            drug.setDrugCode("DRG" + System.currentTimeMillis());
        }
        drugCatalogMapper.insert(drug);
        return Result.success();
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新药品")
    public Result<Void> update(@PathVariable Long id, @RequestBody DrugCatalog drug) {
        drug.setId(id);
        drugCatalogMapper.updateById(drug);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除药品")
    public Result<Void> delete(@PathVariable Long id) {
        drugCatalogMapper.deleteById(id);
        return Result.success();
    }
}
