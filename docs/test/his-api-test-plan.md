# HIS 动态规则中台 - 接口测试计划

## 文档信息

| 属性 | 值 |
|------|-----|
| 项目名称 | HIS Drools+Aviator 规则引擎 |
| 文档版本 | V1.0 |
| 创建日期 | 2026-05-17 |
| 测试范围 | 全部 13 个 Controller，约 100+ 个 API 接口 |
| 测试工具 | Postman / ApiPost |
| 测试目标 | 100% 接口覆盖，100% 通过率 |

---

## 一、测试环境配置

### 1.1 微服务端口清单

| 微服务 | 端口 | 说明 |
|--------|------|------|
| his-gateway | 9000 | API 网关，统一入口 |
| his-rule-service | 9001 | 规则管理、规则组、规则流、审计日志 |
| his-formula-service | - | 公式管理 (独立服务) |
| his-settlement-service | 9002 | 结算管理、沙箱测试 |
| his-drug-service | 9003 | 合理用药检查 |
| his-quality-service | 9004 | 质控指标管理 |
| his-drg-service | 9005 | DRG 分组管理 |
| his-monitor-service | - | 系统监控 |
| his-market-service | - | 规则模板市场 |

### 1.2 环境变量

导入 `his-api-tests.postman_environment.json` 到 Postman/ApiPost：

```
gateway_url           = http://localhost:9000
rule_service_url      = http://localhost:9001
settlement_service_url= http://localhost:9002
drug_service_url      = http://localhost:9003
quality_service_url   = http://localhost:9004
drg_service_url       = http://localhost:9005
tenant_id             = tenant_demo_001
user_id               = admin
```

### 1.3 通用请求头

| Header | 值 | 说明 |
|--------|-----|------|
| Content-Type | application/json | JSON 格式 |
| Authorization | Bearer {{auth_token}} | JWT Token |
| X-Tenant-Id | {{tenant_id}} | 租户隔离 |
| X-User-Id | {{user_id}} | 操作人标识 |

---

## 二、统一响应格式

### 2.1 成功响应

```json
{
  "code": "0",
  "data": { ... },
  "message": "操作成功",
  "timestamp": 1715961600000
}
```

### 2.2 分页响应

```json
{
  "code": "0",
  "data": {
    "list": [ ... ],
    "total": 100,
    "page": 1,
    "pageSize": 10,
    "totalPages": 10,
    "hasNext": true
  },
  "message": "查询成功"
}
```

### 2.3 错误响应

```json
{
  "code": "HIS-001",
  "data": null,
  "message": "患者身份信息缺失",
  "timestamp": 1715961600000
}
```

---

## 三、测试通过标准

### 3.1 功能验证

| 检查项 | 判定标准 |
|--------|---------|
| HTTP 状态码 | 200 (正常) / 400 (参数错误) / 401 (未授权) / 404 (不存在) |
| 响应码 | code="0" 表示成功，非0表示业务错误 |
| 数据完整性 | 必填字段返回完整，类型正确 |
| 业务逻辑 | 计算结果正确，状态变更符合预期 |

### 3.2 参数验证

| 检查项 | 判定标准 |
|--------|---------|
| 必填字段 | 缺失时返回对应错误提示 |
| 格式校验 | 不符合正则/长度限制时返回错误 |
| 类型校验 | 类型不匹配时返回错误 |
| 边界值 | 最小值、最大值、空值、负数等边界情况处理正确 |

### 3.3 错误处理

| 检查项 | 判定标准 |
|--------|---------|
| 异常捕获 | 系统异常返回统一错误格式，不暴露堆栈信息 |
| 降级处理 | 服务不可用时返回降级响应 |
| 超时处理 | 超时返回明确错误提示 |

---

## 四、模块测试用例清单

### 模块 01: 认证模块 (Auth) - 4 个用例

| 用例编号 | 接口 | 方法 | 测试场景 | 预期结果 |
|---------|------|------|---------|---------|
| TC-AUTH-001 | /api/v1/auth/login | POST | 正常登录 | code="0"，返回token |
| TC-AUTH-002 | /api/v1/auth/login | POST | 错误密码 | code≠"0"，提示密码错误 |
| TC-AUTH-003 | /api/v1/auth/login | POST | 空用户名 | code≠"0"，提示参数缺失 |
| TC-AUTH-004 | /api/v1/auth/me | GET | 获取用户信息 | code="0"，返回用户详情 |

