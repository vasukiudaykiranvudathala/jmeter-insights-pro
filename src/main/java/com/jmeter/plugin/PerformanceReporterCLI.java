package com.jmeter.plugin;

import com.jmeter.plugin.ai.AIReportGenerator;
import com.jmeter.plugin.comparator.PerformanceComparator;
import com.jmeter.plugin.export.PDFExporter;
import com.jmeter.plugin.model.ComparisonResult;
import com.jmeter.plugin.model.PerformanceMetrics;
import com.jmeter.plugin.model.TimeSeriesData;
import com.jmeter.plugin.parser.JTLParser;
import com.jmeter.plugin.report.HTMLReportGenerator;

import java.io.File;
import java.util.List;
import java.util.Map;

public class PerformanceReporterCLI {
    
    public static void main(String[] args) {
        // Suppress verbose OpenHTMLToPDF logs
        System.setProperty("org.slf4j.simpleLogger.log.com.openhtmltopdf", "error");
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "error");
        System.setProperty("org.slf4j.simpleLogger.showDateTime", "false");
        System.setProperty("org.slf4j.simpleLogger.showThreadName", "false");
        System.setProperty("org.slf4j.simpleLogger.showLogName", "false");
        
        if (args.length < 1) {
            printUsage();
            System.exit(1);
        }

