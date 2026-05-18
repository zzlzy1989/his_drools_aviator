# Phase 2 收尾功能实现计划

> 更新时间: 2026-05-17
> 状态: 待用户确认

## Context

Phase 2 完成后发现两处功能不完整：
1. **V2-M04 告警通知** - 后端 AlertRule 有 notifyChannels/notifyTarget 字段但未实现实际发送
2. **V2-S07 PDF报告** - 仅有 HTML 报告，无 PDF 生成能力

用户要求"继续完成"这两项功能。

---

## 一、V2-M04 钉钉Webhook告警通知实现

### 1.1 设计方案

使用钉钉自定义机器人 Webhook 发送告警消息：
- 钉钉群机器人 webhook（无需额外权限申请）
- 使用 RestTemplate 发送 POST JSON 请求
- 消息格式：钉钉 markdown 格式

### 1.2 文件变更

| 文件 | 操作 | 说明 |
|------|------|------|
| `his-monitor-service/.../service/NotificationService.java` | 新增 | 钉钉Webhook通知服务 |
| `his-monitor-service/.../service/AlertRuleService.java` | 修改 | checkAlerts() 触发通知 |
| `his-monitor-service/.../dto/DingTalkMessage.java` | 新增 | 钉钉消息格式 DTO |

### 1.3 NotificationService 实现

```java
@Service
@Slf4j
public class NotificationService {

    private final RestTemplate restTemplate = new RestTemplate();

    public void sendDingTalkNotification(AlertRule rule, String message) {
        String webhook = rule.getNotifyTarget(); // 钉钉 webhook URL
        if (webhook == null || webhook.isBlank()) {
            log.warn("钉钉 webhook 未配置，跳过通知");
            return;
        }

        // 钉钉 markdown 格式消息
        DingTalkMessage dto = new DingTalkMessage();
        dto.setMsgtype("markdown");
        dto.getMarkdown().setTitle(rule.getRuleName());
        dto.getMarkdown().setText("### 告警通知\n\n**规则**: " + rule.getRuleName() + "\n\n**消息**: " + message + "\n\n**级别**: " + rule.getLevel());

        try {
            restTemplate.postForEntity(webhook, dto, String.class);
            log.info("[DINGTALK] 告警通知发送成功: {}", rule.getRuleName());
        } catch (Exception e) {
            log.error("[DINGTALK] 告警通知发送失败: {}", e.getMessage());
        }
    }
}
```

### 1.4 DingTalkMessage DTO

```java
@Data
public class DingTalkMessage {
    private String msgtype;
    private MarkdownContent markdown = new MarkdownContent();

    @Data
    public static class MarkdownContent {
        private String title;
        private String text;
    }
}
```

### 1.5 AlertRuleService 触发通知

```java
private final NotificationService notificationService;

public List<AlertRule> checkAlerts(String metricName, BigDecimal value) {
    // ... 检测逻辑找到 triggeredRules ...

    for (AlertRule rule : triggeredRules) {
        if ("DINGTALK".equals(rule.getNotifyChannels())) {
            String message = rule.getMessageTemplate()
                .replace("{metric}", metricName)
                .replace("{value}", value.toString());
            notificationService.sendDingTalkNotification(rule, message);
        }
    }
    return triggeredRules;
}

---

## 二、V2-S07 PDF测试报告实现

### 2.1 设计方案

使用 **Flying Saucer (xhtmlrenderer) + OpenPDF** 将现有 HTML 报告转为 PDF：
- 依赖: `com.github.librepdf:openpdf:1.3.30` + `org.xhtmlrenderer:flying-saucer-pdf:9.1.22`
- 复用现有 `TestReportService.generateHtmlReport()` 生成 HTML
- 新增 `generatePdfReport()` 方法，调用 Flying Saucer 将 HTML 转为 PDF

### 2.2 文件变更

| 文件 | 操作 | 说明 |
|------|------|------|
| `his-settlement-service/pom.xml` | 修改 | 添加 OpenPDF + Flying Saucer 依赖 |
| `his-settlement-service/.../service/TestReportService.java` | 修改 | 新增 generatePdfReport() 方法 |
| `his-settlement-service/.../controller/SandboxController.java` | 修改 | 新增 `/report/{id}/pdf` 端点 |

### 2.3 pom.xml 新增依赖

```xml
<dependency>
    <groupId>com.github.librepdf</groupId>
    <artifactId>openpdf</artifactId>
    <version>1.3.30</version>
</dependency>
<dependency>
    <groupId>org.xhtmlrenderer</groupId>
    <artifactId>flying-saucer-pdf</artifactId>
    <version>9.1.22</version>
</dependency>
```

### 2.4 TestReportService 新增方法

```java
public byte[] generatePdfReport(Long dataSetId, List<Map<String, Object>> executionResults) {
    // 1. 生成 HTML
    String html = generateHtmlReport(dataSetId, executionResults);

    // 2. HTML 转 PDF (Flying Saucer)
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ITextRenderer renderer = new ITextRenderer();

    // 配置中文字体（宋体）
    String fontPath = System.getProperty("java.home") + "/lib/fonts/simsun.ttc";
    renderer.getSharedContext().setReplacedElementFactory(
        new FontReplacedElementFactory(
            FontFactory.createFont(fontPath)
        )
    );

    renderer.setDocumentFromString(html);
    renderer.layout();
    renderer.createPDF(out);
    return out.toByteArray();
}
```

### 2.5 SandboxController 新增端点

```java
@GetMapping(value = "/report/{dataSetId}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
public Result<byte[]> generatePdfReport(@PathVariable Long dataSetId) {
    List<Map<String, Object>> results = sandboxService.batchExecute(dataSetId);
    byte[] pdf = testReportService.generatePdfReport(dataSetId, results);
    return Result.success(pdf);
}
```

### 2.6 中文字体处理

Flying Saucer 需要配置中文字体。将 simsun.ttc 放入 resources/fonts/ 目录，配置字体路径：

```java
// 在 generatePdfReport 中
String fontPath = "classpath:fonts/simsun.ttc";
FontFactory.register(fontPath, "SimSun");
Font font = FontFactory.getFont("SimSun", "UniGB-UCS2-H");
```

---

## 三、验证方案

### 3.1 告警通知验证（DingTalk）

1. 启动 his-monitor-service
2. 在钉钉群中添加"自定义机器人"（群设置 → 智能群助手 → 添加机器人 → 自定义）
3. 复制 webhook URL（如 `https://oapi.dingtalk.com/robot/send?access_token=xxx`）
4. 创建告警规则，设置 notifyChannels=DINGTALK，notifyTarget=webhook URL
5. 触发告警（调用 `/monitor/record` 产生超标数据）
6. 检查钉钉群是否收到告警消息

### 3.2 PDF报告验证

1. 启动 his-settlement-service
2. 访问 `GET /api/v1/sandbox/report/{dataSetId}/pdf`
3. 浏览器下载 PDF 文件，检查中文是否正常显示

---

## 四、风险评估

| 风险 | 影响 | 缓解 |
|------|------|------|
| 钉钉 webhook 无效 | 高 | 需用户提供有效的群机器人 webhook URL |
| PDF 中文乱码 | 高 | 配置中文字体 simsun.ttc |
| Flying Saucer CSS 支持不完整 | 中 | HTML 模板避免复杂 CSS，使用内联样式 |