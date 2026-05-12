---
alwaysApply: true
---
# Rules 配置说明 - HIS Drools+Aviator 规则引擎

## 概述

Rules 是模块化的规范文件集合，按主题组织，在特定开发任务触发时自动关联。覆盖 **Java/Spring Cloud/Drools/Aviator** 完整技术栈的编码规范、测试、安全、API 设计、数据库、错误处理、部署、性能等关键领域。

## 目录结构（v1.1）

```
.trae/rules/
├── README.md              # 本文档（索引）
├── workflow.md            # 7 步强制工作流程 + 3 阻塞节点
├── code-style.md          # Java/Spring/Drools/微服务编码规范
├── testing.md             # JUnit5/MockMvc/DRL规则/集成测试规范
├── security.md            # SQL注入/XSS/Aviator表达式注入/网关安全
│
├── database.md            # MySQL表结构设计/查询优化/索引规范
├── error-handling.md      # ErrorCode体系/异常层级/日志规范
├── docker-deploy.md       # Docker多阶段构建/JVM优化/微服务编排
├── performance.md         # Caffeine缓存/KieBase分组/Sentinel限流
│
├── api-design.md          # REST API 设计规范（Controller/DTO/网关）
├── documentation.md       # DRL规则/Aviator公式/微服务注释规范
└── git-commit-message.md  # Git 提交信息规范
```

---

## 规则详解

### workflow.md ⚡ 最高优先级

**触发条件**: 所有开发任务的入口

| 维度 | 说明 |
|------|------|
| 核心内容 | **7 步强制工作流程**：梳理业务→规划改动(★阻塞)→实现代码→实现审查(★阻塞)→质量审查(★阻塞)→测试验证→知识回写 |
| 阻塞节点 | Step 2(规划)、Step 4(实现审查)、Step 5(质量审查) 必须用户确认 |
| 任务分类 | 按类型决定是否走完整流程（问答/配置修改/规则开发/Bug修复/重构） |
| 微服务适配 | 支持单服务修改、跨服务联调、网关路由变更等场景 |
| 违规处理 | 跳过阻塞节点、修改受保护文件、未读先改 → 自动停止并提醒 |

**关联**: `agent.md` 行为规范、`commands/review.md`

---

### code-style.md ⚡ Java/Spring/Drools/微服务编码规范

**触发条件**: 编写 Java 代码、DRL 规则文件、Aviator 公式、微服务模块

| 章节 | 内容 |
|------|------|
| Java 基础规范 | 类命名(PascalCase)、方法命名(camelCase)、常量(UPPER_SNAKE)、包结构 |
| Spring Boot 规范 | Controller/Service/Mapper 分层、依赖注入(@RequiredArgsConstructor)、配置类 |
| 微服务规范 | 服务拆分原则、Feign 客户端、服务间调用、统一配置管理 |
| Drools DRL 规范 | package 声明、import Fact、rule 命名(业务域_动作_条件)、salience 优先级 |
| Aviator 公式规范 | 表达式语法、自定义函数注册、BigDecimal 类型安全、四舍五入精度 |
| Skill Pipeline 规范 | ISkill 接口实现、Pipeline 编排、ResultLevel 处理、异常降级 |
| BigDecimal 强制 | 金额计算禁止 double/float、使用 BigDecimalUtils 工具类、比较用 compareTo |

---

### testing.md ⚡ 测试规范

**触发条件**: 编写单元测试、集成测试、DRL 规则测试、微服务联调测试

| 章节 | 内容 |
|------|------|
| 单元测试(JUnit5) | 测试类命名(`{ClassName}Test`)、@TestMethodOrder、Mockito 使用 |
| 集成测试(MockMvc) | Controller 层测试模板、JSON 断言、状态码验证 |
| DRL 规则测试 | KieSession 注入、Fact 插入、规则触发验证、fireAllRules 断言 |
| Aviator 公式测试 | 表达式编译缓存测试、边界值测试(零/负数/极大值)、精度测试 |
| Skill Pipeline 测试 | Skill 独立测试、Pipeline 串联测试、BLOCK/WARN 结果验证 |
| 微服务集成测试 | 服务间调用测试、网关路由测试、端到端结算流程测试 |
| 测试覆盖率 | 核心规则 ≥90%、工具类 ≥80%、Controller ≥70%、Skill ≥85% |

