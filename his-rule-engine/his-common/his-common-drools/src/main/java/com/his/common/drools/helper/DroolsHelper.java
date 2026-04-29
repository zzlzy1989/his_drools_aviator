package com.his.common.drools.helper;

import lombok.extern.slf4j.Slf4j;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.Message;
import org.kie.api.builder.Results;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;

import java.io.InputStream;
import java.util.jar.Manifest;

/**
 * Drools 辅助工具类
 *
 * <p>提供 DRL 编译、验证等工具方法</p>
 */
@Slf4j
public class DroolsHelper {

    private static final String DRL_FILE_EXTENSION = ".drl";

    /**
     * 从 classpath 加载并编译 DRL 文件
     *
     * @param drlPath DRL 文件路径（classpath 下）
     * @return 编译是否成功
     */
    public static boolean compileDrlFromClasspath(String drlPath) {
        try (InputStream is = DroolsHelper.class.getResourceAsStream(drlPath)) {
            if (is == null) {
                log.error("DRL 文件未找到: {}", drlPath);
                return false;
            }

            KieServices ks = KieServices.Factory.get();
            KieFileSystem kfs = ks.newKieFileSystem();

            String content = new String(is.readAllBytes());
            kfs.write("src/main/resources/" + drlPath, content);

            KieBuilder kieBuilder = ks.newKieBuilder(kfs);
            kieBuilder.buildAll();

            Results results = kieBuilder.getResults();
            if (results.hasMessages(Message.Level.ERROR)) {
                log.error("DRL 编译错误: {}", results.getMessages());
                return false;
            }

            log.info("DRL 编译成功: {}", drlPath);
            return true;
        } catch (Exception e) {
            log.error("DRL 编译异常: {}", drlPath, e);
            return false;
        }
    }

    /**
     * 验证 DRL 内容的语法正确性
     *
     * @param drlContent DRL 内容
     * @return 验证结果消息列表
     */
    public static Results validateDrl(String drlContent) {
        KieServices ks = KieServices.Factory.get();
        KieFileSystem kfs = ks.newKieFileSystem();
        kfs.write("src/main/resources/temp/temp.drl", drlContent);

        KieBuilder kieBuilder = ks.newKieBuilder(kfs);
        kieBuilder.buildAll();

        return kieBuilder.getResults();
    }

    /**
     * 获取 Drools 版本信息
     *
     * @return 版本字符串
     */
    public static String getVersion() {
        return "8.44.0.Final";
    }

    /**
     * 创建新的 KieSession
     *
     * @param kieContainer KIE 容器
     * @param ruleGroup 规则组名称
     * @return KieSession
     */
    public static KieSession newKieSession(KieContainer kieContainer, String ruleGroup) {
        KieServices ks = KieServices.Factory.get();
        var kieBase = kieContainer.getKieBase(ruleGroup);
        if (kieBase == null) {
            kieBase = kieContainer.getKieBase();
        }
        return kieBase.newKieSession();
    }
}
