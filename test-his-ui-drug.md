# 全院级 HIS 动态规则中台（Skill/Agent 架构）技术文档

> 基于 his-drools-aviator 项目实际代码，支持全院级 HIS 系统所有业务场景（医保、用药、质控等）

**版本**: v1.2  
**最后更新**: 2026-05-17  
**项目路径**: `/home/gaoxu/Documents/trae_projects/gx_project/his_drools_aviator`

---

## 一、整体架构图（微服务版）

```text
[ 医生站 / 护士站 / 计费系统 ] (前端 UI - Vue 3 + Element Plus)
       ↓ 1. 触发业务动作 (如：保存处方、点击结算)
[ his-gateway (:9000) ]  ← Spring Cloud Gateway + CORS + JWT(可配置)
       ↓ 2. 路由到对应微服务
=======================================================================
[ 业务微服务 ] ← his-drug-service / his-settlement-service / his-quality-service
       │
       │ 3. 构建 SkillContext<T>，触发事件
       │
       ▼
[ Skill Pipeline 执行 ] ← 微服务内部 Spring 注入所有 ISkill Bean
       │
       ├─ [Skill 1: 医保限制] → (通过)
       ├─ [Skill 2: 合理用药] → 触发 Drools + Aviator 计算 → (警告/阻断)
       └─ [Skill 3: 院感拦截] → (通过)
              ↓
       汇总执行结果 (List<SkillResult>)
=======================================================================
       ↓ 4. 返回标准化干预指令 (PASS / WARN / BLOCK)
[ 前端 UI ] ← 根据指令：放行提交 / 弹窗警告 / 强行阻断
```

### 技术栈版本（项目实际）

| 组件 | 版本 | 说明 |
| :--- | :--- | :--- |
| **Java** | 21 | LTS 版本 |
| **Spring Boot** | 3.5.0 | 基于 Jakarta EE |
| **Spring Cloud** | 2025.0.0 | 微服务框架 |
| **Spring Cloud Alibaba** | 2025.0.0.0 | Nacos 注册+配置 |
| **Drools** | 8.44.0.Final | 规则引擎 |
| **Aviator** | 5.4.3 | 表达式引擎 |
| **Caffeine** | 3.1.8 | 本地缓存 |
| **Nacos** | 3.2.0 | 注册中心 + 配置中心 |
| **MyBatis-Plus** | 3.5.6 | ORM 框架 |
| **MySQL** | 8.0+ | 数据库 (utf8mb4) |
| **前端** | Vue 3 + Vite + TypeScript | his-rule-engine-web |

### 微服务清单（项目实际 10 个服务）

| 微服务 | 端口 | API 路径 | 说明 |
|--------|------|---------|------|
| his-gateway | 9000 | `/api/v1/**` | 统一入口，路由到各服务 |
| his-rule-service | 9001 | `/api/v1/rules/**`, `/api/v1/flows/**`, `/api/v1/audit-logs/**`, `/api/v2/flows/**` | 规则管理、规则流 |
| his-formula-service | 9002 | `/api/v1/formulas/**` | 公式管理 |
| his-settlement-service | 9003 | `/api/v1/settlements/**`, `/api/v1/sandbox/**` | 结算管理、沙箱测试 |
| his-drug-service | 9004 | `/api/v1/drugs/**` | 合理用药检查 |
| his-quality-service | 9005 | `/api/v1/quality/**` | 质控指标管理 |
| his-drg-service | 9006 | `/api/v1/drg/**` | DRG 分组管理 |
| his-monitor-service | 9007 | `/api/v1/monitor/**` | 监控服务 |
| his-market-service | 9008 | `/api/v1/market/**` | 规则市场服务 |

---

## 二、核心设计要点

