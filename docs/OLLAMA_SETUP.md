# Ollama AI Integration Guide - 100% FREE Local AI!

## 🎉 **Ollama Support Added - Completely Free!**

JMeter Insights Pro now supports **Ollama** for **100% free, local AI analysis**!

**No API keys, no cloud services, no costs - just install and run!**

---

## ✅ **Why Use Ollama?**

### **Advantages**
- ✅ **100% FREE** - No API keys, no subscriptions, no costs
- ✅ **Privacy** - All data stays on your machine
- ✅ **Offline** - Works without internet connection
- ✅ **No Rate Limits** - Generate unlimited reports
- ✅ **Fast** - Runs locally on your hardware
- ✅ **Multiple Models** - Choose from dozens of open-source models

### **Comparison**

| Feature | Ollama | OpenAI | Claude |
|---------|--------|--------|--------|
| **Cost** | ✅ FREE | $0.50/1M tokens | $3.00/1M tokens |
| **Privacy** | ✅ 100% Local | ❌ Cloud | ❌ Cloud |
| **Internet Required** | ❌ No | ✅ Yes | ✅ Yes |
| **Rate Limits** | ❌ None | ✅ Yes | ✅ Yes |
| **Setup** | ⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ |
| **Quality** | ⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ |

---

## 🚀 **Quick Start (3 Steps)**

### **Step 1: Install Ollama**

**macOS:**
```bash
brew install ollama
```

**Linux:**
```bash
curl -fsSL https://ollama.com/install.sh | sh
```

**Windows:**
Download from: https://ollama.com/download

### **Step 2: Start Ollama & Pull a Model**

```bash
# Start Ollama service
ollama serve

# In a new terminal, pull a model (choose one):
ollama pull llama3.2        # Recommended - Fast, good quality (2GB)
ollama pull llama3.2:1b     # Smallest - Very fast (1.3GB)
ollama pull mistral         # Alternative - Good quality (4GB)
ollama pull llama3.1:8b     # Best quality - Slower (4.7GB)
```

### **Step 3: Use with JMeter Reporter**

**JMeter GUI:**
1. Add JMeter Insights Pro listener
2. Set **AI Provider**: `ollama`
3. Set **AI API Key**: `llama3.2` (or your model name)
4. Generate report!

**Command Line:**
```bash
java -jar jmeter-performance-reporter.jar \
  current.jtl baseline.jtl ./reports 5.0 \
  "llama3.2" false ollama
```

**That's it!** No API keys, no cloud setup, completely free! 🎉

---

## 📖 **Detailed Setup**

### **Installing Ollama**

#### **macOS**
```bash
# Using Homebrew (recommended)
brew install ollama

# Or download from website
# Visit: https://ollama.com/download
```

#### **Linux**
```bash
# Automated install script
curl -fsSL https://ollama.com/install.sh | sh

# Or manual install
# Visit: https://github.com/ollama/ollama/blob/main/docs/linux.md
```

#### **Windows**
1. Download installer from: https://ollama.com/download
2. Run the installer
3. Ollama will start automatically

### **Starting Ollama**

**macOS/Linux:**
```bash
# Start in background
ollama serve &

# Or start in foreground (for debugging)
ollama serve
```

**Windows:**
- Ollama starts automatically as a service
- Check system tray for Ollama icon

**Verify it's running:**
```bash
curl http://localhost:11434
# Should return: "Ollama is running"
```

---

## 🤖 **Choosing a Model**

### **Recommended Models**

#### **llama3.2 (Recommended)**
```bash
ollama pull llama3.2
```
- **Size:** 2GB
- **Speed:** Fast
- **Quality:** Excellent
- **Best for:** Most users

#### **llama3.2:1b (Fastest)**
```bash
ollama pull llama3.2:1b
```
- **Size:** 1.3GB
- **Speed:** Very fast
- **Quality:** Good
- **Best for:** Quick reports, older hardware

#### **mistral (Alternative)**
```bash
ollama pull mistral
```
- **Size:** 4GB
- **Speed:** Medium
- **Quality:** Excellent
- **Best for:** Detailed analysis

#### **llama3.1:8b (Best Quality)**
```bash
ollama pull llama3.1:8b
```
- **Size:** 4.7GB
- **Speed:** Slower
- **Quality:** Outstanding
- **Best for:** Production reports, powerful hardware

### **List Available Models**
```bash
# See all models you've downloaded
ollama list

# See all available models online
ollama search llama
```

---

## 💻 **Using Ollama with JMeter Reporter**

### **Method 1: JMeter GUI**

1. **Add JMeter Insights Pro Listener**
2. **Configure Settings:**
   - **AI API Key:** `llama3.2` (or your model name)
   - **AI Provider:** `ollama`
   - **Baseline File:** (optional) for comparison
   - **Output Directory:** Where to save reports
3. **Run Test or Generate Report**

### **Method 2: Command Line**

**Basic Usage:**
```bash
java -jar jmeter-performance-reporter.jar \
  current.jtl baseline.jtl ./reports 5.0 \
  "llama3.2" false ollama
```

**Parameters:**
1. `current.jtl` - Current test results
2. `baseline.jtl` - Baseline for comparison
3. `./reports` - Output directory
4. `5.0` - Threshold percentage
5. `"llama3.2"` - Model name (instead of API key)
6. `false` - Export to PDF (true/false)
7. `ollama` - AI provider

**Using Different Model:**
```bash
java -jar jmeter-performance-reporter.jar \
  current.jtl baseline.jtl ./reports 5.0 \
  "mistral" false ollama
```

