package com.jmeter.plugin.parser;

import com.jmeter.plugin.model.PerformanceMetrics;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import static org.junit.Assert.*;

public class JTLParserTest {
    
    private JTLParser parser;
    
    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();
    
    @Before
    public void setUp() {
        parser = new JTLParser();
    }
    
    @Test
    public void testParseValidJTL() throws IOException {
        File jtlFile = tempFolder.newFile("valid.jtl");
        try (FileWriter writer = new FileWriter(jtlFile)) {
            writer.write("timeStamp,elapsed,label,responseCode,responseMessage,threadName,dataType,success,failureMessage,bytes,sentBytes,grpThreads,allThreads,URL,Latency,IdleTime,Connect\n");
            writer.write("1234567890,100,Transaction1,200,OK,Thread1,text,true,,1024,512,1,1,http://example.com,50,0,10\n");
            writer.write("1234567891,200,Transaction1,200,OK,Thread1,text,true,,2048,512,1,1,http://example.com,100,0,20\n");
            writer.write("1234567892,150,Transaction2,200,OK,Thread1,text,true,,1536,512,1,1,http://example.com,75,0,15\n");
        }
        
        Map<String, PerformanceMetrics> metrics = parser.parseJTLFile(jtlFile.getAbsolutePath());
        
        assertNotNull("Metrics should not be null", metrics);
        assertEquals("Should have 2 transactions", 2, metrics.size());
        assertTrue("Should contain Transaction1", metrics.containsKey("Transaction1"));
        assertTrue("Should contain Transaction2", metrics.containsKey("Transaction2"));
        
        PerformanceMetrics metric1 = metrics.get("Transaction1");
        assertEquals("Transaction1 should have 2 samples", 2, metric1.getTotalCount());
        assertEquals("Transaction1 should have 0 failures", 0, metric1.getFailureCount());
    }
    
    @Test
    public void testParseJTLWithFailures() throws IOException {
        File jtlFile = tempFolder.newFile("with_failures.jtl");
        try (FileWriter writer = new FileWriter(jtlFile)) {
            writer.write("timeStamp,elapsed,label,responseCode,responseMessage,threadName,dataType,success,failureMessage,bytes,sentBytes,grpThreads,allThreads,URL,Latency,IdleTime,Connect\n");
            writer.write("1234567890,100,Transaction1,200,OK,Thread1,text,true,,1024,512,1,1,http://example.com,50,0,10\n");
            writer.write("1234567891,200,Transaction1,500,Error,Thread1,text,false,Server Error,2048,512,1,1,http://example.com,100,0,20\n");
            writer.write("1234567892,150,Transaction1,200,OK,Thread1,text,true,,1536,512,1,1,http://example.com,75,0,15\n");
        }
        
        Map<String, PerformanceMetrics> metrics = parser.parseJTLFile(jtlFile.getAbsolutePath());
        
        PerformanceMetrics metric = metrics.get("Transaction1");
        assertEquals("Should have 3 samples", 3, metric.getTotalCount());
        assertEquals("Should have 1 failure", 1, metric.getFailureCount());
        assertTrue("Error rate should be greater than 0", metric.getErrorRate() > 0);
    }
    
    @Test
    public void testParseEmptyJTL() throws IOException {
        File jtlFile = tempFolder.newFile("empty.jtl");
        try (FileWriter writer = new FileWriter(jtlFile)) {
            writer.write("timeStamp,elapsed,label,responseCode,responseMessage,threadName,dataType,success,failureMessage,bytes,sentBytes,grpThreads,allThreads,URL,Latency,IdleTime,Connect\n");
        }
        
        Map<String, PerformanceMetrics> metrics = parser.parseJTLFile(jtlFile.getAbsolutePath());
        
        assertNotNull("Metrics should not be null", metrics);
        assertTrue("Metrics should be empty", metrics.isEmpty());
    }
    
    @Test
    public void testParseJTLWithMultipleTransactions() throws IOException {
        File jtlFile = tempFolder.newFile("multiple.jtl");
        try (FileWriter writer = new FileWriter(jtlFile)) {
            writer.write("timeStamp,elapsed,label,responseCode,responseMessage,threadName,dataType,success,failureMessage,bytes,sentBytes,grpThreads,allThreads,URL,Latency,IdleTime,Connect\n");
            writer.write("1234567890,100,Login,200,OK,Thread1,text,true,,1024,512,1,1,http://example.com/login,50,0,10\n");
            writer.write("1234567891,200,Search,200,OK,Thread1,text,true,,2048,512,1,1,http://example.com/search,100,0,20\n");
            writer.write("1234567892,150,Checkout,200,OK,Thread1,text,true,,1536,512,1,1,http://example.com/checkout,75,0,15\n");
            writer.write("1234567893,120,Login,200,OK,Thread2,text,true,,1024,512,2,2,http://example.com/login,60,0,12\n");
        }
        
        Map<String, PerformanceMetrics> metrics = parser.parseJTLFile(jtlFile.getAbsolutePath());
        
        assertEquals("Should have 3 unique transactions", 3, metrics.size());
        assertEquals("Login should have 2 samples", 2, metrics.get("Login").getTotalCount());
        assertEquals("Search should have 1 sample", 1, metrics.get("Search").getTotalCount());
        assertEquals("Checkout should have 1 sample", 1, metrics.get("Checkout").getTotalCount());
    }
    
    @Test
    public void testParseJTLCalculatesStatistics() throws IOException {
        File jtlFile = tempFolder.newFile("statistics.jtl");
        try (FileWriter writer = new FileWriter(jtlFile)) {
            writer.write("timeStamp,elapsed,label,responseCode,responseMessage,threadName,dataType,success,failureMessage,bytes,sentBytes,grpThreads,allThreads,URL,Latency,IdleTime,Connect\n");
            for (int i = 1; i <= 100; i++) {
                writer.write(String.format("123456789%d,%d,Transaction1,200,OK,Thread1,text,true,,1024,512,1,1,http://example.com,%d,0,10\n", 
                    i, i * 10, i * 5));
            }
        }
        
        Map<String, PerformanceMetrics> metrics = parser.parseJTLFile(jtlFile.getAbsolutePath());
        
        PerformanceMetrics metric = metrics.get("Transaction1");
        assertEquals("Should have 100 samples", 100, metric.getTotalCount());
        assertTrue("Average should be calculated", metric.getAverageResponseTime() > 0);
        assertTrue("P90 should be calculated", metric.getPercentile90() > 0);
        assertTrue("P95 should be calculated", metric.getPercentile95() > 0);
        assertTrue("P99 should be calculated", metric.getPercentile99() > 0);
    }
    
    @Test(expected = IOException.class)
    public void testParseNonExistentFile() throws IOException {
        parser.parseJTLFile("/nonexistent/file.jtl");
    }
}
