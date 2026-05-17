package com.his.monitor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.monitor.entity.MetricRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 指标记录Mapper
 */
@Mapper
public interface MetricRecordMapper extends BaseMapper<MetricRecord> {
}