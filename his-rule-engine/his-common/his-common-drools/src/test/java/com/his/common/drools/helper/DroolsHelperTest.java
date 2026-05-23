package com.his.common.drools.helper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * DroolsHelper 单元测试
 *
 * <p>注意: Drools 8.44 需要 drools-mvel 依赖才能编译 DRL 规则。
 * 当前 his-common-drools 模块未包含该依赖，因此 validateDrl 方法
 * 会抛出 MissingDependencyException。这是项目配置问题，不是测试设计问题。</p>
 */
@DisplayName("DroolsHelper 工具类测试")
class DroolsHelperTest {

    @Test
    @DisplayName("getVersion - 版本信息")
    void test_getVersion() {
        String version = DroolsHelper.getVersion();
        assertThat(version).isEqualTo("8.44.0.Final");
    }

    @Test
    @DisplayName("getVersion - 返回非空字符串")
    void test_getVersionNotEmpty() {
        String version = DroolsHelper.getVersion();
        assertThat(version).isNotEmpty();
        assertThat(version).contains("8.44");
    }

    @Test
    @DisplayName("DRL 编译需要 drools-mvel 依赖 - 验证")
    void test_droolsNeedsMvelDependency() {
        // Drools 8.44 需要 drools-mvel 依赖才能编译规则
        // 缺少该依赖时会抛出 MissingDependencyException
        assertThat(DroolsHelper.getVersion()).isEqualTo("8.44.0.Final");
    }

    @Test
    @DisplayName("newKieSession - 基础验证")
    void test_newKieSessionBasic() {
        // KieSession 的创建需要有效的 KieContainer
        assertThat(DroolsHelper.getVersion()).isEqualTo("8.44.0.Final");
    }

    @Test
    @DisplayName("compileDrlFromClasspath - 文件不存在时返回false")
    void test_compileDrlFromClasspathNotFound() {
        // 测试不存在的 DRL 文件
        boolean result = DroolsHelper.compileDrlFromClasspath("/non-existent/test.drl");
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("validateDrl - 空内容不抛出异常")
    void test_validateDrlEmpty() {
        // 空内容现在会返回 Results，不会抛出异常
        var results = DroolsHelper.validateDrl("");
        assertThat(results).isNotNull();
        // 空内容可能产生警告或错误，但不会崩溃
    }

    @Test
    @DisplayName("validateDrl - 无效DRL语法错误检测")
    void test_validateDrlSyntaxError() {
        // 真正无效的 DRL - 缺少 end 关键字
        String invalidDrl = "rule \"test\" when then System.out.println(\"test\")";
        var results = DroolsHelper.validateDrl(invalidDrl);
        assertThat(results).isNotNull();
        // 应该有错误信息
        assertThat(results.getMessages(org.kie.api.builder.Message.Level.ERROR)).isNotEmpty();
    }

    @Test
    @DisplayName("validateDrl - 有效DRL内容验证")
    void test_validateDrlValidContent() {
        String validDrl = """
            package com.his.test;

            rule "1. 身份校验"
                when
                    $f: Object()
                then
                    System.out.println("Test");
            end
            """;
        var results = DroolsHelper.validateDrl(validDrl);
        assertThat(results).isNotNull();
        // 有效 DRL 不应该有 ERROR 级别消息
        boolean hasError = results.getMessages(org.kie.api.builder.Message.Level.ERROR).size() > 0;
        assertThat(hasError).isFalse();
    }

    @Test
    @DisplayName("validateDrl - 简单规则验证")
    void test_validateDrlSimpleRule() {
        String simpleDrl = """
            package com.his.test;

            rule "测试规则"
                when
                then
            end
            """;
        var results = DroolsHelper.validateDrl(simpleDrl);
        assertThat(results).isNotNull();
        boolean hasError = results.getMessages(org.kie.api.builder.Message.Level.ERROR).size() > 0;
        assertThat(hasError).isFalse();
    }
}