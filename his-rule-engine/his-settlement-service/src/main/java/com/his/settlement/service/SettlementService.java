package com.his.settlement.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.his.common.SettlementFact;
import com.his.common.SkillContext;
import com.his.common.SkillResult;
import com.his.common.ResultLevel;
import com.his.common.web.context.TenantContext;
import com.his.common.web.exception.BusinessException;
import com.his.common.web.service.AuditLogService;
import com.his.settlement.dto.SettlementDTO;
import com.his.settlement.dto.SettlementUpdateDTO;
import com.his.settlement.dto.SettlementVO;
import com.his.settlement.entity.SettlementResult;
import com.his.settlement.mapper.SettlementResultMapper;
import com.his.settlement.pipeline.SkillPipelineExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    private final FormulaLoaderService formulaLoaderService;
    private final SkillPipelineExecutor skillPipelineExecutor;

    private static final DateTimeFormatter SNO_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    /**
     * 发起结算
     */
    @Transactional
    public SettlementVO settle(SettlementDTO dto) {
        String tenantId = TenantContext.getTenantId();

        // 费用不能为负数
        if (dto.getTotalFee() == null || dto.getTotalFee().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("HIS-104", "费用不能为负数");
        }

        // 患者类型不能为空
        if (!StringUtils.hasText(dto.getPatientType())) {
            throw new BusinessException("HIS-102", "患者类型不能为空");
        }

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

        SettlementFact fact = new SettlementFact();
        fact.setPatientId(dto.getPatientId());
        fact.setPatientType(dto.getPatientType());
        fact.setInsuranceType(dto.getInsuranceType());
        fact.setHospitalLevel(dto.getHospitalLevel());
        fact.setTotalFee(dto.getTotalFee());
        fact.setTenantId(tenantId);
        fact.setSettlementId(String.valueOf(result.getId()));

        SkillContext<SettlementFact> context = new SkillContext<>();
        context.setTenantId(tenantId);
        context.setEventType("EVENT_FEE_SETTLE");
        context.setPayload(fact);

        skillPipelineExecutor.execute(context);

        result.setDeductible(fact.getDeductible());
        result.setRatio(fact.getRatio());

        if (fact.getTotalFee().compareTo(fact.getDeductible()) <= 0) {
            result.setReimburseAmount(BigDecimal.ZERO);
            result.setSelfPayAmount(fact.getTotalFee());
        } else {
            BigDecimal reimburseAmount = formulaLoaderService.executeReimburseFormula(
                    result.getTenantId(),
                    result.getPatientType(),
                    result.getTotalFee(),
                    fact.getDeductible(),
                    fact.getRatio()
            );
            result.setReimburseAmount(reimburseAmount);
            result.setSelfPayAmount(fact.getTotalFee().subtract(reimburseAmount));
        }

        updateSettlementResult(result, context);

        settlementMapper.updateById(result);

        log.info("结算完成: settlementNo={}, visitId={}, result={}",
                result.getSettlementNo(), dto.getVisitId(), result.getResultLevel());

        auditLogService.log("SETTLEMENT", "SETTLEMENT", String.valueOf(result.getId()),
                result.getSettlementNo(), result);

        return convertToVO(result);
    }

    /**
     * 更新结算结果
     */
    private void updateSettlementResult(SettlementResult result, SkillContext<?> context) {
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
     * 分页查询结算记录（支持时间范围和金额范围）
     */
    public IPage<SettlementVO> pageList(Integer page, Integer pageSize, String settlementNo, String patientId, String status,
                                        String startDate, String endDate, Double minAmount, Double maxAmount) {
        String tenantId = TenantContext.getTenantId();

        Page<SettlementResult> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<SettlementResult> wrapper = new LambdaQueryWrapper<SettlementResult>()
                .eq(SettlementResult::getTenantId, tenantId)
                .like(StringUtils.hasText(settlementNo), SettlementResult::getSettlementNo, settlementNo)
                .eq(StringUtils.hasText(patientId), SettlementResult::getPatientId, patientId)
                .eq(StringUtils.hasText(status), SettlementResult::getStatus, status)
                .ge(startDate != null && !startDate.isBlank(), SettlementResult::getCreateTime, parseDate(startDate))
                .le(endDate != null && !endDate.isBlank(), SettlementResult::getCreateTime, parseEndDate(endDate))
                .ge(minAmount != null, SettlementResult::getTotalFee, BigDecimal.valueOf(minAmount))
                .le(maxAmount != null, SettlementResult::getTotalFee, BigDecimal.valueOf(maxAmount))
                .orderByDesc(SettlementResult::getCreateTime);

        IPage<SettlementResult> pageResult = settlementMapper.selectPage(pageParam, wrapper);
        return pageResult.convert(this::convertToVO);
    }

    private LocalDateTime parseDate(String dateStr) {
        try {
            return LocalDateTime.parse(dateStr + " 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        } catch (Exception e) {
            log.warn("日期解析失败: {}", dateStr);
            return null;
        }
    }

    private LocalDateTime parseEndDate(String dateStr) {
        try {
            return LocalDateTime.parse(dateStr + " 23:59:59", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        } catch (Exception e) {
            log.warn("日期解析失败: {}", dateStr);
            return null;
        }
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
     * 更新结算记录
     */
    @Transactional
    public void update(Long id, SettlementUpdateDTO dto) {
        String tenantId = TenantContext.getTenantId();
        SettlementResult result = settlementMapper.selectOne(
                new LambdaQueryWrapper<SettlementResult>()
                        .eq(SettlementResult::getId, id)
                        .eq(SettlementResult::getTenantId, tenantId)
        );
        if (result == null) {
            throw new BusinessException("HIS-005", "结算记录不存在");
        }
        if (dto.getVisitId() != null) {
            result.setVisitId(dto.getVisitId());
        }
        if (dto.getPatientId() != null) {
            result.setPatientId(dto.getPatientId());
        }
        if (dto.getPatientType() != null) {
            result.setPatientType(dto.getPatientType());
        }
        if (dto.getInsuranceType() != null) {
            result.setInsuranceType(dto.getInsuranceType());
        }
        if (dto.getHospitalLevel() != null) {
            result.setHospitalLevel(dto.getHospitalLevel());
        }
        if (dto.getTotalFee() != null) {
            result.setTotalFee(dto.getTotalFee());
        }
        settlementMapper.updateById(result);
        auditLogService.log("UPDATE_SETTLEMENT", "SETTLEMENT", String.valueOf(id),
                result.getSettlementNo(), result);
    }

    /**
     * 删除结算记录
     */
    @Transactional
    public void delete(Long id) {
        String tenantId = TenantContext.getTenantId();
        SettlementResult result = settlementMapper.selectOne(
                new LambdaQueryWrapper<SettlementResult>()
                        .eq(SettlementResult::getId, id)
                        .eq(SettlementResult::getTenantId, tenantId)
        );
        if (result == null) {
            throw new BusinessException("HIS-005", "结算记录不存在");
        }
        settlementMapper.deleteById(id);
        auditLogService.log("DELETE_SETTLEMENT", "SETTLEMENT", String.valueOf(id),
                result.getSettlementNo(), result);
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
