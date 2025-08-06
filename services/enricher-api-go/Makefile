# Go Enricher API - Code Quality Makefile
# =======================================

# Variables
GOCMD=go
GOBUILD=$(GOCMD) build
GOCLEAN=$(GOCMD) clean
GOTEST=$(GOCMD) test
GOGET=$(GOCMD) get
GOMOD=$(GOCMD) mod
GOFMT=gofmt
GOIMPORTS=goimports
GOFUMPT=gofumpt
GOLANGCI_LINT=golangci-lint

# Directories
SRC_DIR=./...
CMD_DIR=./cmd/server
BINARY_NAME=enricher-api
BUILD_DIR=./bin

# Colors
GREEN := \033[0;32m
YELLOW := \033[1;33m
RED := \033[0;31m
BLUE := \033[0;34m
NC := \033[0m # No Color

.PHONY: help
help: ## Show this help message
	@echo "$(BLUE)Go Enricher API - Code Quality Commands$(NC)"
	@echo "========================================"
	@echo ""
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "  $(GREEN)%-20s$(NC) %s\n", $$1, $$2}'

# =============================================================================
# FORMATTING
# =============================================================================

.PHONY: fmt
fmt: ## Format Go code with gofmt
	@echo "$(BLUE)Running gofmt...$(NC)"
	$(GOFMT) -s -w .
	@echo "$(GREEN)Code formatted with gofmt!$(NC)"

.PHONY: fmt-imports
fmt-imports: ## Format imports with goimports
	@echo "$(BLUE)Running goimports...$(NC)"
	$(GOIMPORTS) -w -local enricher-api-go .
	@echo "$(GREEN)Imports formatted!$(NC)"

.PHONY: fmt-fumpt
fmt-fumpt: ## Format code with gofumpt (stricter)
	@echo "$(BLUE)Running gofumpt...$(NC)"
	$(GOFUMPT) -w -extra .
	@echo "$(GREEN)Code formatted with gofumpt!$(NC)"

.PHONY: format
format: fmt-fumpt fmt-imports ## Run all formatters

# =============================================================================
# LINTING
# =============================================================================

.PHONY: lint
lint: ## Run golangci-lint
	@echo "$(BLUE)Running golangci-lint...$(NC)"
	$(GOLANGCI_LINT) run $(SRC_DIR)
	@echo "$(GREEN)Linting completed!$(NC)"

.PHONY: lint-fix
lint-fix: ## Run golangci-lint with auto-fix
	@echo "$(BLUE)Running golangci-lint with auto-fix...$(NC)"
	$(GOLANGCI_LINT) run --fix $(SRC_DIR)
	@echo "$(GREEN)Linting with auto-fix completed!$(NC)"

.PHONY: lint-verbose
lint-verbose: ## Run golangci-lint with verbose output
	@echo "$(BLUE)Running golangci-lint (verbose)...$(NC)"
	$(GOLANGCI_LINT) run -v $(SRC_DIR)

# =============================================================================
# CODE QUALITY CHECKS
# =============================================================================

.PHONY: vet
vet: ## Run go vet
	@echo "$(BLUE)Running go vet...$(NC)"
	$(GOCMD) vet $(SRC_DIR)
	@echo "$(GREEN)go vet completed!$(NC)"

.PHONY: check-fmt
check-fmt: ## Check if code is formatted
	@echo "$(BLUE)Checking code formatting...$(NC)"
	@unformatted=$$($(GOFMT) -l .); \
	if [ -n "$$unformatted" ]; then \
		echo "$(RED)The following files are not formatted:$(NC)"; \
		echo "$$unformatted"; \
		exit 1; \
	else \
		echo "$(GREEN)All files are properly formatted!$(NC)"; \
	fi

.PHONY: check-imports
check-imports: ## Check if imports are properly organized
	@echo "$(BLUE)Checking import organization...$(NC)"
	@unformatted=$$($(GOIMPORTS) -l .); \
	if [ -n "$$unformatted" ]; then \
		echo "$(RED)The following files have unorganized imports:$(NC)"; \
		echo "$$unformatted"; \
		exit 1; \
	else \
		echo "$(GREEN)All imports are properly organized!$(NC)"; \
	fi

.PHONY: check-fumpt
check-fumpt: ## Check if code is formatted with gofumpt
	@echo "$(BLUE)Checking gofumpt formatting...$(NC)"
	@unformatted=$$($(GOFUMPT) -l .); \
	if [ -n "$$unformatted" ]; then \
		echo "$(RED)The following files are not gofumpt formatted:$(NC)"; \
		echo "$$unformatted"; \
		exit 1; \
	else \
		echo "$(GREEN)All files are properly gofumpt formatted!$(NC)"; \
	fi

.PHONY: check
check: check-fumpt check-imports vet lint ## Run all checks

# =============================================================================
# TESTING
# =============================================================================

.PHONY: test
test: ## Run tests
	@echo "$(BLUE)Running tests...$(NC)"
	$(GOTEST) -v $(SRC_DIR)
	@echo "$(GREEN)Tests completed!$(NC)"

.PHONY: test-coverage
test-coverage: ## Run tests with coverage
	@echo "$(BLUE)Running tests with coverage...$(NC)"
	$(GOTEST) -v -coverprofile=coverage.out $(SRC_DIR)
	$(GOCMD) tool cover -html=coverage.out -o coverage.html
	@echo "$(GREEN)Coverage report generated: coverage.html$(NC)"

.PHONY: test-race
test-race: ## Run tests with race detector
	@echo "$(BLUE)Running tests with race detector...$(NC)"
	$(GOTEST) -v -race $(SRC_DIR)
	@echo "$(GREEN)Race tests completed!$(NC)"

# =============================================================================
# BUILD
# =============================================================================

.PHONY: build
build: ## Build the application
	@echo "$(BLUE)Building application...$(NC)"
	mkdir -p $(BUILD_DIR)
	$(GOBUILD) -o $(BUILD_DIR)/$(BINARY_NAME) $(CMD_DIR)
	@echo "$(GREEN)Build completed: $(BUILD_DIR)/$(BINARY_NAME)$(NC)"

.PHONY: clean
clean: ## Clean build artifacts
	@echo "$(BLUE)Cleaning build artifacts...$(NC)"
	$(GOCLEAN)
	rm -rf $(BUILD_DIR)
	rm -f coverage.out coverage.html
	@echo "$(GREEN)Clean completed!$(NC)"

# =============================================================================
# DEPENDENCIES
# =============================================================================

.PHONY: deps
deps: ## Download dependencies
	@echo "$(BLUE)Downloading dependencies...$(NC)"
	$(GOGET) -d -v $(SRC_DIR)
	@echo "$(GREEN)Dependencies downloaded!$(NC)"

.PHONY: deps-update
deps-update: ## Update dependencies
	@echo "$(BLUE)Updating dependencies...$(NC)"
	$(GOGET) -u -d -v $(SRC_DIR)
	$(GOMOD) tidy
	@echo "$(GREEN)Dependencies updated!$(NC)"

.PHONY: deps-verify
deps-verify: ## Verify dependencies
	@echo "$(BLUE)Verifying dependencies...$(NC)"
	$(GOMOD) verify
	@echo "$(GREEN)Dependencies verified!$(NC)"

.PHONY: mod-tidy
mod-tidy: ## Tidy go.mod
	@echo "$(BLUE)Tidying go.mod...$(NC)"
	$(GOMOD) tidy
	@echo "$(GREEN)go.mod tidied!$(NC)"

# =============================================================================
# TOOLS INSTALLATION
# =============================================================================

.PHONY: install-tools
install-tools: ## Install required tools
	@echo "$(BLUE)Installing Go tools...$(NC)"
	$(GOGET) -u golang.org/x/tools/cmd/goimports
	$(GOGET) -u mvdan.cc/gofumpt
	@echo "$(BLUE)Installing golangci-lint...$(NC)"
	curl -sSfL https://raw.githubusercontent.com/golangci/golangci-lint/master/install.sh | sh -s -- -b $$(go env GOPATH)/bin v1.55.2
	@echo "$(GREEN)Tools installed!$(NC)"

.PHONY: check-tools
check-tools: ## Check if tools are installed
	@echo "$(BLUE)Checking tools installation...$(NC)"
	@command -v goimports >/dev/null 2>&1 || { echo "$(RED)goimports not found$(NC)"; exit 1; }
	@command -v gofumpt >/dev/null 2>&1 || { echo "$(RED)gofumpt not found$(NC)"; exit 1; }
	@command -v golangci-lint >/dev/null 2>&1 || { echo "$(RED)golangci-lint not found$(NC)"; exit 1; }
	@echo "$(GREEN)All tools are installed!$(NC)"

# =============================================================================
# PRE-COMMIT HOOKS
# =============================================================================

.PHONY: pre-commit
pre-commit: format check test ## Run pre-commit checks
	@echo "$(GREEN)Pre-commit checks passed!$(NC)"

.PHONY: ci
ci: check-tools check test-coverage test-race ## Run CI pipeline
	@echo "$(GREEN)CI pipeline completed!$(NC)"

# =============================================================================
# DEVELOPMENT
# =============================================================================

.PHONY: run
run: ## Run the application
	@echo "$(BLUE)Running application...$(NC)"
	$(GOCMD) run $(CMD_DIR)/main.go

.PHONY: dev
dev: format check ## Development workflow
	@echo "$(GREEN)Development checks completed!$(NC)"

# =============================================================================
# REPORTING
# =============================================================================

.PHONY: report
report: ## Generate code quality report
	@echo "$(BLUE)Generating code quality report...$(NC)"
	@echo "=== Go Code Quality Report ===" > quality-report.txt
	@echo "Generated on: $$(date)" >> quality-report.txt
	@echo "" >> quality-report.txt
	@echo "=== Linting Results ===" >> quality-report.txt
	-$(GOLANGCI_LINT) run --out-format=tab $(SRC_DIR) >> quality-report.txt 2>&1
	@echo "" >> quality-report.txt
	@echo "=== Test Coverage ===" >> quality-report.txt
	-$(GOTEST) -coverprofile=coverage.out $(SRC_DIR) >> quality-report.txt 2>&1
	@echo "$(GREEN)Quality report generated: quality-report.txt$(NC)"

# =============================================================================
# ALIASES
# =============================================================================

.PHONY: f
f: format ## Alias for format

.PHONY: l
l: lint ## Alias for lint

.PHONY: c
c: check ## Alias for check

.PHONY: t
t: test ## Alias for test