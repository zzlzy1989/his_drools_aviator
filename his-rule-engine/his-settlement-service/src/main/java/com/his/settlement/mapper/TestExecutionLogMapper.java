package com.his.settlement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.settlement.entity.TestExecutionLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 测试执行历史 Mapper
 */
@Mapper
public interface TestExecutionLogMapper extends BaseMapper<TestExecutionLog> {
}