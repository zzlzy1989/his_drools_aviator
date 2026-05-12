package com.his.settlement.listener;

import com.his.settlement.service.FormulaLoaderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.endpoint.event.RefreshEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 配置热更新监听器
 *
 * <p>监听 Nacos 配置变更事件，自动刷新公式缓存</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ConfigRefreshListener {

    private final FormulaLoaderService formulaLoaderService;

    /**
     * 监听 Spring Cloud 刷新事件
     */
    @EventListener
    public void onRefreshEvent(RefreshEvent event) {
        log.info("收到配置刷新事件: {}", event.getEvent());
        formulaLoaderService.clearCache();
        log.info("公式缓存已清空，等待重新加载");
    }

    /**
     * 手动触发全量刷新
     */
    public void triggerFullRefresh() {
        log.info("手动触发全量刷新");
        formulaLoaderService.clearCache();
        log.info("全量刷新完成");
    }

    /**
     * 刷新指定租户的公式缓存
     */
    public void refreshTenantCache(String tenantId) {
        log.info("刷新租户公式缓存: tenantId={}", tenantId);
        formulaLoaderService.clearCache();
        log.info("租户缓存刷新完成: tenantId={}", tenantId);
    }
}
