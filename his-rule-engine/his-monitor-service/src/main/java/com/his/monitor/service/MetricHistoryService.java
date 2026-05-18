package com.his.monitor.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.his.monitor.dto.MetricHistoryQueryDTO;
import com.his.monitor.entity.MetricRecord;
import com.his.monitor.mapper.MetricRecordMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 指标历史存储服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MetricHistoryService {

    private final MetricRecordMapper metricRecordMapper;

    // 内存缓冲，用于批量写入数据库
    private final List<MetricRecord> buffer = new java.util.concurrent.CopyOnWriteArrayList<>();
    private static final int BUFFER_SIZE = 100;
    private static final String DEFAULT_TENANT = "T001";

    // 定时任务每5分钟保存一次内存指标到数据库
    @Scheduled(cron = "0 */5 * * * ?")
    public void flushMetrics() {
        try {
            List<MetricRecord> toSave = new ArrayList<>(buffer);
            buffer.clear();

            if (!toSave.isEmpty()) {
                for (MetricRecord record : toSave) {
                    metricRecordMapper.insert(record);
                }
                log.info("保存指标历史记录: count={}", toSave.size());
            }
        } catch (Exception e) {
            log.error("保存指标历史失败", e);
        }
    }

    /**
     * 记录规则命中指标
     * <p>专门用于记录规则触发的详细信息，包含时间戳</p>
     */
    public void recordRuleHit(String ruleKey) {
        record("rule_hit", new BigDecimal(1), ruleKey);
    }

    /**
     * 记录规则命中指标（带租户）
     */
    public void recordRuleHit(String ruleKey, String tenantId) {
        MetricRecord record = new MetricRecord();
        record.setMetricName("rule_hit");
        record.setMetricValue(BigDecimal.ONE);
        record.setTag(ruleKey);
        record.setTenantId(tenantId != null ? tenantId : DEFAULT_TENANT);
        record.setRecordTime(LocalDateTime.now());
        buffer.add(record);

        if (buffer.size() >= BUFFER_SIZE) {
            flushMetrics();
        }
    }

    /**
     * 记录公式执行指标
     */
    public void recordFormulaExecution(String formulaKey, boolean success) {
        record("formula_exec", success ? BigDecimal.ONE : BigDecimal.ZERO, formulaKey);
    }
    public void record(String metricName, BigDecimal value, String tag) {
        MetricRecord record = new MetricRecord();
        record.setMetricName(metricName);
        record.setMetricValue(value);
        record.setTag(tag);
        record.setTenantId(DEFAULT_TENANT);
        record.setRecordTime(LocalDateTime.now());
        buffer.add(record);

        if (buffer.size() >= BUFFER_SIZE) {
            flushMetrics();
        }
    }

    /**
     * 查询指标历史
     */
    public Map<String, Object> queryHistory(MetricHistoryQueryDTO query) {
        LocalDateTime startTime = query.getStartTime();
        LocalDateTime endTime = query.getEndTime();

        if (startTime == null) {
            startTime = LocalDateTime.now().minusHours(1);
        }
        if (endTime == null) {
            endTime = LocalDateTime.now();
        }

        LambdaQueryWrapper<MetricRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MetricRecord::getTenantId, DEFAULT_TENANT);
        wrapper.eq(StringUtils.hasText(query.getMetricName()), MetricRecord::getMetricName, query.getMetricName());
        wrapper.eq(StringUtils.hasText(query.getTag()), MetricRecord::getTag, query.getTag());
        wrapper.ge(MetricRecord::getRecordTime, startTime);
        wrapper.le(MetricRecord::getRecordTime, endTime);
        wrapper.orderByDesc(MetricRecord::getRecordTime);

        List<MetricRecord> records = metricRecordMapper.selectList(wrapper);

        // 按时间聚合
        List<Map<String, Object>> dataPoints = records.stream()
                .map(r -> {
                    Map<String, Object> point = new LinkedHashMap<>();
                    point.put("time", r.getRecordTime().toString());
                    point.put("value", r.getMetricValue());
                    point.put("tag", r.getTag());
                    return point;
                })
                .collect(Collectors.toList());

        // 额外查询：最新N条
        if (query.getLatest() != null && query.getLatest() > 0) {
            LambdaQueryWrapper<MetricRecord> latestWrapper = new LambdaQueryWrapper<>();
            latestWrapper.eq(MetricRecord::getTenantId, DEFAULT_TENANT);
            latestWrapper.eq(StringUtils.hasText(query.getMetricName()), MetricRecord::getMetricName, query.getMetricName());
            latestWrapper.orderByDesc(MetricRecord::getRecordTime);
            latestWrapper.last("LIMIT " + query.getLatest());
            List<MetricRecord> latestRecords = metricRecordMapper.selectList(latestWrapper);
            wrapper.orderByDesc(MetricRecord::getRecordTime);
        }

        // 统计摘要
        Map<String, Object> summary = new LinkedHashMap<>();
        if (!records.isEmpty()) {
            BigDecimal sum = records.stream().map(MetricRecord::getMetricValue).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal max = records.stream().map(MetricRecord::getMetricValue).max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
            BigDecimal min = records.stream().map(MetricRecord::getMetricValue).min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
            BigDecimal avg = sum.divide(BigDecimal.valueOf(records.size()), 4, RoundingMode.HALF_UP);

            summary.put("count", records.size());
            summary.put("sum", sum);
            summary.put("avg", avg.setScale(4, RoundingMode.HALF_UP));
            summary.put("max", max);
            summary.put("min", min);
        } else {
            summary.put("count", 0);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("metricName", query.getMetricName());
        result.put("startTime", startTime.toString());
        result.put("endTime", endTime.toString());
        result.put("granularity", query.getGranularity());
        result.put("aggregation", query.getAggregation());
        result.put("dataPoints", dataPoints);
        result.put("summary", summary);

        return result;
    }

    /**
     * 获取趋势数据（用于图表）
     */
    public List<Map<String, Object>> getTrend(String metricName, int hours, String tag) {
        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime = endTime.minusHours(hours);

        LambdaQueryWrapper<MetricRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MetricRecord::getTenantId, DEFAULT_TENANT);
        wrapper.eq(MetricRecord::getMetricName, metricName);
        wrapper.eq(StringUtils.hasText(tag), MetricRecord::getTag, tag);
        wrapper.ge(MetricRecord::getRecordTime, startTime);
        wrapper.le(MetricRecord::getRecordTime, endTime);
        wrapper.orderByAsc(MetricRecord::getRecordTime);

        List<MetricRecord> records = metricRecordMapper.selectList(wrapper);

        // 按小时聚合
        DateTimeFormatter hourFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:00");
        Map<String, List<BigDecimal>> grouped = new LinkedHashMap<>();

        for (MetricRecord record : records) {
            String hourKey = record.getRecordTime().format(hourFmt);
            grouped.computeIfAbsent(hourKey, k -> new ArrayList<>()).add(record.getMetricValue());
        }

        return grouped.entrySet().stream()
                .map(e -> {
                    List<BigDecimal> values = e.getValue();
                    BigDecimal avg = values.stream().reduce(BigDecimal.ZERO, BigDecimal::add)
                            .divide(BigDecimal.valueOf(values.size()), 4, RoundingMode.HALF_UP);
                    Map<String, Object> point = new LinkedHashMap<>();
                    point.put("time", e.getKey());
                    point.put("value", avg.setScale(2, RoundingMode.HALF_UP));
                    point.put("count", values.size());
                    return point;
                })
                .collect(Collectors.toList());
    }

    /**
     * 获取规则触发热力图数据
     */
    public Map<String, Object> getRuleHeatmapData(int days) {
        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime = endTime.minusDays(days);

        LambdaQueryWrapper<MetricRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MetricRecord::getTenantId, DEFAULT_TENANT);
        wrapper.eq(MetricRecord::getMetricName, "rule_hit");
        wrapper.ge(MetricRecord::getRecordTime, startTime);
        wrapper.le(MetricRecord::getRecordTime, endTime);

        List<MetricRecord> records = metricRecordMapper.selectList(wrapper);

        // 按规则+小时聚合
        DateTimeFormatter hourFmt = DateTimeFormatter.ofPattern("HH");
        Map<String, Map<String, AtomicLong>> heatData = new ConcurrentHashMap<>();

        for (MetricRecord record : records) {
            String ruleKey = record.getTag();
            String hour = record.getRecordTime().format(hourFmt);
            if (ruleKey != null && hour != null) {
                heatData.computeIfAbsent(ruleKey, k -> new ConcurrentHashMap<>())
                        .computeIfAbsent(hour, k -> new AtomicLong(0))
                        .incrementAndGet();
            }
        }

        // 转换为热力图格式
        List<String> hours = IntStream.range(0, 24).mapToObj(i -> String.format("%02d", i)).toList();
        List<Map<String, Object>> data = heatData.entrySet().stream()
                .map(e -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("ruleKey", e.getKey());
                    for (String h : hours) {
                        row.put(h, e.getValue().getOrDefault(h, new AtomicLong(0)).get());
                    }
                    return row;
                })
                .collect(Collectors.toList());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("hours", hours);
        result.put("rules", data);
        result.put("totalRules", data.size());
        result.put("timeRange", days + "天");

        return result;
    }

    /**
     * 获取按小时分布的规则命中统计
     * <p>用于热力图的时间轴分析</p>
     */
    public Map<String, Object> getHourlyRuleHitDistribution(int days) {
        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime = endTime.minusDays(days);

        LambdaQueryWrapper<MetricRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MetricRecord::getTenantId, DEFAULT_TENANT);
        wrapper.eq(MetricRecord::getMetricName, "rule_hit");
        wrapper.ge(MetricRecord::getRecordTime, startTime);
        wrapper.le(MetricRecord::getRecordTime, endTime);

        List<MetricRecord> records = metricRecordMapper.selectList(wrapper);

        // 按星期几 + 小时聚合
        DateTimeFormatter dowFmt = DateTimeFormatter.ofPattern("u");  // 1=周一, 7=周日
        DateTimeFormatter hourFmt = DateTimeFormatter.ofPattern("HH");
        Map<String, AtomicLong> distribution = new ConcurrentHashMap<>();

        for (MetricRecord record : records) {
            String dow = record.getRecordTime().format(dowFmt);
            String hour = record.getRecordTime().format(hourFmt);
            String key = dow + "-" + hour;
            distribution.computeIfAbsent(key, k -> new AtomicLong(0)).incrementAndGet();
        }

        // 构建 7x24 矩阵
        String[] weekDays = {"周一", "周二", "周三", "周四", "周五", "周六", "周日"};
        List<Map<String, Object>> matrix = new ArrayList<>();
        for (int d = 1; d <= 7; d++) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("day", weekDays[d - 1]);
            row.put("dayOfWeek", d);
            for (int h = 0; h < 24; h++) {
                String key = d + "-" + String.format("%02d", h);
                row.put(String.format("%02d", h), distribution.getOrDefault(key, new AtomicLong(0)).get());
            }
            matrix.add(row);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("matrix", matrix);
        result.put("timeRange", days + "天");

        return result;
    }
}