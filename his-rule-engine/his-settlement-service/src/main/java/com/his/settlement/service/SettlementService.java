package com.his.settlement.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.his.common.SkillContext;
import com.his.common.SkillResult;
import com.his.common.enums.ResultLevel;
import com.his.common.web.context.TenantContext;
import com.his.common.web.exception.BusinessException;
import com.his.common.web.service.AuditLogService;
import com.his.settlement.dto.SettlementDTO;
import com.his.settlement.dto.SettlementVO;
import com.his.settlement.entity.SettlementResult;
import com.his.settlement.mapper.SettlementResultMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * 结算服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SettlementService {

    private final SettlementResultMapper settlementMapper;
    private final AuditLogService auditLogService;

    private static final DateTimeFormatter SNO_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    /**
     * 发起结算
     */
    @Transactional
    public SettlementVO settle(SettlementDTO dto) {
        String tenantId = TenantContext.getTenantId();

        SettlementResult existing = settlementMapper.selectOne(
                new LambdaQueryWrapper<SettlementResult>()
                        .eq(SettlementResult::getTenantId, tenantId)
                        .eq(SettlementResult::getVisitId, dto.getVisitId())
                        .in(SettlementResult::getStatus, "pending", "completed")
        );
        if (existing != null) {
            throw new BusinessException("HIS-201", "该就诊存在进行中的结算");
        }

        SettlementResult result = new SettlementResult();
        result.setSettlementNo(generateSettlementNo());
        result.setVisitId(dto.getVisitId());
        result.setPatientId(dto.getPatientId());
        result.setPatientType(dto.getPatientType());
        result.setInsuranceType(dto.getInsuranceType());
        result.setHospitalLevel(dto.getHospitalLevel());
        result.setTotalFee(dto.getTotalFee());
        result.setStatus("pending");
        result.setResultLevel("PASS");
        result.setTenantId(tenantId);
        result.setCreateBy(tenantId);

        settlementMapper.insert(result);

        SkillContext context = new SkillContext();
        context.setTenantId(tenantId);
        context.setEventType("EVENT_FEE_SETTLE");

        executeSettlementSkills(context, result);

        updateSettlementResult(result, context);

        settlementMapper.updateById(result);

        log.info("结算完成: settlementNo={}, visitId={}, result={}",
                result.getSettlementNo(), dto.getVisitId(), result.getResultLevel());

        auditLogService.log("SETTLEMENT", "SETTLEMENT", String.valueOf(result.getId()),
                result.getSettlementNo(), result);

        return convertToVO(result);
    }

    /**
     * 执行结算规则
     */
    private void executeSettlementSkills(SkillContext context, SettlementResult result) {
        context.addResult(new SkillResult(ResultLevel.PASS, "System", "结算流程启动"));

        if (result.getPatientType() == null || result.getPatientType().isBlank()) {
            context.addResult(new SkillResult(ResultLevel.BLOCK, "IdentityCheck", "患者类型缺失"));
            return;
        }

        BigDecimal deductible = calculateDeductible(result.getPatientType());
        result.setDeductible(deductible);

        BigDecimal ratio = calculateRatio(result.getPatientType(), result.getInsuranceType());
        result.setRatio(ratio);

        if (result.getTotalFee().compareTo(deductible) <= 0) {
            result.setReimburseAmount(BigDecimal.ZERO);
            result.setSelfPayAmount(result.getTotalFee());
            context.addResult(new SkillResult(ResultLevel.WARN, "DeductibleCheck", "未达到起付线"));
            return;
        }

        BigDecimal baseAmount = result.getTotalFee().subtract(deductible);
        BigDecimal reimburseAmount = baseAmount.multiply(ratio).setScale(2, java.math.RoundingMode.HALF_UP);
        result.setReimburseAmount(reimburseAmount);
        result.setSelfPayAmount(result.getTotalFee().subtract(reimburseAmount));

        context.addResult(new SkillResult(ResultLevel.PASS, "ReimburseCalc", "报销金额计算完成"));
    }

    /**
     * 计算起付线
     */
    private BigDecimal calculateDeductible(String patientType) {
        return switch (patientType.toLowerCase()) {
            case "employee" -> new BigDecimal("1000");
            case "resident" -> new BigDecimal("500");
            case "aid" -> new BigDecimal("300");
            default -> BigDecimal.ZERO;
        };
    }

    /**
     * 计算报销比例
     */
    private BigDecimal calculateRatio(String patientType, String insuranceType) {
        BigDecimal baseRatio = switch (patientType.toLowerCase()) {
            case "employee" -> new BigDecimal("0.85");
            case "resident" -> new BigDecimal("0.65");
            case "aid" -> new BigDecimal("0.50");
            default -> new BigDecimal("0.00");
        };

        if ("三级".equals(insuranceType) || "3".equals(insuranceType)) {
            baseRatio = baseRatio.multiply(new BigDecimal("0.9"));
        }

        return baseRatio;
    }

    /**
     * 更新结算结果
     */
    private void updateSettlementResult(SettlementResult result, SkillContext context) {
        boolean hasBlock = context.hasBlock();
        boolean hasWarn = context.getResults().stream()
                .anyMatch(r -> r.getLevel() == ResultLevel.WARN);

        if (hasBlock) {
            result.setResultLevel("BLOCK");
            result.setStatus("failed");
        } else if (hasWarn) {
            result.setResultLevel("WARN");
            result.setStatus("completed");
        } else {
            result.setResultLevel("PASS");
            result.setStatus("completed");
        }
    }

    /**
     * 生成结算单号
     */
    private String generateSettlementNo() {
        return "ST" + LocalDateTime.now().format(SNO_FORMATTER) + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    /**
     * 分页查询结算记录
     */
    public IPage<SettlementVO> pageList(Integer page, Integer pageSize, String patientId, String status) {
        String tenantId = TenantContext.getTenantId();

        Page<SettlementResult> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<SettlementResult> wrapper = new LambdaQueryWrapper<SettlementResult>()
                .eq(SettlementResult::getTenantId, tenantId)
                .eq(patientId != null, SettlementResult::getPatientId, patientId)
                .eq(status != null, SettlementResult::getStatus, status)
                .orderByDesc(SettlementResult::getCreateTime);

        IPage<SettlementResult> pageResult = settlementMapper.selectPage(pageParam, wrapper);
        return pageResult.convert(this::convertToVO);
    }

    /**
     * 根据结算单号查询
     */
    public SettlementVO getBySettlementNo(String settlementNo) {
        String tenantId = TenantContext.getTenantId();
        SettlementResult result = settlementMapper.selectOne(
                new LambdaQueryWrapper<SettlementResult>()
                        .eq(SettlementResult::getTenantId, tenantId)
                        .eq(SettlementResult::getSettlementNo, settlementNo)
        );
        if (result == null) {
            throw new BusinessException("结算记录不存在");
        }
        return convertToVO(result);
    }

    /**
     * 转换为 VO
     */
    private SettlementVO convertToVO(SettlementResult result) {
        SettlementVO vo = new SettlementVO();
        vo.setId(result.getId());
        vo.setSettlementNo(result.getSettlementNo());
        vo.setVisitId(result.getVisitId());
        vo.setPatientId(result.getPatientId());
        vo.setPatientType(result.getPatientType());
        vo.setInsuranceType(result.getInsuranceType());
        vo.setHospitalLevel(result.getHospitalLevel());
        vo.setTotalFee(result.getTotalFee());
        vo.setDeductible(result.getDeductible());
        vo.setRatio(result.getRatio());
        vo.setReimburseAmount(result.getReimburseAmount());
        vo.setSelfPayAmount(result.getSelfPayAmount());
        vo.setResultLevel(result.getResultLevel());
        vo.setStatus(result.getStatus());
        vo.setTenantId(result.getTenantId());
        vo.setCreateBy(result.getCreateBy());
        vo.setCreateTime(result.getCreateTime());
        return vo;
    }
}
