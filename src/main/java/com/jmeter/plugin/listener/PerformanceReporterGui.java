package com.jmeter.plugin.listener;

import com.jmeter.plugin.ai.AIReportGenerator;
import com.jmeter.plugin.comparator.PerformanceComparator;
import com.jmeter.plugin.export.PDFExporter;
import com.jmeter.plugin.model.ComparisonResult;
import com.jmeter.plugin.model.PerformanceMetrics;
import com.jmeter.plugin.model.TimeSeriesData;
import com.jmeter.plugin.parser.JTLParser;
import com.jmeter.plugin.report.HTMLReportGenerator;
import org.apache.jmeter.visualizers.gui.AbstractListenerGui;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.List;
import java.util.Map;

public class PerformanceReporterGui extends AbstractListenerGui {
    private static final long serialVersionUID = 1L;
    private static final Logger log = LoggerFactory.getLogger(PerformanceReporterGui.class);
    
    private JTextField currentJtlFileField;
    private JTextField baselineFileField;
    private JTextField outputDirField;
    private JTextField thresholdField;
    private JTextField aiApiKeyField;
    private JTextField aiEndpointField;
    private JComboBox<String> aiProviderCombo;
    private JComboBox<String> aiModelCombo;
    private JCheckBox exportPdfCheckbox;
    private JButton generateReportButton;

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
            currentJtlFileField.setText(listener.getCurrentJtlFile());
            baselineFileField.setText(listener.getBaselineFile());
            outputDirField.setText(listener.getOutputDir());
            thresholdField.setText(String.valueOf(listener.getThreshold()));
            
            // Set provider first to populate model dropdown
            String provider = listener.getAiProvider();
            if (provider != null && !provider.isEmpty()) {
                aiProviderCombo.setSelectedItem(provider);
            }
            