| 组件 | 职责 | 实际文件路径 |
| :--- | :--- | :--- |
| **事件类型常量** | 定义全院统一的拦截点常量 | `his-common/his-common-core/.../common/HisEventType.java` |
| **统一上下文** | 封装各类业务事实（Fact），屏蔽底层差异 | `his-common/his-common-core/.../common/SkillContext<T>.java` |
| **能力标准接口** | 所有业务 Skill 必须实现的契约 | `his-common/his-common-core/.../common/ISkill.java` |
| **执行结果** | 标准化干预指令（PASS/WARN/BLOCK） | `his-common/his-common-core/.../common/SkillResult.java` |
| **结果级别** | 枚举：放行/警告/阻断 | `his-common/his-common-core/.../common/ResultLevel.java` |
| **错误码** | 统一错误编码体系 | `his-common/his-common-core/.../common/ErrorCode.java` |
| **结算 Fact** | 结算业务数据载体 | `his-common/his-common-core/.../common/SettlementFact.java` |
| **处方 Fact** | 处方审核业务数据载体 | `his-common/his-common-core/.../fact/PrescriptionFact.java` |
| **其他 Fact** | 费用/DRG/院感/质控 Fact | `his-common/his-common-core/.../fact/CostFact.java` 等 |

---

## 三、核心代码（项目实际实现）

### 3.1 统一上下文与结果定义

所有 Skill 相关核心类位于 `com.his.common` 包（his-common-core 模块）。

**ResultLevel.java** — 干预级别枚举

```java
package com.his.common;

public enum ResultLevel {
    PASS,   // 放行
    WARN,   // 警告（弹窗提示，可忽略）
    BLOCK;  // 阻断（强拦截，事务回滚）
}
```

**SkillResult.java** — 规则执行结果

```java
package com.his.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SkillResult {

    private ResultLevel level;
    private String source;      // 产生该结果的 Skill 名称
    private String message;     // 提示/错误内容
}
```

**SkillContext<T>** — 统一上下文（泛型）

```java
package com.his.common;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class SkillContext<T> {

    private String tenantId;
    private String eventType;
    private T payload;
    private List<SkillResult> results = new ArrayList<>();

    public void addResult(SkillResult result) {
        results.add(result);
    }

    public boolean hasBlock() {
        return results.stream().anyMatch(r -> r.getLevel() == ResultLevel.BLOCK);
    }
}
```

**HisEventType.java** — 事件类型常量

```java
package com.his.common;

public final class HisEventType {

    private HisEventType() {}

    public static final String SETTLEMENT_EXECUTE = "SETTLEMENT_EXECUTE";
    public static final String PRESCRIPTION_CHECK = "PRESCRIPTION_CHECK";
    public static final String DRUG_AUDIT = "DRUG_AUDIT";
    public static final String INFECTION_CONTROL = "INFECTION_CONTROL";
    public static final String QUALITY_CONTROL = "QUALITY_CONTROL";
    public static final String DRG_GROUP = "DRG_GROUP";
}
```

**ErrorCode.java** — 统一错误码

```java
package com.his.common;

public enum ErrorCode {

    SUCCESS("0", "操作成功"),

    BUSINESS_PATIENT_INFO_MISSING("HIS-001", "患者身份信息缺失"),
    BUSINESS_RULE_NOT_FOUND("HIS-002", "规则不存在"),
    BUSINESS_SETTLEMENT_IN_PROGRESS("HIS-003", "结算正在进行中"),

    FORMULA_SYNTAX_ERROR("HIS-101", "公式语法错误"),
    FORMULA_PARAM_MISMATCH("HIS-102", "公式参数不匹配"),
    FORMULA_CALC_OVERFLOW("HIS-103", "公式计算溢出"),

    SETTLEMENT_DUPLICATE("HIS-201", "重复结算"),
    SETTLEMENT_REJECTED("HIS-202", "审核拒绝"),

    PERMISSION_DENIED("HIS-301", "无租户权限"),

    SECURITY_INJECTION_DETECTED("HIS-401", "Aviator注入检测"),

    SYSTEM_CONFIG_UNAVAILABLE("HIS-901", "配置中心不可用"),
    SYSTEM_KIE_COMPILE_FAILED("HIS-902", "KIE编译失败"),
    SYSTEM_INTERNAL_ERROR("HIS-999", "系统内部异常");

    private final String code;
    private final String message;

    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() { return code; }
    public String getMessage() { return message; }
}
```

### 3.2 标准能力接口（ISkill）

