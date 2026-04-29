package com.his.drug.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.drug.entity.PatientAllergy;
import org.apache.ibatis.annotations.Mapper;

/**
 * 患者过敏史 Mapper
 */
@Mapper
public interface PatientAllergyMapper extends BaseMapper<PatientAllergy> {
}
