package com.jmeter.plugin.listener;

import com.jmeter.plugin.ai.AIReportGenerator;
import com.jmeter.plugin.comparator.PerformanceComparator;
import com.jmeter.plugin.export.PDFExporter;
import com.jmeter.plugin.model.ComparisonResult;
import com.jmeter.plugin.model.PerformanceMetrics;
import com.jmeter.plugin.model.TimeSeriesData;
import com.jmeter.plugin.parser.JTLParser;
import com.jmeter.plugin.report.HTMLReportGenerator;
import org.apache.jmeter.reporters.AbstractListenerElement;
import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.samplers.SampleListener;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.TestStateListener;
import org.apache.jmeter.visualizers.gui.AbstractListenerGui;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PerformanceReporterListener extends AbstractListenerElement 
        implements SampleListener, TestStateListener, Serializable {
    
    private static final Logger log = LoggerFactory.getLogger(PerformanceReporterListener.class);
    private static final long serialVersionUID = 1L;
    
    private static final String CURRENT_JTL_FILE = "PerformanceReporter.currentJtlFile";
    private static final String BASELINE_FILE = "PerformanceReporter.baselineFile";
    private static final String OUTPUT_DIR = "PerformanceReporter.outputDir";
    private static final String THRESHOLD = "PerformanceReporter.threshold";
    private static final String AI_API_KEY = "PerformanceReporter.aiApiKey";
    private static final String AI_ENDPOINT = "PerformanceReporter.aiEndpoint";
    private static final String AI_PROVIDER = "PerformanceReporter.aiProvider";
    private static final String EXPORT_PDF = "PerformanceReporter.exportPdf";
    
    private transient Map<String, PerformanceMetrics> currentMetrics;
    private transient long testStartTime;
    private transient long testEndTime;

    public PerformanceReporterListener() {
        super();
        currentMetrics = new HashMap<>();
    }

    @Override
    public void sampleOccurred(SampleEvent event) {
        SampleResult result = event.getResult();
        String label = result.getSampleLabel();
        long responseTime = result.getTime();
        boolean success = result.isSuccessful();
        
        synchronized (currentMetrics) {
            PerformanceMetrics metrics = currentMetrics.computeIfAbsent(label, PerformanceMetrics::new);
            metrics.addResponseTime(responseTime, success);
        }
    }

    @Override
    public void sampleStarted(SampleEvent event) {
    }

    @Override
    public void sampleStopped(SampleEvent event) {
    }

    @Override
    public void testStarted() {
        testStarted("");
    }

    @Override
    public void testStarted(String host) {
        currentMetrics = new HashMap<>();
        testStartTime = System.currentTimeMillis();
    }

    @Override
    public void testEnded() {
        testEnded("");
    }

    @Override
    public void testEnded(String host) {
        testEndTime = System.currentTimeMillis();
        
        try {
            LocalDateTime startTime = LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(testStartTime), ZoneId.systemDefault());
            LocalDateTime endTime = LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(testEndTime), ZoneId.systemDefault());
            
            for (PerformanceMetrics metrics : currentMetrics.values()) {
                metrics.setStartTime(startTime);
                metrics.setEndTime(endTime);
                metrics.calculateStatistics();
            }
            
            generateReport();
        } catch (Exception e) {
            log.error("Error generating performance report", e);
        }
    }

    private void generateReport() {
        try {
            // Check if any metrics were collected
            if (currentMetrics.isEmpty()) {
                log.warn("No performance metrics collected. Please ensure your test plan has samplers and they executed successfully.");
                return;
            }
            
            String outputDir = getOutputDir();
            File dir = new File(outputDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            
            String htmlOutputPath = new File(outputDir, "performance_report.html").getAbsolutePath();
            String baselineFile = getBaselineFile();
            double threshold = getThreshold();
            
            HTMLReportGenerator htmlGenerator = new HTMLReportGenerator();
            AIReportGenerator aiGenerator = new AIReportGenerator(getAiApiKey(), getAiEndpoint(), getAiProvider());
            
            if (baselineFile != null && !baselineFile.isEmpty() && new File(baselineFile).exists()) {
                JTLParser parser = new JTLParser();
                Map<String, PerformanceMetrics> baselineMetrics = parser.parseJTLFile(baselineFile);
                
                PerformanceComparator comparator = new PerformanceComparator(threshold);
                List<ComparisonResult> comparisonResults = comparator.compareResults(baselineMetrics, currentMetrics);
                
                String aiSummary = aiGenerator.generateSummary(baselineMetrics, currentMetrics, comparisonResults);
                
                htmlGenerator.generateComparisonReport(
                        htmlOutputPath, baselineMetrics, currentMetrics, 
                        comparisonResults, aiSummary, threshold);
                
                // Comparison report generated
            } else {
                String aiSummary = aiGenerator.generateSingleReportSummary(currentMetrics);
                // Listener collects metrics in real-time, not from JTL file
                // Time-series data not available in this mode
                TimeSeriesData timeSeriesData = new TimeSeriesData();
                // For single reports, threshold is used as error rate threshold
                htmlGenerator.generateSingleReport(htmlOutputPath, currentMetrics, aiSummary, timeSeriesData, threshold);
            }
            
            if (isExportPdf()) {
                String pdfOutputPath = new File(outputDir, "performance_report.pdf").getAbsolutePath();
                PDFExporter pdfExporter = new PDFExporter();
                pdfExporter.exportHTMLToPDF(htmlOutputPath, pdfOutputPath);
            }
            
        } catch (Exception e) {
            log.error("Error generating report", e);
        }
    }

    public String getCurrentJtlFile() {
        return getPropertyAsString(CURRENT_JTL_FILE, "");
    }

    public void setCurrentJtlFile(String currentJtlFile) {
        setProperty(CURRENT_JTL_FILE, currentJtlFile);
    }

    public String getBaselineFile() {
        return getPropertyAsString(BASELINE_FILE, "");
    }

    public void setBaselineFile(String baselineFile) {
        setProperty(BASELINE_FILE, baselineFile);
    }

    public String getOutputDir() {
        return getPropertyAsString(OUTPUT_DIR, System.getProperty("user.dir"));
    }

    public void setOutputDir(String outputDir) {
        setProperty(OUTPUT_DIR, outputDir);
    }

    public double getThreshold() {
        String value = getPropertyAsString(THRESHOLD, "5.0");
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return 5.0;
        }
    }

    public void setThreshold(double threshold) {
        setProperty(THRESHOLD, String.valueOf(threshold));
    }

    public String getAiApiKey() {
        String apiKey = getPropertyAsString(AI_API_KEY, "");
        // For Ollama, use default model if API Key is empty
        if (apiKey.isEmpty() && "ollama".equalsIgnoreCase(getAiProvider())) {
            return "llama3.2";
        }
        return apiKey;
    }

    public void setAiApiKey(String apiKey) {
        setProperty(AI_API_KEY, apiKey);
    }

    public String getAiEndpoint() {
        return getPropertyAsString(AI_ENDPOINT, "https://api.openai.com/v1/chat/completions");
    }

    public void setAiEndpoint(String endpoint) {
        setProperty(AI_ENDPOINT, endpoint);
    }

    public String getAiProvider() {
        return getPropertyAsString(AI_PROVIDER, "openai");
    }

    public void setAiProvider(String provider) {
        setProperty(AI_PROVIDER, provider);
    }

    public boolean isExportPdf() {
        return getPropertyAsBoolean(EXPORT_PDF, false);
    }

    public void setExportPdf(boolean exportPdf) {
        setProperty(EXPORT_PDF, exportPdf);
    }

    public static class PerformanceReporterGui extends AbstractListenerGui {
        private static final long serialVersionUID = 1L;
        
        private JTextField baselineFileField;
        private JTextField outputDirField;
        private JTextField thresholdField;
        private JTextField aiApiKeyField;
        private JTextField aiEndpointField;
        private JCheckBox exportPdfCheckbox;

        public PerformanceReporterGui() {
            super();
            init();
        }

        @Override
        public String getStaticLabel() {
            return "JMeter Insights Pro";
        }

        @Override
        public String getLabelResource() {
            return null;
        }

        @Override
        public void configure(org.apache.jmeter.testelement.TestElement element) {
            super.configure(element);
            if (element instanceof PerformanceReporterListener) {
                PerformanceReporterListener listener = (PerformanceReporterListener) element;
                baselineFileField.setText(listener.getBaselineFile());
                outputDirField.setText(listener.getOutputDir());
                thresholdField.setText(String.valueOf(listener.getThreshold()));
                aiApiKeyField.setText(listener.getAiApiKey());
                aiEndpointField.setText(listener.getAiEndpoint());
                exportPdfCheckbox.setSelected(listener.isExportPdf());
            }
        }

        @Override
        public org.apache.jmeter.testelement.TestElement createTestElement() {
            PerformanceReporterListener listener = new PerformanceReporterListener();
            modifyTestElement(listener);
            return listener;
        }

        @Override
        public void modifyTestElement(org.apache.jmeter.testelement.TestElement element) {
            super.configureTestElement(element);
            if (element instanceof PerformanceReporterListener) {
                PerformanceReporterListener listener = (PerformanceReporterListener) element;
                listener.setBaselineFile(baselineFileField.getText());
                listener.setOutputDir(outputDirField.getText());
                try {
                    listener.setThreshold(Double.parseDouble(thresholdField.getText()));
                } catch (NumberFormatException e) {
                    listener.setThreshold(5.0);
                }
                listener.setAiApiKey(aiApiKeyField.getText());
                listener.setAiEndpoint(aiEndpointField.getText());
                listener.setExportPdf(exportPdfCheckbox.isSelected());
            }
        }

        @Override
        public void clearGui() {
            super.clearGui();
            baselineFileField.setText("");
            outputDirField.setText(System.getProperty("user.dir"));
            thresholdField.setText("5.0");
            aiApiKeyField.setText("");
            aiEndpointField.setText("https://api.openai.com/v1/chat/completions");
            exportPdfCheckbox.setSelected(false);
        }

        private void init() {
            setLayout(new BorderLayout());
            setBorder(makeBorder());
            add(makeTitlePanel(), BorderLayout.NORTH);
            
            JPanel mainPanel = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.insets = new Insets(5, 5, 5, 5);
            
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.weightx = 0.3;
            mainPanel.add(new JLabel("Baseline JTL File (optional):"), gbc);
            
            gbc.gridx = 1;
            gbc.weightx = 0.7;
            baselineFileField = new JTextField(30);
            mainPanel.add(baselineFileField, gbc);
            
            gbc.gridx = 0;
            gbc.gridy = 1;
            gbc.weightx = 0.3;
            mainPanel.add(new JLabel("Output Directory:"), gbc);
            
            gbc.gridx = 1;
            gbc.weightx = 0.7;
            outputDirField = new JTextField(System.getProperty("user.dir"), 30);
            mainPanel.add(outputDirField, gbc);
            
            gbc.gridx = 0;
            gbc.gridy = 2;
            gbc.weightx = 0.3;
            mainPanel.add(new JLabel("Threshold (%):"), gbc);
            
            gbc.gridx = 1;
            gbc.weightx = 0.7;
            thresholdField = new JTextField("5.0", 10);
            mainPanel.add(thresholdField, gbc);
            
            gbc.gridx = 0;
            gbc.gridy = 3;
            gbc.weightx = 0.3;
            mainPanel.add(new JLabel("AI API Key (optional):"), gbc);
            
            gbc.gridx = 1;
            gbc.weightx = 0.7;
            aiApiKeyField = new JTextField(30);
            mainPanel.add(aiApiKeyField, gbc);
            
            gbc.gridx = 0;
            gbc.gridy = 4;
            gbc.weightx = 0.3;
            mainPanel.add(new JLabel("AI Endpoint:"), gbc);
            
            gbc.gridx = 1;
            gbc.weightx = 0.7;
            aiEndpointField = new JTextField("https://api.openai.com/v1/chat/completions", 30);
            mainPanel.add(aiEndpointField, gbc);
            
            gbc.gridx = 0;
            gbc.gridy = 5;
            gbc.gridwidth = 2;
            exportPdfCheckbox = new JCheckBox("Export to PDF");
            mainPanel.add(exportPdfCheckbox, gbc);
            
            add(mainPanel, BorderLayout.CENTER);
        }
    }
}
