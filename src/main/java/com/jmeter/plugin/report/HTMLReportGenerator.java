package com.jmeter.plugin.report;

import com.google.gson.Gson;
import com.jmeter.plugin.model.ComparisonResult;
import com.jmeter.plugin.model.PerformanceMetrics;
import com.jmeter.plugin.model.TimeSeriesData;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class HTMLReportGenerator {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final Gson gson = new Gson();

    public void generateComparisonReport(
            String outputPath,
            Map<String, PerformanceMetrics> baselineMetrics,
            Map<String, PerformanceMetrics> currentMetrics,
            List<ComparisonResult> comparisonResults,
            String aiSummary,
            double thresholdPercent) throws IOException {
        
        String html = buildComparisonHTML(baselineMetrics, currentMetrics, comparisonResults, aiSummary, thresholdPercent);
        Files.writeString(Paths.get(outputPath), html);
    }

    public void generateSingleReport(
            String outputPath,
            Map<String, PerformanceMetrics> metrics,
            String aiSummary,
            TimeSeriesData timeSeriesData,
            double errorRateThreshold) throws IOException {
        
        String html = buildSingleReportHTML(metrics, aiSummary, timeSeriesData, errorRateThreshold);
        Files.writeString(Paths.get(outputPath), html);
    }

    private String buildComparisonHTML(
            Map<String, PerformanceMetrics> baselineMetrics,
            Map<String, PerformanceMetrics> currentMetrics,
            List<ComparisonResult> comparisonResults,
            String aiSummary,
            double thresholdPercent) {
        
        int testCount = comparisonResults.size();
        int failCount = (int) comparisonResults.stream().filter(ComparisonResult::isDegraded).count();
        int improvedCount = (int) comparisonResults.stream().filter(r -> r.getPercentageChange() < 0).count();
        int warningCount = (int) comparisonResults.stream().filter(r -> !r.isDegraded() && r.getPercentageChange() > 0).count();
        
        PerformanceMetrics firstBaseline = baselineMetrics.values().iterator().next();
        PerformanceMetrics firstCurrent = currentMetrics.values().iterator().next();
        
        boolean hasTimestamps = firstBaseline.getStartTime() != null && firstBaseline.getEndTime() != null;
        String baselineStart = hasTimestamps ? firstBaseline.getStartTime().format(DATE_FORMATTER) : "Not Available";
        String baselineEnd = hasTimestamps ? firstBaseline.getEndTime().format(DATE_FORMATTER) : "Not Available";
        String currentStart = firstCurrent.getStartTime() != null ? firstCurrent.getStartTime().format(DATE_FORMATTER) : "Not Available";
        String currentEnd = firstCurrent.getEndTime() != null ? firstCurrent.getEndTime().format(DATE_FORMATTER) : "Not Available";
        
        String chartData = generateChartData(comparisonResults);
        String tableRows = generateComparisonTableRows(comparisonResults, thresholdPercent);
        
        List<ComparisonResult> degradedResults = comparisonResults.stream()
            .filter(ComparisonResult::isDegraded)
            .collect(Collectors.toList());
        
        String summaryTableRows = degradedResults.isEmpty() 
            ? "<tr><td colspan='9' style='text-align:center;padding:2rem;color:var(--jm-green);font-weight:600;'>✓ No transactions exceed the " + String.format("%.2f", thresholdPercent) + "% threshold. All performance metrics are within acceptable limits!</td></tr>"
            : generateComparisonTableRows(degradedResults, thresholdPercent);
        
        String html = getHTMLTemplate();
        html = html.replace("{{TEST_COUNT}}", String.valueOf(testCount));
        html = html.replace("{{FAIL_COUNT}}", String.valueOf(failCount));
        html = html.replace("{{IMPROVED_COUNT}}", String.valueOf(improvedCount));
        html = html.replace("{{WARNING_COUNT}}", String.valueOf(warningCount));
        html = html.replace("{{BASELINE_START}}", baselineStart);
        html = html.replace("{{BASELINE_END}}", baselineEnd);
        html = html.replace("{{CURRENT_START}}", currentStart);
        html = html.replace("{{CURRENT_END}}", currentEnd);
        html = html.replace("{{AI_SUMMARY}}", aiSummary);
        html = html.replace("{{TABLE_ROWS}}", tableRows);
        html = html.replace("{{SUMMARY_TABLE_ROWS}}", summaryTableRows);
        html = html.replace("{{CHART_DATA}}", chartData);
        html = html.replace("{{THRESHOLD}}", String.format("%.2f", thresholdPercent));
        
        return html;
    }

    private String buildSingleReportHTML(
            Map<String, PerformanceMetrics> metrics,
            String aiSummary,
            TimeSeriesData timeSeriesData,
            double errorRateThreshold) {
        
        // Handle empty metrics gracefully
        if (metrics.isEmpty()) {
            throw new IllegalArgumentException("Cannot generate report: no metrics found. Please ensure the JTL file contains valid test results.");
        }
        
        int totalRequests = metrics.values().stream().mapToInt(PerformanceMetrics::getTotalCount).sum();
        int totalErrors = metrics.values().stream().mapToInt(PerformanceMetrics::getFailureCount).sum();
        int passedRequests = totalRequests - totalErrors;
        double errorRate = totalRequests > 0 ? (totalErrors * 100.0 / totalRequests) : 0.0;
        
        PerformanceMetrics firstMetric = metrics.values().iterator().next();
        
        String chartData = generateSingleReportChartData(metrics);
        String timeSeriesChartData = generateTimeSeriesChartData(timeSeriesData);
        String tableRows = generateSingleReportTableRows(metrics, errorRateThreshold);
        String summaryTableRows = generateSingleReportSummaryTableRows(metrics, errorRateThreshold);
        
        String startTime = firstMetric.getStartTime() != null ? firstMetric.getStartTime().format(DATE_FORMATTER) : "Not Available";
        String endTime = firstMetric.getEndTime() != null ? firstMetric.getEndTime().format(DATE_FORMATTER) : "Not Available";
        
        String html = getSingleReportHTMLTemplate();
        html = html.replace("{{TRANSACTION_COUNT}}", String.valueOf(metrics.size()));
        html = html.replace("{{PASSED_REQUESTS}}", String.valueOf(passedRequests));
        html = html.replace("{{TOTAL_REQUESTS}}", String.valueOf(totalRequests));
        html = html.replace("{{TOTAL_ERRORS}}", String.valueOf(totalErrors));
        html = html.replace("{{ERROR_RATE}}", String.format("%.2f", errorRate));
        html = html.replace("{{ERROR_THRESHOLD}}", String.format("%.1f", errorRateThreshold));
        html = html.replace("{{TIME_SERIES_DATA}}", timeSeriesChartData);
        html = html.replace("{{START_TIME}}", startTime);
        html = html.replace("{{END_TIME}}", endTime);
        html = html.replace("{{AI_SUMMARY}}", aiSummary);
        html = html.replace("{{TABLE_ROWS}}", tableRows);
        html = html.replace("{{SUMMARY_TABLE_ROWS}}", summaryTableRows);
        html = html.replace("{{CHART_DATA}}", chartData);
        
        return html;
    }

    private String generateComparisonTableRows(List<ComparisonResult> results, double thresholdPercent) {
        return results.stream()
                .map(r -> generateComparisonRow(r, thresholdPercent))
                .collect(Collectors.joining("\n"));
    }

    private String formatNumber(double value) {
        if (value == Math.floor(value)) {
            return String.format("%.0f", value);
        } else {
            return String.format("%.2f", value);
        }
    }

    private String generateComparisonRow(ComparisonResult result, double thresholdPercent) {
        // AVG Change color coding
        String avgStatusClass = result.getPercentageChange() > thresholdPercent ? "critical" : 
                                result.getPercentageChange() > 0 ? "warning" : 
                                result.getPercentageChange() < 0 ? "improved" : "neutral";
        String statusDot = getStatusDotClass(result, thresholdPercent);
        String arrow = getArrow(result.getPercentageChange());
        String displayValue = String.format("%+.2f%%", result.getPercentageChange());
        
        // P90 Change color coding
        String p90Arrow = getArrow(result.getP90PercentageChange());
        String p90DisplayValue = String.format("%+.2f%%", result.getP90PercentageChange());
        String p90StatusClass = result.getP90PercentageChange() > thresholdPercent ? "critical" : 
                                result.getP90PercentageChange() > 0 ? "warning" : 
                                result.getP90PercentageChange() < 0 ? "improved" : "neutral";
        
        // Error Change color coding
        String errorArrow = result.getErrorRateChange() > 0 ? "↑" : result.getErrorRateChange() < 0 ? "↓" : "→";
        String errorDisplayValue = String.format("%+.2f%%", result.getErrorRateChange());
        String errorStatusClass = result.getErrorRateChange() > 0 ? "critical" : 
                                  result.getErrorRateChange() < 0 ? "improved" : "neutral";
        
        return String.format(
                "<tr><td class='service-name'>%s</td>" +
                "<td class='metric-value'>%d</td>" +
                "<td class='metric-value'>%d</td>" +
                "<td class='metric-value'>%s ms</td>" +
                "<td class='metric-value'>%s ms</td>" +
                "<td><span class='diff-badge %s'><span class='%s'></span> %s %s</span></td>" +
                "<td class='metric-value'>%s ms</td>" +
                "<td class='metric-value'>%s ms</td>" +
                "<td><span class='diff-badge %s'>%s %s</span></td>" +
                "<td class='metric-value'>%.2f%%</td>" +
                "<td class='metric-value'>%.2f%%</td>" +
                "<td><span class='diff-badge %s'>%s %s</span></td></tr>",
                result.getLabel(),
                result.getBaselineSampleCount(),
                result.getCurrentSampleCount(),
                formatNumber(result.getBaselineAvg()),
                formatNumber(result.getCurrentAvg()),
                avgStatusClass,
                statusDot,
                displayValue,
                arrow,
                formatNumber(result.getBaselineP90()),
                formatNumber(result.getCurrentP90()),
                p90StatusClass,
                p90DisplayValue,
                p90Arrow,
                result.getBaselineErrorRate(),
                result.getCurrentErrorRate(),
                errorStatusClass,
                errorDisplayValue,
                errorArrow);
    }

    private String generateSingleReportTableRows(Map<String, PerformanceMetrics> metrics, double errorRateThreshold) {
        return metrics.entrySet().stream()
                .map(e -> generateSingleReportRow(e, errorRateThreshold))
                .collect(Collectors.joining("\n"));
    }

    private String generateSingleReportSummaryTableRows(Map<String, PerformanceMetrics> metrics, double errorRateThreshold) {
        List<Map.Entry<String, PerformanceMetrics>> highErrorTransactions = metrics.entrySet().stream()
                .filter(e -> e.getValue().getErrorRate() > errorRateThreshold)
                .collect(Collectors.toList());
        
        if (highErrorTransactions.isEmpty()) {
            return "<tr><td colspan='9' style='text-align:center;padding:2rem;color:var(--jm-green);font-weight:600;'>✓ No transactions exceed the " + 
                   String.format("%.1f", errorRateThreshold) + "% error rate threshold. All transactions are within acceptable limits!</td></tr>";
        }
        
        return highErrorTransactions.stream()
                .map(e -> generateSingleReportRow(e, errorRateThreshold))
                .collect(Collectors.joining("\n"));
    }

    private String generateSingleReportRow(Map.Entry<String, PerformanceMetrics> entry, double errorRateThreshold) {
        PerformanceMetrics m = entry.getValue();
        // Use configurable threshold: critical if > 2x threshold, warning if > threshold
        String statusClass = m.getErrorRate() > (errorRateThreshold * 2) ? "critical" : m.getErrorRate() > errorRateThreshold ? "warning" : "improved";
        String statusDot = m.getErrorRate() > (errorRateThreshold * 2) ? "sd-red" : m.getErrorRate() > errorRateThreshold ? "sd-yellow" : "sd-green";
        
        return "<tr><td class='td-name'>" + entry.getKey() + "</td>" +
                "<td class='td-num'>" + m.getTotalCount() + "</td>" +
                "<td class='td-num'>" + formatNumber(m.getAverageResponseTime()) + "</td>" +
                "<td class='td-num'>" + formatNumber(m.getPercentile90()) + "</td>" +
                "<td class='td-num'>" + formatNumber(m.getPercentile95()) + "</td>" +
                "<td class='td-num'>" + formatNumber(m.getPercentile99()) + "</td>" +
                "<td class='td-change'><span class='diff-badge " + statusClass + "'><span class='status-dot " + statusDot + "'></span>" + 
                String.format("%.2f", m.getErrorRate()) + "%</span></td>" +
                "<td class='td-num'>" + m.getFormattedThroughput() + "</td>" +
                "<td class='td-num'>" + m.getSuccessCount() + "/" + m.getTotalCount() + "</td></tr>";
    }

    private String getStatusDotClass(ComparisonResult result, double thresholdPercent) {
        if (result.getPercentageChange() > thresholdPercent) {
            return "status-indicator status-critical";
        } else if (result.getPercentageChange() > 0) {
            return "status-indicator status-warning";
        } else if (result.getPercentageChange() < 0) {
            return "status-indicator status-good";
        }
        return "status-indicator status-neutral";
    }

    private String getArrow(double percentageChange) {
        if (percentageChange > 0) {
            return "<span class='trend-icon'>&#x2191;</span>";
        } else if (percentageChange < 0) {
            return "<span class='trend-icon'>&#x2193;</span>";
        }
        return "<span class='trend-icon'>&#x2013;</span>";
    }

    private String generateChartData(List<ComparisonResult> results) {
        List<String> labels = results.stream().map(ComparisonResult::getLabel).collect(Collectors.toList());
        List<Double> baselineAvg = results.stream().map(ComparisonResult::getBaselineAvg).collect(Collectors.toList());
        List<Double> currentAvg = results.stream().map(ComparisonResult::getCurrentAvg).collect(Collectors.toList());
        List<Double> baselineP90 = results.stream().map(ComparisonResult::getBaselineP90).collect(Collectors.toList());
        List<Double> currentP90 = results.stream().map(ComparisonResult::getCurrentP90).collect(Collectors.toList());
        List<Double> baselineErrors = results.stream().map(ComparisonResult::getBaselineErrorRate).collect(Collectors.toList());
        List<Double> currentErrors = results.stream().map(ComparisonResult::getCurrentErrorRate).collect(Collectors.toList());
        
        return String.format("{\"labels\": %s, \"baseline\": %s, \"current\": %s, \"baselineP90\": %s, \"currentP90\": %s, \"baselineErrors\": %s, \"currentErrors\": %s}",
                gson.toJson(labels), gson.toJson(baselineAvg), gson.toJson(currentAvg), 
                gson.toJson(baselineP90), gson.toJson(currentP90),
                gson.toJson(baselineErrors), gson.toJson(currentErrors));
    }

    private String generateSingleReportChartData(Map<String, PerformanceMetrics> metrics) {
        List<String> labels = metrics.keySet().stream().collect(Collectors.toList());
        List<Double> avgData = metrics.values().stream().map(PerformanceMetrics::getAverageResponseTime).collect(Collectors.toList());
        List<Double> p95Data = metrics.values().stream().map(PerformanceMetrics::getPercentile95).collect(Collectors.toList());
        List<Double> errorRates = metrics.values().stream().map(PerformanceMetrics::getErrorRate).collect(Collectors.toList());
        
        return String.format("{\"labels\": %s, \"avg\": %s, \"p95\": %s, \"errors\": %s}",
                gson.toJson(labels), gson.toJson(avgData), gson.toJson(p95Data), gson.toJson(errorRates));
    }
    
    private String generateTimeSeriesChartData(TimeSeriesData timeSeriesData) {
        if (timeSeriesData == null || timeSeriesData.getTimestamps().isEmpty()) {
            return "{\"timestamps\": [], \"responseTimesByTransaction\": {}, \"activeThreads\": [], \"hitsPerSecond\": [], \"tpsPerTransaction\": {}}";
        }
        
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        List<String> timeLabels = timeSeriesData.getTimestamps().stream()
            .map(ts -> sdf.format(new Date(ts)))
            .collect(Collectors.toList());
        
        return String.format("{\"timestamps\": %s, \"responseTimesByTransaction\": %s, \"activeThreads\": %s, \"hitsPerSecond\": %s, \"tpsPerTransaction\": %s}",
                gson.toJson(timeLabels),
                gson.toJson(timeSeriesData.getResponseTimesByTransaction()),
                gson.toJson(timeSeriesData.getActiveThreadsOverTime()),
                gson.toJson(timeSeriesData.getHitsPerSecond()),
                gson.toJson(timeSeriesData.getTransactionsPerSecondByTransaction()));
    }

    private String loadTemplate(String templateName) {
        try (InputStream is = getClass().getResourceAsStream("/templates/" + templateName);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.joining("\n"));
        } catch (Exception e) {
            throw new RuntimeException("Failed to load template: " + templateName, e);
        }
    }

    private String getHTMLTemplate() {
        return loadTemplate("comparison_report.html");
    }

    private String getSingleReportHTMLTemplate() {
        return loadTemplate("single_report.html");
    }
}
