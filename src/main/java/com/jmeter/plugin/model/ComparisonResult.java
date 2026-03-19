package com.jmeter.plugin.model;

public class ComparisonResult {
    private String label;
    private double baselineAvg;
    private double currentAvg;
    private double baselineP90;
    private double currentP90;
    private double baselineErrorRate;
    private double currentErrorRate;
    private int baselineSampleCount;
    private int currentSampleCount;
    private double difference;
    private double percentageChange;
    private double p90PercentageChange;
    private double errorRateChange;
    private boolean degraded;
    private String status;

    public ComparisonResult(String label, double baselineAvg, double currentAvg, 
                           double baselineP90, double currentP90,
                           double baselineErrorRate, double currentErrorRate,
                           int baselineSampleCount, int currentSampleCount,
                           double thresholdPercent) {
        this.label = label;
        this.baselineAvg = baselineAvg;
        this.currentAvg = currentAvg;
        this.baselineP90 = baselineP90;
        this.currentP90 = currentP90;
        this.baselineErrorRate = baselineErrorRate;
        this.currentErrorRate = currentErrorRate;
        this.baselineSampleCount = baselineSampleCount;
        this.currentSampleCount = currentSampleCount;
        this.difference = currentAvg - baselineAvg;
        this.percentageChange = baselineAvg > 0 ? ((currentAvg - baselineAvg) / baselineAvg) * 100 : 0;
        this.p90PercentageChange = baselineP90 > 0 ? ((currentP90 - baselineP90) / baselineP90) * 100 : 0;
        this.errorRateChange = currentErrorRate - baselineErrorRate;
        this.degraded = percentageChange > thresholdPercent;
        
        if (percentageChange > thresholdPercent) {
            status = "critical";
        } else if (percentageChange > 0) {
            status = "warning";
        } else if (percentageChange < 0) {
            status = "improved";
        } else {
            status = "neutral";
        }
    }

    public String getLabel() {
        return label;
    }

    public double getBaselineAvg() {
        return baselineAvg;
    }

    public double getCurrentAvg() {
        return currentAvg;
    }

    public double getDifference() {
        return difference;
    }

    public double getPercentageChange() {
        return percentageChange;
    }

    public boolean isDegraded() {
        return degraded;
    }

    public String getStatus() {
        return status;
    }

    public String getFormattedPercentageChange() {
        return String.format("%+.2f%%", percentageChange);
    }

    public double getBaselineP90() {
        return baselineP90;
    }

    public double getCurrentP90() {
        return currentP90;
    }

    public double getBaselineErrorRate() {
        return baselineErrorRate;
    }

    public double getCurrentErrorRate() {
        return currentErrorRate;
    }

    public double getP90PercentageChange() {
        return p90PercentageChange;
    }

    public double getErrorRateChange() {
        return errorRateChange;
    }

    public int getBaselineSampleCount() {
        return baselineSampleCount;
    }

    public int getCurrentSampleCount() {
        return currentSampleCount;
    }
}