```java
package com.his.common;

public interface ISkill<T> {

    String supportEvent();

    int getOrder();

    void execute(SkillContext<T> context);
}
```

### 3.3 核心网关（SkillGateway）

Skill 执行嵌入各业务微服务内部，Spring 自动注入所有 `ISkill` Bean。

```java
package com.his.settlement.gateway;

import com.his.common.ISkill;
import com.his.common.SkillContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class SkillGateway {

    private final List<ISkill> allSkills;

    public <T> SkillContext<T> fireEvent(String eventType, T payload) {
        SkillContext<T> context = new SkillContext<>();
        context.setEventType(eventType);
        context.setPayload(payload);

        List<ISkill> targetSkills = allSkills.stream()
                .filter(skill -> skill.supportEvent().equals(eventType))
                .sorted(Comparator.comparingInt(ISkill::getOrder))
                .collect(Collectors.toList());

        log.info("触发事件: {}, 匹配到 {} 个 Skill", eventType, targetSkills.size());

        for (ISkill skill : targetSkills) {
            skill.execute(context);

            if (context.hasBlock()) {
                log.warn("事件 {} 被阻断，来源: {}", eventType, skill.getClass().getSimpleName());
                break;
            }
        }

        return context;
    }
}
```

### 3.4 具体 Skill 实现（以合理用药为例）

```java
package com.his.drug.skill;

import com.his.common.*;
import com.his.drug.dto.PrescriptionDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RationalDrugUseSkill implements ISkill<PrescriptionDTO> {

    private final RuleEngineTemplate ruleEngine;

    @Override
    public String supportEvent() {
        return HisEventType.PRESCRIPTION_CHECK;
    }

    @Override
    public int getOrder() {
        return 10;
    }

    @Override
    public void execute(SkillContext<PrescriptionDTO> context) {
        if (context.hasBlock()) {
            return;
        }
        try {
            String ruleGroup = "DRUG_RULES_" + context.getTenantId();
            ruleEngine.fireRules(ruleGroup, context);
        } catch (Exception e) {
            log.error("合理用药规则执行失败, tenantId={}", context.getTenantId(), e);
            context.addResult(new SkillResult(
                ResultLevel.WARN, "RationalDrugUseSkill", "规则执行异常: " + e.getMessage()));
        }
    }
}
```

### 3.5 Fact 对象定义

**SettlementFact.java** — 结算 Fact

```java
package com.his.common;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class SettlementFact {

    private String patientId;
    private String patientName;
    private String patientType;
    private String tenantId;
    private String settlementId;

    private BigDecimal totalFee;
    private BigDecimal deductible;
    private BigDecimal ratio;
    private BigDecimal finalAmount;

    private String insuranceType;
    private String hospitalLevel;
    private LocalDateTime admissionDate;
    private LocalDateTime dischargeDate;

    private String diagnosisCode;
    private String drugCode;
    private Integer drugQuantity;

    private List<SkillResult> results = new ArrayList<>();

    public void addResult(ResultLevel level, String source, String message) {
        this.results.add(new SkillResult(level, source, message));
    }

    public void addResult(SkillResult result) {
        this.results.add(result);
    }

    public boolean hasBlock() {
        return results.stream().anyMatch(r -> r.getLevel() == ResultLevel.BLOCK);
    }

    public ResultLevel getResultLevel() {
        if (hasBlock()) return ResultLevel.BLOCK;
        if (results.stream().anyMatch(r -> r.getLevel() == ResultLevel.WARN)) return ResultLevel.WARN;
        return ResultLevel.PASS;
    }
}
```

**PrescriptionFact.java** — 处方 Fact

```java
package com.his.fact;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

/**
 * 处方 Fact 对象，用于合理用药审核规则。
 */
@Data
public class PrescriptionFact {

    private String prescriptionId;
    private String patientId;
    private String patientName;
    private Boolean isAdult;
    private String weight;
    private Boolean isPregnant;
    private List<String> allergyHistory;

    private List<DrugItem> drugs;
    private String brandDrug;
    private Boolean genericAvailable;
    private String dailyDosage;
    private BigDecimal maxDosage;
    private String doctorLevel;
}

@Data
class DrugItem {
    private String id;
    private String drugCode;
    private String drugName;
    private String drugType;
    private String pregnancyCategory;
    private String antibioticGrade;
    private Integer durationDays;
}
```

