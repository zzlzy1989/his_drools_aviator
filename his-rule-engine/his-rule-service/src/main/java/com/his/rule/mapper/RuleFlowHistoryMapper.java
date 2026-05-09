package com.his.rule.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.rule.entity.RuleFlowHistory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 规则流历史版本 Mapper
 */
@Mapper
public interface RuleFlowHistoryMapper extends BaseMapper<RuleFlowHistory> {
}