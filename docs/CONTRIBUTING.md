# Contributing to JMeter Insights Pro

Thank you for your interest in contributing to JMeter Insights Pro! This document provides guidelines and instructions for contributing to the project.

---

## 🤝 How to Contribute

We welcome contributions in many forms:
- 🐛 Bug reports
- 💡 Feature requests
- 📝 Documentation improvements
- 🔧 Code contributions
- 🧪 Test cases
- 🌍 Translations

---

## 📋 Getting Started

### Prerequisites

- Java 11 or higher
- Maven 3.6 or higher
- JMeter 5.4+ (for testing)
- Git

### Fork and Clone

```bash
# Fork the repository on GitHub
# Then clone your fork
git clone https://github.com/YOUR_USERNAME/jmeter-insights-pro.git
cd jmeter-insights-pro
```

### Build the Project

```bash
# Clean build
mvn clean package -DskipTests

# Build with tests
mvn clean package

# Install to local JMeter
cp target/jmeter-insights-pro-1.0.0.jar $JMETER_HOME/lib/ext/
```

---

## 🐛 Reporting Bugs

### Before Submitting a Bug Report

1. **Check existing issues** - Your bug may already be reported
2. **Use latest version** - Ensure you're using the latest release
3. **Verify it's a bug** - Confirm the issue is reproducible

### Bug Report Template

```markdown
**Description:**
A clear description of the bug

**Steps to Reproduce:**
1. Step one
2. Step two
3. Step three

**Expected Behavior:**
What you expected to happen

**Actual Behavior:**
What actually happened

**Environment:**
- JMeter Version: 5.6.3
- Plugin Version: 1.0.0
- Java Version: 11
- OS: macOS/Windows/Linux

**Screenshots/Logs:**
If applicable, add screenshots or error logs

**JTL File:**
If possible, attach a sample JTL file that reproduces the issue
```

---

## 💡 Suggesting Features

### Feature Request Template

```markdown
**Feature Description:**
Clear description of the proposed feature

**Use Case:**
Why is this feature needed? What problem does it solve?

**Proposed Solution:**
How you envision this feature working

**Alternatives Considered:**
Other approaches you've thought about

**Additional Context:**
Any other relevant information
```

---

## 🔧 Code Contributions

### Development Workflow

1. **Create a branch**
   ```bash
   git checkout -b feature/your-feature-name
   # or
   git checkout -b fix/bug-description
   ```

2. **Make your changes**
   - Write clean, readable code
   - Follow existing code style
   - Add comments where necessary
   - Update documentation

3. **Test your changes**
   ```bash
   # Run tests
   mvn test
   
   # Build and test manually
   mvn clean package
   cp target/jmeter-insights-pro-1.0.0.jar $JMETER_HOME/lib/ext/
   # Test in JMeter GUI
   ```

4. **Commit your changes**
   ```bash
   git add .
   git commit -m "feat: add new feature description"
   # or
   git commit -m "fix: resolve bug description"
   ```

5. **Push and create PR**
   ```bash
   git push origin feature/your-feature-name
   # Then create Pull Request on GitHub
   ```

### Commit Message Convention

We follow conventional commits:

- `feat:` - New feature
- `fix:` - Bug fix
- `docs:` - Documentation changes
- `style:` - Code style changes (formatting, etc.)
- `refactor:` - Code refactoring
- `test:` - Adding or updating tests
- `chore:` - Maintenance tasks

**Examples:**
```
feat: add support for custom AI endpoints
fix: resolve NPE when JTL file is empty
docs: update README with new installation steps
refactor: extract HTML template to separate file
```

---

## 📝 Code Style Guidelines

### Java Code Style

```java
// Use descriptive variable names
String reportOutputPath = "/path/to/report";  // Good
String p = "/path/to/report";                 // Bad

// Add JavaDoc for public methods
/**
 * Generates a comparison report between baseline and current test results.
 * 
 * @param outputPath Path where the report will be saved
 * @param baselineMetrics Performance metrics from baseline test
 * @param currentMetrics Performance metrics from current test
 * @throws IOException if report generation fails
 */
public void generateComparisonReport(String outputPath, ...) {
    // Implementation
}

// Use try-with-resources for file operations
try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
    // Read file
} catch (IOException e) {
    logger.error("Failed to read file", e);
}

// Prefer streams for collections
List<String> filtered = transactions.stream()
    .filter(t -> t.getResponseTime() > threshold)
    .collect(Collectors.toList());
```

### Code Organization

- **Package structure:** Follow existing `com.jmeter.plugin.*` structure
- **Class naming:** Use descriptive names (e.g., `HTMLReportGenerator`, not `HRG`)
- **Method length:** Keep methods focused and under 50 lines when possible
- **Comments:** Explain "why", not "what"

