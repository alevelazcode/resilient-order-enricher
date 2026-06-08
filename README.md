# Resilient Order Enricher

A Java 21 / Spring Boot 3 reactive worker that consumes order events from
Kafka, enriches them with customer and product data from a Go API,
validates them, and stores the result in MongoDB. Resilience is provided
by Resilience4j (circuit breaker + retry), Caffeine (per-instance cache),
Redisson (distributed locks + retry store on Redis), and a backoff-driven
retry scheduler.

The project is laid out in a **strict hexagonal architecture**: the
domain has no Spring or persistence dependencies, the application layer
talks only to ports, and the infrastructure layer plugs adapters into
those ports.

---

## Table of contents

1. [Architecture](#architecture)
2. [Module layout](#module-layout)
3. [Order processing flow](#order-processing-flow)
4. [Resilience model](#resilience-model)
5. [Running locally](#running-locally)
6. [Configuration](#configuration)
7. [API](#api)
8. [Testing](#testing)
9. [Build & quality gates](#build--quality-gates)
10. [Deployment](#deployment)
11. [Roadmap](#roadmap)

---

## Architecture

```
+-----------------------+      +-----------------------+
|  Kafka consumer       |      |  REST controller      |
|  (in-adapter)         |      |  (in-adapter)         |
+----------+------------+      +----------+------------+
           |                              |
           v                              v
   +-------+------------------------------+-------+
   |               application layer              |
   |   ProcessOrderUseCase / QueryOrdersUseCase   |
   |                                              |
   |   ports/out (Interface Segregation):         |
   |   OrderRepository       (write + idempotency)|
   |   OrderQueryRepository  (read side)          |
   |   CustomerProvider   ProductProvider         |
   |   DistributedLock    FailedMessageStore      |
   +----------+------------------+--------+-------+
              |                  |        |
              v                  v        v
   +----------+-----+  +---------+----+  +-+---------+
   | Mongo adapter  |  | HTTP adapter |  | Redis     |
   | (out-adapter)  |  | (out-adapter)|  | adapters  |
   +----------------+  +--------------+  +-----------+

           pure domain: Order, OrderLine, Customer, Product, OrderStatus
              (immutable records, no framework dependencies)
```

Key principles:

- **Dependency rule**: source dependencies point inward
  (`infrastructure → application → domain`). The domain depends on
  nothing.
- **Ports as contracts**: the application layer declares interfaces it
  needs (`OrderRepository`, `CustomerProvider`, …). Infrastructure
  adapters implement them; Spring wires the implementation at runtime.
- **Adapters are swappable**: distributed lock and failed-message store
  ship with a Noop fallback that activates when Redis is unavailable.

## Module layout

```
src/main/java/com/resilient/orderworker
├── OrderWorkerApplication.java
├── domain
│   ├── order/         (Order, OrderLine, OrderStatus)
│   ├── customer/      (Customer)
│   ├── product/       (Product)
│   └── exception/     (CustomerNotFoundException, …)
├── application
│   ├── command/       (ProcessOrderCommand)
│   ├── port/in/       (ProcessOrderUseCase, QueryOrdersUseCase)
│   ├── port/out/      (OrderRepository, OrderQueryRepository,
│   │                   CustomerProvider, ProductProvider,
│   │                   DistributedLock, FailedMessageStore)
│   └── service/       (OrderProcessor, OrderQueryService)
└── infrastructure
    ├── adapter
    │   ├── in/kafka/  (OrderKafkaConsumer, KafkaConfig, OrderMessagePayload)
    │   ├── in/rest/   (OrderController, OrderResponse, …)
    │   ├── out/mongo/ (OrderMongoAdapter, OrderDocument, …)
    │   ├── out/http/  (CustomerHttpAdapter, ProductHttpAdapter, WebClientConfig)
    │   └── out/redis/ (RedissonDistributedLockAdapter, RedisFailedMessageAdapter, …)
    └── config/        (OpenApiConfig)
```

## Order processing flow

1. **Kafka consumer** receives an `OrderMessagePayload` and converts it
   to an immutable `ProcessOrderCommand`.
2. The use case acquires a **distributed lock** keyed by `orderId` so
   only one worker instance processes a given order at a time.
3. **Idempotency check**: if an order with the same `orderId` already
   exists in Mongo, return it without re-enriching.
4. **Enrichment**: in parallel, fetch the customer and all products from
   the Go API. Each call is wrapped by a circuit breaker, retried with
   exponential backoff, and served from a per-instance Caffeine cache
   when possible.
5. **Validation**: the customer must be `ACTIVE`, and every product must
   be present in the response and pass `Product.isValid()` (non-blank id
   + name, positive price).
6. **Persistence**: build the `Order` aggregate (totals computed with
   `BigDecimal`) and save through `OrderRepository`.
7. **Failure path**: any failure is captured by the consumer, stored in
   the Redis-backed `FailedMessageStore` (with an attempt counter and
   exponential backoff), and the Kafka offset is acknowledged so the
   broker does not re-deliver. The scheduler retries entries when their
   next-attempt timestamp is due.

## Resilience model

- **Circuit breaker (Resilience4j)**: per outbound dependency
  (`customerService`, `productService`). Trips on failure rate or slow
  calls. While open, calls fail fast with
  `CallNotPermittedException`.
- **Time limiter (Resilience4j Reactor)**: 4-second hard cap on each
  outbound HTTP fetch via `TimeLimiterOperator`. Times out (and feeds
  the circuit breaker) before the downstream API can saturate the
  worker.
- **Retry (Resilience4j Reactor)**: applied around the network fetch
  (not around cache hits). Retries `ExternalServiceException` only.
  `CallNotPermittedException`, `TimeoutException`,
  `CustomerNotFoundException`, and `ProductNotFoundException` are
  explicitly ignored so retries do not amplify load when the circuit is
  open or the resource is missing.
- **Distributed lock**: Redisson `RLockReactive` keyed by
  `order-lock:{orderId}` with a wait + lease duration. Lock acquisition
  and unlock are fully non-blocking so the worker never holds a reactor
  thread waiting on Redis. Success and failure paths both unlock.
- **Idempotent persistence**: `OrderRepository.existsByOrderId` short
  circuits before any external call, preventing duplicate work on
  redelivery.
- **Failed-message retry**: failures are persisted to Redis with an
  attempt counter; `FailedMessageRetryScheduler` re-submits messages
  once their backoff window elapses. The backoff is governed by the
  `BackoffPolicy` strategy (`ExponentialBackoffPolicy` by default:
  1s → 2s → 4s → 8s → 16s capped at 5 minutes, 5 attempts max). After
  the cap the message moves to a dead-letter set for manual inspection.
- **Reactive cache**: replaces the broken `@Cacheable` pattern. Caffeine
  stores resolved domain objects (`Customer`, `Product`) keyed by id,
  with TTL eviction.

## Running locally

### Prerequisites

- JDK 21 (the project uses Gradle toolchains, so any installed JDK 21 is
  fine).
- Docker + Docker Compose.

### Spin up the full stack

```bash
docker compose up -d --build
```

This starts Kafka (KRaft mode, no Zookeeper), MongoDB 8, Redis 7, the Go
enrichment API, and the Java worker. Healthchecks gate startup so the
worker waits until its dependencies are ready.

- Worker HTTP: <http://localhost:8081>
- Swagger UI: <http://localhost:8081/swagger-ui.html>
- Actuator: <http://localhost:8081/actuator/health>
- Go enrichment API: <http://localhost:8090>

### Push a test message

```bash
./scripts/send-test-message.sh order-123
```

The script publishes a sample order to the `orders` Kafka topic and the
worker enriches it.

### Run the worker against locally-installed dependencies

```bash
SPRING_PROFILES_ACTIVE=dev ./gradlew bootRun
```

`application-dev.yml` points at `localhost` Kafka/Mongo/Redis and uses a
relaxed circuit-breaker configuration suitable for development.

## Configuration

| Property                         | Default                        | Purpose                              |
|----------------------------------|--------------------------------|--------------------------------------|
| `SERVER_PORT`                    | `8081`                         | HTTP port                            |
| `KAFKA_BOOTSTRAP_SERVERS`        | `localhost:9092`               | Kafka brokers                        |
| `KAFKA_ORDERS_TOPIC`             | `orders`                       | Topic to consume                     |
| `KAFKA_CONSUMER_GROUP`           | `order-worker-group`           | Consumer group id                    |
| `MONGODB_URI`                    | `mongodb://localhost:27017`    | Mongo connection                     |
| `MONGODB_DATABASE`               | `order_worker`                 | Mongo database                       |
| `REDIS_HOST` / `REDIS_PORT`      | `localhost` / `6379`           | Redis connection                     |
| `ENRICHER_API_BASE_URL`          | `http://localhost:8080`        | Go enrichment API base URL           |
| `LOG_LEVEL`                      | `INFO`                         | Logger level for app packages        |

Profiles ship in `src/main/resources`:

- `application.yml` — base configuration (used everywhere).
- `application-dev.yml` — local development.
- `application-prod.yml` — production (requires `MONGODB_URI`,
  `REDIS_HOST`, etc., to be set via environment).
- `application-test.yml` — disables Kafka/Mongo/Redis autoconfiguration
  and Redisson so the test slice can boot without external services.

## API

The REST surface is intentionally small (the worker is event driven
first; HTTP is for reads):

| Method | Path                                  | Description                  |
|--------|---------------------------------------|------------------------------|
| GET    | `/api/v1/orders/{orderId}`            | Fetch a single order         |
| GET    | `/api/v1/orders`                      | Paginate + filter orders     |
| GET    | `/api/v1/orders/customer/{customerId}`| Paginated orders for a customer (`page`, `size` query params) |
| GET    | `/actuator/health`                    | Health probe                 |
| GET    | `/actuator/prometheus`                | Metrics in Prometheus format |

Errors follow a single shape (`ErrorResponse`):

```json
{
  "code": "ORDER_NOT_FOUND",
  "message": "Order with ID 'order-99999' was not found",
  "timestamp": "2025-01-08T10:30:00Z"
}
```

The full OpenAPI document is served at `/v3/api-docs` and rendered by
Swagger UI at `/swagger-ui.html`.

## Testing

```bash
./gradlew test            # unit + slice tests (no docker)
./gradlew integrationTest # end-to-end with Kafka + Mongo + Redis testcontainers
```

Test layout:

- `domain/` — pure record / value-object tests (no Spring).
- `application/service/` — use-case tests with Mockito ports.
- `infrastructure/adapter/in/rest/` — `@WebFluxTest` slice that boots
  only the controller + exception handler.
- `infrastructure/adapter/out/redis/` — pure-logic strategy tests
  (`ExponentialBackoffPolicyTest`).
- `integration/` — `@SpringBootTest` against real Kafka, MongoDB, and
  Redis containers via Testcontainers; the Go enrichment API is stubbed
  on the port. Tagged `@Tag("integration")` so it only runs via the
  `integrationTest` Gradle task.

The default `./gradlew check` runs Spotless, Checkstyle, and the
unit/slice tests and fails on any violation.

## Build & quality gates

| Task                  | Purpose                                    |
|-----------------------|--------------------------------------------|
| `./gradlew build`     | Compile, format check, checkstyle, tests   |
| `./gradlew spotlessApply` | Auto-format Java + Kotlin gradle scripts |
| `./gradlew bootJar`   | Produce the executable jar                 |

Coding conventions:

- Google Java Format (AOSP variant) enforced by Spotless.
- Checkstyle covers naming, imports, modifier ordering, missing braces,
  magic numbers, and parameter counts. Suppressions live in
  `config/checkstyle/suppressions.xml`.

## Deployment

The `Dockerfile` is a two-stage build:

1. **Builder** (`eclipse-temurin:21-jdk-jammy`): builds the boot jar and
   extracts the layered jar so each layer is cacheable.
2. **Runtime** (`eclipse-temurin:21-jre-jammy`): a slim JRE image that
   runs `JarLauncher`. Defaults:
   `-XX:MaxRAMPercentage=75 -XX:+UseG1GC -XX:+ExitOnOutOfMemoryError`.

The compose file uses `service_healthy` dependencies, so the worker
waits for Kafka/Mongo/Redis/Go API to report healthy before starting.

## Observability

- `/actuator/health` (liveness/readiness via Spring Boot), `/actuator/metrics`,
  `/actuator/prometheus` for scraping.
- The Kafka listener container has `setObservationEnabled(true)`, so consumer
  poll/processing metrics and traces are emitted automatically through
  Micrometer.
- Logging is wired through `logback-spring.xml`: a plain pattern in `dev` /
  default profiles, and a `LogstashEncoder` JSON layout under the `prod`
  profile so the service can ship straight into ELK / Loki / Cloud Logging.

## Kafka behaviour

- `ErrorHandlingDeserializer` wraps the key/value deserializers so a poison
  pill (bad JSON, wrong schema) is captured as a
  `DeserializationException` and routed to the configured
  `DefaultErrorHandler` instead of crashing the container in a tight
  loop.
- The listener thread `block()`s on the reactive pipeline; the offset is
  acknowledged only after the order has been processed or stored for retry.
  This preserves per-partition ordering, while cross-partition parallelism
  comes from the container `concurrency` setting.
- Topic auto-creation is on for the development compose stack; in
  production prefer explicit topic provisioning via
  `./scripts/setup-kafka.sh` or your platform's topic tooling.

## Roadmap

- Replace the `OrderMongoAdapter` derived queries with a thin
  aggregation pipeline for the combined status + customer filter (the
  current implementation depends on multiple Spring Data derived
  methods).
- Promote the dead-letter set to a real DLQ topic (e.g. `orders-dlq`)
  with a separate replay tool.
- Wire Micrometer Tracing OTLP exporter so the `traceId` / `spanId`
  MDC values populated by `logback-spring.xml` propagate end-to-end.
- Add a Bulkhead on the outbound HTTP adapters to bound concurrent
  enrichment calls in addition to the existing TimeLimiter.
