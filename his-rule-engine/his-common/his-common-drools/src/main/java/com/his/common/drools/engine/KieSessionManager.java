package com.his.common.drools.engine;

import lombok.extern.slf4j.Slf4j;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * KIE Session 管理器
 *
 * <p>管理 KieSession 的创建、缓存和销毁</p>
 */
@Slf4j
@Component
public class KieSessionManager {

    private final KieContainer kieContainer;
    private final Map<String, KieSession> sessionCache = new ConcurrentHashMap<>();

    public KieSessionManager() {
        this.kieContainer = KieServices.Factory.get().getKieClasspathContainer();
        log.info("KieSessionManager initialized with classpath KieContainer");
    }

    public KieSessionManager(KieContainer kieContainer) {
        this.kieContainer = kieContainer;
        log.info("KieSessionManager initialized with provided KieContainer");
    }

    /**
     * 获取指定规则的 KIE Session
     *
     * @param ruleGroup 规则组名称
     * @return KieSession
     */
    public KieSession getKieSession(String ruleGroup) {
        return sessionCache.computeIfAbsent(ruleGroup, this::createKieSession);
    }

    /**
     * 创建新的 KIE Session
     *
     * @param ruleGroup 规则组名称
     * @return 新建的 KieSession
     */
    private KieSession createKieSession(String ruleGroup) {
        KieBase kieBase = kieContainer.getKieBase(ruleGroup);

        if (kieBase == null) {
            log.warn("KIE Base not found for group: {}, using default", ruleGroup);
            kieBase = kieContainer.getKieBase();
        }

        KieSession session = kieBase.newKieSession();
        log.info("Created new KieSession for ruleGroup: {}", ruleGroup);
        return session;
    }

    /**
     * 销毁指定的 KIE Session
     *
     * @param ruleGroup 规则组名称
     */
    public void destroyKieSession(String ruleGroup) {
        KieSession session = sessionCache.remove(ruleGroup);
        if (session != null) {
            session.dispose();
            log.info("Destroyed KieSession for ruleGroup: {}", ruleGroup);
        }
    }

    /**
     * 销毁所有缓存的 KIE Session
     */
    public void destroyAll() {
        sessionCache.forEach((group, session) -> {
            session.dispose();
            log.info("Destroyed KieSession for ruleGroup: {}", group);
        });
        sessionCache.clear();
        log.info("All KieSessions destroyed");
    }

    /**
     * 获取当前缓存的 Session 数量
     *
     * @return 缓存数量
     */
    public int getCacheSize() {
        return sessionCache.size();
    }
}
