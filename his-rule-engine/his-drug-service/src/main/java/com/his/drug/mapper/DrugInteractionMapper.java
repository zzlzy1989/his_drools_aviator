package com.his.drug.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.drug.entity.DrugInteraction;
import org.apache.ibatis.annotations.Mapper;

/**
 * 药品配伍禁忌 Mapper
 */
@Mapper
public interface DrugInteractionMapper extends BaseMapper<DrugInteraction> {
}
