package com.his.drg.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.his.common.web.context.TenantContext;
import com.his.common.web.exception.BusinessException;
import com.his.drg.dto.DrgGroupingDTO;
import com.his.drg.dto.DrgGroupingVO;
import com.his.drg.entity.DrgDefinition;
import com.his.drg.mapper.DrgDefinitionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * DRG 分组服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DrgGroupService {

    private final DrgDefinitionMapper drgDefinitionMapper;

    /**
     * 执行 DRG 分组
     */
    public DrgGroupingVO grouping(DrgGroupingDTO dto) {
        String tenantId = TenantContext.getTenantId();

        DrgDefinition drg = matchDrg(dto, tenantId);

        DrgGroupingVO result = new DrgGroupingVO();
        result.setVisitId(dto.getVisitId());

        if (drg == null) {
            result.setSuccess(false);
            result.setGroupingStatus("UNGROUPED");
            result.setTotalFee(dto.getTotalFee());
            log.info("DRG分组失败: visitId={}, 原因=未匹配到DRG组", dto.getVisitId());
            return result;
        }

        result.setSuccess(true);
        result.setDrgCode(drg.getDrgCode());
        result.setDrgName(drg.getDrgName());
        result.setMdcCode(drg.getMdcCode());
        result.setMdcName(drg.getMdcName());
        result.setBaseWeight(drg.getBaseWeight());
        result.setTotalFee(dto.getTotalFee());

        BigDecimal adjustWeight = calculateAdjustWeight(drg, dto);
        result.setAdjustWeight(adjustWeight);

        BigDecimal standardScore = adjustWeight.multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP);
        result.setStandardScore(standardScore);

        BigDecimal paymentAmount = calculatePayment(drg, adjustWeight, dto.getTotalFee());
        result.setPaymentAmount(paymentAmount);

        result.setGroupingStatus("GROUPED");

        log.info("DRG分组完成: visitId={}, drgCode={}, paymentAmount={}",
                dto.getVisitId(), drg.getDrgCode(), paymentAmount);

        return result;
    }

    /**
     * 匹配 DRG 组
     */
    private DrgDefinition matchDrg(DrgGroupingDTO dto, String tenantId) {
        LambdaQueryWrapper<DrgDefinition> wrapper = new LambdaQueryWrapper<DrgDefinition>()
                .eq(DrgDefinition::getTenantId, tenantId)
                .eq(DrgDefinition::getStatus, "active")
                .eq(DrgDefinition::getDeleted, 0)
                .like(DrgDefinition::getDrgName, dto.getPrimaryDiagnosis().substring(0, Math.min(4, dto.getPrimaryDiagnosis().length())))
                .last("LIMIT 1");

        List<DrgDefinition> candidates = drgDefinitionMapper.selectList(wrapper);

        if (candidates.isEmpty()) {
            wrapper = new LambdaQueryWrapper<DrgDefinition>()
                    .eq(DrgDefinition::getTenantId, tenantId)
                    .eq(DrgDefinition::getStatus, "active")
                    .eq(DrgDefinition::getDeleted, 0)
                    .isNull(DrgDefinition::getRulesText)
                    .last("LIMIT 1");
            candidates = drgDefinitionMapper.selectList(wrapper);
        }

        return candidates.isEmpty() ? null : candidates.get(0);
    }

    /**
     * 计算调整权重
     */
    private BigDecimal calculateAdjustWeight(DrgDefinition drg, DrgGroupingDTO dto) {
        BigDecimal baseWeight = drg.getBaseWeight() != null ? drg.getBaseWeight() : BigDecimal.ONE;
        BigDecimal adjustFactor = drg.getAdjustFactor() != null ? drg.getAdjustFactor() : BigDecimal.ONE;

        BigDecimal feeRatio = dto.getTotalFee().divide(drg.getBaseFee(), 4, RoundingMode.HALF_UP);
        BigDecimal feeWeight = feeRatio.multiply(new BigDecimal("0.3"));

        return baseWeight.multiply(adjustFactor).add(feeWeight).setScale(4, RoundingMode.HALF_UP);
    }

    /**
     * 计算支付金额
     */
    private BigDecimal calculatePayment(DrgDefinition drg, BigDecimal adjustWeight, BigDecimal totalFee) {
        if (drg.getBaseFee() == null) {
            return adjustWeight.multiply(totalFee).setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal standardPayment = adjustWeight.multiply(drg.getBaseFee()).setScale(2, RoundingMode.HALF_UP);

        BigDecimal lower = drg.getBaseFee().multiply(new BigDecimal("0.8"));
        BigDecimal upper = drg.getBaseFee().multiply(new BigDecimal("1.2"));

        if (totalFee.compareTo(lower) < 0) {
            return lower.setScale(2, RoundingMode.HALF_UP);
        } else if (totalFee.compareTo(upper) > 0) {
            return upper.setScale(2, RoundingMode.HALF_UP);
        }

        return standardPayment;
    }
}
