package com.jmeter.plugin.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PerformanceMetrics {
    private String label;
    private List<Long> responseTimes;
    private int successCount;
    private int failureCount;
    private long minResponseTime;
    private long maxResponseTime;
    private double averageResponseTime;
    private double percentile90;
    private double percentile95;
    private double percentile99;
    private double throughput;
    private double errorRate;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    public PerformanceMetrics(String label) {
        this.label = label;
        this.responseTimes = new ArrayList<>();
        this.successCount = 0;
        this.failureCount = 0;
        this.minResponseTime = Long.MAX_VALUE;
        this.maxResponseTime = Long.MIN_VALUE;
    }

    public void addResponseTime(long responseTime, boolean success) {
        responseTimes.add(responseTime);
        if (success) {
            successCount++;
        } else {
            failureCount++;
        }
        minResponseTime = Math.min(minResponseTime, responseTime);
        maxResponseTime = Math.max(maxResponseTime, responseTime);
    }

    public void calculateStatistics() {
        if (responseTimes.isEmpty()) {
            return;
        }

        List<Long> sortedTimes = new ArrayList<>(responseTimes);
        sortedTimes.sort(Long::compareTo);

        averageResponseTime = responseTimes.stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0.0);

        percentile90 = calculatePercentile(sortedTimes, 90);
        percentile95 = calculatePercentile(sortedTimes, 95);
        percentile99 = calculatePercentile(sortedTimes, 99);

        int totalCount = successCount + failureCount;
        errorRate = totalCount > 0 ? (failureCount * 100.0 / totalCount) : 0.0;

        // Calculate throughput like JMeter's Aggregate Report:
        // Throughput = samples / (sum of elapsed times in seconds)
        // This matches JMeter's calculation exactly
        double totalElapsedSeconds = responseTimes.stream()
                .mapToLong(Long::longValue)
                .sum() / 1000.0;
        throughput = totalElapsedSeconds > 0 ? (totalCount / totalElapsedSeconds) : 0.0;
    }

    private double calculatePercentile(List<Long> sortedValues, int percentile) {
        if (sortedValues.isEmpty()) {
            return 0.0;
        }
        int index = (int) Math.ceil(percentile / 100.0 * sortedValues.size()) - 1;
        index = Math.max(0, Math.min(index, sortedValues.size() - 1));
        return sortedValues.get(index);
    }
    
    public void setFromAggregateData(int samples, double average, double median, 
                                     double p90, double p95, double p99,
                                     double min, double max, double errorPercent, 
                                     double throughput) {
        this.successCount = (int) (samples * (100.0 - errorPercent) / 100.0);
        this.failureCount = samples - this.successCount;
        this.averageResponseTime = average;
        this.percentile90 = p90;
        this.percentile95 = p95;
        this.percentile99 = p99;
        this.minResponseTime = (long) min;
        this.maxResponseTime = (long) max;
        this.errorRate = errorPercent;
        this.throughput = throughput;
    }

    public String getLabel() {
        return label;
    }

    public double getAverageResponseTime() {
        return averageResponseTime;
    }

    public double getPercentile90() {
        return percentile90;
    }

    public double getPercentile95() {
        return percentile95;
    }

    public double getPercentile99() {
        return percentile99;
    }

    public long getMinResponseTime() {
        return minResponseTime == Long.MAX_VALUE ? 0 : minResponseTime;
    }

    public long getMaxResponseTime() {
        return maxResponseTime == Long.MIN_VALUE ? 0 : maxResponseTime;
    }

    public int getSuccessCount() {
        return successCount;
    }

    public int getFailureCount() {
        return failureCount;
    }

    public int getTotalCount() {
        return successCount + failureCount;
    }

    public double getErrorRate() {
        return errorRate;
    }

    public double getThroughput() {
        return throughput;
    }
    
    /**
     * Formats throughput intelligently like JMeter's Aggregate Report:
     * - If >= 1.0 req/s: show as "X.XX/sec"
     * - If < 1.0 req/s but >= 0.01 req/s (0.6/min): show as "X.X/min"
     * - If < 0.01 req/s: show as "X.X/hour"
     * This prevents showing 0.00/sec for low throughput values.
     */
    public String getFormattedThroughput() {
        if (throughput >= 1.0) {
            return String.format("%.2f/sec", throughput);
        } else if (throughput >= 0.01) { // 0.6 per minute = 0.01 per second
            return String.format("%.1f/min", throughput * 60);
        } else if (throughput > 0) {
            return String.format("%.1f/hour", throughput * 3600);
        } else {
            return "0.0/sec";
        }
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }
}
