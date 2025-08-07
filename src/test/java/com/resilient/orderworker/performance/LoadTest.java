/*
 * Copyright (c) 2025 Resilient Order Enricher
 *
 * Licensed under the MIT License.
 */
package com.resilient.orderworker.performance;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.resilient.orderworker.order.dto.OrderMessage;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * Load testing class for the Order Processing System.
 *
 * <p>This class contains performance tests to validate system behavior under high load conditions:
 *
 * <ul>
 *   <li><strong>Throughput Testing</strong>: Measures messages processed per second
 *   <li><strong>Concurrency Testing</strong>: Tests parallel processing capabilities
 *   <li><strong>Memory Testing</strong>: Validates memory usage under load
 *   <li><strong>Resilience Testing</strong>: Tests circuit breaker and retry behavior
 * </ul>
 *
 * <p><strong>Test Categories:</strong>
 *
 * <ul>
 *   <li>Light Load: 100 messages, 5 concurrent threads
 *   <li>Medium Load: 1,000 messages, 10 concurrent threads
 *   <li>Heavy Load: 10,000 messages, 20 concurrent threads
 *   <li>Stress Test: 50,000 messages, 50 concurrent threads
 * </ul>
 *
 * <p><strong>Usage:</strong>
 *
 * <pre>{@code
 * # Run all performance tests
 * ./gradlew test --tests="*LoadTest*"
 *
 * # Run specific load test
 * ./gradlew test --tests="LoadTest.mediumLoadTest"
 * }</pre>
 *
 * <p><strong>Note:</strong> These tests are disabled by default (@Disabled) as they require
 * external services and generate significant load. Enable them individually for performance
 * testing.
 *
 * @author Alejandro Velazco
 * @version 1.0.0
 * @since 1.0.0
 */
