package com.his.drug.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 患者过敏史实体
 */
@Data
@TableName("patient_allergy")
public class PatientAllergy {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String patientId;

    private String drugCode;

    private String drugName;

    private String allergyType;

    private String severityLevel;

    private String reaction;

    private String memo;

    private String tenantId;

    private String createBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    private String updateBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
