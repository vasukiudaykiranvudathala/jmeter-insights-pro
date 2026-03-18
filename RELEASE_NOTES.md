# JMeter Insights Pro - Release Notes

## Version 1.0.0 (March 18, 2026)

### 🎉 Initial Release

JMeter Insights Pro is a comprehensive performance analysis plugin that brings AI-powered insights to your JMeter testing workflow.

### ✨ Key Features

#### Multi-AI Provider Support
- **OpenAI**: GPT-4, GPT-4-Turbo, GPT-3.5-Turbo, GPT-4o
- **Claude**: Claude-3-5-Sonnet, Claude-3-Opus, Claude-3-Haiku
- **Gemini**: Gemini-1.5-Pro, Gemini-1.5-Flash
- **Ollama**: Free & local AI (llama3.2, mistral, codellama, and more)

#### Interactive HTML Reports
- **4-Tab Interface**: Overview, Metrics, Charts, AI Analysis
- **Advanced Metrics**: Average, P90, P95, P99 percentiles
- **Intelligent Formatting**: Automatic throughput display (/sec, /min, /hour)
- **Visual Indicators**: Color-coded performance status (Critical, Warning, Improved)
- **Interactive Charts**: Response time, throughput, error rate visualizations

#### Comparison & Analysis
- **Baseline Comparison**: Compare current vs baseline performance
- **Regression Detection**: Configurable threshold-based alerting
- **Error Analysis**: Detailed error rate tracking and visualization
- **AI-Powered Insights**: Automated root cause analysis and recommendations

#### Integration & Export
- **JMeter GUI Integration**: Native listener plugin
- **Command-Line Interface**: Full CLI support for CI/CD
- **PDF Export**: Generate print-friendly PDF reports
- **Cross-Platform**: Works on Windows, macOS, and Linux

### 🔧 Installation

#### Via JMeter Plugins Manager (Recommended)
1. Install JMeter Plugins Manager
2. Open JMeter → Options → Plugins Manager
3. Search for "JMeter Insights Pro"
4. Click Install and restart JMeter

#### Manual Installation
1. Download `jmeter-insights-pro-1.0.0.jar`
2. Copy to `$JMETER_HOME/lib/ext/`
3. Restart JMeter

### 📊 What's New in 1.0.0

#### GUI Improvements
- ✅ Dropdown selection for AI providers (prevents typos)
- ✅ Editable model combo box with recommended models
- ✅ Custom model name support (future-proof)
- ✅ Automatic model list update when provider changes
- ✅ Backward compatibility with old configurations

#### Report Enhancements
- ✅ Accurate throughput calculation matching JMeter Aggregate Report
- ✅ Intelligent decimal formatting (removes .00 from whole numbers)
- ✅ Comprehensive error rate detection (>1% threshold)
- ✅ 4-tab interactive UI with proper content rendering
- ✅ Increased AI token limit (1200 → 2500) for complete summaries

#### AI & Analysis
- ✅ Multi-provider support (OpenAI, Claude, Gemini, Ollama)
- ✅ Detailed error logging for troubleshooting
- ✅ Fallback mode when AI is unavailable
- ✅ Enhanced prompts for better analysis quality

### 🚀 Quick Start

#### Using Ollama (Free & Local)
```bash
# Install Ollama
curl -fsSL https://ollama.ai/install.sh | sh

# Pull a model
ollama pull llama3.2

# Start Ollama
ollama serve
```

In JMeter:
1. Add JMeter Insights Pro listener
2. Set AI Provider: **ollama**
3. Set AI Model: **llama3.2 (Recommended)**
4. Leave AI API Key empty
5. Run your test

#### Using OpenAI
1. Get API key from [OpenAI Platform](https://platform.openai.com/)
2. Set AI Provider: **openai**
3. Set AI Model: **gpt-4 (Recommended)**
4. Enter your API key
5. Run your test

### 📝 Requirements

- **Java**: 11 or higher
- **JMeter**: 5.4 or higher (tested with 5.6.3)
- **Maven**: 3.6 or higher (for building from source)

### 🐛 Known Issues

None reported in this release.

### 📚 Documentation

- [README.md](README.md) - Complete usage guide
- [docs/OLLAMA_SETUP.md](docs/OLLAMA_SETUP.md) - Ollama setup guide
- [docs/COMPREHENSIVE_GUIDE.md](docs/COMPREHENSIVE_GUIDE.md) - Detailed documentation
- [CHANGELOG.md](CHANGELOG.md) - Version history

### 🤝 Contributing

Contributions are welcome! Please see [docs/CONTRIBUTING.md](docs/CONTRIBUTING.md) for guidelines.

### 📄 License

Apache License 2.0 - See [LICENSE](LICENSE) for details.

### 🙏 Acknowledgments

- Apache JMeter team for the excellent testing framework
- Chart.js for beautiful visualizations
- OpenAI, Anthropic, Google, and Ollama for AI capabilities
- JMeter Plugins community for inspiration

### 📞 Support

- **Issues**: [GitHub Issues](https://github.com/vasukiudaykiranvudathala/jmeter-insights-pro/issues)
- **Discussions**: [GitHub Discussions](https://github.com/vasukiudaykiranvudathala/jmeter-insights-pro/discussions)
- **Email**: vvudaykiran@gmail.com

---

**Enjoy AI-powered performance testing with JMeter Insights Pro!** 🚀
