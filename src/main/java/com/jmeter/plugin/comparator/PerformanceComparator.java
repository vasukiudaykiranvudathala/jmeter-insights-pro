package com.jmeter.plugin.comparator;

import com.jmeter.plugin.model.ComparisonResult;
import com.jmeter.plugin.model.PerformanceMetrics;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PerformanceComparator {
    private final double thresholdPercent;

    public PerformanceComparator(double thresholdPercent) {
        this.thresholdPercent = thresholdPercent;
    }

    public List<ComparisonResult> compareResults(
            Map<String, PerformanceMetrics> baselineMetrics,
            Map<String, PerformanceMetrics> currentMetrics) {
        
        List<ComparisonResult> results = new ArrayList<>();
        
        Set<String> allLabels = Stream.concat(
                baselineMetrics.keySet().stream(),
                currentMetrics.keySet().stream()
        ).collect(Collectors.toSet());
        
        for (String label : allLabels) {
            PerformanceMetrics baseline = baselineMetrics.get(label);
            PerformanceMetrics current = currentMetrics.get(label);
            
            if (baseline != null && current != null) {
                ComparisonResult result = new ComparisonResult(
                        label,
                        baseline.getAverageResponseTime(),
                        current.getAverageResponseTime(),
                        baseline.getPercentile90(),
                        current.getPercentile90(),
                        baseline.getErrorRate(),
                        current.getErrorRate(),
                        baseline.getTotalCount(),
                        current.getTotalCount(),
                        thresholdPercent
                );
                results.add(result);
            }
        }
        
        return results;
    }

    public int countDegradedTransactions(List<ComparisonResult> results) {
        return (int) results.stream().filter(ComparisonResult::isDegraded).count();
    }
}
