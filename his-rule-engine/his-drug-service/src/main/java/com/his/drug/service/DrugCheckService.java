package com.his.drug.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.his.common.SkillResult;
import com.his.common.enums.ResultLevel;
import com.his.common.web.context.TenantContext;
import com.his.drug.dto.PrescriptionDTO;
import com.his.drug.dto.PrescriptionReviewVO;
import com.his.drug.entity.DrugCatalog;
import com.his.drug.entity.DrugInteraction;
import com.his.drug.entity.PatientAllergy;
import com.his.drug.mapper.DrugCatalogMapper;
import com.his.drug.mapper.DrugInteractionMapper;
import com.his.drug.mapper.PatientAllergyMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 药品审核服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DrugCheckService {

    private final DrugInteractionMapper drugInteractionMapper;
    private final DrugCatalogMapper drugCatalogMapper;
    private final PatientAllergyMapper patientAllergyMapper;

    /**
     * 审核处方
     */
    public PrescriptionReviewVO reviewPrescription(PrescriptionDTO prescription) {
        String tenantId = TenantContext.getTenantId();
        List<PrescriptionReviewVO.ReviewItem> reviewItems = new ArrayList<>();
        boolean hasBlock = false;
        boolean hasWarn = false;

        List<DrugInteraction> interactions = checkDrugInteractions(prescription, tenantId);
        for (DrugInteraction interaction : interactions) {
            PrescriptionReviewVO.ReviewItem item = new PrescriptionReviewVO.ReviewItem();
            item.setDrugCode(interaction.getDrugCodeA());
            item.setDrugName(interaction.getDrugNameA());
            item.setSource("DrugInteractionCheck");

            if ("contraindication".equals(interaction.getInteractionType())
                    && "severe".equals(interaction.getSeverityLevel())) {
                item.setLevel("BLOCK");
                item.setMessage("严重配伍禁忌: " + interaction.getDrugNameA() + " + " + interaction.getDrugNameB());
                hasBlock = true;
            } else if ("warning".equals(interaction.getInteractionType())) {
                item.setLevel("WARN");
                item.setMessage("配伍警告: " + interaction.getDrugNameA() + " + " + interaction.getDrugNameB());
                hasWarn = true;
            } else {
                item.setLevel("PASS");
                item.setMessage("正常配伍");
            }
            reviewItems.add(item);
        }

        if (reviewItems.isEmpty()) {
            PrescriptionReviewVO.ReviewItem item = new PrescriptionReviewVO.ReviewItem();
            item.setLevel("PASS");
            item.setSource("DrugInteractionCheck");
            item.setMessage("未检测到配伍禁忌");
            reviewItems.add(item);
        }

        checkDosageLimits(prescription, tenantId, reviewItems);

        checkCatalogLimits(prescription, tenantId, reviewItems);

        checkAllergyHistory(prescription, tenantId, reviewItems);

        PrescriptionReviewVO result = new PrescriptionReviewVO();
        result.setVisitId(prescription.getVisitId());
        result.setReviewItems(reviewItems);
        result.setPass(!hasBlock);
        result.setReviewStatus(hasBlock ? "BLOCKED" : (hasWarn ? "WARNED" : "PASSED"));

        log.info("处方审核完成: visitId={}, status={}", prescription.getVisitId(), result.getReviewStatus());

        return result;
    }

    /**
     * 检查配伍禁忌
     */
    private List<DrugInteraction> checkDrugInteractions(PrescriptionDTO prescription, String tenantId) {
        List<String> drugCodes = prescription.getDrugs().stream()
                .map(PrescriptionDTO.DrugItem::getDrugCode)
                .collect(Collectors.toList());

        if (drugCodes.size() < 2) {
            return List.of();
        }

        LambdaQueryWrapper<DrugInteraction> wrapper = new LambdaQueryWrapper<DrugInteraction>()
                .eq(DrugInteraction::getTenantId, tenantId)
                .eq(DrugInteraction::getIsEnabled, 1)
                .eq(DrugInteraction::getDeleted, 0)
                .and(w -> w
                        .in(DrugInteraction::getDrugCodeA, drugCodes)
                        .in(DrugInteraction::getDrugCodeB, drugCodes)
                );

        return drugInteractionMapper.selectList(wrapper);
    }

    /**
     * 检查剂量限制
     */
    private void checkDosageLimits(PrescriptionDTO prescription, String tenantId,
                                    List<PrescriptionReviewVO.ReviewItem> reviewItems) {
        for (PrescriptionDTO.DrugItem drug : prescription.getDrugs()) {
            if (drug.getDosage() == null) {
                continue;
            }

            try {
                double dosage = Double.parseDouble(drug.getDosage().replaceAll("[^0-9.]", ""));
                if (dosage > 100) {
                    PrescriptionReviewVO.ReviewItem item = new PrescriptionReviewVO.ReviewItem();
                    item.setLevel("WARN");
                    item.setDrugCode(drug.getDrugCode());
                    item.setDrugName(drug.getDrugName());
                    item.setSource("DosageLimitCheck");
                    item.setMessage("剂量偏大: " + dosage + drug.getUnit());
                    reviewItems.add(item);
                }
            } catch (NumberFormatException e) {
                log.debug("剂量解析失败: {}", drug.getDosage());
            }
        }
    }

    /**
     * 检查目录限制
     */
    private void checkCatalogLimits(PrescriptionDTO prescription, String tenantId,
                                     List<PrescriptionReviewVO.ReviewItem> reviewItems) {
        for (PrescriptionDTO.DrugItem drug : prescription.getDrugs()) {
            DrugCatalog catalog = drugCatalogMapper.selectOne(
                    new LambdaQueryWrapper<DrugCatalog>()
                            .eq(DrugCatalog::getTenantId, tenantId)
                            .eq(DrugCatalog::getDrugCode, drug.getDrugCode())
                            .eq(DrugCatalog::getIsEnabled, "1")
                            .eq(DrugCatalog::getDeleted, 0)
                            .last("LIMIT 1")
            );

            if (catalog == null) {
                PrescriptionReviewVO.ReviewItem item = new PrescriptionReviewVO.ReviewItem();
                item.setLevel("WARN");
                item.setDrugCode(drug.getDrugCode());
                item.setDrugName(drug.getDrugName());
                item.setSource("CatalogLimitCheck");
                item.setMessage("药品不在医保目录内: " + drug.getDrugName());
                reviewItems.add(item);
                log.warn("药品不在医保目录: code={}, name={}", drug.getDrugCode(), drug.getDrugName());
            }
        }
    }

    /**
     * 检查过敏史
     */
    private void checkAllergyHistory(PrescriptionDTO prescription, String tenantId,
                                     List<PrescriptionReviewVO.ReviewItem> reviewItems) {
        String patientId = prescription.getPatientId();

        List<PatientAllergy> allergies = patientAllergyMapper.selectList(
                new LambdaQueryWrapper<PatientAllergy>()
                        .eq(PatientAllergy::getTenantId, tenantId)
                        .eq(PatientAllergy::getPatientId, patientId)
                        .eq(PatientAllergy::getDeleted, 0)
        );

        if (allergies.isEmpty()) {
            return;
        }

        Set<String> allergyDrugCodes = allergies.stream()
                .map(PatientAllergy::getDrugCode)
                .collect(Collectors.toSet());

        for (PrescriptionDTO.DrugItem drug : prescription.getDrugs()) {
            if (allergyDrugCodes.contains(drug.getDrugCode())) {
                PatientAllergy allergy = allergies.stream()
                        .filter(a -> a.getDrugCode().equals(drug.getDrugCode()))
                        .findFirst()
                        .orElse(null);

                PrescriptionReviewVO.ReviewItem item = new PrescriptionReviewVO.ReviewItem();
                item.setLevel("BLOCK");
                item.setDrugCode(drug.getDrugCode());
                item.setDrugName(drug.getDrugName());
                item.setSource("DrugAllergyCheck");

                String severity = allergy != null ? allergy.getSeverityLevel() : "unknown";
                String reaction = allergy != null ? allergy.getReaction() : "";

                item.setMessage("过敏警告: 患者对 " + drug.getDrugName() + " 过敏(" + severity + "), 反应: " + reaction);
                reviewItems.add(item);

                log.warn("检测到过敏药品: patientId={}, drugCode={}, drugName={}, severity={}",
                        patientId, drug.getDrugCode(), drug.getDrugName(), severity);
            }
        }
    }
}
