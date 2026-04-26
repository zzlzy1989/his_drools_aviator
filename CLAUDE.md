# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**HIS 动态规则中台** (HIS Dynamic Rule Engine) is a medical business rule management platform that separates high-frequency changing business rules (medical insurance reimbursement, quality control, rational drug use, DRG grouping, etc.) from the core HIS system. Built on a **Drools + Aviator hybrid architecture** with Spring Cloud Alibaba microservices.

## Build Commands

```bash
# Full build
cd his-rule-engine && mvn clean compile

# Package
mvn clean package -DskipTests

# Run tests
mvn test

# Single module build
cd his-rule-engine/his-{module} && mvn clean package -DskipTests

# Start a service (requires Nacos running)
cd his-rule-engine/his-{module} && mvn spring-boot:run
```

## Architecture

### Microservices (Ports)

| Service | Port | Purpose |
|---------|------|---------|
| his-gateway | 9000 | API Gateway, routes all requests |
| his-rule-service | 9001 | Rule CRUD, version management, DRL publishing |
| his-formula-service | 9002 | Formula CRUD, syntax validation, Nacos sync |
| his-settlement-service | 9003 | Medical insurance settlement, reimbursement calculation |
| his-drug-service | 9004 | Prescription review, drug compatibility, dosage limits |
| his-quality-service | 9005 | Infection control, quality rules |
| his-drg-service | 9006 | DRG/DIP grouping, weight calculation |

### Module Structure (his-rule-engine/)

```
his-common/                          # Shared libraries
├── his-common-core/                 # Core: Fact objects, enums, exceptions
│   └── com.his.common/
│       ├── ISkill.java             # Skill interface
│       ├── SkillContext.java       # Execution context with tenantId, payload, results
│       ├── SkillResult.java        # Result with PASS/WARN/BLOCK level
│       ├── SettlementFact.java     # Core fact for settlement
│       ├── ResultLevel.java         # PASS, WARN, BLOCK
│       ├── ErrorCode.java          # Error code enum (HIS-xxx)
│       └── HisEventType.java       # Event type constants
├── his-common-web/                 # Web: unified response, exception handling
├── his-common-drools/              # Drools engine wrapper (empty skeleton)
└── his-common-aviator/            # Aviator wrapper, expression cache (empty skeleton)
```

### Core Design: Drools + Aviator Hybrid

**Drools** handles rule orchestration (conditions, priorities, grouping). **Aviator** executes mathematical/logical expressions dynamically at runtime.

```
[Settlement Request] → [Drools Rules] → [Aviator Formula Execution]
                            ↓
                     rule "4. Calculate"
                     when
                         $f: SettlementFact(...)
                     then
                         String formula = "round((totalFee - deductible) * ratio, 2)";
                         BigDecimal amount = AviatorHelper.executeFormula(formula, $f);
```

**Key interfaces:**
- `ISkill<T>` - All skills implement this interface with `supportEvent()`, `getOrder()`, `execute(SkillContext<T>)`
- `SkillContext<T>` - Holds tenantId, eventType, payload, and accumulated results
- `SkillResult` - Has ResultLevel (PASS/WARN/BLOCK), source, message

### Dependency Stack

- **Spring Boot 3.2.5** + **Spring Cloud 2023.0.1** + **Spring Cloud Alibaba 2023.0.1.0**
- **Drools 8.44.0.Final** - Rule engine
- **Aviator 5.4.3** - Expression evaluation
- **Caffeine 3.1.8** - Local cache for compiled expressions
- **MyBatis-Plus 3.5.6** - ORM
- **Nacos** - Service discovery + configuration center

## Project-Specific Conventions

### Fact Objects
All monetary/percentage fields must use `BigDecimal` (never double/float):
```java
public class SettlementFact {
    private BigDecimal totalFee;
    private BigDecimal deductible;
    private BigDecimal ratio;
    private BigDecimal finalAmount;
}
```

### Skill Execution Pattern
Skills are chained via Pipeline. Each skill:
1. Checks `context.hasBlock()` before executing (skip if already BLOCKed)
2. On exception: catches, logs, adds `ResultLevel.WARN` result (never throws)
3. Uses `ResultLevel.BLOCK` to halt the pipeline

### Formula Naming
Formula keys follow pattern: `formula.{category}.{name}`
- Example: `formula.reimburse.resident`, `formula.reimburse.employee`

### Rule Naming (DRL)
```drools
rule "1. 身份校验"
rule "2. 起付线判断"
rule "3. 报销金额计算"
```

### API Routes (via Gateway)
- `/api/v1/rules/**` → his-rule-service
- `/api/v1/formulas/**` → his-formula-service
- `/api/v1/settlements/**` → his-settlement-service
- `/api/v1/drugs/**` → his-drug-service
- `/api/v1/quality/**` → his-quality-service
- `/api/v1/drg/**` → his-drg-service

## Environment Setup

**Prerequisites:**
- JDK 21
- Maven 3.8+
- Nacos Server 2.x (for config center)
- MySQL 8.0 (for persistence)

**Required Environment Variables:**
- `NACOS_ADDR` - Nacos server address (default: 127.0.0.1:8848)
- `MYSQL_HOST`, `MYSQL_PORT`, `MYSQL_USER`, `MYSQL_PASSWORD`

## Status

The project is in **early development** stage. Most modules have skeleton structure with Application class and basic configuration only. Business logic implementation is pending. Core modules like `his-common-aviator` and `his-common-drools` are defined but contain no Java files yet.
