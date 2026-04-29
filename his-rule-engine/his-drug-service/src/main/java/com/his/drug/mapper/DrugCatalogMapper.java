package com.his.drug.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.drug.entity.DrugCatalog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 医保药品目录 Mapper
 */
@Mapper
public interface DrugCatalogMapper extends BaseMapper<DrugCatalog> {
}
