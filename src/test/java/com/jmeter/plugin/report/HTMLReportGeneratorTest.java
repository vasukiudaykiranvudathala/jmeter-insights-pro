package com.jmeter.plugin.report;

import com.jmeter.plugin.comparator.PerformanceComparator;
import com.jmeter.plugin.model.ComparisonResult;
import com.jmeter.plugin.model.PerformanceMetrics;
import com.jmeter.plugin.model.TimeSeriesData;
import org.junit.Before;
import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import static org.junit.Assert.*;

public class HTMLReportGeneratorTest {
    
    private HTMLReportGenerator generator;
    private Map<String, PerformanceMetrics> metrics;
    private TimeSeriesData timeSeriesData;
    
    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();
    
    @Before
    public void setUp() {
        generator = new HTMLReportGenerator();
        metrics = new HashMap<>();
        timeSeriesData = new TimeSeriesData();
        
        PerformanceMetrics metric1 = new PerformanceMetrics("Transaction1");
        metric1.addResponseTime(100, true);
        metric1.addResponseTime(200, true);
        metric1.addResponseTime(150, false);
        metric1.calculateStatistics();
        metrics.put("Transaction1", metric1);
        
        PerformanceMetrics metric2 = new PerformanceMetrics("Transaction2");
        metric2.addResponseTime(50, true);
        metric2.addResponseTime(60, true);
        metric2.calculateStatistics();
        metrics.put("Transaction2", metric2);
    }
    
    @Test
    public void testGenerateSingleReportWithErrorThreshold() throws IOException {
        File outputFile = tempFolder.newFile("report.html");
        double errorThreshold = 5.0;
        
        generator.generateSingleReport(
            outputFile.getAbsolutePath(),
            metrics,
            "AI Summary",
            timeSeriesData,
            errorThreshold
        );
        
        assertTrue("Report file should exist", outputFile.exists());
        String content = new String(Files.readAllBytes(outputFile.toPath()));
        assertTrue("Report should contain error threshold", content.contains("5.0"));
        assertTrue("Report should contain transaction names", content.contains("Transaction1"));
    }
    
