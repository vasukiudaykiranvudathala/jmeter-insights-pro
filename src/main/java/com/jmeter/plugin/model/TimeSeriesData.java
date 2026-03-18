package com.jmeter.plugin.model;

import java.util.*;

public class TimeSeriesData {
    private List<Long> timestamps;
    private Map<String, List<Double>> responseTimesByTransaction;
    private List<Integer> activeThreadsOverTime;
    private List<Double> hitsPerSecond;
    private Map<String, List<Double>> transactionsPerSecondByTransaction;
    
    public TimeSeriesData() {
        this.timestamps = new ArrayList<>();
        this.responseTimesByTransaction = new LinkedHashMap<>();
        this.activeThreadsOverTime = new ArrayList<>();
        this.hitsPerSecond = new ArrayList<>();
        this.transactionsPerSecondByTransaction = new LinkedHashMap<>();
    }
    
    public void addDataPoint(long timestamp, int activeThreads, double hits) {
        timestamps.add(timestamp);
        activeThreadsOverTime.add(activeThreads);
        hitsPerSecond.add(hits);
    }
    
    public void addTransactionResponseTime(String transactionName, double responseTime) {
        responseTimesByTransaction.computeIfAbsent(transactionName, k -> new ArrayList<>()).add(responseTime);
    }
    
    public void addTransactionTPS(String transactionName, double tps) {
        transactionsPerSecondByTransaction.computeIfAbsent(transactionName, k -> new ArrayList<>()).add(tps);
    }
    
    public List<Long> getTimestamps() {
        return timestamps;
    }
    
    public Map<String, List<Double>> getResponseTimesByTransaction() {
        return responseTimesByTransaction;
    }
    
    public List<Integer> getActiveThreadsOverTime() {
        return activeThreadsOverTime;
    }
    
    public List<Double> getHitsPerSecond() {
        return hitsPerSecond;
    }
    
    public Map<String, List<Double>> getTransactionsPerSecondByTransaction() {
        return transactionsPerSecondByTransaction;
    }
}