### 3.6 Controller 层（项目实际）

**DrugController.java** — 药品管理 Controller

```java
package com.his.drug.controller;

import com.his.common.web.result.PageResult;
import com.his.common.web.result.Result;
import com.his.common.web.context.TenantContext;
import com.his.drug.dto.PrescriptionDTO;
import com.his.drug.dto.PrescriptionReviewVO;
import com.his.drug.entity.DrugCatalog;
import com.his.drug.service.DrugCheckService;
import com.his.drug.mapper.DrugCatalogMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * 药品 Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/drugs")
@RequiredArgsConstructor
@Tag(name = "用药审核", description = "处方审核、配伍禁忌检查")
public class DrugController {

    private final DrugCheckService drugCheckService;
    private final DrugCatalogMapper drugCatalogMapper;

    @GetMapping
    @Operation(summary = "分页查询药品目录")
    public Result<PageResult<DrugCatalog>> pageList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String drugName,
            @RequestParam(required = false) String drugType,
            @RequestParam(required = false) String status) {
        LambdaQueryWrapper<DrugCatalog> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(drugName), DrugCatalog::getDrugName, drugName)
               .eq(StringUtils.hasText(drugType), DrugCatalog::getDrugType, drugType)
               .eq(StringUtils.hasText(status), DrugCatalog::getIsEnabled, status)
               .orderByDesc(DrugCatalog::getCreateTime);
        IPage<DrugCatalog> result = drugCatalogMapper.selectPage(new Page<>(page, pageSize), wrapper);
        return Result.success(PageResult.of(result));
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询药品详情")
    public Result<DrugCatalog> getById(@PathVariable Long id) {
        return Result.success(drugCatalogMapper.selectById(id));
    }

    @PostMapping
    @Operation(summary = "新增药品")
    public Result<Void> create(@RequestBody DrugCatalog drug) {
        if (drug.getDrugCode() == null || drug.getDrugCode().isEmpty()) {
            drug.setDrugCode("DRG" + System.currentTimeMillis());
        }
        drugCatalogMapper.insert(drug);
        return Result.success();
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新药品")
    public Result<Void> update(@PathVariable Long id, @RequestBody DrugCatalog drug) {
        drug.setId(id);
        drugCatalogMapper.updateById(drug);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除药品")
    public Result<Void> delete(@PathVariable Long id) {
        drugCatalogMapper.deleteById(id);
        return Result.success();
    }
}
```

### 3.7 DTO 定义（项目实际）

**PrescriptionDTO.java** — 处方审核请求

```java
package com.his.drug.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 处方审核请求 DTO
 */
@Data
public class PrescriptionDTO {

    @NotBlank(message = "就诊ID不能为空")
    private String visitId;

    @NotBlank(message = "患者ID不能为空")
    private String patientId;

    private String patientInfo;

    @NotEmpty(message = "药品列表不能为空")
    private List<DrugItem> drugs;

    private List<String> diagnosisCodes;

    @NotBlank(message = "医生ID不能为空")
    private String doctorId;

    /**
     * 药品项
     */
    @Data
    public static class DrugItem {
        @NotBlank(message = "药品代码不能为空")
        private String drugCode;

        @NotBlank(message = "药品名称不能为空")
        private String drugName;

        private String specification;

        private String dosage;

        private String unit;

        private Integer quantity;

        private String administration;
    }
}
```

**PrescriptionReviewVO.java** — 处方审核结果视图

```java
package com.his.drug.dto;

import lombok.Data;

import java.util.List;

/**
 * 处方审核结果视图对象
 */
@Data
public class PrescriptionReviewVO {

    private String visitId;

    private String reviewStatus;

    private boolean pass;

    private List<ReviewItem> reviewItems;

    /**
     * 审核项
     */
    @Data
    public static class ReviewItem {
        private String level;
        private String source;
        private String message;
        private String drugCode;
        private String drugName;
    }
}
```

### 3.8 微服务启动类（项目实际）

**DrugServiceApplication.java**