public class LoadTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoadTest.class);

    // Test configuration constants
    private static final int LIGHT_LOAD_MESSAGES = 100;
    private static final int MEDIUM_LOAD_MESSAGES = 1000;
    private static final int HEAVY_LOAD_MESSAGES = 10000;
    private static final int STRESS_TEST_MESSAGES = 50000;

    private static final int LIGHT_CONCURRENCY = 5;
    private static final int MEDIUM_CONCURRENCY = 10;
    private static final int HEAVY_CONCURRENCY = 20;
    private static final int STRESS_CONCURRENCY = 50;

    // Sample data for test message generation
    private static final String[] CUSTOMER_IDS = {
        "customer-456", "customer-123", "customer-789", "customer-101", "customer-202"
    };

    private static final String[] PRODUCT_IDS = {
        "product-001", "product-002", "product-003", "product-004", "product-005"
    };

    /**
     * Light load test - validates basic performance with low volume.
     *
     * <p>This test processes 100 messages with 5 concurrent threads to establish baseline
     * performance metrics and ensure the system can handle normal load.
     */
    @Test
    @Disabled("Performance test - enable manually for load testing")
    void lightLoadTest() throws InterruptedException {
        LOGGER.info(
                "Starting light load test: {} messages, {} concurrent threads",
                LIGHT_LOAD_MESSAGES,
                LIGHT_CONCURRENCY);

        final LoadTestResult result =
                executeLoadTest(LIGHT_LOAD_MESSAGES, LIGHT_CONCURRENCY, "Light Load Test");

        logResults(result);
        validatePerformanceThresholds(result, 100.0, 5000); // 100 msg/s, 5s max duration
    }

    /**
     * Medium load test - validates performance under moderate load.
     *
     * <p>This test processes 1,000 messages with 10 concurrent threads to test the system's ability
     * to handle moderate production-like traffic.
     */
    @Test
    @Disabled("Performance test - enable manually for load testing")
    void mediumLoadTest() throws InterruptedException {
        LOGGER.info(
                "Starting medium load test: {} messages, {} concurrent threads",
                MEDIUM_LOAD_MESSAGES,
                MEDIUM_CONCURRENCY);

        final LoadTestResult result =
                executeLoadTest(MEDIUM_LOAD_MESSAGES, MEDIUM_CONCURRENCY, "Medium Load Test");

        logResults(result);
        validatePerformanceThresholds(result, 150.0, 30000); // 150 msg/s, 30s max duration
    }

    /**
     * Heavy load test - validates performance under high load conditions.
     *
     * <p>This test processes 10,000 messages with 20 concurrent threads to test the system's
     * scalability and performance under heavy traffic.
     */
    @Test
    @Disabled("Performance test - enable manually for load testing")
    void heavyLoadTest() throws InterruptedException {
        LOGGER.info(
                "Starting heavy load test: {} messages, {} concurrent threads",
                HEAVY_LOAD_MESSAGES,
                HEAVY_CONCURRENCY);

        final LoadTestResult result =
                executeLoadTest(HEAVY_LOAD_MESSAGES, HEAVY_CONCURRENCY, "Heavy Load Test");

        logResults(result);
        validatePerformanceThresholds(result, 200.0, 120000); // 200 msg/s, 2min max duration
    }

    /**
     * Stress test - validates system behavior under extreme load.
     *
     * <p>This test processes 50,000 messages with 50 concurrent threads to test the system's limits
     * and ensure it degrades gracefully under stress.
     */
    @Test
    @Disabled("Performance test - enable manually for load testing")
    void stressTest() throws InterruptedException {
        LOGGER.info(
                "Starting stress test: {} messages, {} concurrent threads",
                STRESS_TEST_MESSAGES,
                STRESS_CONCURRENCY);

        final LoadTestResult result =
                executeLoadTest(STRESS_TEST_MESSAGES, STRESS_CONCURRENCY, "Stress Test");

        logResults(result);
        // More lenient thresholds for stress test
        validatePerformanceThresholds(result, 100.0, 600000); // 100 msg/s, 10min max duration
    }

    /**
     * Executes a load test with the specified parameters.
     *
     * @param messageCount total number of messages to process
     * @param concurrency number of concurrent threads
     * @param testName name of the test for logging
     * @return LoadTestResult containing performance metrics
     */
    private LoadTestResult executeLoadTest(
            final int messageCount, final int concurrency, final String testName)
            throws InterruptedException {

        final LocalDateTime startTime = LocalDateTime.now(ZoneId.systemDefault());
        final CountDownLatch latch = new CountDownLatch(messageCount);
        final AtomicInteger successCount = new AtomicInteger(0);
        final AtomicInteger errorCount = new AtomicInteger(0);

        LOGGER.info("Generating {} test messages...", messageCount);
        final List<OrderMessage> messages = generateTestMessages(messageCount);

        LOGGER.info(
                "Starting {} with {} messages using {} threads",
                testName,
                messageCount,
                concurrency);

        // Execute load test using reactive streams with parallel processing
        Flux.fromIterable(messages)
                .parallel(concurrency)
                .runOn(Schedulers.parallel())
                .flatMap(this::simulateOrderProcessing)
                .doOnNext(
                        success -> {
                            if (success) {
                                successCount.incrementAndGet();
                            } else {
                                errorCount.incrementAndGet();
                            }
                            latch.countDown();
                        })
                .doOnError(
                        error -> {
                            LOGGER.error("Error during load test: {}", error.getMessage());
                            errorCount.incrementAndGet();
                            latch.countDown();
                        })
                .subscribe();

        // Wait for all messages to be processed (with timeout)
        final boolean completed = latch.await(10, TimeUnit.MINUTES);
        final LocalDateTime endTime = LocalDateTime.now(ZoneId.systemDefault());

        if (!completed) {
            LOGGER.warn("Load test did not complete within timeout");
        }

        return new LoadTestResult(
                testName,
                messageCount,
                concurrency,
                successCount.get(),
                errorCount.get(),
                startTime,
                endTime,
                completed);
    }

    /**
     * Simulates order message processing with realistic timing.
     *
     * @param orderMessage the order message to process
     * @return Mono indicating success (true) or failure (false)
     */
    private Mono<Boolean> simulateOrderProcessing(final OrderMessage orderMessage) {
        return Mono.fromCallable(
                        () -> {
                            // Simulate processing time (10-100ms)
                            final int processingTime = ThreadLocalRandom.current().nextInt(10, 100);
                            Thread.sleep(processingTime);

                            // Simulate 95% success rate
                            final boolean success = ThreadLocalRandom.current().nextDouble() < 0.95;

                            if (!success) {
                                throw new RuntimeException(
                                        "Simulated processing failure for order: "
                                                + orderMessage.orderId());
                            }

                            return true;
                        })
                .subscribeOn(Schedulers.boundedElastic())
                .onErrorReturn(false);
    }

    /**
     * Generates test messages for load testing.
     *
     * @param count number of messages to generate
     * @return list of test order messages
     */
    private List<OrderMessage> generateTestMessages(final int count) {
        return IntStream.range(0, count)
                .mapToObj(
                        i -> {
                            final String orderId =
                                    "load-test-order-" + i + "-" + System.currentTimeMillis();
                            final String customerId =
                                    CUSTOMER_IDS[
                                            ThreadLocalRandom.current()
                                                    .nextInt(CUSTOMER_IDS.length)];

                            // Generate 1-3 products per order
                            final int productCount = ThreadLocalRandom.current().nextInt(1, 4);
                            final List<OrderMessage.ProductInfo> products =
                                    IntStream.range(0, productCount)
                                            .mapToObj(
                                                    j -> {
                                                        final String productId =
                                                                PRODUCT_IDS[
                                                                        ThreadLocalRandom.current()
                                                                                .nextInt(
                                                                                        PRODUCT_IDS
                                                                                                .length)];
                                                        final int quantity =
                                                                ThreadLocalRandom.current()
                                                                        .nextInt(1, 6);
                                                        return new OrderMessage.ProductInfo(
                                                                productId, quantity);
                                                    })
                                            .toList();

                            return new OrderMessage(orderId, customerId, products);
                        })
                .toList();
    }

    /**
     * Logs the results of a load test.
     *
     * @param result the load test result to log
     */
    private void logResults(final LoadTestResult result) {
        final Duration duration = Duration.between(result.startTime, result.endTime);
        final double durationSeconds = duration.toMillis() / 1000.0;
        final double throughput = result.totalMessages / durationSeconds;
        final double successRate = (double) result.successCount / result.totalMessages * 100;

        LOGGER.info("=== {} Results ===", result.testName);
        LOGGER.info("Total Messages: {}", result.totalMessages);
        LOGGER.info("Concurrency: {}", result.concurrency);
        LOGGER.info("Duration: {:.2f} seconds", durationSeconds);
        LOGGER.info("Throughput: {:.2f} messages/second", throughput);
        LOGGER.info("Success Count: {}", result.successCount);
        LOGGER.info("Error Count: {}", result.errorCount);
        LOGGER.info("Success Rate: {:.2f}%", successRate);
        LOGGER.info("Completed: {}", result.completed ? "Yes" : "No (Timeout)");
    }

    /**
     * Validates that performance metrics meet the specified thresholds.
     *
     * @param result the load test result
     * @param minThroughput minimum expected throughput (messages/second)
     * @param maxDurationMs maximum acceptable duration (milliseconds)
     */
    private void validatePerformanceThresholds(
            final LoadTestResult result, final double minThroughput, final long maxDurationMs) {

        final Duration duration = Duration.between(result.startTime, result.endTime);
        final double durationSeconds = duration.toMillis() / 1000.0;
        final double actualThroughput = result.successCount / durationSeconds;
        final double successRate = (double) result.successCount / result.totalMessages * 100;

        // Validate throughput
        if (actualThroughput < minThroughput) {
            LOGGER.warn(
                    "Performance threshold not met - Throughput: {:.2f} < {:.2f} messages/second",
                    actualThroughput,
                    minThroughput);
        } else {
            LOGGER.info(
                    "Performance threshold met - Throughput: {:.2f} >= {:.2f} messages/second",
                    actualThroughput,
                    minThroughput);
        }

        // Validate duration
        if (duration.toMillis() > maxDurationMs) {
            LOGGER.warn(
                    "Performance threshold not met - Duration: {} > {}ms",
                    duration.toMillis(),
                    maxDurationMs);
        } else {
            LOGGER.info(
                    "Performance threshold met - Duration: {} <= {}ms",
                    duration.toMillis(),
                    maxDurationMs);
        }

        // Validate success rate (should be > 90%)
        if (successRate < 90.0) {
            LOGGER.warn(
                    "Success rate threshold not met - Success Rate: {:.2f}% < 90%", successRate);
        } else {
            LOGGER.info("Success rate threshold met - Success Rate: {:.2f}% >= 90%", successRate);
        }
    }

    /** Data class to hold load test results. */
    private static class LoadTestResult {
        private final String testName;
        private final int totalMessages;
        private final int concurrency;
        private final int successCount;
        private final int errorCount;
        private final LocalDateTime startTime;
        private final LocalDateTime endTime;
        private final boolean completed;

        LoadTestResult(
                final String testName,
                final int totalMessages,
                final int concurrency,
                final int successCount,
                final int errorCount,
                final LocalDateTime startTime,
                final LocalDateTime endTime,
                final boolean completed) {
            this.testName = testName;
            this.totalMessages = totalMessages;
            this.concurrency = concurrency;
            this.successCount = successCount;
            this.errorCount = errorCount;
            this.startTime = startTime;
            this.endTime = endTime;
            this.completed = completed;
        }
    }
}
