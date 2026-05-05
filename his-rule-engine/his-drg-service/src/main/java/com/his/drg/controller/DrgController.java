package com.his.drg.controller;

import com.his.common.web.result.PageResult;
import com.his.common.web.result.Result;
import com.his.common.web.context.TenantContext;
import com.his.drg.dto.DrgGroupingDTO;
import com.his.drg.dto.DrgGroupingVO;
import com.his.drg.entity.DrgDefinition;
import com.his.drg.mapper.DrgDefinitionMapper;
import com.his.drg.service.DrgGroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import org.springframework.util.StringUtils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.Valid;

/**
 * DRG Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/drg")
@RequiredArgsConstructor
@Tag(name = "DRG管理", description = "DRG分组、权重计算")
public class DrgController {

    private final DrgGroupService drgGroupService;
    private final DrgDefinitionMapper drgDefinitionMapper;

    @GetMapping
    @Operation(summary = "分页查询DRG定义")
    public Result<PageResult<DrgDefinition>> pageList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String drgName,
            @RequestParam(required = false) String mdcCode,
            @RequestParam(required = false) String status) {
        LambdaQueryWrapper<DrgDefinition> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(drgName), DrgDefinition::getDrgName, drgName)
               .eq(StringUtils.hasText(status), DrgDefinition::getStatus, status)
               .eq(StringUtils.hasText(mdcCode), DrgDefinition::getMdcCode, mdcCode)
               .orderByDesc(DrgDefinition::getCreateTime);
        IPage<DrgDefinition> result = drgDefinitionMapper.selectPage(new Page<>(page, pageSize), wrapper);
        return Result.success(PageResult.of(result));
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询DRG定义详情")
    public Result<DrgDefinition> getById(@PathVariable Long id) {
        return Result.success(drgDefinitionMapper.selectById(id));
    }

    @PostMapping
    @Operation(summary = "新增DRG定义")
    public Result<Void> create(@RequestBody DrgDefinition drg) {
        drgDefinitionMapper.insert(drg);
        return Result.success();
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新DRG定义")
    public Result<Void> update(@PathVariable Long id, @RequestBody DrgDefinition drg) {
        drg.setId(id);
        drgDefinitionMapper.updateById(drg);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除DRG定义")
    public Result<Void> delete(@PathVariable Long id) {
        drgDefinitionMapper.deleteById(id);
        return Result.success();
    }
}
