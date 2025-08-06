# Resilient Order Enricher

A robust, scalable order processing system built with **Java Spring Boot** and **Go**, featuring Kafka message consumption, data enrichment, MongoDB storage, and Redis-based resilience patterns.

## 🚀 Quick Start with Makefile

The project includes a comprehensive **Makefile** with developer-friendly shortcuts for all common tasks:

### 📋 Available Commands

```bash
# Show all available commands
make help

# Quick Start Commands
make start          # Start all services
make test           # Run all tests
make logs           # Show all logs
make clean          # Clean everything
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

# Utilities
make kafka-setup    # Setup Kafka topics
make send-test-message  # Send test message to Kafka
make demo           # Start demo environment with test message
make reset          # Reset everything and start fresh

# Shell Access
make shell-java     # Open shell in Java container
make shell-go       # Open shell in Go container
make shell-mongo    # Open MongoDB shell

# Cleaning
make clean          # Clean everything
make clean-java     # Clean Java project
make clean-go       # Clean Go project
make clean-docker   # Clean Docker containers
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

This project implements a **feature-based architecture** that organizes code by business functionality rather than technical layers. This architectural pattern was chosen specifically for this order processing system because:

**Why Feature-Based for Order Processing?**

- **Business Domain Alignment**: Each feature (`order/`, `customer/`, `product/`) represents a distinct business domain in the order enrichment workflow
- **Team Scalability**: Multiple developers can work on different features without conflicts
- **Maintainability**: Changes to order processing logic don't affect customer or product features
- **Testing Isolation**: Each feature can be tested independently with its own test suite
- **Deployment Flexibility**: Features can be extracted into microservices if needed

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

The project uses environment variables for configuration. Copy `.env.sample` to `.env` to customize:

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

See `env.sample` for the complete configuration reference with all available options.

### Resilience Patterns

- ✅ **Circuit Breaker** - Prevents cascading failures
- ✅ **Exponential Retry** - Handles transient failures
- ✅ **Distributed Locking** - Prevents duplicate processing
- ✅ **Dead Letter Queue** - Handles failed messages
- ✅ **Health Checks** - Service monitoring

## 🧪 Testing Strategy

The project implements comprehensive testing at multiple levels to ensure reliability and maintainability.

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

### Go API Endpoints

- `GET /health` - Health check
- `GET /v1/customers` - List customers
- `GET /v1/customers/:id` - Get customer details
- `GET /v1/products` - List products
- `GET /v1/products/:id` - Get product details

### Java Worker Endpoints

- `GET /actuator/health` - Health check
- `GET /actuator/metrics` - Application metrics

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

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- **Nelson Djalo** - Feature-based architecture inspiration
- **Spring Boot Team** - Excellent framework
- **Echo Framework** - Lightweight Go web framework
- **Apache Kafka** - Reliable message streaming
- **MongoDB** - Flexible document database
- **Redis** - Fast in-memory data store

---

**Happy Coding! 🚀**
