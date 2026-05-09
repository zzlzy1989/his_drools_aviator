package com.his.fact;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

/**
 * 处方 Fact 对象，用于合理用药审核规则。
 */
@Data
public class PrescriptionFact {

    private String prescriptionId;
    private String patientId;
    private String patientName;
    private Boolean isAdult;
    private String weight;
    private Boolean isPregnant;
    private List<String> allergyHistory;

    private List<DrugItem> drugs;
    private String brandDrug;
    private Boolean genericAvailable;
    private String dailyDosage;
    private BigDecimal maxDosage;
    private String doctorLevel;
}

@Data
class DrugItem {
    private String id;
    private String drugCode;
    private String drugName;
    private String drugType;
    private String pregnancyCategory;
    private String antibioticGrade;
    private Integer durationDays;
}
