package com.jmeter.plugin.parser;

import com.jmeter.plugin.model.PerformanceMetrics;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

public class AggregateJTLParser {
    private static final Logger log = LoggerFactory.getLogger(AggregateJTLParser.class);

    public Map<String, PerformanceMetrics> parseAggregateJTL(String filePath) throws IOException {
        Map<String, PerformanceMetrics> metricsMap = new HashMap<>();
        
        try (Reader reader = new FileReader(filePath);
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {
            
            for (CSVRecord record : csvParser) {
                try {
                    String label = record.get("Label");
                    int samples = Integer.parseInt(record.get("# Samples"));
                    double average = Double.parseDouble(record.get("Average"));
                    double median = Double.parseDouble(record.get("Median"));
                    double p90 = Double.parseDouble(record.get("90% Line"));
                    double p95 = Double.parseDouble(record.get("95% Line"));
                    double p99 = Double.parseDouble(record.get("99% Line"));
                    double min = Double.parseDouble(record.get("Min"));
                    double max = Double.parseDouble(record.get("Max"));
                    String errorPercentStr = record.get("Error %").replace("%", "");
                    double errorPercent = Double.parseDouble(errorPercentStr);
                    double throughput = Double.parseDouble(record.get("Throughput"));
                    
                    PerformanceMetrics metrics = new PerformanceMetrics(label);
                    metrics.setFromAggregateData(samples, average, median, p90, p95, p99, 
                                                 min, max, errorPercent, throughput);
                    
                    metricsMap.put(label, metrics);
                    
                } catch (Exception e) {
                    log.warn("Error parsing aggregate record: {}", e.getMessage());
                }
            }
        }
        
        return metricsMap;
    }
}