        try {
            String currentFile = args[0];
            
            // Auto-detect if second parameter is a baseline file or output directory
            String baselineFile = null;
            int paramOffset = 0;
            
            if (args.length > 1 && !args[1].isEmpty()) {
                File potentialBaseline = new File(args[1]);
                // Check if it's a valid JTL file (exists and ends with .jtl or .csv)
                if (potentialBaseline.exists() && 
                    (args[1].toLowerCase().endsWith(".jtl") || args[1].toLowerCase().endsWith(".csv"))) {
                    baselineFile = args[1];
                    paramOffset = 1; // Shift other parameters by 1
                }
            }
            
            String outputDir = args.length > (1 + paramOffset) ? args[1 + paramOffset] : System.getProperty("user.dir");
            double threshold = args.length > (2 + paramOffset) ? Double.parseDouble(args[2 + paramOffset]) : 5.0;
            String aiApiKey = args.length > (3 + paramOffset) ? args[3 + paramOffset] : 
                System.getenv("GEMINI_API_KEY") != null ? System.getenv("GEMINI_API_KEY") :
                System.getenv("ANTHROPIC_API_KEY") != null ? System.getenv("ANTHROPIC_API_KEY") : 
                System.getenv("OPENAI_API_KEY");
            boolean exportPdf = args.length > (4 + paramOffset) ? Boolean.parseBoolean(args[4 + paramOffset]) : false;
            String aiProvider = args.length > (5 + paramOffset) ? args[5 + paramOffset] : 
                System.getenv("GEMINI_API_KEY") != null ? "gemini" :
                System.getenv("ANTHROPIC_API_KEY") != null ? "claude" : "openai";

            File outputDirectory = new File(outputDir);
            if (!outputDirectory.exists()) {
                outputDirectory.mkdirs();
            }

            JTLParser parser = new JTLParser();
            Map<String, PerformanceMetrics> currentMetrics = parser.parseJTLFile(currentFile);
            
            String htmlOutputPath = new File(outputDir, "performance_report.html").getAbsolutePath();
            HTMLReportGenerator htmlGenerator = new HTMLReportGenerator();
            AIReportGenerator aiGenerator = new AIReportGenerator(aiApiKey, null, aiProvider);

            if (baselineFile != null && new File(baselineFile).exists()) {
                System.out.println("Generating comparison report...");
                Map<String, PerformanceMetrics> baselineMetrics = parser.parseJTLFile(baselineFile);
                
                PerformanceComparator comparator = new PerformanceComparator(threshold);
                List<ComparisonResult> comparisonResults = comparator.compareResults(baselineMetrics, currentMetrics);
                
                String aiSummary = aiGenerator.generateSummary(baselineMetrics, currentMetrics, comparisonResults);
                
                htmlGenerator.generateComparisonReport(
                        htmlOutputPath, baselineMetrics, currentMetrics, 
                        comparisonResults, aiSummary, threshold);
                
                int degradedCount = comparator.countDegradedTransactions(comparisonResults);
                System.out.println("Comparison Report Generated:");
                System.out.println("  Total Transactions: " + comparisonResults.size());
                System.out.println("  Degraded Transactions: " + degradedCount);
                System.out.println("  HTML Report: " + htmlOutputPath);
                
                if (degradedCount > 0) {
                    System.out.println("\nDegraded Transactions:");
                    comparisonResults.stream()
                            .filter(ComparisonResult::isDegraded)
                            .forEach(r -> System.out.printf("  - %s: %.2fms -> %.2fms (%+.2f%%)\n",
                                    r.getLabel(), r.getBaselineAvg(), r.getCurrentAvg(), r.getPercentageChange()));
                }
            } else {
                System.out.println("Generating single report...");
                String aiSummary = aiGenerator.generateSingleReportSummary(currentMetrics);
                TimeSeriesData timeSeriesData = parser.parseTimeSeriesData(currentFile);
                // For single reports, threshold is used as error rate threshold
                htmlGenerator.generateSingleReport(htmlOutputPath, currentMetrics, aiSummary, timeSeriesData, threshold);
                
                System.out.println("Performance Report Generated:");
                System.out.println("  Total Transactions: " + currentMetrics.size());
                System.out.println("  HTML Report: " + htmlOutputPath);
            }

            if (exportPdf) {
                String pdfOutputPath = new File(outputDir, "performance_report.pdf").getAbsolutePath();
                PDFExporter pdfExporter = new PDFExporter();
                pdfExporter.exportHTMLToPDF(htmlOutputPath, pdfOutputPath);
                System.out.println("  PDF Report: " + pdfOutputPath);
            }

            System.out.println("\nReport generation completed successfully!");

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void printUsage() {
        System.out.println("JMeter Insights Pro - CLI");
        System.out.println("\nUsage:");
        System.out.println("  Single Report:     java -jar jmeter-insights-pro.jar <current.jtl> [output_dir] [threshold] [api_key] [export_pdf] [ai_provider]");
        System.out.println("  Comparison Report: java -jar jmeter-insights-pro.jar <current.jtl> <baseline.jtl> [output_dir] [threshold] [api_key] [export_pdf] [ai_provider]");
        System.out.println("\nParameters:");
        System.out.println("  current.jtl  - Current test results file (required)");
        System.out.println("  baseline.jtl - Baseline test results file (optional, auto-detected if exists and ends with .jtl/.csv)");
        System.out.println("  output_dir   - Output directory (optional, default: current directory)");
        System.out.println("  threshold    - Performance degradation threshold % (optional, default: 5.0)");
        System.out.println("  api_key      - AI API key or Ollama model name (optional, uses env vars)");
        System.out.println("  export_pdf   - Export to PDF: true/false (optional, default: false)");
        System.out.println("  ai_provider  - AI provider: openai/claude/gemini/ollama (optional, auto-detected)");
        System.out.println("\nEnvironment Variables:");
        System.out.println("  GEMINI_API_KEY    - Google Gemini API key");
        System.out.println("  ANTHROPIC_API_KEY - Claude API key");
        System.out.println("  OPENAI_API_KEY    - OpenAI API key");
        System.out.println("\nExamples:");
        System.out.println("  # Single report with defaults");
        System.out.println("  java -jar jmeter-insights-pro.jar results.jtl");
        System.out.println("\n  # Single report with Ollama AI");
        System.out.println("  java -jar jmeter-insights-pro.jar results.jtl ./reports 10.0 llama3.2 false ollama");
        System.out.println("\n  # Comparison report");
        System.out.println("  java -jar jmeter-insights-pro.jar current.jtl baseline.jtl ./reports 5.0");
        System.out.println("\n  # Comparison with Claude AI and PDF export");
        System.out.println("  java -jar jmeter-insights-pro.jar current.jtl baseline.jtl ./reports 5.0 sk-xxx true claude");
    }
}