### 模块 02: 规则组管理 (Rule Groups) - 9 个用例

| 用例编号 | 接口 | 方法 | 测试场景 | 预期结果 |
|---------|------|------|---------|---------|
| TC-RG-001 | /api/v1/rule-groups/page | GET | 分页列表 | code="0"，返回分页数据 |
| TC-RG-002 | /api/v1/rule-groups | GET | 所有启用规则组 | code="0"，返回数组 |
| TC-RG-003 | /api/v1/rule-groups | POST | 创建规则组 | code="0"，创建成功 |
| TC-RG-004 | /api/v1/rule-groups | POST | 缺少必填字段 | code≠"0"，参数校验错误 |
| TC-RG-005 | /api/v1/rule-groups/{id} | GET | 规则组详情 | code="0"，返回详情 |
| TC-RG-006 | /api/v1/rule-groups/{id} | GET | 不存在的ID | code≠"0"，不存在提示 |
| TC-RG-007 | /api/v1/rule-groups/{id} | PUT | 更新规则组 | code="0"，更新成功 |
| TC-RG-008 | /api/v1/rule-groups/{id}/enabled | PUT | 启用/禁用 | code="0"，状态变更 |
| TC-RG-009 | /api/v1/rule-groups/{id} | DELETE | 删除规则组 | code="0"，删除成功 |

### 模块 03: 规则定义管理 (Rule Definitions) - 10 个用例

| 用例编号 | 接口 | 方法 | 测试场景 | 预期结果 |
|---------|------|------|---------|---------|
| TC-RD-001 | /api/v1/rules | GET | 分页列表 | code="0"，返回分页数据 |
| TC-RD-002 | /api/v1/rules | GET | 按条件筛选 | code="0"，筛选结果正确 |
| TC-RD-003 | /api/v1/rules | POST | 创建规则 | code="0"，创建成功 |
| TC-RD-004 | /api/v1/rules | POST | ruleKey格式错误 | code≠"0"，格式校验错误 |
| TC-RD-005 | /api/v1/rules | POST | DRL内容为空 | code≠"0"，必填校验 |
| TC-RD-006 | /api/v1/rules/{id} | GET | 规则详情 | code="0"，返回详情 |
| TC-RD-007 | /api/v1/rules/{id} | PUT | 更新规则 | code="0"，更新成功 |
| TC-RD-008 | /api/v1/rules/{id}/validate | POST | 校验规则 | code="0"，语法正确 |
| TC-RD-009 | /api/v1/rules/{id}/publish | POST | 发布规则 | code="0"，发布成功 |
| TC-RD-010 | /api/v1/rules/{id} | DELETE | 删除规则 | code="0"，删除成功 |

### 模块 04: 规则流管理 (Rule Flows) - 13 个用例

| 用例编号 | 接口 | 方法 | 测试场景 | 预期结果 |
|---------|------|------|---------|---------|
| TC-RF-001 | /api/v1/flows | GET | 分页列表 | code="0"，返回分页数据 |
| TC-RF-002 | /api/v1/flows/options | GET | 下拉选项 | code="0"，返回数组 |
| TC-RF-003 | /api/v1/flows | POST | 创建规则流 | code="0"，创建成功 |
| TC-RF-004 | /api/v1/flows/{id} | GET | 规则流详情 | code="0"，返回详情 |
| TC-RF-005 | /api/v1/flows/{id} | PUT | 更新规则流 | code="0"，更新成功 |
| TC-RF-006 | /api/v1/flows/{id}/publish | POST | 发布规则流 | code="0"，发布成功 |
| TC-RF-007 | /api/v1/flows/{id}/versions | GET | 版本历史 | code="0"，返回数组 |
| TC-RF-008 | /api/v1/flows/{id}/rollback | POST | 回滚版本 | code="0"，回滚成功 |
| TC-RF-009 | /api/v1/flows/{id}/compare | GET | 版本对比 | code="0"，返回差异 |
| TC-RF-010 | /api/v1/flows/{id}/export | GET | 导出流程 | code="0"，返回JSON |
| TC-RF-011 | /api/v1/flows/import | POST | 导入流程 | code="0"，导入成功 |
| TC-RF-012 | /api/v1/flows/{id}/execute | POST | 执行流程 | code="0"，执行完成 |
| TC-RF-013 | /api/v1/flows/{id} | DELETE | 删除规则流 | code="0"，删除成功 |