**No Model Specified (uses default):**
```bash
# Will use llama3.2 by default
java -jar jmeter-performance-reporter.jar \
  current.jtl baseline.jtl ./reports 5.0 \
  "" false ollama
```

---

## 🔧 **Configuration**

### **Custom Ollama Endpoint**

If Ollama is running on a different host/port:

**JMeter GUI:**
- **AI Endpoint:** `http://your-server:11434/api/generate`
- **AI Provider:** `ollama`

**Command Line:**
```bash
# Set custom endpoint via environment variable
export OLLAMA_HOST=http://your-server:11434

# Or specify in code (requires modification)
```

### **Model Parameters**

The plugin uses these default settings:
- **Temperature:** 0.7 (creativity level)
- **Max Tokens:** 1200 (response length)
- **Stream:** false (wait for complete response)

These are optimized for performance analysis and don't need changing.

---

## 📊 **Performance Tips**

### **For Fast Reports**
```bash
# Use smallest model
ollama pull llama3.2:1b

# Use in reports
java -jar jmeter-performance-reporter.jar \
  current.jtl baseline.jtl ./reports 5.0 \
  "llama3.2:1b" false ollama
```

### **For Best Quality**
```bash
# Use largest model
ollama pull llama3.1:8b

# Use in reports
java -jar jmeter-performance-reporter.jar \
  current.jtl baseline.jtl ./reports 5.0 \
  "llama3.1:8b" false ollama
```

### **Hardware Requirements**

| Model | RAM | GPU | Speed |
|-------|-----|-----|-------|
| llama3.2:1b | 4GB | Optional | ⚡⚡⚡ |
| llama3.2 | 8GB | Optional | ⚡⚡ |
| mistral | 8GB | Recommended | ⚡⚡ |
| llama3.1:8b | 16GB | Recommended | ⚡ |

**GPU Acceleration:**
- Ollama automatically uses GPU if available (NVIDIA, AMD, Apple Silicon)
- Significantly faster with GPU
- Still works fine on CPU only

---

## 🐛 **Troubleshooting**

### **Error: "Connection refused"**
**Cause:** Ollama is not running

**Solution:**
```bash
# Start Ollama
ollama serve

# Verify it's running
curl http://localhost:11434
```

### **Error: "Model not found"**
**Cause:** Model not downloaded

**Solution:**
```bash
# Pull the model
ollama pull llama3.2

# Verify it's available
ollama list
```

### **Slow Performance**
**Cause:** Model too large for your hardware

**Solution:**
```bash
# Use smaller model
ollama pull llama3.2:1b

# Update your reports to use it
```

### **Out of Memory**
**Cause:** Not enough RAM for the model

**Solution:**
1. Use smaller model (llama3.2:1b)
2. Close other applications
3. Increase system swap space

---

## 🆚 **Ollama vs Cloud AI**

### **When to Use Ollama**
✅ You want **zero cost**
✅ You need **privacy** (sensitive data)
✅ You want to work **offline**
✅ You generate **many reports** (no rate limits)
✅ You have **decent hardware** (8GB+ RAM)

### **When to Use OpenAI/Claude**
✅ You need **absolute best quality**
✅ You have **limited hardware**
✅ You want **zero setup**
✅ You generate **few reports** (cost not an issue)

---

## 📝 **Example Workflows**

### **Workflow 1: Quick Local Analysis**
```bash
# Start Ollama (once)
ollama serve &

# Pull model (once)
ollama pull llama3.2

# Generate reports (unlimited)
java -jar jmeter-performance-reporter.jar \
  test1.jtl baseline.jtl ./reports 5.0 \
  "llama3.2" false ollama

java -jar jmeter-performance-reporter.jar \
  test2.jtl baseline.jtl ./reports 5.0 \
  "llama3.2" false ollama
```

### **Workflow 2: CI/CD Integration**
```yaml
# .github/workflows/performance-test.yml
- name: Start Ollama
  run: |
    ollama serve &
    sleep 5
    ollama pull llama3.2

- name: Run Performance Test
  run: jmeter -n -t test.jmx -l results.jtl

- name: Generate AI Report
  run: |
    java -jar jmeter-performance-reporter.jar \
      results.jtl baseline.jtl ./reports 5.0 \
      "llama3.2" true ollama
```

### **Workflow 3: Batch Processing**
```bash
#!/bin/bash
# process_all_tests.sh

ollama serve &
sleep 5

for jtl in tests/*.jtl; do
  echo "Processing $jtl..."
  java -jar jmeter-performance-reporter.jar \
    "$jtl" baseline.jtl ./reports 5.0 \
    "llama3.2" false ollama
done
```

---

## 🎯 **Summary**

**Ollama is the best choice if you want:**
- ✅ **Free** AI analysis
- ✅ **Private** data processing
- ✅ **Offline** capability
- ✅ **Unlimited** reports

**Setup is simple:**
1. Install Ollama
2. Pull a model
3. Set provider to `ollama`
4. Generate reports!

**No API keys, no cloud, no cost - just AI-powered performance analysis!** 🚀

---

## 🔗 **Resources**

- **Ollama Website:** https://ollama.com
- **Ollama GitHub:** https://github.com/ollama/ollama
- **Model Library:** https://ollama.com/library
- **Documentation:** https://github.com/ollama/ollama/tree/main/docs

---

**Last Updated:** March 14, 2026

**Status:** Ollama integration is fully supported and recommended for free, local AI analysis!
