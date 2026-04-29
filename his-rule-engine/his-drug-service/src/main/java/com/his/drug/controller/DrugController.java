package com.his.drug.controller;

import com.his.common.web.result.Result;
import com.his.drug.dto.PrescriptionDTO;
import com.his.drug.dto.PrescriptionReviewVO;
import com.his.drug.service.DrugCheckService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/review")
    @Operation(summary = "审核处方")
    public Result<PrescriptionReviewVO> reviewPrescription(@RequestBody @Valid PrescriptionDTO prescription) {
        return Result.success(drugCheckService.reviewPrescription(prescription));
    }
}
