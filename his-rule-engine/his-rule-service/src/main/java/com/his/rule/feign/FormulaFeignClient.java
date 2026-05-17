package com.his.rule.feign;

import com.his.common.web.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * 公式服务 Feign 客户端
 */
@FeignClient(name = "his-formula-service", path = "/api/v1/formulas")
public interface FormulaFeignClient {

    /**
     * 根据公式Key获取公式详情
     */
    @GetMapping("/key/{formulaKey}")
    Result<Map<String, Object>> getByKey(@PathVariable("formulaKey") String formulaKey);

    /**
     * 分页查询公式
     */
    @GetMapping
    Result<Map<String, Object>> pageList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String formulaKey,
            @RequestParam(required = false) String category);
}
