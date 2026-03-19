package com.jmeter.plugin.ai;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jmeter.plugin.model.ComparisonResult;
import com.jmeter.plugin.model.PerformanceMetrics;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class AIReportGenerator {
    private static final Logger log = LoggerFactory.getLogger(AIReportGenerator.class);
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    
    private final OkHttpClient client;
    private final String apiKey;
    private final String apiEndpoint;
    private final String provider;

    public AIReportGenerator(String apiKey, String apiEndpoint) {
        this(apiKey, apiEndpoint, "openai");
    }

    public AIReportGenerator(String apiKey, String apiEndpoint, String provider) {
        this.apiKey = apiKey;
        this.provider = provider != null ? provider.toLowerCase() : "openai";
        
        if ("claude".equals(this.provider) || "anthropic".equals(this.provider)) {
            this.apiEndpoint = apiEndpoint != null ? apiEndpoint : "https://api.anthropic.com/v1/messages";
        } else if ("gemini".equals(this.provider)) {
            this.apiEndpoint = apiEndpoint != null ? apiEndpoint : "https://generativelanguage.googleapis.com/v1/models/gemini-1.5-pro:generateContent";
        } else if ("ollama".equals(this.provider)) {
            this.apiEndpoint = apiEndpoint != null ? apiEndpoint : "http://localhost:11434/api/generate";
        } else {
            this.apiEndpoint = apiEndpoint != null ? apiEndpoint : "https://api.openai.com/v1/chat/completions";
        }
        
        this.client = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(900, TimeUnit.SECONDS)  // 15 minutes for AI inference
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();
    }

    public String generateSummary(
            Map<String, PerformanceMetrics> baselineMetrics,
            Map<String, PerformanceMetrics> currentMetrics,
            List<ComparisonResult> comparisonResults) {
        
        if ((apiKey == null || apiKey.isEmpty()) && !"ollama".equals(provider)) {
            log.warn("No API key provided for provider '{}', using fallback summary", provider);
            return generateFallbackSummary(comparisonResults);
        }

        try {
            log.info("Generating AI summary using provider: {}, endpoint: {}", provider, apiEndpoint);
            String prompt = buildEnhancedPrompt(baselineMetrics, currentMetrics, comparisonResults);
            String aiResponse = callAIAPI(prompt);
            log.info("AI summary generated successfully");
            return aiResponse;
        } catch (Exception e) {
            log.error("Error generating AI summary with provider '{}': {} - Falling back to basic summary", provider, e.getMessage(), e);
            return generateFallbackSummary(comparisonResults);
        }
    }

    public String generateSingleReportSummary(Map<String, PerformanceMetrics> metrics) {
        if ((apiKey == null || apiKey.isEmpty()) && !"ollama".equals(provider)) {
            log.warn("No API key provided for provider '{}', using fallback summary", provider);
            return generateFallbackSingleSummary(metrics);
        }

        try {
            log.info("Generating AI summary using provider: {}, endpoint: {}", provider, apiEndpoint);
            String prompt = buildEnhancedSingleReportPrompt(metrics);
            String aiResponse = callAIAPI(prompt);
            log.info("AI summary generated successfully");
            return aiResponse;
        } catch (Exception e) {
            log.error("Error generating AI summary with provider '{}': {} - Falling back to basic summary", provider, e.getMessage(), e);
            return generateFallbackSingleSummary(metrics);
        }
    }

    private String buildEnhancedPrompt(
            Map<String, PerformanceMetrics> baselineMetrics,
            Map<String, PerformanceMetrics> currentMetrics,
            List<ComparisonResult> comparisonResults) {
        
        StringBuilder prompt = new StringBuilder();
        prompt.append("You are a senior performance engineer with expertise in regression analysis, analyzing JMeter load test comparison results.\n\n");
        
        prompt.append("=== PERFORMANCE COMPARISON DATA ===\n");
        int degradedCount = 0;
        int improvedCount = 0;
        double maxDegradation = 0.0;
        String worstTransaction = "";
        
        for (ComparisonResult result : comparisonResults) {
            if (result.isDegraded()) {
                degradedCount++;
                if (result.getPercentageChange() > maxDegradation) {
                    maxDegradation = result.getPercentageChange();
                    worstTransaction = result.getLabel();
                }
            }
            if (result.getPercentageChange() < -5) improvedCount++;
            prompt.append(String.format("Transaction: %s\n", result.getLabel()));
            prompt.append(String.format("  - Avg Response Time: %.2fms → %.2fms (%+.2f%%)\n",
                    result.getBaselineAvg(), result.getCurrentAvg(), result.getPercentageChange()));
            prompt.append(String.format("  - P90: %.2fms → %.2fms (%+.2f%%)\n",
                    result.getBaselineP90(), result.getCurrentP90(), result.getP90PercentageChange()));
            prompt.append(String.format("  - Error Rate: %.2f%% → %.2f%% (%+.2f%%)\n",
                    result.getBaselineErrorRate(), result.getCurrentErrorRate(), result.getErrorRateChange()));
            prompt.append(String.format("  - Status: %s\n\n", result.isDegraded() ? "⚠️ DEGRADED" : "✓ ACCEPTABLE"));
        }
        
        prompt.append(String.format("\nSummary: %d degraded, %d improved, %d total transactions\n", degradedCount, improvedCount, comparisonResults.size()));
        if (degradedCount > 0) {
            prompt.append(String.format("Worst Regression: %s with %.2f%% degradation\n\n", worstTransaction, maxDegradation));
        } else {
            prompt.append("No performance regressions detected\n\n");
        }
        
        prompt.append("=== JTL DATA CONTEXT ===\n");
        prompt.append("JTL files contain:\n");
        prompt.append("- Response times (avg, min, max, percentiles)\n");
        prompt.append("- Error rates and success/failure status\n");
        prompt.append("- Throughput (requests per second)\n");
        prompt.append("- Timestamps (to analyze trends over time)\n");
        prompt.append("- Error messages (if captured)\n");
        prompt.append("- Bytes sent/received\n");
        prompt.append("- Latency and connect time\n\n");
        prompt.append("JTL files DO NOT contain:\n");
        prompt.append("- Infrastructure metrics (CPU, memory, disk, network)\n");
        prompt.append("- Application logs or stack traces\n");
        prompt.append("- Database query execution details\n");
        prompt.append("- Code-level profiling or method execution times\n");
        prompt.append("- JVM metrics (GC, heap usage)\n\n");
        
        prompt.append("=== ANALYSIS INSTRUCTIONS ===\n");
        prompt.append("Provide a professional, data-driven analysis following this EXACT structure:\n\n");
        
        prompt.append("<h4>Executive Summary</h4>\n");
        prompt.append("<p>Write 3-4 sentences summarizing: (1) regression severity based on actual degradation percentages, (2) business impact on users, (3) clear deployment recommendation (GO/NO-GO with specific justification based on the data).</p>\n\n");
        
        prompt.append("<h4>Critical Findings</h4>\n");
        prompt.append("<ul>\n");
        prompt.append("List the top 3 most critical regressions from the actual comparison data above. For each, include: transaction name, baseline value, current value, percentage change, and severity assessment (Critical/High/Medium).\n");
        prompt.append("</ul>\n\n");
        
        prompt.append("<h4>Performance Patterns & Correlation</h4>\n");
        prompt.append("<p>Analyze patterns:\n");
        prompt.append("- Are specific transaction types affected (e.g., all database operations, all API calls)?\n");
        prompt.append("- Is degradation consistent across all metrics or isolated to specific ones?\n");
        prompt.append("- Are there correlations between error rate increases and response time degradation?\n");
        prompt.append("- Do improvements in some areas offset degradations in others?</p>\n\n");
        
        prompt.append("<h4>Observable Patterns (JTL Data Only)</h4>\n");
        prompt.append("<p><em>Analysis based strictly on JTL metrics: response times, error rates, throughput, and timestamps.</em></p>\n");
        prompt.append("<ul>\n");
        prompt.append("<li><strong>Response Time Patterns:</strong> Which transactions are slowest? Is degradation uniform across all transactions or isolated to specific ones?</li>\n");
        prompt.append("<li><strong>Error Patterns:</strong> Which transactions have highest error rates? What error messages appear in the JTL (if any)?</li>\n");
        prompt.append("<li><strong>Percentile Analysis:</strong> Calculate P99/P95 ratio - values >2x indicate inconsistent performance with outliers</li>\n");
        prompt.append("<li><strong>Correlation:</strong> Do transactions with high error rates also have slow response times?</li>\n");
        prompt.append("<li><strong>Trend:</strong> Based on timestamps, is degradation immediate or gradual over the test duration?</li>\n");
        prompt.append("</ul>\n");
        prompt.append("<p><strong>⚠️ Important:</strong> JTL files don't contain infrastructure metrics, code profiling, or database query data. Recommend enabling APM tools, application logs, and database monitoring for root cause investigation.</p>\n\n");
        
        prompt.append("<h4>Risk Assessment</h4>\n");
        prompt.append("<p><strong>Overall Risk Level:</strong> Based on the actual degradation data, assign one of: LOW/MEDIUM/HIGH/CRITICAL. Justify based on specific degradation percentages and error rates from the data.<br>\n");
        prompt.append("<strong>Production Impact:</strong> Quantify user impact using actual metrics (e.g., 'Users will experience X% slower response times on Y critical transactions').<br>\n");
        prompt.append("<strong>Deployment Recommendation:</strong> Provide clear GO or NO-GO recommendation with specific justification based on the comparison data.</p>\n\n");
        
        prompt.append("<h4>Recommendations (Based on JTL Observations)</h4>\n");
        prompt.append("<p>Analyze the actual comparison data and provide specific recommendations:</p>\n");
        prompt.append("<ul>\n");
        prompt.append("<li><strong>[CRITICAL] Block Deployment:</strong> If any transaction shows error rate >5% or response time degradation >50%, recommend blocking deployment. List specific transaction names and actual metrics from the data.</li>\n");
        prompt.append("<li><strong>[HIGH] Investigate:</strong> List specific transactions from the comparison data that require investigation. Include actual baseline vs current values and percentage changes.</li>\n");
        prompt.append("<li><strong>[MEDIUM] Performance Targets:</strong> Based on observed baseline and current metrics, suggest realistic SLO thresholds.</li>\n");
        prompt.append("<li><strong>[MONITORING] Setup:</strong> Recommend specific JMeter assertions based on actual performance thresholds observed in the data.</li>\n");
        prompt.append("<li><strong>[TESTING] Further Analysis:</strong> Recommend additional test types based on patterns visible in the comparison (e.g., soak test if gradual degradation, spike test if throughput concerns).</li>\n");
        prompt.append("<li><strong>[OBSERVABILITY] Enable Deeper Analysis:</strong> Recommend enabling APM tools, JVM metrics, database query logging, and application logs to diagnose root causes that JTL comparison data cannot reveal.</li>\n");
        prompt.append("</ul>\n\n");
        
        prompt.append("<h4>Rollback Plan</h4>\n");
        prompt.append("<p>If degradation is severe (based on the actual comparison data):\n");
        prompt.append("- <strong>Immediate Action:</strong> Specify what to rollback or disable based on which transactions show the worst degradation.\n");
        prompt.append("- <strong>Mitigation Steps:</strong> Suggest temporary fixes based on observed patterns (e.g., increase timeouts if response times degraded, add circuit breakers if errors increased).\n");
        prompt.append("- <strong>Validation:</strong> Specify how to verify rollback success using specific metrics from the baseline data.</p>\n\n");
        
        prompt.append("CRITICAL RULES:\n");
        prompt.append("- DO NOT use placeholder text like '[Transaction X]' or '[List specific transactions...]'\n");
        prompt.append("- ALWAYS use actual transaction names and metrics from the comparison data provided above\n");
        prompt.append("- Use EXACT HTML structure with <h4>, <p>, <ul>, <li>, <strong> tags\n");
        prompt.append("- Every recommendation MUST reference specific transactions with actual baseline vs current values\n");
        prompt.append("- Provide clear GO/NO-GO deployment recommendation based on actual degradation severity\n");
        prompt.append("- Quantify business impact using actual percentage changes from the data\n");
        prompt.append("- Be technical but concise (600-800 words)\n");
        prompt.append("- Use professional tone with specific, actionable advice\n");
        prompt.append("- Prioritize recommendations by severity visible in the comparison data");
        
        return prompt.toString();
    }

    private String buildEnhancedSingleReportPrompt(Map<String, PerformanceMetrics> metrics) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("You are a senior performance engineer with expertise in web application optimization, analyzing JMeter load test results.\n\n");
        
        prompt.append("=== PERFORMANCE TEST DATA ===\n");
        for (Map.Entry<String, PerformanceMetrics> entry : metrics.entrySet()) {
            PerformanceMetrics m = entry.getValue();
            prompt.append(String.format("Transaction: %s\n", entry.getKey()));
            prompt.append(String.format("  - Avg: %.2fms, P90: %.2fms, P95: %.2fms, P99: %.2fms\n",
                    m.getAverageResponseTime(), m.getPercentile90(), m.getPercentile95(), m.getPercentile99()));
            prompt.append(String.format("  - Error Rate: %.2f%%, Throughput: %s\n",
                    m.getErrorRate(), m.getFormattedThroughput()));
            prompt.append(String.format("  - Samples: %d total, %d successful\n\n",
                    m.getTotalCount(), m.getSuccessCount()));
        }
        
        double avgResponseTime = metrics.values().stream()
                .mapToDouble(PerformanceMetrics::getAverageResponseTime).average().orElse(0.0);
        double avgErrorRate = metrics.values().stream()
                .mapToDouble(PerformanceMetrics::getErrorRate).average().orElse(0.0);
        double totalThroughput = metrics.values().stream()
                .mapToDouble(PerformanceMetrics::getThroughput).sum();
        
        prompt.append(String.format("\nOverall Metrics: Avg Response: %.2fms | Error Rate: %.2f%% | Total Throughput: %.2f req/s\n\n", 
                avgResponseTime, avgErrorRate, totalThroughput));
        
        prompt.append("=== INDUSTRY BENCHMARKS & CONTEXT ===\n");
        prompt.append("Use these benchmarks to assess performance:\n");
        prompt.append("- Excellent: <100ms avg, <200ms P95, <0.1% errors\n");
        prompt.append("- Good: <300ms avg, <500ms P95, <1% errors\n");
        prompt.append("- Acceptable: <1000ms avg, <2000ms P95, <5% errors\n");
        prompt.append("- Poor: >1000ms avg, >2000ms P95, >5% errors\n");
        prompt.append("- P99/P95 ratio >2x indicates tail latency issues\n");
        prompt.append("- Error rate >1% requires immediate investigation\n\n");
        
        prompt.append("=== ANALYSIS INSTRUCTIONS ===\n");
        prompt.append("Provide a professional, data-driven analysis following this EXACT structure:\n\n");
        
        prompt.append("<h4>Executive Summary</h4>\n");
        prompt.append("<p>Write 3-4 sentences summarizing: (1) overall performance assessment against industry benchmarks, (2) key metrics (avg RT, P95, error rate), (3) production readiness verdict (GO/NO-GO with justification).</p>\n\n");
        
        prompt.append("<h4>Performance Breakdown</h4>\n");
        prompt.append("<p><strong>Slowest Transactions:</strong></p>\n");
        prompt.append("<ul>\n");
        prompt.append("List the 3 slowest transactions from the actual test data above. For each transaction, show: transaction name, average response time, P95 value, and comparison to benchmarks (Excellent/Good/Acceptable/Poor).\n");
        prompt.append("</ul>\n");
        prompt.append("<p><strong>High Error Rates:</strong></p>\n");
        prompt.append("<ul>\n");
        prompt.append("CRITICAL: Review the 'Error Rate' field for EVERY transaction in the test data above. List EVERY transaction where Error Rate is greater than 1.00%. Format: 'Transaction Name (X.XX% error rate)'. If you see transactions with 100.00% error rate, you MUST list them. Only write 'No transactions exceed 1% error rate' if you have verified that ALL transactions have error rate ≤1.00%.\n");
        prompt.append("</ul>\n");
        prompt.append("<p><strong>Tail Latency Concerns:</strong></p>\n");
        prompt.append("<ul>\n");
        prompt.append("List all transactions where P99/P95 ratio is >2x from the actual test data. Show transaction name and the actual ratio. If none exist, write 'No significant tail latency issues detected'.\n");
        prompt.append("</ul>\n\n");
        
        prompt.append("<h4>Observable Patterns (JTL Data Only)</h4>\n");
        prompt.append("<p><em>Analysis based strictly on JTL metrics: response times, error rates, throughput, and timestamps.</em></p>\n");
        prompt.append("<ul>\n");
        prompt.append("<li><strong>Response Time Distribution:</strong> Which transactions have slowest avg/P95/P99? What is the P99/P95 ratio (>2x indicates inconsistent performance)?</li>\n");
        prompt.append("<li><strong>Error Analysis:</strong> Which transactions have error rates >1%? What error messages appear in the JTL file?</li>\n");
        prompt.append("<li><strong>Throughput Patterns:</strong> What is the achieved throughput (req/s) for each transaction? Are there any bottlenecks visible?</li>\n");
        prompt.append("<li><strong>Correlation:</strong> Do high error rates correlate with slow response times for the same transactions?</li>\n");
        prompt.append("<li><strong>Consistency:</strong> Are response times stable or highly variable (check min vs max vs avg)?</li>\n");
        prompt.append("</ul>\n");
        prompt.append("<p><strong>⚠️ Important:</strong> JTL files don't reveal WHY performance issues occur. Enable APM tools, application logs, database query logging, and infrastructure monitoring to diagnose root causes.</p>\n\n");
        
        prompt.append("<h4>Capacity Assessment</h4>\n");
        prompt.append("<p><strong>Current Load Handling:</strong> Based on actual throughput data, state the total req/s achieved and the average response time observed.<br>\n");
        prompt.append("<strong>Scalability Concerns:</strong> Identify specific bottlenecks visible in the data (e.g., transactions with low throughput, high response times, or errors).<br>\n");
        prompt.append("<strong>Recommended SLOs:</strong> Based on observed performance, suggest specific thresholds (e.g., P95 <500ms, P99 <1000ms, error rate <1%).</p>\n\n");
        
        prompt.append("<h4>Recommendations (Based on JTL Observations)</h4>\n");
        prompt.append("<p>Analyze the actual test data and provide specific recommendations:</p>\n");
        prompt.append("<ul>\n");
        prompt.append("<li><strong>[CRITICAL] Block Production:</strong> If any transaction has error rate >5% or P95 >2000ms, recommend blocking deployment with specific transaction names and metrics.</li>\n");
        prompt.append("<li><strong>[HIGH] Investigate:</strong> List specific transactions from the data that require investigation (those with P95 >1000ms or error rate >1%). Include actual metric values.</li>\n");
        prompt.append("<li><strong>[MEDIUM] Performance Targets:</strong> Based on the observed metrics in the data, suggest realistic SLO thresholds.</li>\n");
        prompt.append("<li><strong>[MONITORING] JMeter Assertions:</strong> Recommend specific JMeter assertions based on observed performance (e.g., Duration Assertion with actual threshold values).</li>\n");
        prompt.append("<li><strong>[TESTING] Additional Load Tests:</strong> Recommend additional test types based on patterns seen (soak test if memory concerns, spike test if throughput issues, etc.).</li>\n");
        prompt.append("<li><strong>[OBSERVABILITY] Enable Deeper Analysis:</strong> Recommend enabling APM tools, JVM profiling, database query logging, and application logs to diagnose root causes that JTL data cannot reveal.</li>\n");
        prompt.append("</ul>\n\n");
        
        prompt.append("<h4>Implementation Roadmap</h4>\n");
        prompt.append("<ol>\n");
        prompt.append("<li><strong>Immediate:</strong> List quick wins based on the data that can be deployed immediately (e.g., add monitoring, adjust timeouts).</li>\n");
        prompt.append("<li><strong>Short-term:</strong> List optimizations requiring code changes based on observed bottlenecks (e.g., optimize specific slow transactions).</li>\n");
        prompt.append("<li><strong>Long-term:</strong> List architectural improvements if patterns suggest systemic issues (e.g., caching layer, database scaling).</li>\n");
        prompt.append("</ol>\n\n");
        
        prompt.append("CRITICAL RULES:\n");
        prompt.append("- DO NOT use placeholder text like '[Transaction 1 name]' or '[List transactions...]'\n");
        prompt.append("- ALWAYS use actual transaction names, metrics, and values from the test data provided above\n");
        prompt.append("- Use EXACT HTML structure with <h4>, <p>, <ul>, <li>, <strong> tags\n");
        prompt.append("- Every recommendation MUST reference specific transactions and include actual numbers from the data\n");
        prompt.append("- Compare all metrics against industry benchmarks (Excellent: <100ms, Good: <300ms, Acceptable: <1000ms, Poor: >1000ms)\n");
        prompt.append("- Be technical but concise (600-800 words)\n");
        prompt.append("- Use professional tone with specific, actionable advice\n");
        prompt.append("- Include expected improvement percentages or absolute values where applicable");
        
        return prompt.toString();
    }

    private String callAIAPI(String prompt) throws IOException {
        if ("claude".equals(provider) || "anthropic".equals(provider)) {
            return callClaudeAPI(prompt);
        } else if ("gemini".equals(provider)) {
            return callGeminiAPI(prompt);
        } else if ("ollama".equals(provider)) {
            return callOllamaAPI(prompt);
        } else {
            return callOpenAIAPI(prompt);
        }
    }

    private String callOpenAIAPI(String prompt) throws IOException {
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("model", "gpt-3.5-turbo");
        requestBody.addProperty("max_tokens", 2500);
        requestBody.addProperty("temperature", 0.7);
        
        JsonObject message = new JsonObject();
        message.addProperty("role", "user");
        message.addProperty("content", prompt);
        
        com.google.gson.JsonArray messages = new com.google.gson.JsonArray();
        messages.add(message);
        requestBody.add("messages", messages);

        Request request = new Request.Builder()
                .url(apiEndpoint)
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(requestBody.toString(), JSON))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("AI API call failed: " + response.code());
            }

            String responseBody = response.body().string();
            JsonObject jsonResponse = JsonParser.parseString(responseBody).getAsJsonObject();
            
            return jsonResponse.getAsJsonArray("choices")
                    .get(0).getAsJsonObject()
                    .getAsJsonObject("message")
                    .get("content").getAsString().trim();
        }
    }

    private String callClaudeAPI(String prompt) throws IOException {
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("model", "claude-3-5-sonnet-20241022");
        requestBody.addProperty("max_tokens", 2500);
        
        JsonObject message = new JsonObject();
        message.addProperty("role", "user");
        message.addProperty("content", prompt);
        
        com.google.gson.JsonArray messages = new com.google.gson.JsonArray();
        messages.add(message);
        requestBody.add("messages", messages);

        Request request = new Request.Builder()
                .url(apiEndpoint)
                .addHeader("x-api-key", apiKey)
                .addHeader("anthropic-version", "2023-06-01")
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(requestBody.toString(), JSON))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "No error details";
                log.error("Claude API error response: {}", errorBody);
                throw new IOException("Claude API call failed: " + response.code());
            }

            String responseBody = response.body().string();
            JsonObject jsonResponse = JsonParser.parseString(responseBody).getAsJsonObject();
            
            return jsonResponse.getAsJsonArray("content")
                    .get(0).getAsJsonObject()
                    .get("text").getAsString().trim();
        }
    }

    private String callGeminiAPI(String prompt) throws IOException {
        JsonObject requestBody = new JsonObject();
        
        JsonObject textPart = new JsonObject();
        textPart.addProperty("text", prompt);
        
        JsonObject part = new JsonObject();
        com.google.gson.JsonArray parts = new com.google.gson.JsonArray();
        parts.add(textPart);
        
        JsonObject content = new JsonObject();
        content.add("parts", parts);
        
        com.google.gson.JsonArray contents = new com.google.gson.JsonArray();
        contents.add(content);
        requestBody.add("contents", contents);
        
        JsonObject generationConfig = new JsonObject();
        generationConfig.addProperty("temperature", 0.7);
        generationConfig.addProperty("maxOutputTokens", 2500);
        requestBody.add("generationConfig", generationConfig);

        String urlWithKey = apiEndpoint + "?key=" + apiKey;
        
        Request request = new Request.Builder()
                .url(urlWithKey)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(requestBody.toString(), JSON))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "No error details";
                log.error("Gemini API error response: {}", errorBody);
                throw new IOException("Gemini API call failed: " + response.code());
            }

            String responseBody = response.body().string();
            JsonObject jsonResponse = JsonParser.parseString(responseBody).getAsJsonObject();
            
            return jsonResponse.getAsJsonArray("candidates")
                    .get(0).getAsJsonObject()
                    .getAsJsonObject("content")
                    .getAsJsonArray("parts")
                    .get(0).getAsJsonObject()
                    .get("text").getAsString().trim();
        }
    }

    private String callOllamaAPI(String prompt) throws IOException {
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("model", apiKey != null && !apiKey.isEmpty() ? apiKey : "llama3.2");
        requestBody.addProperty("prompt", prompt);
        requestBody.addProperty("stream", false);
        
        JsonObject options = new JsonObject();
        options.addProperty("temperature", 0.7);
        options.addProperty("num_predict", 2500);
        requestBody.add("options", options);

        Request request = new Request.Builder()
                .url(apiEndpoint)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(requestBody.toString(), JSON))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "No error details";
                log.error("Ollama API error response: {}", errorBody);
                throw new IOException("Ollama API call failed: " + response.code());
            }

            String responseBody = response.body().string();
            JsonObject jsonResponse = JsonParser.parseString(responseBody).getAsJsonObject();
            
            return jsonResponse.get("response").getAsString().trim();
        }
    }

    private String generateFallbackSummary(List<ComparisonResult> comparisonResults) {
        StringBuilder summary = new StringBuilder();
        summary.append("<h3>Performance Analysis Summary</h3>");
        
        long degradedCount = comparisonResults.stream().filter(ComparisonResult::isDegraded).count();
        long improvedCount = comparisonResults.stream().filter(r -> r.getPercentageChange() < 0).count();
        
        summary.append("<p><strong>Overall Assessment:</strong> ");
        if (degradedCount == 0) {
            summary.append("All transactions performed within acceptable thresholds. ");
        } else {
            summary.append(String.format("%d transaction(s) showed performance degradation. ", degradedCount));
        }
        
        if (improvedCount > 0) {
            summary.append(String.format("%d transaction(s) showed improvement. ", improvedCount));
        }
        summary.append("</p>");
        
        if (degradedCount > 0) {
            summary.append("<p><strong>Key Regressions:</strong></p><ul>");
            comparisonResults.stream()
                    .filter(ComparisonResult::isDegraded)
                    .forEach(r -> summary.append(String.format(
                            "<li>%s degraded by %.2f%% (%.2fms → %.2fms)</li>",
                            r.getLabel(), r.getPercentageChange(), r.getBaselineAvg(), r.getCurrentAvg())));
            summary.append("</ul>");
        }
        
        if (improvedCount > 0) {
            summary.append("<p><strong>Key Improvements:</strong></p><ul>");
            comparisonResults.stream()
                    .filter(r -> r.getPercentageChange() < -5)
                    .limit(5)
                    .forEach(r -> summary.append(String.format(
                            "<li>%s improved by %.2f%% (%.2fms → %.2fms)</li>",
                            r.getLabel(), Math.abs(r.getPercentageChange()), r.getBaselineAvg(), r.getCurrentAvg())));
            summary.append("</ul>");
        }
        
        summary.append("<p><strong>Recommendations:</strong></p><ul>");
        if (degradedCount > 0) {
            summary.append("<li>Investigate root cause of degraded transactions</li>");
            summary.append("<li>Review recent code changes and infrastructure modifications</li>");
        } else {
            summary.append("<li>Continue monitoring performance trends</li>");
        }
        summary.append("<li>Set up automated performance regression testing</li>");
        summary.append("</ul>");
        
        return summary.toString();
    }

    private String generateFallbackSingleSummary(Map<String, PerformanceMetrics> metrics) {
        StringBuilder summary = new StringBuilder();
        summary.append("<h3>Performance Test Summary</h3>");
        
        double avgResponseTime = metrics.values().stream()
                .mapToDouble(PerformanceMetrics::getAverageResponseTime)
                .average()
                .orElse(0.0);
        
        double avgErrorRate = metrics.values().stream()
                .mapToDouble(PerformanceMetrics::getErrorRate)
                .average()
                .orElse(0.0);
        
        summary.append(String.format("<p><strong>Overall Assessment:</strong> Tested %d transaction(s) with an average response time of %.2fms and error rate of %.2f%%.</p>",
                metrics.size(), avgResponseTime, avgErrorRate));
        
        List<Map.Entry<String, PerformanceMetrics>> slowTransactions = metrics.entrySet().stream()
                .filter(e -> e.getValue().getAverageResponseTime() > avgResponseTime * 1.5)
                .limit(5)
                .toList();
        
        if (!slowTransactions.isEmpty()) {
            summary.append("<p><strong>Transactions Requiring Attention:</strong></p><ul>");
            slowTransactions.forEach(e -> summary.append(String.format(
                    "<li>%s: Avg %.2fms, P95 %.2fms, Error Rate %.2f%%</li>",
                    e.getKey(), e.getValue().getAverageResponseTime(),
                    e.getValue().getPercentile95(), e.getValue().getErrorRate())));
            summary.append("</ul>");
        }
        
        summary.append("<p><strong>Recommendations:</strong></p><ul>");
        if (avgErrorRate > 1.0) {
            summary.append("<li>Investigate and reduce error rate</li>");
        }
        if (!slowTransactions.isEmpty()) {
            summary.append("<li>Optimize slow transactions identified above</li>");
        }
        summary.append("<li>Establish baseline for future comparisons</li>");
        summary.append("<li>Monitor trends over time</li>");
        summary.append("</ul>");
        
        return summary.toString();
    }
}
