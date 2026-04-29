package com.his.drug.dto;

import lombok.Data;

import java.util.List;

/**
 * 处方审核结果视图对象
 */
@Data
public class PrescriptionReviewVO {

    private String visitId;

    private String reviewStatus;

    private boolean pass;

    private List<ReviewItem> reviewItems;

    /**
     * 审核项
     */
    @Data
    public static class ReviewItem {
        private String level;
        private String source;
        private String message;
        private String drugCode;
        private String drugName;
    }
}