```java
package com.his.drug;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = {"com.his.drug", "com.his.common"})
@EnableDiscoveryClient
@EnableFeignClients
@MapperScan({"com.his.drug.mapper", "com.his.common.mapper"})
public class DrugServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DrugServiceApplication.class, args);
    }
}
```

---

## 四、动态 Skill 的来源（项目实际方案）

项目当前采用 **V1 阶段**：Spring `@Service` 静态注入 + DRL/公式从数据库动态读取。

| 阶段 | 加载方式 | 适用场景 | 状态 |
| :--- | :--- | :--- | :--- |
| **V1: 静态 Bean 注入** | Spring `@Service` + DRL 从数据库动态读取 | 逻辑主干稳定，仅阈值、判断条件、公式变化（占 80% 场景） | ✅ 已实现 |
| **V2: 脚本化 Skill** | 使用 Groovy 脚本直接实现 `ISkill` 接口，数据库存储脚本 | 新增简单的自定义卡控逻辑（如：某个科室周末禁开某药） | 规划中 |
| **V3: Jar 包热加载** | 通过自定义 `ClassLoader` 动态加载编译好的独立 Jar 包 | 极其复杂的全新业务逻辑接入（如全新的 DRG/DIP 分组结算引擎） | 规划中 |

**V1 阶段核心流程**：

```
[ 业务请求 ] → [ SkillGateway.fireEvent() ]
                      ↓
           Spring 注入所有 ISkill Bean
                      ↓
           按 supportEvent() 过滤 + getOrder() 排序
                      ↓
           Pipeline 顺序执行 → 异常时降级为 WARN
                      ↓
           返回 SkillContext（含 results 列表）
```

---

## 五、网关路由配置（项目实际）

### 5.1 Spring Cloud Gateway 路由

```yaml
# his-gateway/src/main/resources/application.yml
server:
  port: 9000

spring:
  cloud:
    gateway:
      routes:
        - id: his-rule-service
          uri: lb://his-rule-service
          predicates:
            - Path=/api/v1/rules/**,/api/v1/flows/**,/api/v1/audit-logs/**
        - id: his-formula-service
          uri: lb://his-formula-service
          predicates:
            - Path=/api/v1/formulas/**
        - id: his-settlement-service
          uri: lb://his-settlement-service
          predicates:
            - Path=/api/v1/settlements/**,/api/v1/sandbox/**
        - id: his-drug-service
          uri: lb://his-drug-service
          predicates:
            - Path=/api/v1/drugs/**
        - id: his-quality-service
          uri: lb://his-quality-service
          predicates:
            - Path=/api/v1/quality/**
        - id: his-drg-service
          uri: lb://his-drg-service
          predicates:
            - Path=/api/v1/drg/**
        - id: his-market-service
          uri: lb://his-market-service
          predicates:
            - Path=/api/v1/market/**
        - id: his-monitor-service
          uri: lb://his-monitor-service
          predicates:
            - Path=/api/v1/monitor/**
```

### 5.2 CORS 配置

```yaml
his:
  cors:
    allowed-origins:
      - http://localhost:3000
      - http://localhost:8080
      - http://localhost:8999
    allowed-methods: GET,POST,PUT,DELETE,OPTIONS,PATCH
    allowed-headers: Origin,Content-Type,Accept,X-Tenant-Id,Authorization
    allow-credentials: true
    max-age: 3600
```

---

## 六、项目目录结构（实际）

