package com.jmeter.plugin.model;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

public class PerformanceMetricsTest {
    
    private PerformanceMetrics metrics;
    
    @Before
    public void setUp() {
        metrics = new PerformanceMetrics("TestTransaction");
    }
    
    @Test
    public void testAddResponseTimeAndCalculateAverage() {
        metrics.addResponseTime(100, true);
        metrics.addResponseTime(200, true);
        metrics.addResponseTime(300, true);
        metrics.calculateStatistics();
        
        assertEquals("Total count should be 3", 3, metrics.getTotalCount());
        assertEquals("Average should be 200", 200.0, metrics.getAverageResponseTime(), 0.01);
    }
    
    @Test
    public void testCalculateMinMax() {
        metrics.addResponseTime(150, true);
        metrics.addResponseTime(100, true);
        metrics.addResponseTime(300, true);
        metrics.addResponseTime(200, true);
        metrics.calculateStatistics();
        
        assertEquals("Min should be 100", 100, metrics.getMinResponseTime());
        assertEquals("Max should be 300", 300, metrics.getMaxResponseTime());
    }
    
    @Test
    public void testCalculatePercentiles() {
        for (int i = 1; i <= 100; i++) {
            metrics.addResponseTime(i, true);
        }
        metrics.calculateStatistics();
        
        assertEquals("90th percentile should be ~90", 90.0, metrics.getPercentile90(), 5.0);
        assertEquals("95th percentile should be ~95", 95.0, metrics.getPercentile95(), 5.0);
        assertEquals("99th percentile should be ~99", 99.0, metrics.getPercentile99(), 5.0);
    }
    
    @Test
    public void testErrorRateCalculation() {
        metrics.addResponseTime(100, true);
        metrics.addResponseTime(200, true);
        metrics.addResponseTime(150, false);
        metrics.addResponseTime(180, false);
        metrics.calculateStatistics();
        
        assertEquals("Failure count should be 2", 2, metrics.getFailureCount());
        assertEquals("Error rate should be 50%", 50.0, metrics.getErrorRate(), 0.01);
    }
    
    @Test
    public void testSetFromAggregateData() {
        metrics.setFromAggregateData(100, 150.0, 140.0, 200.0, 220.0, 250.0, 50.0, 300.0, 10.0, 5.5);
        
        assertEquals("Average should be 150", 150.0, metrics.getAverageResponseTime(), 0.01);
        assertEquals("90th percentile should be 200", 200.0, metrics.getPercentile90(), 0.01);
        assertEquals("Error rate should be 10%", 10.0, metrics.getErrorRate(), 0.01);
        assertEquals("Throughput should be 5.5", 5.5, metrics.getThroughput(), 0.01);
    }
    
    @Test
    public void testEmptyMetrics() {
        metrics.calculateStatistics();
        
        assertEquals("Empty metrics should have 0 samples", 0, metrics.getTotalCount());
        assertEquals("Empty metrics should have 0 average", 0.0, metrics.getAverageResponseTime(), 0.01);
        assertEquals("Empty metrics should have 0 error rate", 0.0, metrics.getErrorRate(), 0.01);
    }
    
    @Test
    public void testAllSuccessfulSamples() {
        metrics.addResponseTime(100, true);
        metrics.addResponseTime(200, true);
        metrics.addResponseTime(150, true);
        metrics.calculateStatistics();
        
        assertEquals("Failure count should be 0", 0, metrics.getFailureCount());
        assertEquals("Error rate should be 0%", 0.0, metrics.getErrorRate(), 0.01);
    }
    
    @Test
    public void testAllFailedSamples() {
        metrics.addResponseTime(100, false);
        metrics.addResponseTime(200, false);
        metrics.calculateStatistics();
        
        assertEquals("Failure count should be 2", 2, metrics.getFailureCount());
        assertEquals("Error rate should be 100%", 100.0, metrics.getErrorRate(), 0.01);
    }
    
    @Test
    public void testLabelGetter() {
        assertEquals("Label should be TestTransaction", "TestTransaction", metrics.getLabel());
    }
}
