# Resilient Order Enricher — developer Makefile
#
# This file intentionally stays small. Most build logic lives in Gradle for the
# Java worker and `go` for the enrichment service. The Makefile only orchestrates
# the docker stack and offers a few convenience shortcuts.

GO_PROJECT := services/enricher-api-go

GREEN := \033[0;32m
YELLOW := \033[1;33m
NC := \033[0m

.DEFAULT_GOAL := help

# ---------------------------------------------------------------------------
# Help
# ---------------------------------------------------------------------------

.PHONY: help
help: ## Show this help message
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort \
	  | awk 'BEGIN {FS = ":.*?## "}; {printf "  $(GREEN)%-20s$(NC) %s\n", $$1, $$2}'

# ---------------------------------------------------------------------------
# Docker stack
# ---------------------------------------------------------------------------

.PHONY: up
up: ## Start the full stack with Docker Compose (build images as needed)
	docker compose up -d --build
	@echo "$(GREEN)Stack started$(NC)"
	@echo "  Worker:   http://localhost:8081"
	@echo "  Swagger:  http://localhost:8081/swagger-ui.html"
	@echo "  Go API:   http://localhost:8090"

.PHONY: down
down: ## Stop the full stack
	docker compose down

.PHONY: restart
restart: down up ## Restart the full stack

.PHONY: status
status: ## Show docker-compose service status
	docker compose ps

.PHONY: logs
logs: ## Tail logs for the full stack
	docker compose logs -f

.PHONY: logs-worker
logs-worker: ## Tail logs for the Java worker only
	docker compose logs -f order-worker

.PHONY: logs-api
logs-api: ## Tail logs for the Go enrichment API
	docker compose logs -f enricher-api

# ---------------------------------------------------------------------------
# Java
# ---------------------------------------------------------------------------

.PHONY: java-build
java-build: ## Compile and package the worker jar
	./gradlew clean bootJar

.PHONY: java-test
java-test: ## Run the Java unit + slice tests
	./gradlew test

.PHONY: java-check
java-check: ## Run spotless + checkstyle + tests
	./gradlew check

.PHONY: java-format
java-format: ## Apply spotless formatting
	./gradlew spotlessApply

.PHONY: java-run
java-run: ## Run the worker against locally-installed dependencies
	SPRING_PROFILES_ACTIVE=dev ./gradlew bootRun

# ---------------------------------------------------------------------------
# Go enrichment API
# ---------------------------------------------------------------------------

.PHONY: go-build
go-build: ## Build the Go enrichment API binary
	cd $(GO_PROJECT) && go build -o bin/enricher-api ./cmd/server

.PHONY: go-test
go-test: ## Run the Go tests
	cd $(GO_PROJECT) && go test ./...

# ---------------------------------------------------------------------------
# Convenience
# ---------------------------------------------------------------------------

.PHONY: send-test-message
send-test-message: ## Publish a sample order onto the orders topic
	./scripts/send-test-message.sh test-$$(date +%s)

.PHONY: check-mongo
check-mongo: ## Inspect orders persisted in MongoDB
	docker compose exec -T mongo mongosh --quiet --eval \
	  "db = db.getSiblingDB('order_worker'); db.orders.find().pretty()"

.PHONY: check-redis
check-redis: ## List all keys currently in Redis
	docker compose exec -T redis redis-cli KEYS '*'

.PHONY: health
health: ## Hit the worker actuator health endpoint
	@curl -fsS http://localhost:8081/actuator/health | tee /dev/null

# ---------------------------------------------------------------------------
# Cleanup
# ---------------------------------------------------------------------------

.PHONY: clean
clean: ## Remove Java + Go build artifacts and docker volumes
	@echo "$(YELLOW)Removing build artifacts$(NC)"
	./gradlew clean
	cd $(GO_PROJECT) && rm -rf bin
	docker compose down -v
