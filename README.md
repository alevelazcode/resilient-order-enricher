# Resilient Order Enricher

A robust, scalable order processing system built with **Java Spring Boot** and
**Go**, featuring Kafka message consumption, data enrichment, MongoDB storage,
and Redis-based resilience patterns.

## 📋 Table of Contents

- [Resilient Order Enricher](#resilient-order-enricher)
  - [📋 Table of Contents](#-table-of-contents)
  - [🎯 Technical Challenge Statement](#-technical-challenge-statement)
    - [**Technical Challenge: Java \& Go Worker for Order Processing with Data Enrichment and Resilience**](#technical-challenge-java--go-worker-for-order-processing-with-data-enrichment-and-resilience)
      - [**Overview**](#overview)
      - [**Detailed Requirements**](#detailed-requirements)
      - [**Suggested Technologies and Tools**](#suggested-technologies-and-tools)
      - [**Additional Considerations**](#additional-considerations)
  - [🔧 Technical Prerequisites](#-technical-prerequisites)
    - [**Required Software**](#required-software)
    - [**Installation Guides by Operating System**](#installation-guides-by-operating-system)
      - [**🍎 macOS**](#-macos)
      - [**🐧 Linux (Ubuntu/Debian)**](#-linux-ubuntudebian)
      - [**🪟 Windows**](#-windows)
      - [**🐧 WSL (Windows Subsystem for Linux)**](#-wsl-windows-subsystem-for-linux)
    - [**Verification Commands**](#verification-commands)
    - [**IDE Setup (Optional but Recommended)**](#ide-setup-optional-but-recommended)
      - [**VS Code Extensions**](#vs-code-extensions)
      - [**IntelliJ IDEA**](#intellij-idea)
      - [**Eclipse**](#eclipse)
    - [**Troubleshooting**](#troubleshooting)
      - [**Common Issues**](#common-issues)
      - [**System Requirements**](#system-requirements)
    - [**Next Steps**](#next-steps)
    - [**✅ Quick Setup Checklist**](#-quick-setup-checklist)
  - [✅ **Verification Guide: How to Check Each Objective**](#-verification-guide-how-to-check-each-objective)
    - [**🔍 Quick Verification Commands**](#-quick-verification-commands)
    - [**📋 Detailed Verification Steps**](#-detailed-verification-steps)
      - [**1. ✅ Kafka Message Consumption**](#1--kafka-message-consumption)
      - [**2. ✅ Data Enrichment via Go API**](#2--data-enrichment-via-go-api)
      - [**3. ✅ Data Validation**](#3--data-validation)
      - [**4. ✅ MongoDB Storage**](#4--mongodb-storage)
      - [**5. ✅ Error Handling and Retry Logic**](#5--error-handling-and-retry-logic)
      - [**6. ✅ Distributed Locking**](#6--distributed-locking)
    - [**🧪 Automated Testing Verification**](#-automated-testing-verification)
    - [**📊 Performance and Scalability Verification**](#-performance-and-scalability-verification)
    - [**🔧 Code Quality Verification**](#-code-quality-verification)
  - [🚀 Quick Start with Makefile](#-quick-start-with-makefile)
    - [📋 Available Commands](#-available-commands)
    - [🎯 Automatic Formatting on Save (Like Husky)](#-automatic-formatting-on-save-like-husky)
      - [**Setup Automatic Formatting**](#setup-automatic-formatting)
      - [**How It Works**](#how-it-works)
      - [**Supported IDEs**](#supported-ides)
      - [**Manual Formatting**](#manual-formatting)
      - [**Quality Checks**](#quality-checks)
    - [🎯 Most Used Commands](#-most-used-commands)
    - [🚨 Quick Troubleshooting](#-quick-troubleshooting)
    - [🎪 Demo Mode](#-demo-mode)
    - [🔧 Development Workflow](#-development-workflow)
    - [🏆 Code Quality Commands](#-code-quality-commands)
    - [🐛 Troubleshooting](#-troubleshooting)
  - [🏗️ Architecture](#️-architecture)
    - [Feature-Based Architecture](#feature-based-architecture)
    - [Java Spring Boot Structure](#java-spring-boot-structure)
    - [Go Echo API Structure](#go-echo-api-structure)
    - [Technology Stack](#technology-stack)
  - [📊 System Overview](#-system-overview)
    - [Data Flow](#data-flow)
    - [Docker Services](#docker-services)
      - [Infrastructure Services](#infrastructure-services)
      - [Application Services](#application-services)
    - [Environment Configuration](#environment-configuration)
    - [Resilience Patterns](#resilience-patterns)
  - [🧪 Testing Strategy](#-testing-strategy)
    - [Java Testing Framework](#java-testing-framework)
    - [Go Testing Framework](#go-testing-framework)
  - [🏆 Code Quality \& Standards](#-code-quality--standards)
    - [Java Code Quality Tools](#java-code-quality-tools)
    - [Go Code Quality Tools](#go-code-quality-tools)
    - [Pre-commit Hooks](#pre-commit-hooks)
    - [Quality Commands](#quality-commands)
  - [🐳 Docker \& Deployment](#-docker--deployment)
    - [Local Development](#local-development)
    - [Production](#production)
  - [📈 Monitoring \& Observability](#-monitoring--observability)
    - [Health Checks](#health-checks)
    - [Metrics](#metrics)
  - [🔍 Troubleshooting](#-troubleshooting-1)
    - [Common Issues](#common-issues-1)
    - [Logs](#logs)
  - [📚 API Documentation](#-api-documentation)
    - [Interactive Swagger UI](#interactive-swagger-ui)
    - [Java Spring Boot API Endpoints](#java-spring-boot-api-endpoints)
    - [Go Enrichment API Endpoints](#go-enrichment-api-endpoints)
    - [API Response Examples](#api-response-examples)
    - [Swagger Features](#swagger-features)
    - [Testing with Swagger UI](#testing-with-swagger-ui)
  - [🎯 Performance \& Scalability](#-performance--scalability)
    - [Optimizations](#optimizations)
    - [Scaling](#scaling)
  - [🤝 Contributing](#-contributing)
    - [Development Setup](#development-setup)
    - [Code Standards](#code-standards)
  - [📄 License](#-license)
  - [🙏 Acknowledgments](#-acknowledgments)

---

## 🎯 Technical Challenge Statement

### **Technical Challenge: Java & Go Worker for Order Processing with Data Enrichment and Resilience**

#### **Overview**

Develop a Java worker that processes orders efficiently and reliably. The worker
must:

- Consume messages from a Kafka topic containing basic order information.
- Enrich the data by calling external APIs written in Go.
- Persist the processed data in MongoDB.

#### **Detailed Requirements**

**1. Kafka Message Consumption**

- Subscribe to a Kafka topic.
- Each message contains:
  - Order ID
  - Customer ID
  - Product list

**2. Data Enrichment**

- Call a Go API to:
  - Retrieve product details (name, description, price, etc.).
  - Retrieve customer details.

**3. Data Validation**

- Ensure that:
  - Products exist in the catalog.
  - The customer exists and is active.

**4. Storage in MongoDB**

- Save processed orders using the following structure:

```json
{
  "_id": ObjectId(),
  "orderId": "order-123",
  "customerId": "customer-456",
  "products": [
    {
      "productId": "product-789",
      "name": "Laptop",
      "price": 999
    }
  ]
}
```

**5. Error Handling and Retry Logic**

- Implement exponential back-off retries for API calls.
- Use Redis to store failed messages along with an attempt counter.
- Configure maximum retries and wait time between attempts.

**6. Handling Locked Customers**

- Use a distributed lock (Redis) to prevent multiple instances from processing
  the same order simultaneously.

#### **Suggested Technologies and Tools**

- Java 21 (mandatory)
- Spring Boot, Java WebFlux (mandatory)
- Go
- Kafka
- MongoDB
- Redis
- GitHub / GitLab for version control

#### **Additional Considerations**

- Design: Modular, well-structured, and easy to maintain.
- Testing: Include unit tests.
- Performance: Optimize with caching, indexes, and best practices.
- Scalability: Plan for growing order volumes.

## 🔧 Technical Prerequisites

Before running this project, ensure you have the following tools installed on
your system:

### **Required Software**

| Tool               | Version | Purpose                       |
| ------------------ | ------- | ----------------------------- |
| **Java**           | 21+     | Java runtime and development  |
| **Go**             | 1.21+   | Go runtime and development    |
| **Docker**         | 24.0+   | Containerization              |
| **Docker Compose** | 2.20+   | Multi-container orchestration |
| **Git**            | 2.30+   | Version control               |
| **Make**           | 4.0+    | Build automation              |

### **Installation Guides by Operating System**

#### **🍎 macOS**

**Using Homebrew (Recommended):**

```bash
# Install Homebrew if not already installed
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"

# Install Java 21
brew install openjdk@21
echo 'export PATH="/opt/homebrew/opt/openjdk@21/bin:$PATH"' >> ~/.zshrc
echo 'export JAVA_HOME="/opt/homebrew/opt/openjdk@21"' >> ~/.zshrc
source ~/.zshrc

# Install Go
brew install go

# Install Docker Desktop
brew install --cask docker

# Install Git (usually pre-installed)
brew install git

# Install Make (usually pre-installed)
brew install make
```

#### **🐧 Linux (Ubuntu/Debian)**

**Using Package Manager:**

```bash
# Update package list
sudo apt update

# Install Java 21
sudo apt install openjdk-21-jdk
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
echo 'export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64' >> ~/.bashrc

# Install Go
wget https://go.dev/dl/go1.21.0.linux-amd64.tar.gz
sudo tar -C /usr/local -xzf go1.21.0.linux-amd64.tar.gz
echo 'export PATH=$PATH:/usr/local/go/bin' >> ~/.bashrc
source ~/.bashrc

# Install Docker
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh
sudo usermod -aG docker $USER
newgrp docker

# Install Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

# Install Git
sudo apt install git

# Install Make
sudo apt install make
```

#### **🪟 Windows**

**Using Chocolatey (Recommended):**

```powershell
# Install Chocolatey if not already installed
Set-ExecutionPolicy Bypass -Scope Process -Force; [System.Net.ServicePointManager]::SecurityProtocol = [System.Net.ServicePointManager]::SecurityProtocol -bor 3072; iex ((New-Object System.Net.WebClient).DownloadString('https://community.chocolatey.org/install.ps1'))

# Install Java 21
choco install openjdk21

# Install Go
choco install golang

# Install Docker Desktop
choco install docker-desktop

# Install Git
choco install git

# Install Make (via Chocolatey)
choco install make
```

**Using Scoop:**

```powershell
# Install Scoop if not already installed
Set-ExecutionPolicy RemoteSigned -Scope CurrentUser
irm get.scoop.sh | iex

# Install Java 21
scoop install openjdk21

# Install Go
scoop install go

# Install Git
scoop install git

# Install Make
scoop install make
```

**Manual Installation:**

1. **Java 21**: Download from
   [Oracle](https://www.oracle.com/java/technologies/downloads/) or
   [OpenJDK](https://adoptium.net/)
2. **Go**: Download from [golang.org](https://golang.org/dl/)
3. **Docker Desktop**: Download from
   [docker.com](https://www.docker.com/products/docker-desktop/)
4. **Git**: Download from [git-scm.com](https://git-scm.com/download/win)

#### **🐧 WSL (Windows Subsystem for Linux)**

**Using Ubuntu on WSL:**

```bash
# Update WSL
wsl --update

# Install Ubuntu if not already installed
wsl --install -d Ubuntu

# Follow Linux installation guide above
# (Same as Ubuntu/Debian instructions)
```

### **Verification Commands**

After installation, verify all tools are working:

```bash
# Verify Java
java --version
javac --version
echo $JAVA_HOME

# Verify Go
go version
go env GOPATH

# Verify Docker
docker --version
docker-compose --version
docker run hello-world

# Verify Git
git --version

# Verify Make
make --version
```

### **IDE Setup (Optional but Recommended)**

#### **VS Code Extensions**

```bash
# Install recommended extensions
code --install-extension vscjava.vscode-java-pack
code --install-extension redhat.java
code --install-extension vscjava.vscode-gradle
code --install-extension golang.go
code --install-extension ms-vscode.vscode-json
code --install-extension redhat.vscode-yaml
code --install-extension esbenp.prettier-vscode
code --install-extension ms-vscode.vscode-docker
```

#### **IntelliJ IDEA**

1. Install **IntelliJ IDEA Community** (free) or **Ultimate**
2. Install plugins:
   - **Java** (built-in)
   - **Go** (built-in)
   - **Docker** (built-in)
   - **Gradle** (built-in)

#### **Eclipse**

1. Download **Eclipse IDE for Java Developers**
2. Install plugins:
   - **GoClipse** for Go support
   - **Docker Tooling**

### **Troubleshooting**

#### **Common Issues**

**Java Issues:**

```bash
# If Java not found
export JAVA_HOME=/path/to/your/java
export PATH=$JAVA_HOME/bin:$PATH
```

**Go Issues:**

```bash
# If Go not found
export GOPATH=$HOME/go
export PATH=$PATH:$GOPATH/bin
```

**Docker Issues:**

```bash
# If Docker permission denied
sudo usermod -aG docker $USER
newgrp docker
```

**Make Issues:**

```bash
# On Windows, ensure you're using WSL or Git Bash
# Make is not natively available on Windows
```

#### **System Requirements**

- **RAM**: Minimum 8GB, Recommended 16GB+
- **Storage**: Minimum 10GB free space
- **CPU**: Multi-core processor recommended
- **OS**: macOS 10.15+, Ubuntu 20.04+, Windows 10/11, or WSL2

### **Next Steps**

Once all prerequisites are installed:

1. **Clone the repository**:

   ```bash
   git clone https://github.com/yourusername/resilient-order-enricher.git
   cd resilient-order-enricher
   ```

2. **Setup automatic formatting**:

   ```bash
   make setup-formatting
   ```

3. **Start the services**:

   ```bash
   make start
   ```

4. **Run tests**:

   ```bash
   make test
   ```

### **✅ Quick Setup Checklist**

Before running the project, verify you have:

- [ ] **Java 21+** installed (`java --version`)
- [ ] **Go 1.21+** installed (`go version`)
- [ ] **Docker** running (`docker --version`)
- [ ] **Docker Compose** available (`docker-compose --version`)
- [ ] **Git** installed (`git --version`)
- [ ] **Make** available (`make --version`)
- [ ] **8GB+ RAM** available
- [ ] **10GB+ free storage**
- [ ] **Ports 8081, 8090, 9092, 27017, 6380** available

**Quick verification command:**

```bash
# Run this to check all prerequisites
make check-prerequisites

# Or manually check each tool
java --version && go version && docker --version && docker-compose --version && git --version && make --version && echo "✅ All prerequisites met!"
```

---

## ✅ **Verification Guide: How to Check Each Objective**

This section provides step-by-step instructions to verify that each technical
challenge requirement is met.

### **🔍 Quick Verification Commands**

```bash
# 1. Run complete verification (recommended)
make verify               # Start services + run tests + quality + smoke through Kafka

# 2. Quick smoke verification
make verify-smoke         # Start services + health + send message + check Mongo

# 3. Verify everything works
make health               # Check all services are healthy
make check-mongo          # Verify data was stored in MongoDB
```

### **📋 Detailed Verification Steps**

#### **1. ✅ Kafka Message Consumption**

**Objective**: Subscribe to a Kafka topic with messages containing `orderId`,
`customerId`, `products[]`

**Verification Steps**:

```bash
# Start services
make start

# Check Kafka is running
make logs-kafka

# Send test message
make send-test-message

# Verify message was consumed (check Java Worker logs)
make logs-java
```

**Expected Result**:

- Kafka topic `orders` is created
- Test message is sent successfully
- Java Worker logs show message consumption
- No errors in processing

**Code Location**:

- `src/main/java/com/resilient/orderworker/order/consumer/OrderConsumer.java`
- `src/main/java/com/resilient/orderworker/order/dto/OrderMessage.java`

#### **2. ✅ Data Enrichment via Go API**

**Objective**: Call Go APIs to retrieve product and customer details

**Verification Steps**:

```bash
# Check Go API is running
curl http://localhost:8090/health

# Test customer API
curl http://localhost:8090/v1/customers/customer-456

# Test product API
curl http://localhost:8090/v1/products/product-789

# Check enrichment in Java Worker logs
make logs-java
```

**Expected Result**:

- Go API responds with customer/product data
- Java Worker logs show successful API calls
- Data is enriched with customer names and product details

**Code Location**:

- `services/enricher-api-go/` (Go API)
- `src/main/java/com/resilient/orderworker/customer/service/CustomerService.java`
- `src/main/java/com/resilient/orderworker/product/service/ProductService.java`

#### **3. ✅ Data Validation**

**Objective**: Ensure products exist in catalog and customers are active

**Verification Steps**:

```bash
# Test with valid data
make send-test-message

# Test with invalid customer (should fail)
curl -X POST http://localhost:8081/api/v1/orders \
  -H "Content-Type: application/json" \
  -d '{"orderId":"test-123","customerId":"invalid-customer","products":[{"productId":"product-789","quantity":1}]}'

# Check validation logs
make logs-java
```

**Expected Result**:

- Valid orders are processed successfully
- Invalid customers/products are rejected with appropriate errors
- Validation errors are logged

**Code Location**:

- `src/main/java/com/resilient/orderworker/order/service/OrderProcessingService.java`
  (lines 116-136)

#### **4. ✅ MongoDB Storage**

**Objective**: Save processed orders with exact required structure

**Verification Steps**:

```bash
# Send test message
make send-test-message

# Check MongoDB data
make check-mongo

# Or manually check
docker exec order-worker-mongo-1 mongosh --eval "db = db.getSiblingDB('order_worker'); db.orders.find().pretty()"
```

**Expected Result**:

- Orders are stored in MongoDB with exact structure:
  - `_id`: ObjectId
  - `orderId`: "order-123"
  - `customerId`: "customer-456"
  - `products`: Array with `productId`, `name`, `price`

**Code Location**:

- `src/main/java/com/resilient/orderworker/order/entity/Order.java`
- `src/main/java/com/resilient/orderworker/order/repository/OrderRepository.java`

#### **5. ✅ Error Handling and Retry Logic**

**Objective**: Exponential back-off retries with Redis storage

**Verification Steps**:

```bash
# Check Redis is running
make check-redis

# Check retry configuration
cat src/main/resources/application.yml | grep -A 20 "resilience4j"

# Test with failing API (temporarily stop Go API)
make stop
make start enricher-api  # Start only Go API
# Send message and check retry behavior
make send-test-message
make logs-java
```

**Expected Result**:

- Retry attempts are logged with exponential backoff
- Circuit breaker patterns are implemented
- Failed messages are handled gracefully

**Code Location**:

- `src/main/resources/application.yml` (lines 77-123)
- `src/main/java/com/resilient/orderworker/customer/service/CustomerService.java`
- `src/main/java/com/resilient/orderworker/product/service/ProductService.java`

#### **6. ✅ Distributed Locking**

**Objective**: Use Redis to prevent multiple instances from processing the same
order

**Verification Steps**:

```bash
# Check Redis locks
make check-redis

# Send duplicate message (should be handled by locking)
make send-test-message
make send-test-message  # Same order ID

# Check locking behavior in logs
make logs-java
```

**Expected Result**:

- Duplicate orders are handled by distributed locking
- Lock acquisition/release is logged
- No duplicate processing occurs

**Code Location**:

- `src/main/java/com/resilient/orderworker/infrastructure/redis/RedisDistributedLockService.java`
- `src/main/java/com/resilient/orderworker/order/service/OrderProcessingService.java`

### **🧪 Automated Testing Verification**

**Run all tests to verify implementation**:

```bash
# Run all tests
make test

# Run with coverage
make test-coverage

# Run specific test categories
make test-java    # Java tests only
make test-go      # Go tests only
```

**Expected Result**:

- All tests pass (50+ tests)
- High test coverage (>80%)
- Integration tests with real dependencies

### **📊 Performance and Scalability Verification**

**Check performance optimizations**:

```bash
# Check service health and metrics
make health

# View application metrics
make metrics

# Check database indexes
docker exec order-worker-mongo-1 mongosh --eval "db = db.getSiblingDB('order_worker'); db.orders.getIndexes()"
```

**Expected Result**:

- Services respond quickly (<2s)
- Database queries are optimized
- System can handle concurrent requests

### **🔧 Code Quality Verification**

**Check code quality standards**:

```bash
# Run code quality checks
make quality-check

# Check formatting
make check-format

# Run linting
make lint
```

**Expected Result**:

- Code follows style guidelines
- No linting errors
- High code quality scores

---

## 🚀 Quick Start with Makefile

**⚠️ Prerequisites Required**: Before starting, ensure you have all
[Technical Prerequisites](#-technical-prerequisites) installed.

**🔍 Quick Check**: Run `make check-prerequisites` to verify all tools are
installed correctly.

The project includes a comprehensive **Makefile** with developer-friendly
shortcuts for all common tasks:

### 📋 Available Commands

```bash
# Core operations
make start          # Start all services
make stop           # Stop all services
make test           # Run all tests
make build          # Build both projects
make clean          # Clean everything

# Development
make dev            # Start development environment
make format         # Format all code automatically
make lint           # Run linting checks
make setup-formatting # Setup automatic formatting on save

# Quality checks
make ci             # Run complete CI pipeline
make quality-check  # Run comprehensive quality checks

# Monitoring
make logs           # Show all logs
make health         # Check service health
make status         # Show service status
```

### 🎯 Automatic Formatting on Save (Like Husky)

This project includes **automatic formatting on save** similar to Husky in
JavaScript environments. Here's how it works:

#### **Setup Automatic Formatting**

```bash
# Setup automatic formatting for your IDE
make setup-formatting
```

This command will:

- ✅ Configure VS Code settings for automatic formatting
- ✅ Set up pre-commit hooks for auto-fixing
- ✅ Create EditorConfig for consistent formatting
- ✅ Install Git hooks for automatic formatting on commit
- ✅ Configure IDE-specific formatting rules

#### **How It Works**

1. **On Save**: Your IDE will automatically format code when you save files
2. **On Commit**: Pre-commit hooks automatically fix formatting issues
3. **On Push**: Additional checks ensure code quality before pushing

#### **Supported IDEs**

- **VS Code**: Fully configured with recommended extensions
- **IntelliJ IDEA**: Manual setup instructions provided
- **Eclipse**: Manual setup instructions provided

#### **Manual Formatting**

```bash
# Format all code manually
make format

# Format specific languages
make format-java    # Format Java code
make format-go      # Format Go code
```

#### **Quality Checks**

```bash
# Run all quality checks
make quality-check

# Run specific checks
make lint           # Linting only
make check-format   # Format checking only
```

### 🎯 Most Used Commands

```bash
# Service Management
make start          # Start all services with Docker Compose
make stop           # Stop all services
make restart        # Restart all services
make status         # Show service status

# Development
make dev            # Start development environment (hot reload)
make build          # Build both Java and Go projects
make test           # Run all tests
make test-coverage  # Run tests with coverage

# Monitoring & Debugging
make logs           # Show all service logs
make logs-java      # Show Java Worker logs
make logs-go        # Show Go API logs
make health         # Check service health
make check-mongo    # Check MongoDB data
make check-redis    # Check Redis data

# API Documentation
make swagger        # Open Swagger UI (http://localhost:8081/swagger-ui.html)
make api-docs       # View OpenAPI JSON (http://localhost:8081/v3/api-docs)
```

### 🚨 Quick Troubleshooting

**If services won't start:**

```bash
# Check if Docker is running
docker --version
docker-compose --version

# Check if ports are available
lsof -i :8081  # Java Worker
lsof -i :8090  # Go API
lsof -i :9092  # Kafka
lsof -i :27017 # MongoDB
lsof -i :6380  # Redis
```

**If tests fail:**

```bash
# Check Java version
java --version  # Should be 21+

# Check Go version
go version      # Should be 1.21+

# Clean and rebuild
make clean
make build
make test
```

### 🎪 Demo Mode

Run a complete demo with one command:

```bash
make demo
```

This will:

1. Start all services
2. Setup Kafka topics
3. Send a test message
4. Show you how to check logs

### 🔧 Development Workflow

```bash
# 1. Setup environment
cp .env.sample .env     # Copy environment configuration
make install-tools     # Install code quality tools (one-time)
make dev               # Start development environment

# 2. Make changes to code

# 3. Format and validate code
make format            # Auto-format all code
make lint              # Check code quality

# 4. Run tests
make test

# 5. Check logs
make logs-java

# 6. Send test message
make send-test-message

# 7. Check results
make check-mongo

# 8. Pre-commit validation
make pre-commit        # Final quality checks
```

### 🏆 Code Quality Commands

```bash
# Code Formatting
make format            # Format all code (Java + Go)
make format-java       # Format Java with Spotless
make format-go         # Format Go with gofumpt + goimports
make check-format      # Verify code is properly formatted

# Code Linting
make lint              # Lint all code (Java + Go)
make lint-java         # Lint Java with Checkstyle + Error Prone
make lint-go           # Lint Go with golangci-lint
make lint-fix          # Auto-fix linting issues

# Quality Assurance
make quality-check     # Run comprehensive quality checks
make quality-report    # Generate detailed quality report
make pre-commit        # Run complete pre-commit pipeline

# Tools Management
make install-tools     # Install all code quality tools
make check-tools       # Verify tools are installed
```

### 🐛 Troubleshooting

```bash
# Reset everything
make reset

# Check service health
make health

# Show debug information
make debug

# View all logs with timestamps
make logs-all
```

## 🏗️ Architecture

### Feature-Based Architecture

This project implements a **feature-based architecture** that organizes code by
business functionality rather than technical layers. This architectural pattern
was chosen specifically for this order processing system because:

**Why Feature-Based for Order Processing?**

- **Business Domain Alignment**: Each feature (`order/`, `customer/`,
  `product/`) represents a distinct business domain in the order enrichment
  workflow
- **Team Scalability**: Multiple developers can work on different features
  without conflicts
- **Maintainability**: Changes to order processing logic don't affect customer
  or product features
- **Testing Isolation**: Each feature can be tested independently with its own
  test suite
- **Deployment Flexibility**: Features can be extracted into microservices if
  needed

### Java Spring Boot Structure

```
src/main/java/com/resilient/orderworker/
├── order/                           # 📦 Core Order Processing Feature
│   ├── consumer/                    # Kafka message consumers
│   │   └── OrderConsumer.java       # Listens to orders topic
│   ├── service/                     # Business logic
│   │   └── OrderProcessingService.java # Main orchestration logic
│   ├── repository/                  # Data access layer
│   │   └── OrderRepository.java     # MongoDB reactive repository
│   ├── entity/                      # Domain entities
│   │   └── Order.java               # Order document model
│   └── dto/                         # Data transfer objects
│       └── OrderMessage.java        # Kafka message structure
├── customer/                        # 📦 Customer Enrichment Feature
│   ├── service/                     # Customer API integration
│   │   └── CustomerService.java     # Go API client with resilience
│   └── dto/                         # Customer data models
│       └── CustomerResponse.java    # Customer API response
├── product/                         # 📦 Product Enrichment Feature
│   ├── service/                     # Product API integration
│   │   └── ProductService.java      # Go API client with resilience
│   └── dto/                         # Product data models
│       └── ProductResponse.java     # Product API response
├── common/                          # 🔧 Shared Utilities
│   └── exception/                   # Custom exceptions
│       ├── CustomerNotFoundException.java
│       ├── ProductNotFoundException.java
│       ├── ExternalApiException.java
│       └── OrderProcessingException.java
└── infrastructure/                  # 🏗️ Technical Configuration
    ├── kafka/                       # Kafka configuration
    │   └── KafkaConfig.java         # Consumer setup
    ├── mongodb/                     # MongoDB configuration
    │   └── MongoConfig.java         # Reactive client setup
    ├── redis/                       # Redis configuration
    │   ├── RedisConfig.java         # Connection setup
    │   └── DistributedLockService.java # Locking mechanism
    └── webclient/                   # HTTP client configuration
        └── WebClientConfig.java     # Go API client setup
```

### Go Echo API Structure

```
services/enricher-api-go/
├── cmd/server/                      # 🚀 Application Entry Point
│   ├── main.go                      # Server setup and routing
│   └── main_test.go                 # Integration tests
├── internal/                        # 🔒 Private Application Code
│   ├── customer/                    # Customer domain
│   │   ├── model.go                 # Customer data models
│   │   ├── repository.go            # In-memory data access
│   │   ├── service.go               # Business logic
│   │   ├── handler.go               # HTTP handlers
│   │   └── service_test.go          # Unit tests
│   └── product/                     # Product domain
│       ├── model.go                 # Product data models
│       ├── repository.go            # In-memory data access
│       ├── service.go               # Business logic
│       ├── handler.go               # HTTP handlers
│       └── service_test.go          # Unit tests
├── .golangci.yml                    # Linting configuration
└── Makefile                         # Development commands
```

### Technology Stack

**Backend Services:**

- **Java 21** - LTS version with modern language features
- **Spring Boot 3.3.5** - Application framework with reactive support
- **Spring WebFlux** - Reactive programming for non-blocking I/O
- **Go 1.23** - Fast, efficient language for API services
- **Echo Framework** - Lightweight, high-performance Go web framework

**Message Broker & Storage:**

- **Apache Kafka 7.7.0** - Distributed event streaming platform
- **MongoDB 8.0** - Document database for order storage
- **Redis 7.2** - In-memory store for distributed locking and caching

**Development & Deployment:**

- **Docker** + **Docker Compose** - Containerization and orchestration
- **Gradle 9.0** - Build automation for Java
- **Go Modules** - Dependency management for Go

**Quality & Testing:**

- **Resilience4j** - Circuit breaker, retry, and bulkhead patterns
- **TestContainers** - Integration testing with real dependencies
- **JUnit 5** - Testing framework for Java
- **Mockito** - Mocking framework
- **Spotless** - Code formatting
- **Checkstyle** - Code quality checks
- **Error Prone** - Static analysis
- **golangci-lint** - Go linting with 25+ checks

## 📊 System Overview

### Data Flow

1. **Order Message** → Kafka topic `orders`
2. **Java Worker** → Consumes and processes messages
3. **Data Enrichment** → Calls Go APIs for customer/product details
4. **Validation** → Ensures data integrity
5. **Storage** → Saves enriched orders to MongoDB
6. **Resilience** → Redis-based retry mechanism and distributed locks

### Docker Services

The system is composed of 6 Docker services orchestrated with Docker Compose:

#### Infrastructure Services

**🔹 Zookeeper** (`confluentinc/cp-zookeeper:7.7.0`)

- **Purpose**: Kafka cluster coordination and metadata management
- **Port**: Internal 2181
- **Role**: Ensures Kafka broker availability and partition leadership

**🔹 Kafka** (`confluentinc/cp-kafka:7.7.0`)

- **Purpose**: Message streaming platform for order events
- **Ports**: 9092 (external), 29092 (internal)
- **Topics**: `orders` (main processing), `orders-dlq` (dead letter queue)
- **Features**: Auto-topic creation, single replica (development)

**🔹 MongoDB** (`mongo:8.0`)

- **Purpose**: Persistent storage for enriched orders
- **Port**: 27017
- **Database**: `order_worker`
- **Storage**: Document-based with reactive driver support

**🔹 Redis** (`redis:7.2-alpine`)

- **Purpose**: Distributed locking and caching
- **Port**: 6380 (external), 6379 (internal)
- **Features**: Persistence enabled, used for preventing duplicate processing

#### Application Services

**🔹 Enricher API** (Go Echo Service)

- **Purpose**: Customer and product data enrichment APIs
- **Port**: 8090 (external), 8080 (internal)
- **Endpoints**:
  - `GET /v1/customers/:id` - Customer details
  - `GET /v1/products/:id` - Product information
  - `GET /health` - Health check
- **Features**: In-memory data store, RESTful design

**🔹 Order Worker** (Java Spring Boot Service)

- **Purpose**: Main order processing engine
- **Port**: 8081
- **Features**:
  - Kafka message consumption
  - Data enrichment orchestration
  - MongoDB persistence
  - Circuit breaker patterns
  - Distributed locking

### Environment Configuration

The project uses environment variables for configuration. Copy `.env.sample` to
`.env` to customize:

```bash
# Copy sample environment file
cp .env.sample .env

# Edit configuration as needed
vi .env
```

**Key Configuration Sections:**

```bash
# Application Settings
BASE_PKG=com.resilient.orderworker
SPRING_PORT=8081
GO_PORT=8080

# Kafka Configuration
KAFKA_BOOTSTRAP_SERVERS=localhost:9092
KAFKA_ORDERS_TOPIC=orders
KAFKA_CONSUMER_GROUP=order-worker-group

# Database Settings
MONGODB_URI=mongodb://localhost:27017
MONGODB_DATABASE=order_worker

# Redis Configuration
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_TIMEOUT=3000ms

# External Services
ENRICHER_API_BASE_URL=http://localhost:8090
ENRICHER_API_TIMEOUT=5000ms

# Resilience Settings
CIRCUIT_BREAKER_FAILURE_RATE_THRESHOLD=50
RETRY_MAX_ATTEMPTS=3
RETRY_WAIT_DURATION=1s
```

See `env.sample` for the complete configuration reference with all available
options.

### Resilience Patterns

- ✅ **Circuit Breaker** - Prevents cascading failures
- ✅ **Exponential Retry** - Handles transient failures
- ✅ **Distributed Locking** - Prevents duplicate processing
- ✅ **Dead Letter Queue** - Handles failed messages
- ✅ **Health Checks** - Service monitoring

## 🧪 Testing Strategy

The project implements comprehensive testing at multiple levels to ensure
reliability and maintainability.

### Java Testing Framework

**Testing Stack:**

- **JUnit 5** - Primary testing framework with parameterized tests
- **Mockito** - Mocking framework for unit tests
- **AssertJ** - Fluent assertions for readable test code
- **TestContainers** - Integration testing with real dependencies
- **Spring Boot Test** - Application context testing
- **Reactor Test** - Reactive stream testing

**Test Structure:**

```
src/test/java/com/resilient/orderworker/
├── order/service/
│   └── OrderProcessingServiceTest.java    # Core business logic tests
├── customer/service/
│   └── CustomerServiceTest.java           # External API client tests
├── product/service/
│   └── ProductServiceTest.java            # External API client tests
└── integration/
    └── OrderProcessingIntegrationTest.java # End-to-end tests
```

**Test Categories:**

```bash
# Unit Tests - Fast, isolated, mocked dependencies
make test-java

# Integration Tests - Real dependencies via TestContainers
make test-integration

# Coverage Reports - JaCoCo HTML reports
make test-coverage
```

### Go Testing Framework

**Testing Stack:**

- **Go testing package** - Built-in testing framework
- **testify/assert** - Assertion library for clear test failures
- **Echo testing** - HTTP handler testing utilities
- **Table-driven tests** - Parameterized testing patterns

**Test Structure:**

```
services/enricher-api-go/
├── internal/customer/
│   └── service_test.go              # Customer business logic tests
├── internal/product/
│   └── service_test.go              # Product business logic tests
└── cmd/server/
    └── main_test.go                 # HTTP integration tests
```

**Test Categories:**

```bash
# Unit Tests - Service and repository testing
cd services/enricher-api-go && make test

# Integration Tests - HTTP endpoint testing
cd services/enricher-api-go && make test-integration

# Coverage Reports - Go coverage HTML reports
cd services/enricher-api-go && make test-coverage
```

## 🏆 Code Quality & Standards

### Java Code Quality Tools

**Formatting & Style:**

- **Spotless** - Google Java Format with AOSP style
- **Checkstyle** - 50+ quality rules for naming, complexity, security
- **Error Prone** - Static analysis for bug detection and performance

**Configuration Files:**

```
├── build.gradle.kts                    # Quality plugin configuration
├── config/checkstyle/
│   ├── checkstyle.xml                  # Style rules
│   └── suppressions.xml                # Rule exemptions
```

**Quality Standards:**

- Maximum line length: 120 characters
- Google Java Format style guide
- No magic numbers (detected and flagged)
- Null safety with Error Prone NullAway
- License headers automatically managed

### Go Code Quality Tools

**Formatting & Linting:**

- **gofumpt** - Stricter formatting than standard gofmt
- **goimports** - Import organization and unused import removal
- **golangci-lint** - 25+ linters including security and performance

**Configuration Files:**

```
services/enricher-api-go/
├── .golangci.yml                       # Linting configuration
└── Makefile                            # Quality commands
```

**Quality Standards:**

- Function length: 100 lines maximum
- Cyclomatic complexity: 10 maximum
- Line length: 120 characters
- Security analysis with gosec
- Code duplication detection

### Pre-commit Hooks

**Automated Quality Checks:**

```bash
# Install pre-commit hooks
make install-tools

# Manual execution
make pre-commit
```

**Hooks Include:**

- Code formatting verification
- Linting checks
- Unit test execution
- Security scanning
- Dockerfile linting
- YAML/JSON validation

### Quality Commands

```bash
# Format all code
make format              # Both Java and Go
make format-java         # Spotless formatting
make format-go           # gofumpt + goimports

# Lint all code
make lint                # Both Java and Go
make lint-java           # Checkstyle + Error Prone
make lint-go             # golangci-lint

# Quality assurance
make quality-check       # Comprehensive validation
make quality-report      # Detailed quality metrics
make pre-commit          # Full pre-commit pipeline
```

## 🐳 Docker & Deployment

### Local Development

```bash
# Start all services
make start

# Development mode with hot reload
make dev

# Check service status
make status
```

### Production

```bash
# Build and deploy
make deploy

# Production environment
make production
```

## 📈 Monitoring & Observability

### Health Checks

```bash
# Check all services
make health

# Individual service checks
curl http://localhost:8081/actuator/health  # Java Worker
curl http://localhost:8090/health           # Go API
```

### Metrics

```bash
# View metrics
make metrics

# Application metrics
curl http://localhost:8081/actuator/metrics
```

## 🔍 Troubleshooting

### Common Issues

1. **Services not starting**

   ```bash
   make reset          # Reset everything
   make debug          # Check debug info
   ```

2. **Kafka connection issues**

   ```bash
   make kafka-setup    # Recreate topics
   make logs-kafka     # Check Kafka logs
   ```

3. **Test failures**

   ```bash
   make clean          # Clean build artifacts
   make test           # Re-run tests
   ```

### Logs

```bash
# All services
make logs

# Specific service
make logs-java
make logs-go
make logs-kafka

# With timestamps
make logs-all
```

## 📚 API Documentation

### Interactive Swagger UI

The project includes comprehensive OpenAPI 3.0 documentation with interactive
Swagger UI for easy API exploration and testing.

**🎯 Access Swagger UI:**

```bash
# Start the application
make start

# Access Swagger UI
http://localhost:8081/swagger-ui.html

# Access OpenAPI JSON
http://localhost:8081/v3/api-docs
```

### Code Documentation (Javadoc + Godoc)

The project generates comprehensive code documentation for both Java and Go
components.

#### **Java Documentation (Javadoc)**

**Generate and View Java Documentation:**

```bash
# Generate Java documentation
make docs-java

# Generate and open in browser
make open-docs

# Manual access
open build/docs/javadoc/index.html
```

**Features:**

- ✅ Complete API documentation with JavaDoc comments
- ✅ Class hierarchy and inheritance diagrams
- ✅ Method signatures and parameter descriptions
- ✅ Package organization and structure
- ✅ Search functionality
- ✅ Cross-references between classes

#### **Go Documentation (Godoc)**

**Generate and View Go Documentation:**

```bash
# Start Go documentation server
make docs-go

# Open Go documentation in browser (requires server running)
make open-godoc

# Start both documentation servers
make docs-serve
```

**Features:**

- ✅ Interactive documentation server at `http://localhost:6060`
- ✅ Package documentation with examples
- ✅ Function signatures and documentation
- ✅ Type definitions and interfaces
- ✅ Source code browsing
- ✅ Cross-package references

#### **Documentation Commands Summary**

```bash
# Generate all documentation
make docs                    # Java + Go documentation

# Java-specific
make docs-java              # Generate Javadoc
make open-docs              # Generate and open Javadoc in browser

# Go-specific
make docs-go                # Start Godoc server
make open-godoc             # Open Godoc in browser (requires server)
make docs-serve             # Start both documentation servers

# Manual access
# Java: build/docs/javadoc/index.html
# Go: http://localhost:6060/pkg/enricher-api-go/
```

#### **Documentation Structure**

**Java Documentation:**

```
build/docs/javadoc/
├── index.html              # Main documentation page
├── com/resilient/orderworker/
│   ├── order/              # Order processing classes
│   ├── customer/           # Customer enrichment classes
│   ├── product/            # Product enrichment classes
│   └── infrastructure/     # Technical infrastructure
└── resources/              # CSS, JS, images
```

**Go Documentation:**

```
http://localhost:6060/pkg/enricher-api-go/
├── cmd/server/             # Application entry point
├── internal/customer/      # Customer domain
├── internal/product/       # Product domain
└── pkg/                    # Public packages
```

### Java Spring Boot API Endpoints

**Order Management API:**

| Method | Endpoint                               | Description             | Response              |
| ------ | -------------------------------------- | ----------------------- | --------------------- |
| `GET`  | `/api/v1/orders/{orderId}`             | Get order by ID         | Single enriched order |
| `GET`  | `/api/v1/orders`                       | List orders (paginated) | Paginated order list  |
| `GET`  | `/api/v1/orders/customer/{customerId}` | Get orders by customer  | Customer's order list |

**Management & Monitoring:**

| Method | Endpoint            | Description              | Response      |
| ------ | ------------------- | ------------------------ | ------------- |
| `GET`  | `/actuator/health`  | Application health check | Health status |
| `GET`  | `/actuator/metrics` | Application metrics      | Metrics data  |
| `GET`  | `/actuator/info`    | Application information  | App info      |

### Go Enrichment API Endpoints

**Customer Enrichment:**

| Method   | Endpoint                    | Description           | Response         |
| -------- | --------------------------- | --------------------- | ---------------- |
| `GET`    | `/v1/customers`             | List all customers    | Customer array   |
| `GET`    | `/v1/customers/{id}`        | Get customer details  | Customer object  |
| `GET`    | `/v1/customers/{id}/status` | Check customer status | Status info      |
| `POST`   | `/v1/customers`             | Create new customer   | Created customer |
| `PUT`    | `/v1/customers/{id}`        | Update customer       | Updated customer |
| `DELETE` | `/v1/customers/{id}`        | Delete customer       | Success status   |

**Product Enrichment:**

| Method   | Endpoint                         | Description         | Response        |
| -------- | -------------------------------- | ------------------- | --------------- |
| `GET`    | `/v1/products`                   | List all products   | Product array   |
| `GET`    | `/v1/products/{id}`              | Get product details | Product object  |
| `GET`    | `/v1/products/{id}/availability` | Check availability  | Stock status    |
| `POST`   | `/v1/products`                   | Create new product  | Created product |
| `PUT`    | `/v1/products/{id}`              | Update product      | Updated product |
| `DELETE` | `/v1/products/{id}`              | Delete product      | Success status  |

**Health Check:**

| Method | Endpoint  | Description          | Response      |
| ------ | --------- | -------------------- | ------------- |
| `GET`  | `/health` | Service health check | Health status |

### API Response Examples

**Order Response:**

```json
{
  "orderId": "order-12345",
  "customerId": "customer-67890",
  "customerName": "John Doe",
  "customerStatus": "ACTIVE",
  "products": [
    {
      "productId": "product-001",
      "name": "Gaming Laptop",
      "description": "High-performance gaming laptop with RTX graphics",
      "price": 1299.99,
      "category": "Electronics",
      "inStock": true
    }
  ],
  "totalAmount": 1299.99,
  "processedAt": "2025-01-08T10:30:00",
  "status": "COMPLETED"
}
```

**Paginated Response:**

```json
{
  "content": [...],
  "page": 0,
  "size": 20,
  "totalElements": 150,
  "totalPages": 8,
  "first": true,
  "last": false,
  "hasNext": true,
  "hasPrevious": false
}
```

### Swagger Features

**📋 Comprehensive Documentation:**

- Complete API specification with OpenAPI 3.0
- Interactive endpoint testing
- Request/response examples
- Parameter validation details
- Error response documentation

**🔧 Development Features:**

- Try-it-out functionality for all endpoints
- Real-time request/response testing
- Copy as cURL commands
- Response time measurement
- Schema validation

**📊 Advanced Features:**

- Pagination support documentation
- Filter parameter examples
- Sort options specification
- Security scheme definitions (for future authentication)
- Actuator endpoints integration

### Testing with Swagger UI

1. **Start the application:**

   ```bash
   make start
   ```

2. **Open Swagger UI:**

   ```
   http://localhost:8081/swagger-ui.html
   ```

3. **Test endpoints:**

   - Click on any endpoint
   - Click "Try it out"
   - Fill in parameters
   - Click "Execute"
   - View response

4. **Example workflow:**

   ```bash
   # Get all orders
   GET /api/v1/orders

   # Get specific order
   GET /api/v1/orders/order-12345

   # Filter by status
   GET /api/v1/orders?status=COMPLETED

   # Paginate results
   GET /api/v1/orders?page=0&size=10&sort=processedAt,desc
   ```

## 🎯 Performance & Scalability

### Optimizations

- **Reactive Programming** - Non-blocking I/O
- **Connection Pooling** - Efficient resource usage
- **Caching** - Redis-based caching
- **Parallel Processing** - Concurrent message processing
- **Database Indexing** - Optimized queries

### Scaling

- **Horizontal Scaling** - Multiple worker instances
- **Kafka Partitioning** - Parallel message processing
- **Load Balancing** - Service discovery ready
- **Resource Management** - Memory and CPU optimization

## 🤝 Contributing

### Development Setup

1. **Clone the repository**

   ```bash
   git clone https://github.com/alevelazcode/resilient-order-enricher
   cd order-worker
   ```

2. **Setup environment configuration**

   ```bash
   # Copy and customize environment variables
   cp env.sample .env

   # Edit configuration as needed
   vi .env
   ```

3. **Install development tools**

   ```bash
   make install-tools
   ```

4. **Start development environment**

   ```bash
   make dev
   ```

5. **Run tests**

   ```bash
   make test
   ```

6. **Check code quality**

   ```bash
   make lint
   make format
   ```

### Code Standards

- **Java**: Follow Spring Boot conventions
- **Go**: Follow Go best practices
- **Testing**: Minimum 80% coverage
- **Documentation**: Comprehensive README and inline docs

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file
for details.

## 🙏 Acknowledgments

- **Nelson Djalo** - Feature-based architecture inspiration
- **Spring Boot Team** - Excellent framework
- **Echo Framework** - Lightweight Go web framework
- **Apache Kafka** - Reliable message streaming
- **MongoDB** - Flexible document database
- **Redis** - Fast in-memory data store

---
