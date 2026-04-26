package com.his.quality.dto;

import lombok.Data;

import java.util.List;

/**
 * 质控检查结果视图对象
 */
@Data
public class QualityCheckVO {

    private String visitId;

    private String checkStatus;

    private boolean pass;

    private List<CheckItem> checkItems;

    @Data
    public static class CheckItem {
        private String level;
        private String source;
        private String message;
        private String code;
    }
}
