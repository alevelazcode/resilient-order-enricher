# 🚀 Makefile Quick Reference

## 🎯 Essential Commands

| Command        | Description                 | Use Case             |
| -------------- | --------------------------- | -------------------- |
| `make help`    | Show all available commands | Getting started      |
| `make start`   | Start all services          | Development setup    |
| `make stop`    | Stop all services           | Clean shutdown       |
| `make restart` | Restart all services        | After config changes |
| `make status`  | Show service status         | Check what's running |

## 🔧 Development Workflow

| Command              | Description                   | Use Case                |
| -------------------- | ----------------------------- | ----------------------- |
| `make dev`           | Start development environment | Local development       |
| `make build`         | Build both projects           | Before deployment       |
| `make test`          | Run all tests                 | Quality assurance       |
| `make test-coverage` | Run tests with coverage       | Code quality check      |
| `make logs`          | Show all logs                 | Debugging               |
| `make logs-java`     | Show Java Worker logs         | Java-specific debugging |
| `make logs-go`       | Show Go API logs              | Go-specific debugging   |

## 🐛 Troubleshooting

| Command         | Description                      | Use Case               |
| --------------- | -------------------------------- | ---------------------- |
| `make health`   | Check service health             | Verify services are up |
| `make reset`    | Reset everything and start fresh | When things go wrong   |
| `make debug`    | Show debug information           | System diagnostics     |
| `make clean`    | Clean everything                 | Free up space          |
| `make logs-all` | Show all logs with timestamps    | Detailed debugging     |

## 🎪 Demo & Testing

| Command                  | Description            | Use Case             |
| ------------------------ | ---------------------- | -------------------- |
| `make demo`              | Start demo environment | Showcase the system  |
| `make kafka-setup`       | Setup Kafka topics     | Initial setup        |
| `make send-test-message` | Send test message      | Testing the flow     |
| `make check-mongo`       | Check MongoDB data     | Verify data storage  |
| `make check-redis`       | Check Redis data       | Verify caching/locks |

## 🐳 Docker & Containers

| Command             | Description                  | Use Case            |
| ------------------- | ---------------------------- | ------------------- |
| `make shell-java`   | Open shell in Java container | Debug Java app      |
| `make shell-go`     | Open shell in Go container   | Debug Go app        |
| `make shell-mongo`  | Open MongoDB shell           | Database inspection |
| `make clean-docker` | Clean Docker containers      | Free up resources   |

## 📊 Monitoring

| Command        | Description              | Use Case               |
| -------------- | ------------------------ | ---------------------- |
| `make metrics` | Show application metrics | Performance monitoring |
| `make health`  | Check service health     | Health monitoring      |

## 🧹 Cleaning

| Command             | Description            | Use Case              |
| ------------------- | ---------------------- | --------------------- |
| `make clean`        | Clean everything       | Full cleanup          |
| `make clean-java`   | Clean Java project     | Java-specific cleanup |
| `make clean-go`     | Clean Go project       | Go-specific cleanup   |
| `make clean-docker` | Clean Docker resources | Docker cleanup        |

## 🚀 Deployment

| Command           | Description                  | Use Case              |
| ----------------- | ---------------------------- | --------------------- |
| `make deploy`     | Build and deploy             | Production deployment |
| `make production` | Start production environment | Production setup      |

## 📚 Utilities

| Command              | Description                  | Use Case              |
| -------------------- | ---------------------------- | --------------------- |
| `make dependencies`  | Update dependencies          | Dependency management |
| `make docs`          | Generate documentation       | Documentation         |
| `make readme`        | Show README                  | Quick reference       |
| `make install-tools` | Install code quality tools   | Development setup     |
| `make check-tools`   | Check if tools are installed | Tool verification     |

## 🏆 Code Quality

| Command               | Description                 | Use Case                 |
| --------------------- | --------------------------- | ------------------------ |
| `make format`         | Format all code (Java + Go) | Code formatting          |
| `make format-java`    | Format Java with Spotless   | Java-specific formatting |
| `make format-go`      | Format Go with gofumpt      | Go-specific formatting   |
| `make check-format`   | Check if code is formatted  | Format verification      |
| `make lint`           | Lint all code (Java + Go)   | Code quality check       |
| `make lint-java`      | Lint Java with Checkstyle   | Java-specific linting    |
| `make lint-go`        | Lint Go with golangci-lint  | Go-specific linting      |
| `make lint-fix`       | Auto-fix linting issues     | Quick fixes              |
| `make quality-check`  | Run comprehensive checks    | Full quality validation  |
| `make quality-report` | Generate quality report     | Quality assessment       |
| `make pre-commit`     | Run pre-commit pipeline     | Before committing        |

## 🎯 Quick Start Sequence

```bash
# 1. Start everything
make start

# 2. Setup Kafka
make kafka-setup

# 3. Send test message
make send-test-message

# 4. Check results
make check-mongo

# 5. View logs
make logs
```

## 🔄 Development Loop

```bash
# 1. Setup development environment
make install-tools
make dev

# 2. Make code changes

# 3. Format and check code quality
make format
make lint

# 4. Run tests
make test

# 5. Check logs
make logs-java

# 6. Send test message
make send-test-message

# 7. Verify results
make check-mongo

# 8. Pre-commit validation
make pre-commit
```

## 🏆 Code Quality Workflow

```bash
# Install tools (one time setup)
make install-tools

# Format code automatically
make format

# Check for issues
make lint

# Auto-fix what can be fixed
make lint-fix

# Run comprehensive quality checks
make quality-check

# Generate quality report
make quality-report

# Pre-commit pipeline
make pre-commit
```

## 🆘 Emergency Commands

```bash
# When everything is broken
make reset

# When you need to see what's wrong
make debug

# When you need to start fresh
make clean && make start

# When you need to check health
make health
```

## 📝 Notes

- All commands use colored output for better readability
- Commands are designed to be idempotent (safe to run multiple times)
- The Makefile includes comprehensive error handling
- Use `make help` to see all available commands
- Commands are organized by functionality for easy discovery

---

**Happy Coding! 🚀**
