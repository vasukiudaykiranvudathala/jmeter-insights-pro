package com.jmeter.plugin.parser;

import com.jmeter.plugin.model.PerformanceMetrics;
import com.jmeter.plugin.model.TimeSeriesData;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

public class JTLParser {
    private static final Logger log = LoggerFactory.getLogger(JTLParser.class);

    public Map<String, PerformanceMetrics> parseJTLFile(String filePath) throws IOException {
        try (Reader reader = new FileReader(filePath);
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {
            
            boolean isAggregate = csvParser.getHeaderNames().contains("# Samples") && 
                                 csvParser.getHeaderNames().contains("90% Line");
            
            if (isAggregate) {
                return parseAggregateFormat(filePath);
            } else {
                return parseRawFormat(filePath);
            }
        }
    }
    
    private Map<String, PerformanceMetrics> parseAggregateFormat(String filePath) throws IOException {
        AggregateJTLParser aggregateParser = new AggregateJTLParser();
        return aggregateParser.parseAggregateJTL(filePath);
    }
    
    private Map<String, PerformanceMetrics> parseRawFormat(String filePath) throws IOException {
        Map<String, PerformanceMetrics> metricsMap = new HashMap<>();
        Map<String, Long> minTimestampPerLabel = new HashMap<>();
        Map<String, Long> maxTimestampPerLabel = new HashMap<>();
        
        try (Reader reader = new FileReader(filePath);
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {
            
            boolean hasTimestamp = csvParser.getHeaderNames().contains("timeStamp");
            
            for (CSVRecord record : csvParser) {
                try {
                    long elapsed = Long.parseLong(record.get("elapsed"));
                    String label = record.get("label");
                    boolean success = "true".equalsIgnoreCase(record.get("success"));
                    
                    if (hasTimestamp) {
                        long timestamp = Long.parseLong(record.get("timeStamp"));
                        long endTimestamp = timestamp + elapsed;
                        
                        // Track per-transaction min/max timestamps
                        minTimestampPerLabel.merge(label, timestamp, Math::min);
                        maxTimestampPerLabel.merge(label, endTimestamp, Math::max);
                    }
                    
                    PerformanceMetrics metrics = metricsMap.computeIfAbsent(label, PerformanceMetrics::new);
                    metrics.addResponseTime(elapsed, success);
                    
                } catch (Exception e) {
                    log.warn("Error parsing record: {}", e.getMessage());
                }
            }
            
            // Set per-transaction start/end times for accurate throughput calculation
            if (hasTimestamp) {
                for (Map.Entry<String, PerformanceMetrics> entry : metricsMap.entrySet()) {
                    String label = entry.getKey();
                    PerformanceMetrics metrics = entry.getValue();
                    
                    if (minTimestampPerLabel.containsKey(label)) {
                        LocalDateTime startTime = LocalDateTime.ofInstant(
                                Instant.ofEpochMilli(minTimestampPerLabel.get(label)), ZoneId.systemDefault());
                        LocalDateTime endTime = LocalDateTime.ofInstant(
                                Instant.ofEpochMilli(maxTimestampPerLabel.get(label)), ZoneId.systemDefault());
                        
                        metrics.setStartTime(startTime);
                        metrics.setEndTime(endTime);
                    }
                }
            }
            
            for (PerformanceMetrics metrics : metricsMap.values()) {
                metrics.calculateStatistics();
            }
        }
        
        return metricsMap;
    }
    
    public TimeSeriesData parseTimeSeriesData(String filePath) throws IOException {
        TimeSeriesData timeSeriesData = new TimeSeriesData();
        
        try (Reader reader = new FileReader(filePath);
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {
            
            boolean hasTimestamp = csvParser.getHeaderNames().contains("timeStamp");
            boolean hasAllThreads = csvParser.getHeaderNames().contains("allThreads");
            
            if (!hasTimestamp) {
                log.warn("No timestamp column found in JTL file, returning empty time series data");
                return timeSeriesData;
            }
            
            // Group data by time intervals (1 second intervals) and by transaction
            Map<Long, List<CSVRecord>> recordsBySecond = new TreeMap<>();
            Map<Long, Map<String, List<CSVRecord>>> recordsBySecondAndTransaction = new TreeMap<>();
            Map<Long, Set<String>> activeThreadsBySecond = new TreeMap<>();
            
            for (CSVRecord record : csvParser) {
                try {
                    long timestamp = Long.parseLong(record.get("timeStamp"));
                    long secondTimestamp = (timestamp / 1000) * 1000; // Round to nearest second
                    String label = record.get("label");
                    
                    recordsBySecond.computeIfAbsent(secondTimestamp, k -> new ArrayList<>()).add(record);
                    recordsBySecondAndTransaction.computeIfAbsent(secondTimestamp, k -> new HashMap<>())
                        .computeIfAbsent(label, k -> new ArrayList<>()).add(record);
                    
                    if (hasAllThreads) {
                        String threadName = record.get("allThreads");
                        activeThreadsBySecond.computeIfAbsent(secondTimestamp, k -> new HashSet<>()).add(threadName);
                    }
                } catch (Exception e) {
                    log.warn("Error parsing time series record: {}", e.getMessage());
                }
            }
            
            // Get all unique transaction names
            Set<String> allTransactions = new LinkedHashSet<>();
            for (Map<String, List<CSVRecord>> transactionMap : recordsBySecondAndTransaction.values()) {
                allTransactions.addAll(transactionMap.keySet());
            }
            
            // Calculate metrics for each second
            for (Map.Entry<Long, List<CSVRecord>> entry : recordsBySecond.entrySet()) {
                long timestamp = entry.getKey();
                List<CSVRecord> records = entry.getValue();
                Map<String, List<CSVRecord>> transactionRecords = recordsBySecondAndTransaction.get(timestamp);
                
                // Count active threads
                int activeThreads = hasAllThreads ? 
                    activeThreadsBySecond.getOrDefault(timestamp, new HashSet<>()).size() : 
                    records.size();
                
                // Calculate hits per second (total requests in this second)
                double hitsPerSecond = records.size();
                
                // Add overall data point
                timeSeriesData.addDataPoint(timestamp, activeThreads, hitsPerSecond);
                
                // Calculate per-transaction metrics
                for (String transactionName : allTransactions) {
                    List<CSVRecord> txRecords = transactionRecords.getOrDefault(transactionName, new ArrayList<>());
                    
                    if (!txRecords.isEmpty()) {
                        // Calculate average response time for this transaction
                        double avgResponseTime = txRecords.stream()
                            .mapToLong(r -> {
                                try {
                                    return Long.parseLong(r.get("elapsed"));
                                } catch (Exception e) {
                                    return 0L;
                                }
                            })
                            .average()
                            .orElse(0.0);
                        
                        // Calculate successful transactions per second for this transaction
                        long successfulCount = txRecords.stream()
                            .filter(r -> {
                                try {
                                    return "true".equalsIgnoreCase(r.get("success"));
                                } catch (Exception e) {
                                    return false;
                                }
                            })
                            .count();
                        
                        timeSeriesData.addTransactionResponseTime(transactionName, avgResponseTime);
                        timeSeriesData.addTransactionTPS(transactionName, successfulCount);
                    } else {
                        // Add 0 for transactions with no data in this second
                        timeSeriesData.addTransactionResponseTime(transactionName, 0.0);
                        timeSeriesData.addTransactionTPS(transactionName, 0.0);
                    }
                }
            }
        }
        
        return timeSeriesData;
    }
}
