package com.his.common.drools.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Drools 配置属性
 *
 * <p>配置 Drools 规则引擎相关参数</p>
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "his.drools")
public class DroolsConfig {

    /**
     * 是否启用 Drools
     */
    private boolean enabled = true;

    /**
     * KIE Session 池大小
     */
    private int sessionPoolSize = 10;

    /**
     * 规则执行超时时间（毫秒）
     */
    private long executionTimeoutMs = 10000;

    /**
     * KIE Base 名称
     */
    private String kieBaseName = "defaultKieBase";

    /**
     * KIE Session 名称
     */
    private String kieSessionName = "defaultKieSession";

    /**
     * 规则文件扫描路径
     */
    private String ruleFilePath = "rules/";

    /**
     * 是否开启规则追踪
     */
    private boolean ruleTraceEnabled = false;
}
