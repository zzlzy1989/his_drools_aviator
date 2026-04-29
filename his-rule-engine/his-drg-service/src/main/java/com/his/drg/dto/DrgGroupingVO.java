package com.his.drg.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * DRG 分组结果视图对象
 */
@Data
public class DrgGroupingVO {

    private String visitId;

    private String drgCode;

    private String drgName;

    private String mdcCode;

    private String mdcName;

    private BigDecimal baseWeight;

    private BigDecimal adjustWeight;

    private BigDecimal standardScore;

    private BigDecimal totalFee;

    private BigDecimal paymentAmount;

    private String groupingStatus;

    private boolean success;
}
