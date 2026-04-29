package com.his.settlement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.settlement.entity.FormulaEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 公式 Mapper
 */
@Mapper
public interface FormulaEntityMapper extends BaseMapper<FormulaEntity> {

    @Select("SELECT * FROM aviator_formula WHERE formula_key = #{formulaKey} AND tenant_id = #{tenantId} AND deleted = 0 LIMIT 1")
    FormulaEntity selectByKey(@Param("formulaKey") String formulaKey, @Param("tenantId") String tenantId);
}