### 模块 05: 公式管理 (Formulas) - 14 个用例

| 用例编号 | 接口 | 方法 | 测试场景 | 预期结果 |
|---------|------|------|---------|---------|
| TC-FM-001 | /api/v1/formulas | GET | 分页列表 | code="0"，返回分页数据 |
| TC-FM-002 | /api/v1/formulas/{id} | GET | 公式详情 | code="0"，返回详情 |
| TC-FM-003 | /api/v1/formulas | POST | 创建公式 | code="0"，创建成功 |
| TC-FM-004 | /api/v1/formulas | POST | formulaKey格式错误 | code≠"0"，格式校验错误 |
| TC-FM-005 | /api/v1/formulas/{id} | PUT | 更新公式 | code="0"，更新成功 |
| TC-FM-006 | /api/v1/formulas/{id}/validate | POST | 校验公式 | code="0"，语法正确 |
| TC-FM-007 | /api/v1/formulas/{id}/validate | POST | 语法错误公式 | code≠"0"，语法错误 |
| TC-FM-008 | /api/v1/formulas/{id}/test | POST | 测试公式 | code="0"，返回结果 |
| TC-FM-009 | /api/v1/formulas/{id}/versions | GET | 版本历史 | code="0"，返回数组 |
| TC-FM-010 | /api/v1/formulas/{id}/publish | POST | 发布公式 | code="0"，发布成功 |
| TC-FM-011 | /api/v1/formulas/{id} | DELETE | 删除公式 | code="0"，删除成功 |
| TC-FM-012 | /api/v1/formulas/cache/stats | GET | 缓存统计 | code="0"，返回统计 |
| TC-FM-013 | /api/v1/formulas/cache/refresh | POST | 刷新缓存 | code="0"，刷新成功 |
| TC-FM-014 | /api/v1/formulas/{id}/rollback | POST | 回滚公式 | code="0"，回滚成功 |

### 模块 06: 结算管理 (Settlements) - 8 个用例

| 用例编号 | 接口 | 方法 | 测试场景 | 预期结果 |
|---------|------|------|---------|---------|
| TC-ST-001 | /api/v1/settlements | POST | 执行结算 | code="0"，返回结算结果 |
| TC-ST-002 | /api/v1/settlements | POST | 缺少必填字段 | code≠"0"，参数校验错误 |
| TC-ST-003 | /api/v1/settlements | POST | 金额为零 | code="0"，正常处理零金额 |
| TC-ST-004 | /api/v1/settlements | POST | 金额极大值 | code="0"，正常处理大金额 |
| TC-ST-005 | /api/v1/settlements | GET | 分页列表 | code="0"，返回分页数据 |
| TC-ST-006 | /api/v1/settlements/{no} | GET | 结算详情 | code="0"，返回详情 |
| TC-ST-007 | /api/v1/settlements/{id} | PUT | 更新结算 | code="0"，更新成功 |
| TC-ST-008 | /api/v1/settlements/{id} | DELETE | 删除结算 | code="0"，删除成功 |

### 模块 07: 沙箱测试 (Sandbox) - 17 个用例