---

### security.md ⚡ 安全检查

**触发条件**: 编写 SQL 查询、接收用户输入、执行 Aviator 公式、配置网关路由

| 章节 | 内容 |
|------|------|
| SQL 注入防护 | MyBatis `#{}` 参数绑定强制使用、禁止 `${}` 字符串拼接、LIKE 参数化 |
| XSS 防护 | 输入过滤(HtmlUtils.htmlEscape)、输出编码、富文本白名单 |
| Aviator 表达式注入 | 白名单函数限制、禁用 dangerous 函数、沙箱模式(SandboxAviatorEvaluator) |
| 网关安全 | JWT 鉴权、限流熔断(Sentinel)、路由白名单、CORS 配置 |
| 微服务间安全 | Feign 调用鉴权、服务间 Token 传递、内部接口保护 |
| 认证与授权 | Spring Security JWT、接口权限注解(@PreAuthorize)、数据权限过滤 |
| 敏感数据 | 日志脱敏(手机号/身份证/银行卡)、密码 bcrypt 加密、配置加密(Jasypt) |

---

### database.md ✨ 数据库设计

**触发条件**: 创建表、编写 Mapper XML、添加索引

| 章节 | 内容 |
|------|------|
| 表命名规范 | `his_{业务域}_{实体}` 格式、统一 utf8mb4、必须含 id/create_time/update_time |
| 核心表结构 | rule_definition / aviator_formula / settlement_result / settlement_detail / audit_log / drg_definition / quality_indicator |
| 索引规范 | 主键 BIGINT 自增、业务唯一键 uk_xxx、查询索引 idx_xxx、联合索引最左前缀 |
| 查询优化 | 避免 SELECT *、分页查询(PageHelper/MyBatis-Plus)、大表必带索引条件 |
| 事务管理 | @Transactional 只读优化、传播行为(REQUIRED)、隔离级别(READ_COMMITTED) |

---

### error-handling.md ✨ 错误处理

**触发条件**: 编写 API 错误响应、定义异常类、记录日志

| 章节 | 内容 |
|------|------|
| 统一错误码 | `HIS-{模块}-{三位数字}` 格式、20+ 标准错误码、业务模块示例 |
| 异常层级 | HisBaseException → BusinessException / SystemException / FormulaException / GatewayException |
| 错误响应格式 | `{code, message, detail, timestamp, traceId}` |
| 全局异常处理器 | @RestControllerAdvice + @ExceptionHandler 分层捕获 |
| 微服务错误传播 | Feign 错误解码、网关错误响应统一格式、链路追踪 traceId 传递 |
| 日志规范 | 5 级别(ERROR/WARN/INFO/DEBUG/TRACE)、格式(JSON)、敏感信息脱敏 |

---

### docker-deploy.md ✨ Docker 部署

**触发条件**: 构建 Docker 镜像、编写 docker-compose.yml、容器运维

| 章节 | 内容 |
|------|------|
| 多阶段构建 | Maven 构建 + JRE 运行、镜像精简(alpine)、非 root 用户 |
| JVM 优化 | G1GC、堆内存(-Xms512m -Xmx1024m)、GC 暂停目标(200ms) |
| 微服务编排 | gateway + rule-service + settlement-service + drug-service + quality-service + mysql + nacos |
| 环境变量 | SPRING_PROFILES_ACTIVE / MYSQL_HOST / NACOS_SERVER_ADDR / 服务发现配置 |
| 安全加固 | 非 root 用户、只读文件系统(可选)、资源限制(memory/cpu)、网络隔离 |

---

### performance.md ✨ 性能优化

**触发条件**: 发现性能瓶颈、新功能预防性优化、生产调优

