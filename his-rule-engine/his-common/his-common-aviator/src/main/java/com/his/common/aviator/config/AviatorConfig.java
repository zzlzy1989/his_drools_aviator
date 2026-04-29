package com.his.common.aviator.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Aviator 配置属性
 *
 * <p>配置 Aviator 表达式引擎相关参数</p>
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "his.aviator")
public class AviatorConfig {

    /**
     * 是否启用 Aviator
     */
    private boolean enabled = true;

    /**
     * 表达式缓存最大数量
     */
    private int cacheMaxSize = 5000;

    /**
     * 缓存过期时间（分钟）
     */
    private long cacheExpireMinutes = 30;

    /**
     * 表达式最大长度
     */
    private int maxExpressionLength = 512;

    /**
     * 是否始终将浮点数解析为 BigDecimal
     */
    private boolean alwaysParseFloatingPointAsBigDecimal = true;

    /**
     * 是否启用科学计数法
     */
    private boolean scientificNotationEnabled = false;

    /**
     * 编译超时时间（毫秒）
     */
    private long compileTimeoutMs = 1000;

    /**
     * 执行超时时间（毫秒）
     */
    private long executeTimeoutMs = 100;
}