| 用例编号 | 接口 | 方法 | 测试场景 | 预期结果 |
|---------|------|------|---------|---------|
| TC-SB-001 | /api/v1/sandbox/datasets | GET | 数据集列表 | code="0"，返回列表 |
| TC-SB-002 | /api/v1/sandbox/datasets | POST | 创建数据集 | code="0"，创建成功 |
| TC-SB-003 | /api/v1/sandbox/datasets/{id} | GET | 数据集详情 | code="0"，返回详情 |
| TC-SB-004 | /api/v1/sandbox/datasets/{id} | PUT | 更新数据集 | code="0"，更新成功 |
| TC-SB-005 | /api/v1/sandbox/datasets/{id} | DELETE | 删除数据集 | code="0"，删除成功 |
| TC-SB-006 | /api/v1/sandbox/execute/{caseId} | POST | 执行单用例 | code="0"，返回结果 |
| TC-SB-007 | /api/v1/sandbox/batch-execute/{dataSetId} | POST | 批量执行 | code="0"，返回批量结果 |
| TC-SB-008 | /api/v1/sandbox/report/{dataSetId} | GET | HTML报告 | 返回HTML内容 |
| TC-SB-009 | /api/v1/sandbox/report/{dataSetId}/pdf | GET | PDF报告 | 返回PDF流 |
| TC-SB-010 | /api/v1/sandbox/execution-logs | GET | 执行日志列表 | code="0"，返回列表 |
| TC-SB-011 | /api/v1/sandbox/execution-logs/{id} | GET | 执行日志详情 | code="0"，返回详情 |
| TC-SB-012 | /api/v1/sandbox/suites | GET | 测试套件列表 | code="0"，返回列表 |
| TC-SB-013 | /api/v1/sandbox/suites | POST | 创建测试套件 | code="0"，创建成功 |
| TC-SB-014 | /api/v1/sandbox/suites/{id} | GET | 测试套件详情 | code="0"，返回详情 |
| TC-SB-015 | /api/v1/sandbox/suites/{id} | PUT | 更新测试套件 | code="0"，更新成功 |
| TC-SB-016 | /api/v1/sandbox/suites/{id} | DELETE | 删除测试套件 | code="0"，删除成功 |
| TC-SB-017 | /api/v1/sandbox/suites/{id}/execute | POST | 执行测试套件 | code="0"，返回执行结果 |

### 模块 08: 合理用药 (Drugs) - 5 个用例

| 用例编号 | 接口 | 方法 | 测试场景 | 预期结果 |
|---------|------|------|---------|---------|
| TC-DR-001 | /api/v1/drugs | GET | 药品目录分页 | code="0"，返回列表 |
| TC-DR-002 | /api/v1/drugs/{id} | GET | 药品详情 | code="0"，返回详情 |
| TC-DR-003 | /api/v1/drugs | POST | 新增药品 | code="0"，创建成功 |
| TC-DR-004 | /api/v1/drugs/{id} | PUT | 更新药品 | code="0"，更新成功 |
| TC-DR-005 | /api/v1/drugs/{id} | DELETE | 删除药品 | code="0"，删除成功 |

### 模块 09: 质控指标 (Quality) - 5 个用例

| 用例编号 | 接口 | 方法 | 测试场景 | 预期结果 |
|---------|------|------|---------|---------|
| TC-QC-001 | /api/v1/quality | GET | 质控规则分页 | code="0"，返回列表 |
| TC-QC-002 | /api/v1/quality/{id} | GET | 质控规则详情 | code="0"，返回详情 |
| TC-QC-003 | /api/v1/quality | POST | 新增质控规则 | code="0"，创建成功 |
| TC-QC-004 | /api/v1/quality/{id} | PUT | 更新质控规则 | code="0"，更新成功 |
| TC-QC-005 | /api/v1/quality/{id} | DELETE | 删除质控规则 | code="0"，删除成功 |

### 模块 10: DRG 分组 (DRG) - 5 个用例

| 用例编号 | 接口 | 方法 | 测试场景 | 预期结果 |
|---------|------|------|---------|---------|
| TC-DRG-001 | /api/v1/drg | GET | DRG定义分页 | code="0"，返回列表 |
| TC-DRG-002 | /api/v1/drg/{id} | GET | DRG定义详情 | code="0"，返回详情 |
| TC-DRG-003 | /api/v1/drg | POST | 新增DRG定义 | code="0"，创建成功 |
| TC-DRG-004 | /api/v1/drg/{id} | PUT | 更新DRG定义 | code="0"，更新成功 |
| TC-DRG-005 | /api/v1/drg/{id} | DELETE | 删除DRG定义 | code="0"，删除成功 |

### 模块 11: 系统监控 (Monitor) - 14 个用例

