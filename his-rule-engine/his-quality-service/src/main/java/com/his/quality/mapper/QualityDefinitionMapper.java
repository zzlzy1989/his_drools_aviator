package com.his.quality.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.quality.entity.QualityDefinition;
import org.apache.ibatis.annotations.Mapper;

/**
 * 质控规则 Mapper
 */
@Mapper
public interface QualityDefinitionMapper extends BaseMapper<QualityDefinition> {
}