    @Test
    public void testGenerateSingleReportWithLowErrorThreshold() throws IOException {
        File outputFile = tempFolder.newFile("report_low_threshold.html");
        double errorThreshold = 1.0;
        
        generator.generateSingleReport(
            outputFile.getAbsolutePath(),
            metrics,
            "AI Summary",
            timeSeriesData,
            errorThreshold
        );
        
        String content = new String(Files.readAllBytes(outputFile.toPath()));
        assertTrue("Report should show threshold value", content.contains("1.0"));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testGenerateSingleReportWithEmptyMetrics() throws IOException {
        File outputFile = tempFolder.newFile("empty_report.html");
        Map<String, PerformanceMetrics> emptyMetrics = new HashMap<>();
        
        generator.generateSingleReport(
            outputFile.getAbsolutePath(),
            emptyMetrics,
            "AI Summary",
            timeSeriesData,
            5.0
        );
    }
    
    @Test
    public void testSuccessMessageWhenNoHighErrorRates() throws IOException {
        Map<String, PerformanceMetrics> lowErrorMetrics = new HashMap<>();
        
        PerformanceMetrics metric = new PerformanceMetrics("LowErrorTransaction");
        metric.addResponseTime(100, true);
        metric.addResponseTime(200, true);
        metric.addResponseTime(150, true);
        metric.calculateStatistics();
        lowErrorMetrics.put("LowErrorTransaction", metric);
        
        File outputFile = tempFolder.newFile("success_report.html");
        double errorThreshold = 5.0;
        
        generator.generateSingleReport(
            outputFile.getAbsolutePath(),
            lowErrorMetrics,
            "AI Summary",
            timeSeriesData,
            errorThreshold
        );
        
        String content = new String(Files.readAllBytes(outputFile.toPath()));
        assertTrue("Report should contain success message", 
            content.contains("No transactions exceed") || content.contains("within acceptable limits"));
    }
    
    @Test
    public void testErrorRateThresholdHighlighting() throws IOException {
        Map<String, PerformanceMetrics> mixedMetrics = new HashMap<>();
        
        PerformanceMetrics lowError = new PerformanceMetrics("LowError");
        lowError.addResponseTime(100, true);
        lowError.addResponseTime(200, true);
        lowError.calculateStatistics();
        mixedMetrics.put("LowError", lowError);
        
        PerformanceMetrics mediumError = new PerformanceMetrics("MediumError");
        mediumError.addResponseTime(100, true);
        mediumError.addResponseTime(200, false);
        mediumError.addResponseTime(150, false);
        mediumError.calculateStatistics();
        mixedMetrics.put("MediumError", mediumError);
        
        PerformanceMetrics highError = new PerformanceMetrics("HighError");
        highError.addResponseTime(100, false);
        highError.addResponseTime(200, false);
        highError.addResponseTime(150, false);
        highError.calculateStatistics();
        mixedMetrics.put("HighError", highError);
        
        File outputFile = tempFolder.newFile("highlighting_report.html");
        double errorThreshold = 30.0;
        
        generator.generateSingleReport(
            outputFile.getAbsolutePath(),
            mixedMetrics,
            "AI Summary",
            timeSeriesData,
            errorThreshold
        );
        
        String content = new String(Files.readAllBytes(outputFile.toPath()));
        assertTrue("Report should contain warning or critical classes", 
            content.contains("warning") || content.contains("critical"));
    }
    
    @Test
    public void testGenerateComparisonReport() throws IOException {
        Map<String, PerformanceMetrics> baselineMetrics = new HashMap<>();
        Map<String, PerformanceMetrics> currentMetrics = new HashMap<>();
        
        PerformanceMetrics baseline = new PerformanceMetrics("Transaction1");
        baseline.addResponseTime(100, true);
        baseline.addResponseTime(200, true);
        baseline.calculateStatistics();
        baselineMetrics.put("Transaction1", baseline);
        
        PerformanceMetrics current = new PerformanceMetrics("Transaction1");
        current.addResponseTime(150, true);
        current.addResponseTime(250, true);
        current.calculateStatistics();
        currentMetrics.put("Transaction1", current);
        
        PerformanceComparator comparator = new PerformanceComparator(5.0);
        List<ComparisonResult> comparisonResults = comparator.compareResults(baselineMetrics, currentMetrics);
        
        File outputFile = tempFolder.newFile("comparison_report.html");
        
        generator.generateComparisonReport(
            outputFile.getAbsolutePath(),
            baselineMetrics,
            currentMetrics,
            comparisonResults,
            "AI Summary",
            5.0
        );
        
        assertTrue("Comparison report should exist", outputFile.exists());
        String content = new String(Files.readAllBytes(outputFile.toPath()));
        assertTrue("Report should contain baseline and current data", 
            content.contains("Baseline") && content.contains("Current"));
    }
    
    @Test(expected = NoSuchElementException.class)
    public void testGenerateComparisonReportWithEmptyBaseline() throws IOException {
        Map<String, PerformanceMetrics> emptyBaseline = new HashMap<>();
        Map<String, PerformanceMetrics> currentMetrics = new HashMap<>();
        
        PerformanceMetrics current = new PerformanceMetrics("Transaction1");
        current.addResponseTime(100, true);
        current.calculateStatistics();
        currentMetrics.put("Transaction1", current);
        
        PerformanceComparator comparator = new PerformanceComparator(5.0);
        List<ComparisonResult> comparisonResults = comparator.compareResults(emptyBaseline, currentMetrics);
        
        File outputFile = tempFolder.newFile("invalid_comparison.html");
        
        generator.generateComparisonReport(
            outputFile.getAbsolutePath(),
            emptyBaseline,
            currentMetrics,
            comparisonResults,
            "AI Summary",
            5.0
        );
    }
    
    @Test(expected = NoSuchElementException.class)
    public void testGenerateComparisonReportWithEmptyCurrent() throws IOException {
        Map<String, PerformanceMetrics> baselineMetrics = new HashMap<>();
        Map<String, PerformanceMetrics> emptyCurrent = new HashMap<>();
        
        PerformanceMetrics baseline = new PerformanceMetrics("Transaction1");
        baseline.addResponseTime(100, true);
        baseline.calculateStatistics();
        baselineMetrics.put("Transaction1", baseline);
        
        PerformanceComparator comparator = new PerformanceComparator(5.0);
        List<ComparisonResult> comparisonResults = comparator.compareResults(baselineMetrics, emptyCurrent);
        
        File outputFile = tempFolder.newFile("invalid_comparison2.html");
        
        generator.generateComparisonReport(
            outputFile.getAbsolutePath(),
            baselineMetrics,
            emptyCurrent,
            comparisonResults,
            "AI Summary",
            5.0
        );
    }
}