| 用例编号 | 接口 | 方法 | 测试场景 | 预期结果 |
|---------|------|------|---------|---------|
| TC-MN-001 | /api/v1/monitor/metrics | GET | 获取监控指标 | code="0"，返回指标数据 |
| TC-MN-002 | /api/v1/monitor/alerts | GET | 获取告警列表 | code="0"，返回告警列表 |
| TC-MN-003 | /api/v1/monitor/top-rules | GET | 热门规则 | code="0"，返回规则统计 |
| TC-MN-004 | /api/v1/monitor/record | POST | 记录执行指标 | code="0"，记录成功 |
| TC-MN-005 | /api/v1/monitor/alert | POST | 记录告警 | code="0"，记录成功 |
| TC-MN-006 | /api/v1/monitor/history | GET | 指标历史 | code="0"，返回历史数据 |
| TC-MN-007 | /api/v1/monitor/trend | GET | 趋势数据 | code="0"，返回趋势 |
| TC-MN-008 | /api/v1/monitor/heatmap | GET | 热力图数据 | code="0"，返回热力图 |
| TC-MN-009 | /api/v1/monitor/alert-rules | GET | 告警规则列表 | code="0"，返回列表 |
| TC-MN-010 | /api/v1/monitor/alert-rules/{id} | GET | 告警规则详情 | code="0"，返回详情 |
| TC-MN-011 | /api/v1/monitor/alert-rules | POST | 创建告警规则 | code="0"，创建成功 |
| TC-MN-012 | /api/v1/monitor/alert-rules/{id} | PUT | 更新告警规则 | code="0"，更新成功 |
| TC-MN-013 | /api/v1/monitor/alert-rules/{id} | DELETE | 删除告警规则 | code="0"，删除成功 |
| TC-MN-014 | /api/v1/monitor/ws/status | GET | WebSocket状态 | code="0"，返回状态 |

### 模块 12: 规则模板市场 (Market) - 20 个用例

| 用例编号 | 接口 | 方法 | 测试场景 | 预期结果 |
|---------|------|------|---------|---------|
| TC-MK-001 | /api/v1/market/templates | GET | 模板分页 | code="0"，返回列表 |
| TC-MK-002 | /api/v1/market/templates/{id} | GET | 模板详情 | code="0"，返回详情 |
| TC-MK-003 | /api/v1/market/templates | POST | 发布模板 | code="0"，发布成功 |
| TC-MK-004 | /api/v1/market/templates/{id} | PUT | 更新模板 | code="0"，更新成功 |
| TC-MK-005 | /api/v1/market/templates/{id} | DELETE | 删除模板 | code="0"，删除成功 |
| TC-MK-006 | /api/v1/market/templates/{id}/install | POST | 安装模板 | code="0"，安装成功 |
| TC-MK-007 | /api/v1/market/templates/{id}/install | DELETE | 卸载模板 | code="0"，卸载成功 |
| TC-MK-008 | /api/v1/market/templates/my | GET | 我的模板 | code="0"，返回列表 |
| TC-MK-009 | /api/v1/market/templates/subscribed | GET | 已订阅模板 | code="0"，返回列表 |
| TC-MK-010 | /api/v1/market/templates/{id}/ratings | GET | 评分列表 | code="0"，返回评分 |
| TC-MK-011 | /api/v1/market/templates/{id}/rating-summary | GET | 评分汇总 | code="0"，返回汇总 |
| TC-MK-012 | /api/v1/market/templates/{id}/ratings | POST | 提交评分 | code="0"，提交成功 |
| TC-MK-013 | /api/v1/market/templates/favorites | GET | 收藏列表 | code="0"，返回列表 |
| TC-MK-014 | /api/v1/market/templates/{id}/favorite | POST | 收藏模板 | code="0"，收藏成功 |
| TC-MK-015 | /api/v1/market/templates/{id}/favorite | DELETE | 取消收藏 | code="0"，取消成功 |
| TC-MK-016 | /api/v1/market/templates/{id}/favorite-status | GET | 收藏状态 | code="0"，返回状态 |
| TC-MK-017 | /api/v1/market/templates/{id}/check-update | GET | 检查更新 | code="0"，返回更新信息 |
| TC-MK-018 | /api/v1/market/templates/{id}/upgrade | POST | 升级模板 | code="0"，升级成功 |