```
his-drools-aviator/
├── .trae/                              # Harness 工程配置
├── his-rule-engine/                    # 后端微服务
│   ├── his-common/                     # 公共模块
│   │   ├── his-common-core/            #   核心：ISkill/SkillContext/ErrorCode 等
│   │   ├── his-common-web/             #   Web 公共组件
│   │   ├── his-common-drools/          #   Drools 公共组件
│   │   └── his-common-aviator/         #   Aviator 公共组件
│   ├── his-gateway/                    # API 网关 (:9000)
│   ├── his-rule-service/               # 规则管理服务 (:9001)
│   ├── his-formula-service/            # 公式管理服务 (:9002)
│   ├── his-settlement-service/         # 结算服务 (:9003)
│   ├── his-drug-service/               # 合理用药服务 (:9004)
│   ├── his-quality-service/            # 质控服务 (:9005)
│   ├── his-drg-service/                # DRG 服务 (:9006)
│   ├── his-monitor-service/            # 监控服务 (:9007)
│   └── his-market-service/             # 规则市场服务 (:9008)
├── his-rule-engine-web/                # 前端 (Vue 3 + Vite + TypeScript)
│   └── src/
│       ├── api/                        #   API 接口
│       ├── views/                      #   页面组件
│       ├── components/                 #   公共组件
│       ├── router/                     #   路由配置
│       └── stores/                     #   状态管理
├── docker/                             # Docker 部署配置
│   └── nginx/                          #   Nginx 配置
├── pom.xml                             # Maven 父 POM
├── README.md                           # 项目说明
└── drools_aviator.md                   # Drools+Aviator 混合架构文档
```

---

## 七、这样剥离的好处（对 HIS 架构的颠覆）

| 收益维度 | 传统 HIS 架构 | Skill 中台架构 |
| :--- | :--- | :--- |
| **系统稳定性** | 改一个医保规则，可能导致处方保存接口崩溃。 | 规则沙箱隔离。某个 Skill 抛异常，降级为 WARN，**HIS 永不宕机**。 |
| **扩展性** | 每接入一家第三方监管平台，HIS 核心代码要写一堆 if-else。 | 新增一个实现 `ISkill` 的 Skill 即可，**HIS 底座零代码修改**。 |
| **复用能力** | 门诊收费、住院结算的医保校验逻辑散落在两处。 | 只需抛出 `SETTLEMENT_EXECUTE` 事件，**底层共享同一个结算 Skill**。 |
| **商业模式** | 靠堆人头驻场改 Bug、改硬编码赚取人天费。 | **规则即服务 (RaaS)**：卖引擎底座，各医院自主沉淀知识库，形成护城河。 |

---

## 八、注意事项

1. **事务一致性边界**
   * **同步拦截（BLOCK）**：必须在 HIS 数据库事务 `Commit` 之前调用 SkillGateway。一旦返回 BLOCK，HIS 直接触发 `Rollback` 并透传 Message 给前端弹窗。
   * **异步分析（WARN）**：对于"病历质控"等耗时超过 500ms 的长规则，建议 HIS 通过 MQ 抛出事件，不阻塞当前流程，Skill 算完后通过 WebSocket 推送警告。
2. **性能压舱石**
   * 网关拦截是全院级别的，调用频率极高。
   * 务必对 `Drools KieBase` 和 `Aviator Expression` 进行 **Caffeine 本地缓存**。
   * 复杂上下文组装（如查阅历史 90 天用药记录）应做到**按需懒加载 (Lazy Loading)**，只有当 Drools 规则确实用到该条件时，再去 DB 查询。
3. **闭环反馈机制**
   * 当 Skill 给出 `WARN` 级别的拦截时，医生如果选择"强制双签名忽略"，该行为及原因应当回调并记录到 Rule 库中，用于后期**反哺优化规则引擎**，防止频繁误报导致的"警告疲劳"。
4. **微服务部署**
   * 项目采用微服务架构，10 个服务独立部署：his-gateway(:9000)、his-rule-service(:9001)、his-formula-service(:9002)、his-settlement-service(:9003)、his-drug-service(:9004)、his-quality-service(:9005)、his-drg-service(:9006)、his-monitor-service(:9007)、his-market-service(:9008)。
   * Skill 执行在各自业务微服务内部完成，不依赖独立网关服务。
5. **技术栈要求**
   * Java 21 + Spring Boot 3.5.0（Jakarta EE），所有注解使用 `jakarta.*` 包。
   * Drools 8.44.0.Final + Aviator 5.4.3 混合架构。
   * Nacos 3.2.0 作为注册中心和配置中心。
   * MyBatis-Plus 3.5.6 作为 ORM 框架。
   * 前端使用 Vue 3 + Vite + TypeScript + Element Plus。

---

最后更新: 2026-05-17 | 基于项目实际代码更新 v1.2
