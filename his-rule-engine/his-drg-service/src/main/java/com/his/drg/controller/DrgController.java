package com.his.drg.controller;

import com.his.common.web.result.Result;
import com.his.drg.dto.DrgGroupingDTO;
import com.his.drg.dto.DrgGroupingVO;
import com.his.drg.service.DrgGroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/grouping")
    @Operation(summary = "执行DRG分组")
    public Result<DrgGroupingVO> grouping(@RequestBody @Valid DrgGroupingDTO dto) {
        return Result.success(drgGroupService.grouping(dto));
    }
}
