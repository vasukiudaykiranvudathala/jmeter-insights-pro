# JMeter Insights Pro - Comprehensive Guide

## 📋 Table of Contents

1. [Overview](#overview)
2. [Features](#features)
3. [Installation](#installation)
4. [Usage Methods](#usage-methods)
   - [CLI Usage](#cli-usage)
   - [GUI Usage](#gui-usage)
   - [CI/CD Integration](#cicd-integration)
5. [Report Types](#report-types)
   - [Single Report](#single-report)
   - [Comparison Report](#comparison-report)
6. [AI-Powered Analysis](#ai-powered-analysis)
   - [Supported AI Models](#supported-ai-models)
   - [AI vs Fallback Mode](#ai-vs-fallback-mode)
7. [Advanced Features](#advanced-features)
8. [Troubleshooting](#troubleshooting)

---

## Overview

**JMeter Insights Pro** is a powerful plugin that transforms JMeter test results (JTL files) into beautiful, interactive HTML reports with optional AI-powered performance analysis. It supports both single test analysis and baseline comparison, making it ideal for performance regression testing in CI/CD pipelines.

### Key Capabilities

- 📊 **Beautiful HTML Reports** - Modern, responsive design with interactive charts
- 🤖 **AI-Powered Analysis** - Intelligent insights using OpenAI, Claude, or Ollama
- 📈 **Comparison Mode** - Compare baseline vs current performance with color-coded metrics
- 🎯 **Single Report Mode** - Analyze individual test runs
- 🚀 **CI/CD Ready** - Command-line interface for automation
- 🖥️ **JMeter GUI Integration** - Native listener for interactive use
- 📄 **PDF Export** - Generate printable reports (browser-based)
- 🆓 **Free AI Option** - Use Ollama for local, free AI analysis

---

## Features

### Report Features

✅ **Performance Metrics**
- Average, P90, P95, P99 response times
- Error rates and throughput
- Success/failure counts
- Transaction-level breakdown

✅ **Visual Analytics**
- Interactive Chart.js visualizations
- Color-coded performance indicators
- Sortable, filterable tables
- Responsive design for all devices

✅ **AI Analysis** (Optional)
- Executive summary
- Critical issues identification
- Root cause analysis
- Risk assessment
- Actionable recommendations

✅ **Comparison Analysis**
- Baseline vs current comparison
- Percentage change calculations
- Threshold-based alerting
- Regression detection

---

## Installation

### Prerequisites

- Apache JMeter 5.x or higher
- Java 8 or higher
- Maven (for building from source)

### Build and Install

```bash
# Clone the repository
git clone <repository-url>
cd jmeter-performance-reporter

# Build the plugin
mvn clean package -DskipTests

# Install to JMeter
cp target/jmeter-insights-pro-1.0.0.jar /path/to/jmeter/lib/ext/

# Restart JMeter
```

**Installation Path Examples:**
- macOS: `/Applications/apache-jmeter-5.6.3/lib/ext/`
- Linux: `/opt/jmeter/lib/ext/`
- Windows: `C:\apache-jmeter-5.6.3\lib\ext\`

---

## Usage Methods

### CLI Usage

The command-line interface is perfect for automation, CI/CD pipelines, and batch processing.

#### Basic Syntax

```bash
java -jar jmeter-insights-pro-1.0.0.jar <current.jtl> [baseline.jtl] [output_dir] [threshold] [ai_api_key] [export_pdf] [ai_provider]
```

#### Parameters

| Parameter | Description | Required | Default |
|-----------|-------------|----------|---------|
| `current.jtl` | Current JTL file path | Yes | - |
| `baseline.jtl` | Baseline JTL file path (optional) | No | - |
| `output_dir` | Output directory for reports | No | Current directory |
| `threshold` | Performance degradation threshold (%) | No | 5.0 |
| `ai_api_key` | AI API key or model name | No | From env vars |
| `export_pdf` | Enable PDF export (true/false) | No | false |
| `ai_provider` | AI provider (openai/claude/gemini/ollama) | No | Auto-detect |

#### CLI Examples

**1. Comparison Report with OpenAI**

```bash
java -jar jmeter-insights-pro-1.0.0.jar \
  current.jtl \
  baseline.jtl \
  ./reports \
  5.0 \
  "sk-proj-..." \
  false \
  openai
```

**2. Comparison Report with Claude**

```bash
java -jar jmeter-insights-pro-1.0.0.jar \
  current.jtl \
  baseline.jtl \
  ./reports \
  5.0 \
  "sk-ant-..." \
  false \
  claude
```

**3. Comparison Report with Ollama (Free, Local)**

```bash
# Set up Ollama first
ollama pull llama3.2

# Generate report
java -jar jmeter-insights-pro-1.0.0.jar \
  current.jtl \
  baseline.jtl \
  ./reports \
  5.0 \
  "llama3.2" \
  false \
  ollama
```

**4. Single Report (No Baseline)**

```bash
java -jar jmeter-insights-pro-1.0.0.jar \
  test-results.jtl \
  ./reports \
  5.0 \
  "" \
  false
```

**5. Without AI Analysis**

```bash
java -jar jmeter-insights-pro-1.0.0.jar \
  current.jtl \
  baseline.jtl \
  ./reports \
  5.0 \
  "" \
  false
```

#### Environment Variables

Set API keys via environment variables (recommended for security):

```bash
# OpenAI
export OPENAI_API_KEY="sk-proj-..."

# Claude
export ANTHROPIC_API_KEY="sk-ant-..."

# Gemini (experimental)
export GEMINI_API_KEY="AIza..."

# Run without specifying API key
java -jar jmeter-insights-pro-1.0.0.jar \
  current.jtl baseline.jtl ./reports 5.0 "" false openai
```

**Priority Order:**
1. Command-line argument
2. Environment variable
3. Fallback mode (no AI)

#### CLI Advantages

✅ **Automation** - Perfect for CI/CD pipelines
✅ **Batch Processing** - Process multiple reports
✅ **Scripting** - Easy integration with shell scripts
✅ **Headless** - No GUI required
✅ **Version Control** - Store commands in scripts

---

### GUI Usage

The JMeter GUI integration provides an interactive way to generate reports during test development.

#### Setup Steps

1. **Open JMeter**
2. **Add Listener**
   - Right-click Test Plan → Add → Listener → **JMeter Insights Pro**
3. **Configure Settings**

#### GUI Configuration

**Basic Settings:**

| Field | Description | Example |
|-------|-------------|---------|
| Baseline JTL File | Path to baseline results | `/path/to/baseline.jtl` |
| Current JTL File | Path to current results | `/path/to/current.jtl` |
| Output Directory | Where to save reports | `./reports` |
| Threshold (%) | Performance degradation limit | `5.0` |

**AI Settings:**

| Field | Description | Example |
|-------|-------------|---------|
| AI API Key | Your API key or model name | `sk-proj-...` or `llama3.2` |
| AI Provider | Provider selection | `openai`, `claude`, `ollama` |
| AI Endpoint | Custom endpoint (optional) | Default endpoints used |

**Options:**

- ☑️ **Export PDF** - Enable browser-based PDF export
- ☑️ **Enable AI Analysis** - Generate AI-powered insights

#### GUI Workflow

1. **Configure Test Plan**
   - Add samplers, thread groups, etc.

2. **Add JMeter Insights Pro Listener**
   - Configure baseline and current JTL files
   - Set threshold and AI settings

3. **Run Test**
   - Execute test plan
   - Results saved to JTL file

4. **Generate Report**
   - Click "Generate Report" button
   - Report opens in browser automatically

#### GUI Advantages

✅ **Visual Configuration** - Easy point-and-click setup
✅ **Immediate Feedback** - See results instantly
✅ **Test Development** - Perfect for iterative testing
✅ **No Command Line** - User-friendly interface
✅ **Integrated Workflow** - Part of JMeter test plan

---

### CI/CD Integration

Integrate performance testing into your continuous integration pipeline.

#### Jenkins Pipeline

```groovy
pipeline {
    agent any
    
    environment {
        OPENAI_API_KEY = credentials('openai-api-key')
        JMETER_HOME = '/opt/jmeter'
        REPORTER_JAR = '/opt/jmeter/lib/ext/jmeter-insights-pro-1.0.0.jar'
    }
    
    stages {
        stage('Run JMeter Test') {
            steps {
                sh '''
                    ${JMETER_HOME}/bin/jmeter -n -t test-plan.jmx \
                        -l current-results.jtl
                '''
            }
        }
        
        stage('Generate Performance Report') {
            steps {
                sh '''
                    java -jar ${REPORTER_JAR} \
                        current-results.jtl \
                        baseline-results.jtl \
                        ./reports \
                        5.0 \
                        "" \
                        false \
                        openai
                '''
            }
        }
        
        stage('Publish Report') {
            steps {
                publishHTML([
                    reportDir: 'reports',
                    reportFiles: 'performance-report.html',
                    reportName: 'Performance Report'
                ])
            }
        }
        
        stage('Check Threshold') {
            steps {
                script {
                    // Parse report and fail build if threshold exceeded
                    def report = readFile('reports/performance-report.html')
                    if (report.contains('FAIL_COUNT')) {
                        error('Performance regression detected!')
                    }
                }
            }
        }
    }
    
    post {
        always {
            archiveArtifacts artifacts: 'reports/**', fingerprint: true
        }
    }
}
```

#### GitHub Actions

```yaml
name: Performance Testing

on:
  push:
    branches: [ main ]
  pull_request:
    branches: [ main ]

jobs:
  performance-test:
    runs-on: ubuntu-latest
    
    steps:
    - uses: actions/checkout@v3
    
    - name: Set up JDK
      uses: actions/setup-java@v3
      with:
        java-version: '11'
        distribution: 'temurin'
    
    - name: Install JMeter
      run: |
        wget https://archive.apache.org/dist/jmeter/binaries/apache-jmeter-5.6.3.tgz
        tar -xzf apache-jmeter-5.6.3.tgz
        
    - name: Install JMeter Insights Pro
      run: |
        cp jmeter-insights-pro-1.0.0.jar \
           apache-jmeter-5.6.3/lib/ext/
    
    - name: Run JMeter Test
      run: |
        apache-jmeter-5.6.3/bin/jmeter -n -t test-plan.jmx \
          -l current-results.jtl
    
    - name: Generate Performance Report
      env:
        OPENAI_API_KEY: ${{ secrets.OPENAI_API_KEY }}
      run: |
        java -jar apache-jmeter-5.6.3/lib/ext/jmeter-insights-pro-1.0.0.jar \
          current-results.jtl \
          baseline-results.jtl \
          ./reports \
          5.0 \
          "" \
          false \
          openai
    
    - name: Upload Report
      uses: actions/upload-artifact@v3
      with:
        name: performance-report
        path: reports/
    
    - name: Comment PR with Results
      if: github.event_name == 'pull_request'
      uses: actions/github-script@v6
      with:
        script: |
          const fs = require('fs');
          const report = fs.readFileSync('reports/performance-report.html', 'utf8');
          // Extract summary and post as comment
```

#### GitLab CI

```yaml
performance-test:
  stage: test
  image: openjdk:11
  
  variables:
    JMETER_VERSION: "5.6.3"
  
  before_script:
    - apt-get update && apt-get install -y wget
    - wget https://archive.apache.org/dist/jmeter/binaries/apache-jmeter-${JMETER_VERSION}.tgz
    - tar -xzf apache-jmeter-${JMETER_VERSION}.tgz
    - cp jmeter-insights-pro-1.0.0.jar apache-jmeter-${JMETER_VERSION}/lib/ext/
  
  script:
    - apache-jmeter-${JMETER_VERSION}/bin/jmeter -n -t test-plan.jmx -l current-results.jtl
    - |
      java -jar apache-jmeter-${JMETER_VERSION}/lib/ext/jmeter-insights-pro-1.0.0.jar \
        current-results.jtl \
        baseline-results.jtl \
        ./reports \
        5.0 \
        "${OPENAI_API_KEY}" \
        false \
        openai
  
  artifacts:
    paths:
      - reports/
    expire_in: 30 days
  
  only:
    - main
    - merge_requests
```

#### Docker Integration

```dockerfile
FROM openjdk:11-jre-slim

# Install JMeter
RUN apt-get update && \
    apt-get install -y wget && \
    wget https://archive.apache.org/dist/jmeter/binaries/apache-jmeter-5.6.3.tgz && \
    tar -xzf apache-jmeter-5.6.3.tgz && \
    rm apache-jmeter-5.6.3.tgz

# Copy JMeter Insights Pro
COPY jmeter-insights-pro-1.0.0.jar /apache-jmeter-5.6.3/lib/ext/

# Set working directory
WORKDIR /tests

# Entry point script
COPY entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

ENTRYPOINT ["/entrypoint.sh"]
```

**entrypoint.sh:**
```bash
#!/bin/bash
set -e

# Run JMeter test
/apache-jmeter-5.6.3/bin/jmeter -n -t "$TEST_PLAN" -l current-results.jtl

# Generate report
java -jar /apache-jmeter-5.6.3/lib/ext/jmeter-insights-pro-1.0.0.jar \
  current-results.jtl \
  "${BASELINE_JTL}" \
  ./reports \
  5.0 \
  "" \
  false \
  openai
```

**Usage:**
```bash
docker run -v $(pwd):/tests \
  -e OPENAI_API_KEY="sk-proj-..." \
  -e TEST_PLAN="test-plan.jmx" \
  -e BASELINE_JTL="baseline.jtl" \
  performance-reporter
```

#### CI/CD Advantages

✅ **Automated Testing** - Run on every commit/PR
✅ **Regression Detection** - Catch performance issues early
✅ **Historical Tracking** - Archive reports over time
✅ **Quality Gates** - Fail builds on threshold violations
✅ **Team Visibility** - Share reports with stakeholders

---

## Report Types

### Single Report

Analyzes a single test run without comparison.

#### When to Use

- ✅ First-time performance testing
- ✅ Exploratory testing
- ✅ Baseline establishment
- ✅ Ad-hoc analysis

#### Features

- Performance metrics for all transactions
- Error rate analysis
- Throughput measurements
- Response time percentiles (P90, P95, P99)
- AI-powered insights (optional)

#### Example

```bash
java -jar jmeter-insights-pro-1.0.0.jar \
  test-results.jtl \
  ./reports \
  5.0 \
  "" \
  false
```

**Output:**
- `performance-report.html` - Interactive HTML report
- Metrics table with all transactions
- Performance charts
- AI analysis (if enabled)

#### Single Report Advantages

✅ **Simple Setup** - Only one JTL file needed
✅ **Quick Analysis** - Fast report generation
✅ **Baseline Creation** - Establish performance baseline
✅ **Standalone** - No comparison needed

---

### Comparison Report

Compares baseline performance against current test results.

#### When to Use

- ✅ Regression testing
- ✅ CI/CD pipelines
- ✅ Release validation
- ✅ Performance monitoring
- ✅ Before/after optimization

#### Features

- Side-by-side metric comparison
- Percentage change calculations
- Color-coded performance indicators:
  - 🔴 **Critical** - Exceeds threshold (regression)
  - 🟡 **Warning** - Degraded but within threshold
  - 🟢 **Improved** - Better performance
  - ⚪ **Neutral** - No change
- Threshold-based filtering
- AI-powered regression analysis

#### Metrics Compared

| Metric | Description |
|--------|-------------|
| **AVG Change** | Average response time change |
| **P90 Change** | 90th percentile change |
| **Error % Change** | Error rate change |

#### Example

```bash
java -jar jmeter-insights-pro-1.0.0.jar \
  current-v1.1.jtl \
  baseline-v1.0.jtl \
  ./reports \
  5.0 \
  "" \
  false \
  openai
```

**Output:**
- `performance-comparison-report.html` - Comparison report
- Summary statistics (improved, degraded, critical)
- Detailed comparison table
- AI regression analysis

#### Comparison Report Advantages

✅ **Regression Detection** - Identify performance degradation
✅ **Threshold Alerts** - Automatic issue flagging
✅ **Trend Analysis** - Track performance over time
✅ **Data-Driven Decisions** - Quantify improvements/regressions
✅ **CI/CD Integration** - Automated quality gates

---

## AI-Powered Analysis

### Supported AI Models

#### 1. OpenAI (Recommended)

**Models:** GPT-4, GPT-3.5-turbo

**Pros:**
- ✅ Excellent analysis quality
- ✅ Fast response times
- ✅ Reliable and stable
- ✅ $5 free credit for new users

**Cons:**
- ❌ Requires API key
- ❌ Usage-based pricing

**Setup:**
```bash
# Get API key from https://platform.openai.com/api-keys
export OPENAI_API_KEY="sk-proj-..."

# Use in CLI
java -jar jmeter-insights-pro-1.0.0.jar \
  current.jtl baseline.jtl ./reports 5.0 "" false openai
```

**Cost:** ~$0.01-0.05 per report

---

#### 2. Claude (Anthropic)

**Models:** Claude 3 Sonnet, Claude 3 Opus

**Pros:**
- ✅ Best-in-class analysis quality
- ✅ Detailed, nuanced insights
- ✅ Excellent reasoning
- ✅ Long context window

**Cons:**
- ❌ Requires API key
- ❌ Slightly slower than OpenAI
- ❌ Higher cost

**Setup:**
```bash
# Get API key from https://console.anthropic.com/
export ANTHROPIC_API_KEY="sk-ant-..."

# Use in CLI
java -jar jmeter-insights-pro-1.0.0.jar \
  current.jtl baseline.jtl ./reports 5.0 "" false claude
```

**Cost:** ~$0.05-0.15 per report

---

#### 3. Ollama (Free, Local)

**Models:** llama3.2, llama3.1, mistral, codellama, etc.

**Pros:**
- ✅ **100% FREE** - No API costs
- ✅ **Private** - Data stays local
- ✅ **No API key needed**
- ✅ **Unlimited usage**
- ✅ **Offline capable**

**Cons:**
- ❌ Requires local installation
- ❌ Slower than cloud APIs
- ❌ Lower quality than GPT-4/Claude
- ❌ Requires decent hardware (8GB+ RAM)

**Setup:**
```bash
# Install Ollama (macOS)
brew install ollama

# Or download from https://ollama.ai

# Pull a model
ollama pull llama3.2

# Start Ollama service
ollama serve

# Use in CLI
java -jar jmeter-insights-pro-1.0.0.jar \
  current.jtl baseline.jtl ./reports 5.0 "llama3.2" false ollama
```

**Recommended Models:**
- `llama3.2` - Best balance (3B params, fast)
- `llama3.1` - Higher quality (8B params, slower)
- `mistral` - Good alternative (7B params)

**Cost:** FREE (hardware only)

---

#### 4. Google Gemini (Experimental)

**Status:** ⚠️ Experimental - Not recommended

**Models:** gemini-1.5-flash, gemini-pro

**Issues:**
- Model availability problems
- Frequent API changes
- Regional restrictions

**Recommendation:** Use OpenAI or Claude instead

---

### AI vs Fallback Mode

#### With AI Analysis

**What You Get:**

1. **Executive Summary**
   - High-level performance overview
   - Key findings and trends
   - Business impact assessment

2. **Critical Issues**
   - Prioritized list of problems
   - Severity classification
   - Immediate action items

3. **Root Cause Analysis**
   - Potential causes of issues
   - Technical explanations
   - System bottleneck identification

4. **Risk Assessment**
   - Impact on users/business
   - Scalability concerns
   - Production readiness

5. **Actionable Recommendations**
   - Specific optimization steps
   - Configuration tuning suggestions
   - Architecture improvements

**Example AI Output:**
```
CRITICAL ISSUES DETECTED

Executive Summary:
The current test shows significant performance degradation compared to baseline,
with average response times increasing by 42.3%. Three critical transactions
exceed the 5% threshold, indicating potential scalability issues.

Critical Issues:
1. SOW_SO_T06_NewRelic_Load_Observability_Analysis (+42.33%)
   - Severity: CRITICAL
   - Impact: High user-facing latency
   - Action: Investigate database query optimization

2. SOW_SO_T09_NewRelic_Load_MYSQL (+9.80%)
   - Severity: WARNING
   - Impact: Backend processing delays
   - Action: Review connection pool settings

Root Cause Analysis:
The performance degradation appears to be related to increased database
query times, possibly due to missing indexes or inefficient queries...

Recommendations:
1. Add database indexes on frequently queried columns
2. Implement query result caching
3. Optimize N+1 query patterns
4. Consider database connection pool tuning
```

---

#### Without AI (Fallback Mode)

**What You Get:**

- ✅ All performance metrics
- ✅ Comparison tables
- ✅ Color-coded indicators
- ✅ Interactive charts
- ✅ Threshold alerts

**What You Don't Get:**

- ❌ AI-generated insights
- ❌ Root cause analysis
- ❌ Recommendations
- ❌ Executive summary
- ❌ Risk assessment

**Fallback Summary:**
```
Performance Comparison Summary

Total Transactions: 5
Critical Issues: 2 (exceed 5.0% threshold)
Warnings: 1 (degraded but within threshold)
Improved: 0
Neutral: 2

Please review the detailed metrics table for specific performance data.
```

---

### AI Feature Comparison

| Feature | With AI | Without AI |
|---------|---------|------------|
| Performance Metrics | ✅ | ✅ |
| Comparison Tables | ✅ | ✅ |
| Color Coding | ✅ | ✅ |
| Charts | ✅ | ✅ |
| Executive Summary | ✅ | ❌ |
| Root Cause Analysis | ✅ | ❌ |
| Recommendations | ✅ | ❌ |
| Risk Assessment | ✅ | ❌ |
| Prioritization | ✅ | ❌ |

---

### When to Use AI

**Use AI When:**
- ✅ You need actionable insights
- ✅ Team lacks performance expertise
- ✅ Complex performance issues
- ✅ Stakeholder presentations
- ✅ Production incident analysis

**Skip AI When:**
- ✅ Quick metric checks
- ✅ No API key available
- ✅ Cost constraints
- ✅ Privacy/security requirements
- ✅ Offline environments

---

## Advanced Features

### PDF Export

Generate printable PDF reports using browser print functionality.

**Enable in CLI:**
```bash
java -jar jmeter-insights-pro-1.0.0.jar \
  current.jtl baseline.jtl ./reports 5.0 "" true openai
```

**Enable in GUI:**
- ☑️ Check "Export PDF" option

**How to Export:**
1. Open HTML report in browser
2. Click "⎙ Export PDF" button
3. Use browser print dialog
4. Select "Save as PDF"

---

### Custom Thresholds

Set performance degradation thresholds based on your requirements.

**Examples:**
- **Strict:** `2.0` - Flag 2% degradation
- **Standard:** `5.0` - Flag 5% degradation (default)
- **Relaxed:** `10.0` - Flag 10% degradation

**Impact:**
- Affects color coding (critical vs warning)
- Filters "Critical Issues" table
- Influences AI analysis severity

---

### Filtering and Sorting

**Table Features:**
- 🔍 **Search** - Filter transactions by name
- ⬆️⬇️ **Sort** - Click column headers to sort
- 🎨 **Color Coding** - Visual performance indicators

---

### Multiple Reports

Generate reports for multiple test runs:

```bash
#!/bin/bash
for test in test1 test2 test3; do
  java -jar jmeter-insights-pro-1.0.0.jar \
    ${test}-results.jtl \
    baseline.jtl \
    ./reports/${test} \
    5.0 \
    "" \
    false \
    openai
done
```

---

## Troubleshooting

### Common Issues

#### 1. "No such file or directory"

**Problem:** JTL file not found

**Solution:**
```bash
# Use absolute paths
java -jar jmeter-insights-pro-1.0.0.jar \
  /full/path/to/current.jtl \
  /full/path/to/baseline.jtl \
  /full/path/to/reports \
  5.0
```

---

#### 2. "NumberFormatException"

**Problem:** Incorrect parameter order

**Solution:** Ensure all parameters are in correct order:
```bash
java -jar jmeter-insights-pro-1.0.0.jar \
  current.jtl \       # 1. Current (required)
  baseline.jtl \      # 2. Baseline (optional)
  ./reports \         # 3. Output dir
  5.0 \               # 4. Threshold
  "" \                # 5. API key
  false \             # 6. Export PDF
  openai              # 7. AI provider
```

---

#### 3. AI API Errors

**Problem:** API call failed

**Solutions:**

**OpenAI:**
```bash
# Check API key
echo $OPENAI_API_KEY

# Verify key format (starts with sk-proj-)
# Get new key: https://platform.openai.com/api-keys
```

**Claude:**
```bash
# Check API key
echo $ANTHROPIC_API_KEY

# Verify key format (starts with sk-ant-)
# Get new key: https://console.anthropic.com/
```

**Ollama:**
```bash
# Check if Ollama is running
curl http://localhost:11434/api/tags

# Start Ollama
ollama serve

# Pull model if needed
ollama pull llama3.2
```

---

#### 4. Empty or Corrupted JTL Files

**Problem:** Report generation fails

**Solution:**
- Ensure JTL files are valid CSV format
- Check file size (not empty)
- Verify JMeter test completed successfully
- Use JMeter's "View Results Tree" to validate

---

#### 5. Out of Memory

**Problem:** Java heap space error

**Solution:**
```bash
# Increase heap size
java -Xmx2g -jar jmeter-insights-pro-1.0.0.jar ...
```

---

### Getting Help

- 📖 **Documentation:** Check existing docs (BUILD_AND_INSTALL.md, OLLAMA_SETUP.md)
- 🐛 **Issues:** Report bugs on GitHub
- 💬 **Discussions:** Ask questions in discussions
- 📧 **Support:** Contact maintainers

---

## Quick Reference

### CLI Cheat Sheet

```bash
# Comparison with OpenAI
java -jar jmeter-insights-pro-1.0.0.jar current.jtl baseline.jtl ./reports 5.0 "" false openai

# Comparison with Ollama (free)
java -jar jmeter-insights-pro-1.0.0.jar current.jtl baseline.jtl ./reports 5.0 "llama3.2" false ollama

# Single report (no baseline)
java -jar jmeter-insights-pro-1.0.0.jar test.jtl ./reports 5.0 "" false

# Without AI
java -jar jmeter-insights-pro-1.0.0.jar current.jtl baseline.jtl ./reports 5.0 "" false

# With PDF export
java -jar jmeter-insights-pro-1.0.0.jar current.jtl baseline.jtl ./reports 5.0 "" true openai
```

### Environment Variables

```bash
export OPENAI_API_KEY="sk-proj-..."
export ANTHROPIC_API_KEY="sk-ant-..."
export GEMINI_API_KEY="AIza..."  # Not recommended
```

### AI Model Recommendations

| Use Case | Recommended Model | Why |
|----------|------------------|-----|
| **Best Quality** | Claude 3 Opus | Most detailed analysis |
| **Best Value** | OpenAI GPT-3.5 | Good quality, low cost |
| **Free/Private** | Ollama llama3.2 | No cost, local |
| **Fast** | OpenAI GPT-3.5 | Quick responses |
| **Offline** | Ollama | No internet needed |

---

## Summary

JMeter Insights Pro is a versatile tool that adapts to your workflow:

- 🖥️ **GUI** - Interactive testing and development
- 💻 **CLI** - Automation and CI/CD
- 🤖 **AI** - Intelligent insights (OpenAI, Claude, Ollama)
- 📊 **Reports** - Single or comparison analysis
- 🆓 **Free Option** - Use Ollama for zero-cost AI
- 🔄 **Fallback** - Works without AI

Choose the method that fits your needs and start generating beautiful, insightful performance reports today!

---

**Version:** 1.0.0  
**Last Updated:** March 2026  
**License:** MIT
