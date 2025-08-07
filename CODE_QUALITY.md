# 🏆 Code Quality Guide

This project enforces high code quality standards through automated formatting,
linting, and static analysis tools for both **Java** and **Go** codebases.

## 🚀 Quick Start

```bash
# Install all tools
make install-tools

# Format all code
make format

# Run all quality checks
make quality-check

# Generate quality report
make quality-report
```

## 📋 Available Commands

### 🔧 Formatting

| Command             | Description                             |
| ------------------- | --------------------------------------- |
| `make format`       | Format all code (Java + Go)             |
| `make format-java`  | Format Java code with Spotless          |
| `make format-go`    | Format Go code with gofumpt + goimports |
| `make check-format` | Check if all code is properly formatted |

### 🔍 Linting

| Command          | Description                             |
| ---------------- | --------------------------------------- |
| `make lint`      | Lint all code (Java + Go)               |
| `make lint-java` | Lint Java with Checkstyle + Error Prone |
| `make lint-go`   | Lint Go with golangci-lint              |
| `make lint-fix`  | Auto-fix linting issues                 |

### 🏆 Quality Assurance

| Command               | Description                      |
| --------------------- | -------------------------------- |
| `make quality-check`  | Run comprehensive quality checks |
| `make quality-report` | Generate detailed quality report |
| `make pre-commit`     | Run pre-commit pipeline          |

### 🛠️ Tools Management

| Command              | Description                    |
| -------------------- | ------------------------------ |
| `make install-tools` | Install all code quality tools |
| `make check-tools`   | Verify tool installation       |

## 📊 Java Code Quality

### Tools Used