            // Handle API key - don't load if it's a model name (for backward compatibility)
            String apiKey = listener.getAiApiKey();
            if (apiKey != null && !apiKey.isEmpty()) {
                // Check if this looks like a model name rather than an API key
                boolean isModelName = apiKey.startsWith("llama") || apiKey.startsWith("mistral") || 
                                     apiKey.startsWith("phi") || apiKey.startsWith("gemma") || 
                                     apiKey.startsWith("codellama") || apiKey.startsWith("qwen") ||
                                     apiKey.equals("gpt-3.5-turbo") || apiKey.equals("gpt-4") ||
                                     apiKey.startsWith("claude-") || apiKey.startsWith("gemini-");
                
                if (isModelName && "ollama".equalsIgnoreCase(provider)) {
                    // This is a model name from old config, put it in model field instead
                    aiModelCombo.setSelectedItem(apiKey);
                    aiApiKeyField.setText(""); // Clear API key field
                } else {
                    // This is an actual API key
                    aiApiKeyField.setText(apiKey);
                }
            }
            
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
            listener.setCurrentJtlFile(currentJtlFileField.getText());
            listener.setBaselineFile(baselineFileField.getText());
            listener.setOutputDir(outputDirField.getText());
            try {
                listener.setThreshold(Double.parseDouble(thresholdField.getText()));
            } catch (NumberFormatException e) {
                listener.setThreshold(5.0);
            }
            listener.setAiApiKey(aiApiKeyField.getText());
            listener.setAiEndpoint(aiEndpointField.getText());
            listener.setAiProvider((String) aiProviderCombo.getSelectedItem());
            listener.setExportPdf(exportPdfCheckbox.isSelected());
        }
    }

    @Override
    public void clearGui() {
        super.clearGui();
        currentJtlFileField.setText("");
        baselineFileField.setText("");
        outputDirField.setText(System.getProperty("user.dir"));
        thresholdField.setText("5.0");
        aiApiKeyField.setText(""); // Keep empty - model will be used for Ollama
        aiEndpointField.setText("");
        aiProviderCombo.setSelectedItem("ollama");
        updateModelDropdown(); // Populate model dropdown
        aiModelCombo.setSelectedItem("llama3.2 (Recommended)"); // Set recommended model
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
        
        int row = 0;
        
        // Current JTL File
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.3;
        mainPanel.add(new JLabel("Current JTL File:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.6;
        currentJtlFileField = new JTextField(25);
        mainPanel.add(currentJtlFileField, gbc);
        
        gbc.gridx = 2;
        gbc.weightx = 0.1;
        JButton browseCurrentButton = new JButton("Browse...");
        browseCurrentButton.addActionListener(e -> browseForFile(currentJtlFileField));
        mainPanel.add(browseCurrentButton, gbc);
        
        row++;
        
        // Baseline File
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.3;
        mainPanel.add(new JLabel("Baseline JTL File (optional):"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.6;
        baselineFileField = new JTextField(25);
        mainPanel.add(baselineFileField, gbc);
        
        gbc.gridx = 2;
        gbc.weightx = 0.1;
        JButton browseBaselineButton = new JButton("Browse...");
        browseBaselineButton.addActionListener(e -> browseForFile(baselineFileField));
        mainPanel.add(browseBaselineButton, gbc);
        
        row++;
        
        // Output Directory
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.3;
        mainPanel.add(new JLabel("Output Directory:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.6;
        outputDirField = new JTextField(System.getProperty("user.dir"), 25);
        mainPanel.add(outputDirField, gbc);
        
        gbc.gridx = 2;
        gbc.weightx = 0.1;
        JButton browseOutputButton = new JButton("Browse...");
        browseOutputButton.addActionListener(e -> browseForDirectory(outputDirField));
        mainPanel.add(browseOutputButton, gbc);
        
        row++;
        
        // Threshold for comparison / Error rate for single report
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.3;
        JPanel thresholdLabelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        JLabel thresholdLabel = new JLabel("Threshold % / Error Rate %: ");
        JLabel thresholdInfo = new JLabel("ⓘ");
        thresholdInfo.setForeground(new Color(70, 130, 180));
        thresholdInfo.setToolTipText("Comparison: performance degradation threshold. Single Report: error rate threshold");
        thresholdLabelPanel.add(thresholdLabel);
        thresholdLabelPanel.add(thresholdInfo);
        mainPanel.add(thresholdLabelPanel, gbc);
        
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 0.7;
        thresholdField = new JTextField("5.0", 10);
        thresholdField.setToolTipText("For comparison reports: degradation threshold. For single reports: acceptable error rate threshold");
        mainPanel.add(thresholdField, gbc);
        gbc.gridwidth = 1;
        
        row++;
        
        // AI API Key
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.3;
        JPanel apiKeyLabelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        JLabel apiKeyLabel = new JLabel("AI API Key (optional): ");
        JLabel apiKeyInfo = new JLabel("ⓘ");
        apiKeyInfo.setForeground(new Color(70, 130, 180));
        apiKeyInfo.setToolTipText("Mac/Linux: export OPENAI_API_KEY=\"key\". Windows: set OPENAI_API_KEY=key");
        apiKeyLabelPanel.add(apiKeyLabel);
        apiKeyLabelPanel.add(apiKeyInfo);
        mainPanel.add(apiKeyLabelPanel, gbc);
        
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 0.7;
        aiApiKeyField = new JTextField(30);
        aiApiKeyField.setToolTipText("Mac/Linux: export OPENAI_API_KEY=\"key\". Windows: set OPENAI_API_KEY=key");
        mainPanel.add(aiApiKeyField, gbc);
        gbc.gridwidth = 1;
        
        row++;
        
        // AI Provider
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.3;
        mainPanel.add(new JLabel("AI Provider:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 0.7;
        String[] providers = {"ollama", "openai", "claude", "gemini"};
        aiProviderCombo = new JComboBox<>(providers);
        aiProviderCombo.setSelectedItem("ollama");
        aiProviderCombo.addActionListener(e -> updateModelDropdown());
        mainPanel.add(aiProviderCombo, gbc);
        gbc.gridwidth = 1;
        
        row++;
        
        // AI Model
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.3;
        JPanel modelLabelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        JLabel modelLabel = new JLabel("AI Model: ");
        JLabel modelInfo = new JLabel("ⓘ");
        modelInfo.setForeground(new Color(70, 130, 180));
        modelInfo.setToolTipText("Select from suggestions or enter custom model name. Recommended: llama3.2 (Ollama), gpt-4 (OpenAI), claude-3-5-sonnet (Claude)");
        modelLabelPanel.add(modelLabel);
        modelLabelPanel.add(modelInfo);
        mainPanel.add(modelLabelPanel, gbc);
        
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 0.7;
        aiModelCombo = new JComboBox<>();
        aiModelCombo.setEditable(true); // Allow custom model names
        updateModelDropdown(); // Initialize with suggested models
        mainPanel.add(aiModelCombo, gbc);
        gbc.gridwidth = 1;
        
        row++;
        
        // AI Endpoint
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.3;
        mainPanel.add(new JLabel("AI Endpoint (optional):"), gbc);
        
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 0.7;
        aiEndpointField = new JTextField(30);
        mainPanel.add(aiEndpointField, gbc);
        gbc.gridwidth = 1;
        
        row++;
        
        // Export PDF
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.3;
        mainPanel.add(new JLabel("Export PDF:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 0.7;
        exportPdfCheckbox = new JCheckBox();
        mainPanel.add(exportPdfCheckbox, gbc);
        gbc.gridwidth = 1;
        
        row++;
        
        // Generate Report Button
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 3;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(15, 5, 5, 5);
        generateReportButton = new JButton("Generate Report from JTL Files");
        generateReportButton.setFont(new Font("SansSerif", Font.BOLD, 12));
        generateReportButton.addActionListener(e -> generateReportFromJtl());
        mainPanel.add(generateReportButton, gbc);
        
        add(mainPanel, BorderLayout.CENTER);
        
        // Add info panel
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setBorder(BorderFactory.createTitledBorder("Information"));
        JTextArea infoText = new JTextArea(
            "JMeter Insights Pro generates HTML reports with AI-powered analysis from JTL files.\n\n" +
            "POST-ANALYSIS MODE:\n" +
            "• Specify Current JTL File (required) - must contain test results\n" +
            "• Leave Baseline File empty for single report\n" +
            "• Provide Baseline File for comparison report\n" +
            "• Click 'Generate Report from JTL Files' to create the report\n\n" +
            "THRESHOLD CONFIGURATION:\n" +
            "• Comparison Reports: Performance degradation threshold (e.g., 5.0 = 5%)\n" +
            "• Single Reports: Error rate threshold (e.g., 1.0 = 1% error rate)\n" +
            "  - Transactions exceeding threshold are highlighted as warnings\n" +
            "  - Transactions exceeding 2x threshold are marked critical\n\n" +
            "AI CONFIGURATION:\n" +
            "• AI Provider: Select from dropdown (ollama, openai, claude, gemini)\n" +
            "• AI Model: Choose recommended model OR type custom model name\n" +
            "  - Recommended models marked for best quality analysis\n" +
            "  - You can type any model name (e.g., new Ollama models like llama3.3)\n" +
            "• AI API Key: Optional for Ollama, required for OpenAI/Claude/Gemini\n" +
            "  - OpenAI: sk-proj-..., Claude: sk-ant-..., Gemini: API key\n" +
            "  - Ollama: Leave empty (selected model is used automatically)\n" +
            "• AI Endpoint: Optional, for custom endpoints (Azure OpenAI, corporate proxies)\n" +
            "• Ollama: Free & local! Install Ollama, select/type model name, leave API key empty"
        );
        infoText.setEditable(false);
        infoText.setBackground(mainPanel.getBackground());
        infoText.setFont(new Font("SansSerif", Font.PLAIN, 11));
        infoText.setWrapStyleWord(true);
        infoText.setLineWrap(true);
        infoPanel.add(infoText, BorderLayout.CENTER);
        
        add(infoPanel, BorderLayout.SOUTH);
    }
    
    private void updateModelDropdown() {
        String provider = (String) aiProviderCombo.getSelectedItem();
        aiModelCombo.removeAllItems();
        
        // Add recommended models as suggestions based on provider
        if ("ollama".equals(provider)) {
            aiModelCombo.addItem("llama3.2 (Recommended)");
            aiModelCombo.addItem("llama3.1");
            aiModelCombo.addItem("llama2");
            aiModelCombo.addItem("mistral");
            aiModelCombo.addItem("codellama");
            aiModelCombo.addItem("phi");
            aiModelCombo.addItem("gemma");
            aiModelCombo.addItem("qwen");
        } else if ("openai".equals(provider)) {
            aiModelCombo.addItem("gpt-4 (Recommended)");
            aiModelCombo.addItem("gpt-4-turbo");
            aiModelCombo.addItem("gpt-3.5-turbo");
            aiModelCombo.addItem("gpt-4o");
            aiModelCombo.addItem("gpt-4o-mini");
        } else if ("claude".equals(provider)) {
            aiModelCombo.addItem("claude-3-5-sonnet-20241022 (Recommended)");
            aiModelCombo.addItem("claude-3-opus-20240229");
            aiModelCombo.addItem("claude-3-sonnet-20240229");
            aiModelCombo.addItem("claude-3-haiku-20240307");
        } else if ("gemini".equals(provider)) {
            aiModelCombo.addItem("gemini-1.5-pro (Recommended)");
            aiModelCombo.addItem("gemini-1.5-flash");
            aiModelCombo.addItem("gemini-pro");
        }
        
        // Select the first item (recommended model) by default
        if (aiModelCombo.getItemCount() > 0) {
            aiModelCombo.setSelectedIndex(0);
        }
    }
    
    private void browseForFile(JTextField textField) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setDialogTitle("Select JTL File");
        
        String currentPath = textField.getText();
        if (currentPath != null && !currentPath.isEmpty()) {
            fileChooser.setCurrentDirectory(new File(currentPath).getParentFile());
        }
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            textField.setText(fileChooser.getSelectedFile().getAbsolutePath());
        }
    }
    
    private void browseForDirectory(JTextField textField) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setDialogTitle("Select Output Directory");
        
        String currentPath = textField.getText();
        if (currentPath != null && !currentPath.isEmpty()) {
            fileChooser.setCurrentDirectory(new File(currentPath));
        }
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            textField.setText(fileChooser.getSelectedFile().getAbsolutePath());
        }
    }
    
    private void generateReportFromJtl() {
        String currentJtlFile = currentJtlFileField.getText().trim();
        
        if (currentJtlFile.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Please specify a Current JTL File to generate report from.",
                "Missing JTL File",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (!new File(currentJtlFile).exists()) {
            JOptionPane.showMessageDialog(this,
                "Current JTL File does not exist: " + currentJtlFile,
                "File Not Found",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        generateReportButton.setEnabled(false);
        generateReportButton.setText("Generating Report...");
        
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            private String errorMessage = null;
            
            @Override
            protected Void doInBackground() {
                try {
                    String outputDir = outputDirField.getText().trim();
                    if (outputDir.isEmpty()) {
                        outputDir = System.getProperty("user.dir");
                    }
                    
                    File dir = new File(outputDir);
                    if (!dir.exists()) {
                        dir.mkdirs();
                    }
                    
                    String htmlOutputPath = new File(outputDir, "performance_report.html").getAbsolutePath();
                    String baselineFile = baselineFileField.getText().trim();
                    double threshold;
                    try {
                        threshold = Double.parseDouble(thresholdField.getText().trim());
                    } catch (NumberFormatException e) {
                        threshold = 5.0;
                    }
                    
                    String provider = (String) aiProviderCombo.getSelectedItem();
                    if (provider == null || provider.isEmpty()) {
                        provider = "ollama";
                    }
                    
                    // Get model name and clean it (remove "(Recommended)" suffix)
                    String modelName = (String) aiModelCombo.getSelectedItem();
                    if (modelName != null && modelName.contains(" (Recommended)")) {
                        modelName = modelName.replace(" (Recommended)", "").trim();
                    }
                    if (modelName == null || modelName.isEmpty()) {
                        modelName = "llama3.2"; // Default fallback
                    }
                    
                    String apiKey = aiApiKeyField.getText().trim();
                    if (apiKey.isEmpty()) {
                        // For Ollama, use selected model from dropdown
                        if ("ollama".equalsIgnoreCase(provider)) {
                            apiKey = modelName;
                        } else {
                            // For other providers, try to get from environment
                            if ("claude".equalsIgnoreCase(provider) || "anthropic".equalsIgnoreCase(provider)) {
                                apiKey = System.getenv("ANTHROPIC_API_KEY");
                            } else if ("gemini".equalsIgnoreCase(provider)) {
                                apiKey = System.getenv("GEMINI_API_KEY");
                            } else {
                                apiKey = System.getenv("OPENAI_API_KEY");
                            }
                        }
                    }
                    
                    String endpoint = aiEndpointField.getText().trim();
                    if (endpoint.isEmpty()) {
                        endpoint = null;
                    }
                    
                    JTLParser parser = new JTLParser();
                    Map<String, PerformanceMetrics> currentMetrics = parser.parseJTLFile(currentJtlFile);
                    
                    HTMLReportGenerator htmlGenerator = new HTMLReportGenerator();
                    AIReportGenerator aiGenerator = new AIReportGenerator(apiKey, endpoint, provider);
                    
                    if (baselineFile != null && !baselineFile.isEmpty() && new File(baselineFile).exists()) {
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
                        TimeSeriesData timeSeriesData = parser.parseTimeSeriesData(currentJtlFile);
                        // For single reports, threshold is used as error rate threshold
                        htmlGenerator.generateSingleReport(htmlOutputPath, currentMetrics, aiSummary, timeSeriesData, threshold);
                    }
                    
                    if (exportPdfCheckbox.isSelected()) {
                        String pdfOutputPath = new File(outputDir, "performance_report.pdf").getAbsolutePath();
                        PDFExporter pdfExporter = new PDFExporter();
                        pdfExporter.exportHTMLToPDF(htmlOutputPath, pdfOutputPath);
                    }
                    
                } catch (Exception e) {
                    log.error("Error generating report from JTL", e);
                    errorMessage = e.getMessage();
                }
                return null;
            }
            
            @Override
            protected void done() {
                generateReportButton.setEnabled(true);
                generateReportButton.setText("Generate Report from JTL Files");
                
                if (errorMessage != null) {
                    JOptionPane.showMessageDialog(PerformanceReporterGui.this,
                        "Error generating report:\n" + errorMessage,
                        "Report Generation Failed",
                        JOptionPane.ERROR_MESSAGE);
                } else {
                    String outputDir = outputDirField.getText().trim();
                    if (outputDir.isEmpty()) {
                        outputDir = System.getProperty("user.dir");
                    }
                    String reportPath = new File(outputDir, "performance_report.html").getAbsolutePath();
                    
                    int result = JOptionPane.showConfirmDialog(PerformanceReporterGui.this,
                        "Report generated successfully!\n\nLocation: " + reportPath + "\n\nOpen report now?",
                        "Report Generated",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.INFORMATION_MESSAGE);
                    
                    if (result == JOptionPane.YES_OPTION) {
                        try {
                            Desktop.getDesktop().open(new File(reportPath));
                        } catch (Exception e) {
                            log.error("Error opening report", e);
                        }
                    }
                }
            }
        };
        
        worker.execute();
    }
}