| 章节 | 内容 |
|------|------|
| Caffeine 缓存 | Expression 对象池(5000条)、30分钟过期、编译耗时监控(>1ms告警) |
| KieBase 分组 | 按业务域(reimbursement/settlement/validation)分组、独立加载卸载 |
| Sentinel 限流 | QPS 限流、并发线程数控制、熔断降级、系统自适应保护 |
| 并发控制 | KieSession 线程安全(StatelessKieSession)、规则执行超时(10s)、线程池隔离 |
| 数据库优化 | 连接池(HikariCP)、慢查询阈值(1s)、批量操作(batchSize=1000) |
| Aviator 优化 | 编译结果缓存(必做)、避免频繁 compile、自定义函数预注册 |

---

### api-design.md 📋 API 设计

**触发条件**: 设计新接口、编写 Controller、定义 DTO、配置网关路由

| 章节 | 内容 |
|------|------|
| URL 规范 | `/api/v1/{模块}/{资源}` 版本化管理、名词复数、kebab-case 查询参数 |
| Controller 模板 | @RestController + @RequestMapping + @RequiredArgsConstructor + @Slf4j |
| DTO 规范 | CreateDTO / UpdateDTO / QueryDTO / VO 分离、@Valid 校验、Swagger 注解 |
| 统一响应 | Result\<T\> 成功/失败包装、PageResult\<T\> 分页包装、HTTP 状态码语义 |
| 网关路由 | Spring Cloud Gateway 路由配置、过滤器链、跨域处理、限流配置 |
| 接口清单 | 规则管理(/api/v1/rules/*)、结算(/api/v1/settlement/*)、公式(/api/v1/formulas/*)、用药(/api/v1/drugs/*)、质控(/api/v1/quality/*)、DRG(/api/v1/drg/*) |

---

### documentation.md 📋 文档规范

**触发条件**: 编写 DRL 规则注释、Aviator 公式说明、Skill/Agent 文档、微服务文档

| 章节 | 内容 |
|------|------|
| DRL 规则文档 | 规则头注释(包名/功能/作者/版本/触发事件)、前置条件、依赖公式 |
| Aviator 公式文档 | 公式头注释(标识/描述/参数/返回值/示例)、边界说明、版本历史 |
| Skill/Agent 文档 | 接口说明(RuleSkill)、输入输出定义、使用示例、注意事项 |
| 微服务文档 | 服务职责、依赖服务、配置项、健康检查、部署说明 |
| 代码注释 | Javadoc 标准(@param/@return/@throws)、复杂逻辑行内注释、TODO/FIXME 标记 |

---

### git-commit-message.md 📋 Git 提交规范

**触发条件**: 提交代码、编写 Commit Message

| 章节 | 内容 |
|------|------|
| 提交格式 | `<type>(<scope>): <subject>`  Conventional Commits 规范 |
| Type 类型 | feat/fix/docs/style/refactor/test/chore/deploy |
| Scope 范围 | rule/formula/settlement/drug/quality/drg/gateway/common |
| Subject 规范 | 中文描述、动词开头、不超过 50 字符 |
| Body 规范 | 详细说明改动原因、影响范围、不兼容变更 |

---

## 触发场景速查

| 开发任务 | 关联规则文件 |
|---------|------------|
| 新建 Rule Definition | workflow → code-style → database → api-design → testing |
| 编写 DRL 规则 | workflow → code-style(Drools章节) → documentation → testing(DRL测试) |
| 编写 Aviator 公式 | workflow → code-style(Aviator章节) → security(注入防护) → performance → testing |
| 开发 Settlement API | workflow → api-design → error-handling → security → testing |
| 新增微服务模块 | workflow → code-style(微服务章节) → api-design → docker-deploy → testing |
| 配置网关路由 | workflow → api-design(网关章节) → security(网关安全) → performance |
| Docker 部署 | docker-deploy → performance → security(Docker加固) |
| 性能优化 | performance → database(查询优化) → code-style(BigDecimal) |
| Bug 修复 | workflow → error-handling → testing → security |

---

最后更新: 2026-05-12 | v1.1 HIS Drools+Aviator 规则引擎版（微服务架构）
