package com.his.rule.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.rule.entity.RuleDefinition;
import org.apache.ibatis.annotations.Mapper;

/**
 * 规则定义 Mapper
 */
@Mapper
public interface RuleDefinitionMapper extends BaseMapper<RuleDefinition> {
}
