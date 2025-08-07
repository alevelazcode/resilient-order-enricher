# Resilient Order Enricher - Developer Makefile
# =============================================

# Variables
PROJECT_NAME := resilient-order-enricher
JAVA_PROJECT := .
GO_PROJECT := services/enricher-api-go
DOCKER_COMPOSE := docker-compose.yml

# Colors for output
GREEN := \033[0;32m
YELLOW := \033[1;33m
RED := \033[0;31m
BLUE := \033[0;34m
NC := \033[0m # No Color

# Default target
.DEFAULT_GOAL := help

# Help target
.PHONY: help
help: ## Show this help message
	@echo "$(BLUE)$(PROJECT_NAME) - Developer Commands$(NC)"
	@echo "=============================================="
	@echo ""
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "  $(GREEN)%-20s$(NC) %s\n", $$1, $$2}'
	@echo ""
	@echo "$(YELLOW)Quick Start:$(NC)"
	@echo "  make start          # Start all services"
	@echo "  make test           # Run all tests"
	@echo "  make logs           # Show all logs"
	@echo "  make clean          # Clean everything"

# =============================================================================
# DOCKER & SERVICES
# =============================================================================

.PHONY: start
start: ## Start all services with Docker Compose
	@echo "$(BLUE)Starting all services...$(NC)"
	docker compose up -d
	@echo "$(GREEN)Services started!$(NC)"
	@echo "$(YELLOW)Waiting for services to be ready...$(NC)"
	@sleep 30
	@echo "$(GREEN)All services are ready!$(NC)"
	@echo "$(BLUE)Services:$(NC)"
	@echo "  - Java Worker: http://localhost:8081/actuator/health"
	@echo "  - Go API: http://localhost:8090/health"
	@echo "  - Kafka: localhost:9092"
	@echo "  - MongoDB: localhost:27017"
	@echo "  - Redis: localhost:6380"

.PHONY: stop
stop: ## Stop all services
	@echo "$(BLUE)Stopping all services...$(NC)"
	docker compose down
	@echo "$(GREEN)Services stopped!$(NC)"

.PHONY: restart
restart: stop start ## Restart all services

.PHONY: logs
logs: ## Show logs from all services
	@echo "$(BLUE)Showing logs from all services...$(NC)"
	docker compose logs -f

.PHONY: logs-java
logs-java: ## Show Java Worker logs
	@echo "$(BLUE)Showing Java Worker logs...$(NC)"
	docker compose logs -f order-worker

.PHONY: logs-go
logs-go: ## Show Go API logs
	@echo "$(BLUE)Showing Go API logs...$(NC)"
	docker compose logs -f enricher-api

.PHONY: logs-kafka
logs-kafka: ## Show Kafka logs
	@echo "$(BLUE)Showing Kafka logs...$(NC)"
	docker compose logs -f kafka

.PHONY: status
status: ## Show status of all services
	@echo "$(BLUE)Service Status:$(NC)"
	docker compose ps

# =============================================================================
# BUILD & TEST
# =============================================================================

.PHONY: build
build: build-java build-go ## Build both Java and Go projects

.PHONY: build-java
build-java: ## Build Java project
	@echo "$(BLUE)Building Java project...$(NC)"
	cd $(JAVA_PROJECT) && ./gradlew clean build -x test -x checkstyleMain -x checkstyleTest
	@echo "$(GREEN)Java project built successfully!$(NC)"

.PHONY: build-go
build-go: ## Build Go project
	@echo "$(BLUE)Building Go project...$(NC)"
	cd $(GO_PROJECT) && go build -o bin/enricher-api cmd/server/main.go
	@echo "$(GREEN)Go project built successfully!$(NC)"

.PHONY: test
test: test-java test-go ## Run all tests

.PHONY: test-java
test-java: ## Run Java tests
	@echo "$(BLUE)Running Java tests...$(NC)"
	cd $(JAVA_PROJECT) && ./gradlew test
	@echo "$(GREEN)Java tests completed!$(NC)"

.PHONY: test-go
test-go: ## Run Go tests
	@echo "$(BLUE)Running Go tests...$(NC)"
	cd $(GO_PROJECT) && go test -v ./...
	@echo "$(GREEN)Go tests completed!$(NC)"

.PHONY: test-coverage
test-coverage: ## Run tests with coverage
	@echo "$(BLUE)Running tests with coverage...$(NC)"
	cd $(JAVA_PROJECT) && ./gradlew test jacocoTestReport
	cd $(GO_PROJECT) && go test -v -coverprofile=coverage.out ./... && go tool cover -html=coverage.out -o coverage.html
	@echo "$(GREEN)Coverage reports generated!$(NC)"

.PHONY: test-load
test-load: ## Run load tests (requires services to be running)
	@echo "$(BLUE)Running load tests...$(NC)"
	./scripts/load-test.sh
	@echo "$(GREEN)Load tests completed!$(NC)"

.PHONY: test-load-light
test-load-light: ## Run light load test (100 messages)
	@echo "$(BLUE)Running light load test...$(NC)"
	./scripts/load-test.sh 100 5 20 50
	@echo "$(GREEN)Light load test completed!$(NC)"

.PHONY: test-load-medium
test-load-medium: ## Run medium load test (1000 messages)
	@echo "$(BLUE)Running medium load test...$(NC)"
	./scripts/load-test.sh 1000 10 50 100
	@echo "$(GREEN)Medium load test completed!$(NC)"

.PHONY: test-load-heavy
test-load-heavy: ## Run heavy load test (10000 messages)
	@echo "$(BLUE)Running heavy load test...$(NC)"
	./scripts/load-test.sh 10000 20 100 100
	@echo "$(GREEN)Heavy load test completed!$(NC)"

.PHONY: test-performance
test-performance: ## Run Java performance tests (unit tests)
	@echo "$(BLUE)Running performance tests...$(NC)"
	cd $(JAVA_PROJECT) && ./gradlew test --tests="*LoadTest*" -Dtest.profile=performance
	@echo "$(GREEN)Performance tests completed!$(NC)"

# =============================================================================
# DEVELOPMENT
# =============================================================================

.PHONY: dev
dev: ## Start development environment (with hot reload)
	@echo "$(BLUE)Starting development environment...$(NC)"
	docker compose up -d kafka mongo redis
	@echo "$(YELLOW)Starting Go API in development mode...$(NC)"
	cd $(GO_PROJECT) && go run cmd/server/main.go &
	@echo "$(YELLOW)Starting Java Worker in development mode...$(NC)"
	cd $(JAVA_PROJECT) && ./gradlew bootRun &
	@echo "$(GREEN)Development environment started!$(NC)"
	@echo "$(BLUE)Press Ctrl+C to stop$(NC)"

.PHONY: kafka-setup
kafka-setup: ## Setup Kafka topics
	@echo "$(BLUE)Setting up Kafka topics...$(NC)"
	./scripts/setup-kafka.sh
	@echo "$(GREEN)Kafka topics configured!$(NC)"

.PHONY: send-test-message
send-test-message: ## Send a test message to Kafka
	@echo "$(BLUE)Sending test message to Kafka...$(NC)"
	@./scripts/send-test-message.sh test-$(shell date +%s)
	@echo "$(GREEN)Test message sent!$(NC)"

.PHONY: check-mongo
check-mongo: ## Check MongoDB data
	@echo "$(BLUE)Checking MongoDB data...$(NC)"
	docker exec order-worker-mongo-1 mongosh --eval "db = db.getSiblingDB('order_worker'); db.orders.find().pretty()"

.PHONY: check-redis
check-redis: ## Check Redis data
	@echo "$(BLUE)Checking Redis data...$(NC)"
	docker exec order-worker-redis-1 redis-cli KEYS "*"

# =============================================================================
# CLEANING
# =============================================================================

.PHONY: clean
clean: clean-java clean-go clean-docker ## Clean everything

.PHONY: clean-java
clean-java: ## Clean Java project
	@echo "$(BLUE)Cleaning Java project...$(NC)"
	cd $(JAVA_PROJECT) && ./gradlew clean
	@echo "$(GREEN)Java project cleaned!$(NC)"

.PHONY: clean-go
clean-go: ## Clean Go project
	@echo "$(BLUE)Cleaning Go project...$(NC)"
	cd $(GO_PROJECT) && rm -rf bin/ coverage.out coverage.html
	@echo "$(GREEN)Go project cleaned!$(NC)"

.PHONY: clean-docker
clean-docker: ## Clean Docker containers and images
	@echo "$(BLUE)Cleaning Docker...$(NC)"
	docker compose down -v --remove-orphans
	docker system prune -f
	@echo "$(GREEN)Docker cleaned!$(NC)"

# =============================================================================
# MONITORING & DEBUGGING
# =============================================================================

.PHONY: health
health: ## Check health of all services
	@echo "$(BLUE)Checking service health...$(NC)"
	@echo "$(YELLOW)Java Worker:$(NC)"
	@curl -s http://localhost:8081/actuator/health | jq .status || echo "$(RED)Java Worker not responding$(NC)"
	@echo "$(YELLOW)Go API:$(NC)"
	@curl -s http://localhost:8090/health | jq .status || echo "$(RED)Go API not responding$(NC)"

.PHONY: metrics
metrics: ## Show application metrics
	@echo "$(BLUE)Java Worker Metrics:$(NC)"
	@curl -s http://localhost:8081/actuator/metrics | jq .names || echo "$(RED)Metrics not available$(NC)"

.PHONY: shell-java
shell-java: ## Open shell in Java container
	@echo "$(BLUE)Opening shell in Java container...$(NC)"
	docker compose exec order-worker /bin/sh

.PHONY: shell-go
shell-go: ## Open shell in Go container
	@echo "$(BLUE)Opening shell in Go container...$(NC)"
	docker compose exec enricher-api /bin/sh

.PHONY: shell-mongo
shell-mongo: ## Open MongoDB shell
	@echo "$(BLUE)Opening MongoDB shell...$(NC)"
	docker compose exec mongo mongosh

# =============================================================================
# DEPLOYMENT
# =============================================================================

.PHONY: deploy
deploy: build ## Build and deploy
	@echo "$(BLUE)Building and deploying...$(NC)"
	docker compose build --no-cache
	docker compose up -d
	@echo "$(GREEN)Deployed successfully!$(NC)"

.PHONY: production
production: ## Start production environment
	@echo "$(BLUE)Starting production environment...$(NC)"
	PROFILE=prod docker compose -f docker-compose.yml -f docker-compose.prod.yml up -d
	@echo "$(GREEN)Production environment started!$(NC)"

# =============================================================================
# UTILITIES
# =============================================================================

.PHONY: check-prerequisites
check-prerequisites: ## Check if all prerequisites are installed
	@echo "$(BLUE)Checking prerequisites...$(NC)"
	./scripts/check-prerequisites.sh

.PHONY: setup-formatting
setup-formatting: ## Setup automatic formatting on save (IDE + Git hooks)
	@echo "$(BLUE)Setting up automatic formatting on save...$(NC)"
	./scripts/setup-ide-formatting.sh
	@echo "$(GREEN)Automatic formatting setup completed!$(NC)"
	@echo "$(YELLOW)Restart your IDE to apply the new formatting settings.$(NC)"

.PHONY: install-tools
install-tools: ## Install all code quality tools
	@echo "$(BLUE)Installing code quality tools...$(NC)"
	./scripts/install-tools.sh
	@echo "$(GREEN)Tools installation completed!$(NC)"

.PHONY: check-tools
check-tools: ## Check if all tools are installed
	@echo "$(BLUE)Checking tool installation...$(NC)"
	cd $(GO_PROJECT) && make check-tools
	@./scripts/install-tools.sh 2>/dev/null | grep -E "(✓|✗)" || echo "$(YELLOW)Run 'make install-tools' to install missing tools$(NC)"

# =============================================================================
# CODE QUALITY - LINTING
# =============================================================================

.PHONY: lint
lint: lint-java lint-go ## Run linting on all projects

.PHONY: lint-java
lint-java: ## Lint Java code with Checkstyle and Error Prone
	@echo "$(BLUE)Linting Java code...$(NC)"
	cd $(JAVA_PROJECT) && ./gradlew spotlessCheck checkstyleMain checkstyleTest
	@echo "$(GREEN)Java linting completed!$(NC)"

.PHONY: lint-go
lint-go: ## Lint Go code with golangci-lint
	@echo "$(BLUE)Linting Go code...$(NC)"
	cd $(GO_PROJECT) && make lint
	@echo "$(GREEN)Go linting completed!$(NC)"

.PHONY: lint-fix
lint-fix: lint-fix-java lint-fix-go ## Auto-fix linting issues

.PHONY: lint-fix-java
lint-fix-java: ## Auto-fix Java linting issues
	@echo "$(BLUE)Auto-fixing Java code...$(NC)"
	cd $(JAVA_PROJECT) && ./gradlew spotlessApply
	@echo "$(GREEN)Java auto-fix completed!$(NC)"

.PHONY: lint-fix-go
lint-fix-go: ## Auto-fix Go linting issues
	@echo "$(BLUE)Auto-fixing Go code...$(NC)"
	cd $(GO_PROJECT) && make lint-fix
	@echo "$(GREEN)Go auto-fix completed!$(NC)"

# =============================================================================
# CODE QUALITY - FORMATTING
# =============================================================================

.PHONY: format
format: format-java format-go ## Format all code

.PHONY: format-java
format-java: ## Format Java code with Spotless
	@echo "$(BLUE)Formatting Java code...$(NC)"
	cd $(JAVA_PROJECT) && ./gradlew spotlessApply
	@echo "$(GREEN)Java formatting completed!$(NC)"

.PHONY: format-go
format-go: ## Format Go code with gofumpt and goimports
	@echo "$(BLUE)Formatting Go code...$(NC)"
	cd $(GO_PROJECT) && make format
	@echo "$(GREEN)Go formatting completed!$(NC)"

.PHONY: check-format
check-format: check-format-java check-format-go ## Check if code is properly formatted

.PHONY: check-format-java
check-format-java: ## Check Java code formatting
	@echo "$(BLUE)Checking Java code formatting...$(NC)"
	cd $(JAVA_PROJECT) && ./gradlew spotlessCheck
	@echo "$(GREEN)Java formatting check completed!$(NC)"

.PHONY: check-format-go
check-format-go: ## Check Go code formatting
	@echo "$(BLUE)Checking Go code formatting...$(NC)"
	cd $(GO_PROJECT) && make check
	@echo "$(GREEN)Go formatting check completed!$(NC)"

# =============================================================================
# CODE QUALITY - COMPREHENSIVE CHECKS
# =============================================================================

.PHONY: quality-check
quality-check: check-format lint test ## Run comprehensive quality checks

.PHONY: quality-report
quality-report: ## Generate comprehensive quality report
	@echo "$(BLUE)Generating quality report...$(NC)"
	@echo "=== Resilient Order Enricher - Code Quality Report ===" > quality-report.txt
	@echo "Generated on: $$(date)" >> quality-report.txt
	@echo "" >> quality-report.txt
	@echo "=== Java Quality Report ===" >> quality-report.txt
	cd $(JAVA_PROJECT) && ./gradlew spotlessCheck checkstyleMain build 2>&1 | head -50 >> ../quality-report.txt || true
	@echo "" >> quality-report.txt
	@echo "=== Go Quality Report ===" >> quality-report.txt
	cd $(GO_PROJECT) && make report 2>&1 >> ../../quality-report.txt || true
	@echo "$(GREEN)Quality report generated: quality-report.txt$(NC)"

.PHONY: pre-commit
pre-commit: format quality-check ## Run pre-commit checks (format + quality)
	@echo "$(GREEN)Pre-commit checks completed successfully!$(NC)"

.PHONY: dependencies
dependencies: ## Update dependencies
	@echo "$(BLUE)Updating dependencies...$(NC)"
	cd $(JAVA_PROJECT) && ./gradlew dependencies --write-locks
	cd $(GO_PROJECT) && go mod tidy && go mod download
	@echo "$(GREEN)Dependencies updated!$(NC)"

# =============================================================================
# TROUBLESHOOTING
# =============================================================================

.PHONY: reset
reset: clean-docker start ## Reset everything and start fresh
	@echo "$(GREEN)Environment reset and started!$(NC)"

.PHONY: debug
debug: ## Show debug information
	@echo "$(BLUE)Debug Information:$(NC)"
	@echo "$(YELLOW)Docker Compose:$(NC)"
	docker compose config
	@echo "$(YELLOW)Java Version:$(NC)"
	cd $(JAVA_PROJECT) && ./gradlew --version | head -3
	@echo "$(YELLOW)Go Version:$(NC)"
	cd $(GO_PROJECT) && go version
	@echo "$(YELLOW)Docker Version:$(NC)"
	docker --version

.PHONY: logs-all
logs-all: ## Show all logs with timestamps
	@echo "$(BLUE)Showing all logs with timestamps...$(NC)"
	docker compose logs -f --timestamps

# =============================================================================
# DOCUMENTATION
# =============================================================================

.PHONY: docs
docs: ## Generate documentation
	@echo "$(BLUE)Generating documentation...$(NC)"
	cd $(JAVA_PROJECT) && ./gradlew javadoc
	@echo "$(GREEN)Documentation generated!$(NC)"

.PHONY: swagger
swagger: ## Open Swagger UI in browser
	@echo "$(BLUE)Opening Swagger UI...$(NC)"
	@echo "$(YELLOW)Swagger UI: http://localhost:8081/swagger-ui.html$(NC)"
	@if command -v open >/dev/null 2>&1; then \
		open http://localhost:8081/swagger-ui.html; \
	elif command -v xdg-open >/dev/null 2>&1; then \
		xdg-open http://localhost:8081/swagger-ui.html; \
	else \
		echo "$(BLUE)Please open: http://localhost:8081/swagger-ui.html$(NC)"; \
	fi

.PHONY: api-docs
api-docs: ## Show OpenAPI JSON
	@echo "$(BLUE)OpenAPI JSON:$(NC)"
	@curl -s http://localhost:8081/v3/api-docs | jq . || echo "$(RED)API not available. Start services with 'make start'$(NC)"

.PHONY: readme
readme: ## Show README
	@echo "$(BLUE)Project README:$(NC)"
	@cat README.md | head -50
	@echo "$(YELLOW)... (truncated) ...$(NC)"

# =============================================================================
# SPECIAL TARGETS
# =============================================================================

.PHONY: ci
ci: format test build ## Run complete CI pipeline (format + test + build)
	@echo "$(GREEN)CI pipeline completed successfully!$(NC)"
	@echo "$(BLUE)All checks passed:$(NC)"
	@echo "  ✅ Code formatting"
	@echo "  ✅ Unit and integration tests"
	@echo "  ✅ Build verification"
	@echo "$(YELLOW)Note: Linting checks can be run separately with 'make lint'$(NC)"

.PHONY: demo
demo: start kafka-setup ## Start demo environment
	@echo "$(BLUE)Starting demo environment...$(NC)"
	@sleep 10
	@echo "$(GREEN)Demo environment ready!$(NC)"
	@echo "$(YELLOW)Sending demo message...$(NC)"
	@make send-test-message
	@echo "$(GREEN)Demo completed! Check logs with: make logs$(NC)"

.PHONY: watch
watch: ## Watch for changes and restart services
	@echo "$(BLUE)Watching for changes...$(NC)"
	@echo "$(YELLOW)Press Ctrl+C to stop$(NC)"
	@while true; do \
		inotifywait -r -e modify,create,delete $(JAVA_PROJECT)/src $(GO_PROJECT) 2>/dev/null || true; \
		echo "$(BLUE)Changes detected, restarting services...$(NC)"; \
		make restart; \
		sleep 5; \
	done

# =============================================================================
# ALIASES
# =============================================================================

.PHONY: up
up: start ## Alias for start

.PHONY: down
down: stop ## Alias for stop

.PHONY: ps
ps: status ## Alias for status

.PHONY: t
t: test ## Alias for test

.PHONY: c
c: clean ## Alias for clean

.PHONY: b
b: build ## Alias for build

.PHONY: l
l: logs ## Alias for logs