### 模块 13: 审计日志 (Audit Logs) - 2 个用例

| 用例编号 | 接口 | 方法 | 测试场景 | 预期结果 |
|---------|------|------|---------|---------|
| TC-AL-001 | /api/v1/audit-logs | GET | 分页日志列表 | code="0"，返回列表 |
| TC-AL-002 | /api/v1/audit-logs | GET | 按条件筛选 | code="0"，筛选结果正确 |

---

## 五、测试用例总计

| 模块 | 用例数量 | 优先级 |
|------|---------|--------|
| 01. 认证模块 | 4 | P0 |
| 02. 规则组管理 | 9 | P0 |
| 03. 规则定义管理 | 10 | P0 |
| 04. 规则流管理 | 13 | P0 |
| 05. 公式管理 | 14 | P0 |
| 06. 结算管理 | 8 | P0 |
| 07. 沙箱测试 | 17 | P1 |
| 08. 合理用药 | 5 | P1 |
| 09. 质控指标 | 5 | P1 |
| 10. DRG 分组 | 5 | P1 |
| 11. 系统监控 | 14 | P2 |
| 12. 规则模板市场 | 20 | P2 |
| 13. 审计日志 | 2 | P2 |
| **合计** | **126** | - |

---

## 六、导入和使用说明

### 6.1 Postman 导入步骤

1. 打开 Postman
2. 点击 `Import` 按钮
3. 选择 `Files` 标签
4. 选择以下文件:
   - `his-api-tests.postman_collection.json` - 测试用例集合
   - `his-api-tests.postman_environment.json` - 环境变量
5. 导入后，在右上角选择 `HIS Drools+Aviator API Testing Environment` 环境
6. 展开 Collection，按模块执行测试

### 6.2 ApiPost 导入步骤

1. 打开 ApiPost
2. 点击项目设置 -> `导入`
3. 选择 `Postman Collection v2.1` 格式
4. 选择 `his-api-tests.postman_collection.json`
5. 导入环境变量: 选择 `his-api-tests.postman_environment.json`
6. 切换环境后开始测试

### 6.3 执行顺序建议

1. **先执行认证模块** - 获取 Token
2. **再执行基础数据模块** - 规则组、规则、公式
3. **再执行业务模块** - 结算、沙箱、用药
4. **最后执行辅助模块** - 监控、市场、审计

### 6.4 批量执行

在 Postman 中使用 `Collection Runner`:
1. 点击 Collection 名称右侧的 `...`
2. 选择 `Run collection`
3. 设置迭代次数和延迟
4. 点击 `Run` 开始批量测试

---

## 七、测试数据准备

### 7.1 预置测试数据

在执行测试前，需要准备以下测试数据:

```json
// 测试患者信息
{
  "patientId": "P_TEST_001",
  "patientType": "employee",
  "insuranceType": "basic",
  "hospitalLevel": "三级"
}

// 测试费用数据
{
  "totalFee": 10000.00,
  "deductible": 500.00,
  "ratio": 0.75
}

// 测试药品数据
{
  "drugCode": "DRUG_TEST_001",
  "drugName": "测试药品A",
  "specification": "10mg*12片",
  "dosage": "1片",
  "quantity": 2
}
```

### 7.2 清理测试数据

测试完成后，建议清理测试创建的数据:
- 删除测试创建的规则组
- 删除测试创建的规则
- 删除测试创建的公式
- 删除测试创建的规则流
- 删除测试创建的结算记录

---

## 八、常见问题排查

| 问题 | 原因 | 解决方案 |
|------|------|---------|
| 401 Unauthorized | Token 过期或无效 | 重新登录获取 Token |
| 404 Not Found | 服务未启动或路由错误 | 检查微服务状态和网关路由配置 |
| 500 Internal Server Error | 服务内部异常 | 查看服务日志，检查数据库连接 |
| 响应超时 | 服务响应慢 | 检查服务性能，增加超时时间 |
| 参数校验失败 | 请求格式不正确 | 检查请求体和字段格式 |

---

最后更新: 2026-05-17 | 版本 V1.0