---

## 🧪 Testing Guidelines

### Unit Tests

```java
@Test
public void testMetricsCalculation() {
    // Arrange
    PerformanceMetrics metrics = new PerformanceMetrics();
    metrics.addSample(100);
    metrics.addSample(200);
    
    // Act
    double average = metrics.getAverage();
    
    // Assert
    assertEquals(150.0, average, 0.01);
}
```

### Integration Tests

- Test with real JTL files
- Verify HTML report generation
- Test AI integration (with mocked responses)
- Verify PDF export functionality

### Test Coverage

- Aim for >70% code coverage
- Focus on critical paths
- Test edge cases and error conditions

---

## 📚 Documentation

### Update Documentation When:

- Adding new features
- Changing existing behavior
- Adding configuration options
- Fixing bugs that affect usage

### Documentation Files to Update:

- `README.md` - Main documentation
- `COMPREHENSIVE_GUIDE.md` - Detailed guide
- `CHANGELOG.md` - Version history
- JavaDoc comments in code
- Example files

---

## 🔍 Pull Request Process

### PR Checklist

Before submitting a PR, ensure:

- [ ] Code builds successfully (`mvn clean package`)
- [ ] All tests pass (`mvn test`)
- [ ] New features have tests
- [ ] Documentation is updated
- [ ] Commit messages follow convention
- [ ] Code follows style guidelines
- [ ] No unnecessary files included
- [ ] Branch is up-to-date with main

### PR Template

```markdown
## Description
Brief description of changes

## Type of Change
- [ ] Bug fix
- [ ] New feature
- [ ] Documentation update
- [ ] Refactoring
- [ ] Performance improvement

## Testing
How has this been tested?

## Screenshots (if applicable)
Add screenshots for UI changes

## Checklist
- [ ] Code builds successfully
- [ ] Tests pass
- [ ] Documentation updated
- [ ] Follows code style guidelines
```

### Review Process

1. **Automated checks** - CI/CD runs tests
2. **Code review** - Maintainer reviews code
3. **Feedback** - Address review comments
4. **Approval** - PR approved by maintainer
5. **Merge** - PR merged to main branch

---

## 🌍 Internationalization

Currently, the plugin is in English. We welcome translations!

### Adding a Translation

1. Create language file: `src/main/resources/messages_XX.properties`
2. Translate all strings
3. Update code to use ResourceBundle
4. Test with your locale

---

## 🎨 UI/UX Contributions

### HTML Report Design

- Follow existing design system
- Use CSS variables for theming
- Ensure responsive design
- Test on multiple browsers
- Maintain accessibility standards

### Color Scheme

```css
--jm-accent: #e8612b;    /* Primary accent */
--jm-red: #c0392b;       /* Critical/degraded */
--jm-yellow: #d68910;    /* Warning */
--jm-green: #1e8449;     /* Improved/success */
```

---

## 🚀 Release Process

### Version Numbering

We follow Semantic Versioning (SemVer):

- **MAJOR** (1.0.0) - Breaking changes
- **MINOR** (1.1.0) - New features, backward compatible
- **PATCH** (1.0.1) - Bug fixes, backward compatible

### Release Checklist

1. Update version in `pom.xml`
2. Update `CHANGELOG.md`
3. Run full test suite
4. Build release JAR
5. Create GitHub release
6. Update Plugin Manager listing
7. Announce release

---

## 📞 Getting Help

### Communication Channels

- **GitHub Issues** - Bug reports and feature requests
- **GitHub Discussions** - Questions and general discussion
- **Email** - For private inquiries

### Response Times

- **Bug reports** - Within 48 hours
- **Feature requests** - Within 1 week
- **Pull requests** - Within 1 week

---

## 📜 Code of Conduct

### Our Pledge

We are committed to providing a welcoming and inclusive environment for all contributors.

### Expected Behavior

- Be respectful and professional
- Accept constructive criticism gracefully
- Focus on what's best for the project
- Show empathy towards others

### Unacceptable Behavior

- Harassment or discrimination
- Trolling or insulting comments
- Personal attacks
- Publishing others' private information

### Enforcement

Violations may result in:
1. Warning
2. Temporary ban
3. Permanent ban

Report violations to the project maintainers.

---

## 🙏 Recognition

Contributors will be:
- Listed in `CONTRIBUTORS.md`
- Mentioned in release notes
- Credited in documentation

---

## 📄 License

By contributing, you agree that your contributions will be licensed under the MIT License.

---

## ❓ Questions?

If you have questions about contributing, please:
1. Check existing documentation
2. Search closed issues
3. Open a new discussion
4. Contact maintainers

Thank you for contributing to JMeter Insights Pro! 🚀