- **[Spotless](https://github.com/diffplug/spotless)**: Code formatting with
  Google Java Format
- **[Checkstyle](https://checkstyle.sourceforge.io/)**: Code style checking
- **[Error Prone](https://errorprone.info/)**: Static analysis for bug detection

### Configuration Files

- `build.gradle.kts`: Tool configuration
- `config/checkstyle/checkstyle.xml`: Checkstyle rules
- `config/checkstyle/suppressions.xml`: Checkstyle suppressions

### Features

✅ **Google Java Format**: Consistent code formatting  
✅ **Import Organization**: Automatic import sorting and cleanup  
✅ **License Headers**: Automatic license header management  
✅ **Checkstyle Rules**: 50+ quality rules including:

- Naming conventions
- Code complexity limits
- Whitespace consistency
- Security best practices

✅ **Error Prone Checks**: Advanced static analysis including:

- Null pointer detection with NullAway
- Dead code detection
- Performance anti-patterns
- Common bug patterns

### Java Commands

```bash
# Format Java code
./gradlew spotlessApply

# Check Java formatting
./gradlew spotlessCheck

# Run Checkstyle
./gradlew checkstyleMain checkstyleTest

# Build with Error Prone
./gradlew build
```

## 🔧 Go Code Quality

### Tools Used

- **[gofumpt](https://github.com/mvdan/gofumpt)**: Stricter gofmt formatting
- **[goimports](https://godoc.org/golang.org/x/tools/cmd/goimports)**: Import
  organization
- **[golangci-lint](https://github.com/golangci/golangci-lint)**: Comprehensive
  linting

### Configuration Files

- `services/enricher-api-go/.golangci.yml`: golangci-lint configuration
- `services/enricher-api-go/Makefile`: Go-specific commands

### Features

✅ **25+ Linters Enabled**:

- `gofmt`, `goimports`, `gofumpt`: Formatting
- `govet`, `staticcheck`: Standard Go analysis
- `revive`: golint replacement
- `gosec`: Security analysis
- `gocyclo`: Cyclomatic complexity
- `goconst`: Repeated string detection
- `dupl`: Code duplication
- `misspell`: Spelling errors
- `unparam`: Unused parameters
- `gocritic`: Opinionated linting

✅ **Code Complexity Limits**:

- Function length: 100 lines
- Cyclomatic complexity: 10
- Cognitive complexity: 20
- Nested if depth: 4

✅ **Smart Configuration**:

- Test file exemptions
- Magic number allowlists
- Local package preferences

### Go Commands

```bash
cd services/enricher-api-go

# Format Go code
make format

# Check formatting
make check

# Run linting
make lint

# Auto-fix issues
make lint-fix

# Generate report
make report
```

## 🔄 Pre-commit Hooks

Automated quality checks run on every commit using
[pre-commit](https://pre-commit.com/).

### Setup

```bash
# Install pre-commit
make install-tools

# Install hooks (automatic with install-tools)
pre-commit install

# Run hooks manually
pre-commit run --all-files
```

### Hooks Included

**Generic Hooks:**

- Trailing whitespace removal
- End-of-file fixing
- YAML/JSON validation
- Large file detection
- Merge conflict detection

**Java Hooks:**

- Spotless formatting check
- Checkstyle validation
- Unit test execution

**Go Hooks:**

- gofumpt formatting check
- goimports validation
- go vet analysis
- golangci-lint execution
- Unit test execution

**Additional Hooks:**

- Dockerfile linting (hadolint)
- YAML/JSON formatting (prettier)
- Secret detection
- Spell checking

## 📈 Quality Metrics

### Java Metrics

- **Checkstyle**: 0 violations allowed
- **Test Coverage**: Target 80%+
- **Error Prone**: All errors must be fixed
- **Code Style**: Google Java Format enforced

### Go Metrics

- **Linting**: 0 golangci-lint issues
- **Test Coverage**: Target 80%+
- **Complexity**: Functions <100 lines, complexity <10
- **Code Style**: gofumpt enforced

## 🎯 Best Practices

### Development Workflow

1. **Before Coding:**

   ```bash
   make check-tools  # Ensure tools are installed
   ```

2. **During Development:**

   ```bash
   make format       # Format code regularly
   make lint         # Check for issues
   ```

3. **Before Committing:**

   ```bash
   make pre-commit   # Run full quality pipeline
   ```

4. **CI/CD Integration:**
   ```bash
   make quality-check    # Comprehensive checks
   make quality-report   # Generate reports
   ```

### Code Style Guidelines

**Java:**

- Use Google Java Format style
- Maximum line length: 120 characters
- Use meaningful variable names
- Add JavaDoc for public APIs
- Prefer composition over inheritance
- Use Optional for nullable returns

**Go:**

- Follow effective Go guidelines
- Use gofumpt formatting
- Maximum line length: 120 characters
- Use meaningful variable names
- Add package comments
- Use context.Context for cancellation
- Handle all errors explicitly

### Configuration Customization

**Java (build.gradle.kts):**

```kotlin
spotless {
    java {
        // Customize formatting rules
        googleJavaFormat("1.22.0").aosp()
        importOrder("java", "javax", "org", "com", "")
        // Add custom rules
    }
}
```

**Go (.golangci.yml):**

```yaml
linters-settings:
  funlen:
    lines: 100 # Customize function length limit
  gocyclo:
    min-complexity: 10 # Customize complexity limit
```

## 🚨 Troubleshooting

### Common Issues

**"Tools not found" Error:**

```bash
make install-tools
```

**"Pre-commit hooks failing":**

```bash
make format        # Fix formatting issues
make lint-fix      # Auto-fix linting issues
```

**"Checkstyle violations":**

- Review `config/checkstyle/checkstyle.xml`
- Add suppressions in `config/checkstyle/suppressions.xml`

**"golangci-lint timeout":**

```bash
# Increase timeout in .golangci.yml
run:
  timeout: 10m
```

### IDE Integration

**IntelliJ IDEA / GoLand:**

- Install Spotless plugin
- Configure Google Java Format
- Install Go plugins for formatting
- Enable golangci-lint integration

**VS Code:**

- Install Java Extension Pack
- Install Go extension
- Configure formatOnSave
- Install golangci-lint extension

## 📊 Quality Reports

Generate comprehensive quality reports:

```bash
make quality-report
```

Report includes:

- Formatting check results
- Linting violations
- Test coverage
- Code complexity metrics
- Security scan results

View report: `quality-report.txt`

## 🔗 Resources

- [Spotless Documentation](https://github.com/diffplug/spotless)
- [Checkstyle Rules](https://checkstyle.sourceforge.io/checks.html)
- [Error Prone Bug Patterns](https://errorprone.info/bugpatterns)
- [golangci-lint Linters](https://golangci-lint.run/usage/linters/)
- [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)
- [Effective Go](https://go.dev/doc/effective_go)
- [Go Code Review Comments](https://github.com/golang/go/wiki/CodeReviewComments)

---

**Maintain high code quality standards! 🏆**
