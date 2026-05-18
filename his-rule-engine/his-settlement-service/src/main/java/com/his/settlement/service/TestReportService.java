package com.his.settlement.service;

import com.lowagie.text.pdf.BaseFont;
import com.his.settlement.entity.TestCase;
import com.his.settlement.entity.TestDataSet;
import com.his.settlement.mapper.TestCaseMapper;
import com.his.settlement.mapper.TestDataSetMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * 测试报告生成服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TestReportService {

    private final TestDataSetMapper dataSetMapper;
    private final TestCaseMapper testCaseMapper;

    private static final DateTimeFormatter DF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public String generateHtmlReport(Long dataSetId, List<Map<String, Object>> executionResults) {
        TestDataSet dataSet = dataSetMapper.selectById(dataSetId);
        if (dataSet == null) {
            return "<h1>数据集不存在</h1>";
        }

        int total = executionResults.size();
        int passed = 0, failed = 0, errors = 0;
        for (Map<String, Object> r : executionResults) {
            String status = r.get("status") != null ? r.get("status").toString() : "UNKNOWN";
            if ("PASS".equals(status)) passed++;
            else if ("FAILED".equals(status)) failed++;
            else errors++;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html lang=\"zh-CN\"><head>");
        sb.append("<meta charset=\"UTF-8\">");
        sb.append("<title>测试报告 - ").append(escapeHtml(dataSet.getDataSetName())).append("</title>");
        sb.append("<style>");
        sb.append("body{font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,sans-serif;margin:40px;background:#f5f7fa}");
        sb.append(".container{max-width:1200px;margin:0 auto;background:white;border-radius:8px;padding:30px;box-shadow:0 2px 12px rgba(0,0,0,0.1)}");
        sb.append("h1{color:#303133;border-bottom:2px solid #409EFF;padding-bottom:15px}");
        sb.append(".summary{display:grid;grid-template-columns:repeat(4,1fr);gap:20px;margin:30px 0}");
        sb.append(".summary-card{padding:20px;border-radius:8px;text-align:center}");
        sb.append(".summary-card.total{background:#ecf5ff}");
        sb.append(".summary-card.passed{background:#f0f9eb}");
        sb.append(".summary-card.failed{background:#fef0f0}");
        sb.append(".summary-card.errors{background:#fdf6ec}");
        sb.append(".summary-card .value{font-size:36px;font-weight:bold}");
        sb.append(".summary-card .label{font-size:14px;color:#909399;margin-top:8px}");
        sb.append(".summary-card.passed .value{color:#67C23A}");
        sb.append(".summary-card.failed .value{color:#F56C6C}");
        sb.append(".summary-card.errors .value{color:#E6A23C}");
        sb.append("table{width:100%;border-collapse:collapse;margin-top:20px}");
        sb.append("th,td{padding:12px 15px;text-align:left;border-bottom:1px solid #ebeef5}");
        sb.append("th{background:#f5f7fa;color:#606266;font-weight:600}");
        sb.append("tr:hover{background:#f5f7fa}");
        sb.append(".status{padding:4px 10px;border-radius:4px;font-size:12px}");
        sb.append(".status.pass{background:#67C23A;color:white}");
        sb.append(".status.failed{background:#F56C6C;color:white}");
        sb.append(".status.error{background:#E6A23C;color:white}");
        sb.append(".diff-table{font-size:13px;margin-top:8px}");
        sb.append(".diff-table td{padding:4px 8px;background:#fef0f0}");
        sb.append(".footer{margin-top:30px;color:#909399;font-size:12px;text-align:center}");
        sb.append("</style></head><body>");
        sb.append("<div class=\"container\">");
        sb.append("<h1>").append(escapeHtml(dataSet.getDataSetName())).append("</h1>");
        sb.append("<p style=\"color:#606266\">").append(escapeHtml(dataSet.getDescription() != null ? dataSet.getDescription() : "")).append("</p>");

        sb.append("<div class=\"summary\">");
        sb.append("<div class=\"summary-card total\"><div class=\"value\">").append(total).append("</div><div class=\"label\">总用例数</div></div>");
        sb.append("<div class=\"summary-card passed\"><div class=\"value\">").append(passed).append("</div><div class=\"label\">通过</div></div>");
        sb.append("<div class=\"summary-card failed\"><div class=\"value\">").append(failed).append("</div><div class=\"label\">失败</div></div>");
        sb.append("<div class=\"summary-card errors\"><div class=\"value\">").append(errors).append("</div><div class=\"label\">异常</div></div>");
        sb.append("</div>");

        sb.append("<table><thead><tr><th>用例ID</th><th>用例名称</th><th>状态</th><th>耗时</th><th>消息</th><th>详情</th></tr></thead><tbody>");

        for (Map<String, Object> result : executionResults) {
            String caseId = result.get("caseId") != null ? result.get("caseId").toString() : "-";
            String caseName = result.get("caseName") != null ? result.get("caseName").toString() : "-";
            String status = result.get("status") != null ? result.get("status").toString() : "UNKNOWN";
            String elapsed = result.get("elapsed") != null ? result.get("elapsed").toString() : "-";
            String message = result.get("message") != null ? result.get("message").toString() : "";
            @SuppressWarnings("unchecked")
            List<Map<String, String>> diffs = (List<Map<String, String>>) result.get("diff");

            String statusClass = "PASS".equals(status) ? "pass" : "FAILED".equals(status) ? "failed" : "error";

            sb.append("<tr>");
            sb.append("<td>").append(escapeHtml(caseId)).append("</td>");
            sb.append("<td>").append(escapeHtml(caseName)).append("</td>");
            sb.append("<td><span class=\"status ").append(statusClass).append("\">").append(status).append("</span></td>");
            sb.append("<td>").append(escapeHtml(elapsed)).append("</td>");
            sb.append("<td>").append(escapeHtml(message)).append("</td>");
            sb.append("<td>");

            if (diffs != null && !diffs.isEmpty()) {
                sb.append("<details><summary>").append(diffs.size()).append(" 项不匹配</summary>");
                sb.append("<table class=\"diff-table\"><tr><th>字段</th><th>期望值</th><th>实际值</th></tr>");
                for (Map<String, String> diff : diffs) {
                    sb.append("<tr>");
                    sb.append("<td>").append(escapeHtml(diff.get("field"))).append("</td>");
                    sb.append("<td>").append(escapeHtml(diff.get("expected"))).append("</td>");
                    sb.append("<td>").append(escapeHtml(diff.get("actual"))).append("</td>");
                    sb.append("</tr>");
                }
                sb.append("</table></details>");
            }
            sb.append("</td></tr>");
        }

        sb.append("</tbody></table>");
        sb.append("<div class=\"footer\"><p>生成时间: ").append(LocalDateTime.now().format(DF)).append(" | HIS 规则引擎测试沙箱</p></div>");
        sb.append("</div></body></html>");

        return sb.toString();
    }

    private String escapeHtml(String str) {
        if (str == null) return "";
        return str.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");
    }

    /**
     * 生成 PDF 报告
     */
    public byte[] generatePdfReport(Long dataSetId, List<Map<String, Object>> executionResults) {
        String html = generateHtmlReport(dataSetId, executionResults);

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            ITextRenderer renderer = new ITextRenderer();

            // 配置中文字体 - 由 HTML 中的 CSS @font-face 配置处理
            // BaseFont bfChinese = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);

            renderer.setDocumentFromString(html);
            renderer.layout();
            renderer.createPDF(out);

            log.info("PDF报告生成成功: dataSetId={}, size={}", dataSetId, out.size());
            return out.toByteArray();
        } catch (Exception e) {
            log.error("PDF报告生成失败: dataSetId={}", dataSetId, e);
            throw new RuntimeException("PDF报告生成失败: " + e.getMessage(), e);
        }
    }
}