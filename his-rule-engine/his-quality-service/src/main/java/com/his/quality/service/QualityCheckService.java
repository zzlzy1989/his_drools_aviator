package com.his.quality.service;

import com.his.common.web.context.TenantContext;
import com.his.quality.dto.QualityCheckDTO;
import com.his.quality.dto.QualityCheckVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 质控检查服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QualityCheckService {

    /**
     * 执行质控检查
     */
    public QualityCheckVO check(QualityCheckDTO dto) {
        String tenantId = TenantContext.getTenantId();
        List<QualityCheckVO.CheckItem> checkItems = new ArrayList<>();
        boolean hasBlock = false;
        boolean hasWarn = false;

        checkItems.addAll(checkInfectionControl(dto, tenantId));
        checkItems.addAll(checkQualityIndicators(dto, tenantId));

        for (QualityCheckVO.CheckItem item : checkItems) {
            if ("BLOCK".equals(item.getLevel())) {
                hasBlock = true;
            } else if ("WARN".equals(item.getLevel())) {
                hasWarn = true;
            }
        }

        QualityCheckVO result = new QualityCheckVO();
        result.setVisitId(dto.getVisitId());
        result.setCheckItems(checkItems);
        result.setPass(!hasBlock);
        result.setCheckStatus(hasBlock ? "BLOCKED" : (hasWarn ? "WARNED" : "PASSED"));

        log.info("质控检查完成: visitId={}, status={}", dto.getVisitId(), result.getCheckStatus());

        return result;
    }

    /**
     * 院感检查
     */
    private List<QualityCheckVO.CheckItem> checkInfectionControl(QualityCheckDTO dto, String tenantId) {
        List<QualityCheckVO.CheckItem> items = new ArrayList<>();

        if (dto.getDiagnosisCodes() == null || dto.getDiagnosisCodes().isEmpty()) {
            return items;
        }

        for (String code : dto.getDiagnosisCodes()) {
            if (code.startsWith("J18") || code.startsWith("J12")) {
                QualityCheckVO.CheckItem item = new QualityCheckVO.CheckItem();
                item.setLevel("WARN");
                item.setSource("InfectionControlCheck");
                item.setCode(code);
                item.setMessage("疑似感染性疾病诊断，需关注院感防控");
                items.add(item);
            }
        }

        return items;
    }

    /**
     * 质控指标检查
     */
    private List<QualityCheckVO.CheckItem> checkQualityIndicators(QualityCheckDTO dto, String tenantId) {
        List<QualityCheckVO.CheckItem> items = new ArrayList<>();

        if (dto.getProcedureCodes() != null) {
            for (String code : dto.getProcedureCodes()) {
                if ("S51.0".equals(code) || "S51.9".equals(code)) {
                    QualityCheckVO.CheckItem item = new QualityCheckVO.CheckItem();
                    item.setLevel("WARN");
                    item.setSource("QualityIndicatorCheck");
                    item.setCode(code);
                    item.setMessage("浅表外伤处理，需评估抗生素使用必要性");
                    items.add(item);
                }
            }
        }

        return items;
    }
}
