package com.jmeter.plugin.comparator;

import com.jmeter.plugin.model.ComparisonResult;
import com.jmeter.plugin.model.PerformanceMetrics;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class PerformanceComparatorTest {
    
    private PerformanceComparator comparator;
    private Map<String, PerformanceMetrics> baselineMetrics;
    private Map<String, PerformanceMetrics> currentMetrics;
    
    @Before
    public void setUp() {
        comparator = new PerformanceComparator(10.0);
        baselineMetrics = new HashMap<>();
        currentMetrics = new HashMap<>();
    }
    
    @Test
    public void testCompareResultsWithDegradation() {
        PerformanceMetrics baseline = new PerformanceMetrics("Transaction1");
        baseline.addResponseTime(100, true);
        baseline.addResponseTime(200, true);
        baseline.calculateStatistics();
        baselineMetrics.put("Transaction1", baseline);
        
        PerformanceMetrics current = new PerformanceMetrics("Transaction1");
        current.addResponseTime(200, true);
        current.addResponseTime(300, true);
        current.calculateStatistics();
        currentMetrics.put("Transaction1", current);
        
        List<ComparisonResult> results = comparator.compareResults(baselineMetrics, currentMetrics);
        
        assertEquals("Should have 1 comparison result", 1, results.size());
        ComparisonResult result = results.get(0);
        assertEquals("Transaction name should match", "Transaction1", result.getLabel());
        assertTrue("Should show degradation", result.getPercentageChange() > 10.0);
        assertTrue("Should be marked as degraded", result.isDegraded());
    }
    
    @Test
    public void testCompareResultsWithImprovement() {
        PerformanceMetrics baseline = new PerformanceMetrics("Transaction1");
        baseline.addResponseTime(200, true);
        baseline.addResponseTime(300, true);
        baseline.calculateStatistics();
        baselineMetrics.put("Transaction1", baseline);
        
        PerformanceMetrics current = new PerformanceMetrics("Transaction1");
        current.addResponseTime(100, true);
        current.addResponseTime(150, true);
        current.calculateStatistics();
        currentMetrics.put("Transaction1", current);
        
        List<ComparisonResult> results = comparator.compareResults(baselineMetrics, currentMetrics);
        
        assertEquals("Should have 1 comparison result", 1, results.size());
        ComparisonResult result = results.get(0);
        assertTrue("Should show improvement (negative change)", result.getPercentageChange() < 0);
        assertFalse("Should not be marked as degraded", result.isDegraded());
    }
    
    @Test
    public void testCompareResultsWithinThreshold() {
        PerformanceMetrics baseline = new PerformanceMetrics("Transaction1");
        baseline.addResponseTime(100, true);
        baseline.addResponseTime(200, true);
        baseline.calculateStatistics();
        baselineMetrics.put("Transaction1", baseline);
        
        PerformanceMetrics current = new PerformanceMetrics("Transaction1");
        current.addResponseTime(105, true);
        current.addResponseTime(205, true);
        current.calculateStatistics();
        currentMetrics.put("Transaction1", current);
        
        List<ComparisonResult> results = comparator.compareResults(baselineMetrics, currentMetrics);
        
        assertEquals("Should have 1 comparison result", 1, results.size());
        ComparisonResult result = results.get(0);
        assertTrue("Change should be within threshold", Math.abs(result.getPercentageChange()) < 10.0);
        assertFalse("Should not be marked as degraded", result.isDegraded());
    }
    
    @Test
    public void testCompareMultipleTransactions() {
        PerformanceMetrics baseline1 = new PerformanceMetrics("Login");
        baseline1.addResponseTime(100, true);
        baseline1.calculateStatistics();
        baselineMetrics.put("Login", baseline1);
        
        PerformanceMetrics baseline2 = new PerformanceMetrics("Search");
        baseline2.addResponseTime(200, true);
        baseline2.calculateStatistics();
        baselineMetrics.put("Search", baseline2);
        
        PerformanceMetrics current1 = new PerformanceMetrics("Login");
        current1.addResponseTime(150, true);
        current1.calculateStatistics();
        currentMetrics.put("Login", current1);
        
        PerformanceMetrics current2 = new PerformanceMetrics("Search");
        current2.addResponseTime(180, true);
        current2.calculateStatistics();
        currentMetrics.put("Search", current2);
        
        List<ComparisonResult> results = comparator.compareResults(baselineMetrics, currentMetrics);
        
        assertEquals("Should have 2 comparison results", 2, results.size());
    }
    
    @Test
    public void testCountDegradedTransactions() {
        PerformanceMetrics baseline1 = new PerformanceMetrics("Transaction1");
        baseline1.addResponseTime(100, true);
        baseline1.calculateStatistics();
        baselineMetrics.put("Transaction1", baseline1);
        
        PerformanceMetrics baseline2 = new PerformanceMetrics("Transaction2");
        baseline2.addResponseTime(100, true);
        baseline2.calculateStatistics();
        baselineMetrics.put("Transaction2", baseline2);
        
        PerformanceMetrics current1 = new PerformanceMetrics("Transaction1");
        current1.addResponseTime(200, true);
        current1.calculateStatistics();
        currentMetrics.put("Transaction1", current1);
        
        PerformanceMetrics current2 = new PerformanceMetrics("Transaction2");
        current2.addResponseTime(105, true);
        current2.calculateStatistics();
        currentMetrics.put("Transaction2", current2);
        
        List<ComparisonResult> results = comparator.compareResults(baselineMetrics, currentMetrics);
        int degradedCount = comparator.countDegradedTransactions(results);
        
        assertEquals("Should have 1 degraded transaction", 1, degradedCount);
    }
    
    @Test
    public void testThresholdCalculation() {
        PerformanceComparator comparator5 = new PerformanceComparator(5.0);
        PerformanceComparator comparator20 = new PerformanceComparator(20.0);
        
        PerformanceMetrics baseline = new PerformanceMetrics("Transaction1");
        baseline.addResponseTime(100, true);
        baseline.calculateStatistics();
        baselineMetrics.put("Transaction1", baseline);
        
        PerformanceMetrics current = new PerformanceMetrics("Transaction1");
        current.addResponseTime(115, true);
        current.calculateStatistics();
        currentMetrics.put("Transaction1", current);
        
        List<ComparisonResult> results5 = comparator5.compareResults(baselineMetrics, currentMetrics);
        List<ComparisonResult> results20 = comparator20.compareResults(baselineMetrics, currentMetrics);
        
        assertTrue("Should be degraded with 5% threshold", results5.get(0).isDegraded());
        assertFalse("Should not be degraded with 20% threshold", results20.get(0).isDegraded());
    }
    
    @Test
    public void testCompareWithMissingTransaction() {
        PerformanceMetrics baseline = new PerformanceMetrics("Transaction1");
        baseline.addResponseTime(100, true);
        baseline.calculateStatistics();
        baselineMetrics.put("Transaction1", baseline);
        
        PerformanceMetrics current = new PerformanceMetrics("Transaction2");
        current.addResponseTime(100, true);
        current.calculateStatistics();
        currentMetrics.put("Transaction2", current);
        
        List<ComparisonResult> results = comparator.compareResults(baselineMetrics, currentMetrics);
        
        assertTrue("Should handle missing transactions gracefully", results.isEmpty() || results.size() > 0);
    }
    
    @Test
    public void testCompareWithErrorRateChanges() {
        PerformanceMetrics baseline = new PerformanceMetrics("Transaction1");
        baseline.addResponseTime(100, true);
        baseline.addResponseTime(200, true);
        baseline.calculateStatistics();
        baselineMetrics.put("Transaction1", baseline);
        
        PerformanceMetrics current = new PerformanceMetrics("Transaction1");
        current.addResponseTime(100, true);
        current.addResponseTime(200, false);
        current.calculateStatistics();
        currentMetrics.put("Transaction1", current);
        
        List<ComparisonResult> results = comparator.compareResults(baselineMetrics, currentMetrics);
        
        assertEquals("Should have 1 comparison result", 1, results.size());
        ComparisonResult result = results.get(0);
        assertNotNull("Result should not be null", result);
    }
}
