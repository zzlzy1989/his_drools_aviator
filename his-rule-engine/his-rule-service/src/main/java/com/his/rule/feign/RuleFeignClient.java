package com.his.rule.feign;

import com.his.common.web.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * 规则服务 Feign 客户端
 */
@FeignClient(name = "his-rule-service", path = "/api/v1/rules")
public interface RuleFeignClient {

    /**
     * 根据规则Key获取规则详情
     */
    @GetMapping("/key/{ruleKey}")
    Result<Map<String, Object>> getByKey(@PathVariable("ruleKey") String ruleKey);

    /**
     * 分页查询规则
     */
    @GetMapping
    Result<Map<String, Object>> pageList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String ruleKey,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status);
}
