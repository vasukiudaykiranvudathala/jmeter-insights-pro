# Changelog

All notable changes to JMeter Insights Pro will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.0] - 2026-03-18

### Added
- 🎉 Initial release of JMeter Insights Pro
- 🤖 Multi-AI provider support: OpenAI, Claude, Gemini, and Ollama (free & local!)
- 📊 Beautiful, interactive HTML reports with Chart.js visualizations
- 📈 Comparison mode for baseline vs current performance analysis
- 🎨 Color-coded performance indicators (Critical, Warning, Improved, Neutral)
- 💻 Command-line interface (CLI) for CI/CD integration
- 🖥️ JMeter GUI integration as native listener
- 📄 PDF export capability
- 🆓 Free local AI option via Ollama (no API key required)
- 🔍 Sortable and filterable performance metrics tables
- ⚙️ Configurable performance degradation thresholds
- 📉 Regression detection with threshold-based alerting
- 🌐 Responsive design for all devices
- 📝 Comprehensive documentation and examples
- 🔄 CI/CD pipeline examples (Jenkins, GitHub Actions, GitLab)
- 🎯 Advanced metrics: P90, P95, P99 percentiles
- 📊 Intelligent throughput formatting (/sec, /min, /hour)
- 🎨 4-tab interactive UI (Overview, Metrics, Charts, AI Analysis)

### Features
- **AI Analysis**
  - Executive summary generation
  - Critical issues identification
  - Root cause analysis
  - Risk assessment
  - Actionable recommendations
  - Fallback mode when AI is unavailable
  - Detailed error logging for troubleshooting

- **Report Types**
  - Single test report analysis
  - Baseline comparison reports
  - Performance metrics (Avg, P90, P95, P99)
  - Error rate tracking with visual indicators
  - Throughput analysis with intelligent formatting
  - 4-tab interactive interface (Overview, Metrics, Charts, AI Analysis)

- **Supported AI Providers**
  - OpenAI (gpt-4, gpt-4-turbo, gpt-3.5-turbo, gpt-4o, gpt-4o-mini)
  - Anthropic Claude (claude-3-5-sonnet, claude-3-opus, claude-3-sonnet, claude-3-haiku)
  - Ollama (llama3.2, llama3.1, llama2, mistral, codellama, phi, gemma, qwen)
  - Google Gemini (gemini-1.5-pro, gemini-1.5-flash, gemini-pro)

- **GUI Improvements**
  - Dropdown selection for AI providers (prevents typos)
  - Editable model combo box with recommended models
  - Custom model name support (future-proof for new models)
  - Automatic model list update when provider changes
  - Backward compatibility with old configurations

- **Integration**
  - Jenkins pipeline support
  - GitHub Actions workflows
  - GitLab CI/CD
  - Docker containerization
  - Environment variable configuration

### Documentation
- Comprehensive usage guide
- CLI reference and examples
- GUI setup instructions
- CI/CD integration guides
- Ollama setup documentation
- Troubleshooting guide
- Publishing guide for JMeter Plugin Manager

### Technical
- Java 11+ support
- JMeter 5.0+ compatibility (tested with 5.6.3)
- Maven-based build system
- Shaded JAR with all dependencies
- Proper plugin descriptor files for JMeter Plugin Manager
- Unit tests with JaCoCo coverage

---

## Future Releases

### Planned for v1.1.0
- [ ] **APM/Observability Integration** - Correlate performance degradation with infrastructure metrics
  - New Relic integration (CPU, Memory, GC, Threads)
  - Prometheus integration
  - Automatic metric correlation (±5 min window)
  - Enhanced root cause analysis with APM context
- [ ] Additional chart types (scatter plots, heatmaps)
- [ ] Custom report templates
- [ ] Email notification support
- [ ] Historical trend analysis
- [ ] Performance budgets
- [ ] Custom AI prompts
- [ ] Expanded unit test coverage

### Planned for v2.0.0
- [ ] Real-time monitoring dashboard
- [ ] Database storage for historical data
- [ ] Multi-test comparison (3+ tests)
- [ ] Advanced filtering and grouping
- [ ] Custom metrics support
- [ ] API for programmatic access

---

[1.0.0]: https://github.com/vasukivudathala/jmeter-insights-pro/releases/tag/v1.0.0
