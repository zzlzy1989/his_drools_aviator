package com.his.quality.controller;

import com.his.common.web.result.Result;
import com.his.quality.dto.QualityCheckDTO;
import com.his.quality.dto.QualityCheckVO;
import com.his.quality.service.QualityCheckService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/check")
    @Operation(summary = "执行质控检查")
    public Result<QualityCheckVO> check(@RequestBody @Valid QualityCheckDTO dto) {
        return Result.success(qualityCheckService.check(dto));
    }
}
