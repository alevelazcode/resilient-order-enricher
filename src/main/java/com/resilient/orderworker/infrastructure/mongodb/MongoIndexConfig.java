/*
 * Copyright (c) 2025 Resilient Order Enricher
 *
 * Licensed under the MIT License.
 */
package com.resilient.orderworker.infrastructure.mongodb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.stereotype.Component;

import com.resilient.orderworker.order.entity.Order;

import reactor.core.publisher.Mono;

/**
 * Configuration for MongoDB indexes to optimize query performance.
 *
 * <p>This component automatically creates essential indexes when the application starts:
 *
 * <ul>
 *   <li><strong>orderId Index</strong>: Unique index for fast order lookups
 *   <li><strong>customerId Index</strong>: Compound index for customer-based queries
 *   <li><strong>status Index</strong>: Index for filtering by processing status
 *   <li><strong>processedAt Index</strong>: Index for time-based queries and sorting
 *   <li><strong>Compound Indexes</strong>: Multi-field indexes for complex queries
 * </ul>
 *
 * <p>These indexes significantly improve performance for:
 *
 * <ul>
 *   <li>Order retrieval by ID (primary use case)
 *   <li>Customer order history queries
 *   <li>Status-based filtering and monitoring
 *   <li>Time-range queries for analytics
 *   <li>Paginated order listings
 * </ul>
 *
 * @author Alejandro Velazco
 * @version 1.0.0
 * @since 1.0.0
 * @see Order
 * @see ReactiveMongoTemplate
 */
@Component
public class MongoIndexConfig implements ApplicationRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(MongoIndexConfig.class);

    private final ReactiveMongoTemplate mongoTemplate;

    /**
     * Constructs a new MongoIndexConfig with the provided MongoDB template.
     *
     * @param mongoTemplate reactive MongoDB template for index operations
     */
    public MongoIndexConfig(final ReactiveMongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * Creates essential indexes for the Order collection on application startup.
     *
     * <p>This method is called automatically when the application starts and ensures that all
     * necessary indexes exist for optimal query performance.
     *
     * @param args application arguments (unused)
     */
    @Override
    public void run(final ApplicationArguments args) {
        LOGGER.info("Creating MongoDB indexes for optimal performance");

        createOrderIndexes()
                .doOnSuccess(v -> LOGGER.info("Successfully created all MongoDB indexes"))
                .doOnError(
                        error ->
                                LOGGER.error(
                                        "Failed to create MongoDB indexes: {}", error.getMessage()))
                .subscribe();
    }

    /**
     * Creates all indexes for the Order collection.
     *
     * @return Mono that completes when all indexes are created
     */
    private Mono<Void> createOrderIndexes() {
        return Mono.when(
                createOrderIdIndex(),
                createCustomerIdIndex(),
                createStatusIndex(),
                createProcessedAtIndex(),
                createCompoundIndexes());
    }

    /**
     * Creates a unique index on orderId field for fast order lookups.
     *
     * @return Mono that completes when the index is created
     */
    private Mono<Void> createOrderIdIndex() {
        LOGGER.debug("Creating unique index on orderId field");

        final Index orderIdIndex =
                new Index().on("orderId", Sort.Direction.ASC).unique().named("idx_orderId_unique");

        return mongoTemplate
                .indexOps(Order.class)
                .ensureIndex(orderIdIndex)
                .doOnSuccess(name -> LOGGER.debug("Created orderId index: {}", name))
                .then();
    }

    /**
     * Creates an index on customerId field for customer-based queries.
     *
     * @return Mono that completes when the index is created
     */
    private Mono<Void> createCustomerIdIndex() {
        LOGGER.debug("Creating index on customerId field");

        final Index customerIdIndex =
                new Index().on("customerId", Sort.Direction.ASC).named("idx_customerId");

        return mongoTemplate
                .indexOps(Order.class)
                .ensureIndex(customerIdIndex)
                .doOnSuccess(name -> LOGGER.debug("Created customerId index: {}", name))
                .then();
    }

    /**
     * Creates an index on status field for filtering by processing status.
     *
     * @return Mono that completes when the index is created
     */
    private Mono<Void> createStatusIndex() {
        LOGGER.debug("Creating index on status field");

        final Index statusIndex = new Index().on("status", Sort.Direction.ASC).named("idx_status");

        return mongoTemplate
                .indexOps(Order.class)
                .ensureIndex(statusIndex)
                .doOnSuccess(name -> LOGGER.debug("Created status index: {}", name))
                .then();
    }

    /**
     * Creates an index on processedAt field for time-based queries and sorting.
     *
     * @return Mono that completes when the index is created
     */
    private Mono<Void> createProcessedAtIndex() {
        LOGGER.debug("Creating index on processedAt field");

        final Index processedAtIndex =
                new Index()
                        .on("processedAt", Sort.Direction.DESC) // Most recent first
                        .named("idx_processedAt_desc");

        return mongoTemplate
                .indexOps(Order.class)
                .ensureIndex(processedAtIndex)
                .doOnSuccess(name -> LOGGER.debug("Created processedAt index: {}", name))
                .then();
    }

    /**
     * Creates compound indexes for complex queries.
     *
     * @return Mono that completes when all compound indexes are created
     */
    private Mono<Void> createCompoundIndexes() {
        return Mono.when(
                createCustomerStatusIndex(),
                createStatusProcessedAtIndex(),
                createCustomerProcessedAtIndex());
    }

    /**
     * Creates a compound index on customerId and status for customer status queries.
     *
     * @return Mono that completes when the index is created
     */
    private Mono<Void> createCustomerStatusIndex() {
        LOGGER.debug("Creating compound index on customerId and status");

        final Index customerStatusIndex =
                new Index()
                        .on("customerId", Sort.Direction.ASC)
                        .on("status", Sort.Direction.ASC)
                        .named("idx_customerId_status");

        return mongoTemplate
                .indexOps(Order.class)
                .ensureIndex(customerStatusIndex)
                .doOnSuccess(
                        name -> LOGGER.debug("Created customerId-status compound index: {}", name))
                .then();
    }

    /**
     * Creates a compound index on status and processedAt for status-based time queries.
     *
     * @return Mono that completes when the index is created
     */
    private Mono<Void> createStatusProcessedAtIndex() {
        LOGGER.debug("Creating compound index on status and processedAt");

        final Index statusProcessedAtIndex =
                new Index()
                        .on("status", Sort.Direction.ASC)
                        .on("processedAt", Sort.Direction.DESC)
                        .named("idx_status_processedAt");

        return mongoTemplate
                .indexOps(Order.class)
                .ensureIndex(statusProcessedAtIndex)
                .doOnSuccess(
                        name -> LOGGER.debug("Created status-processedAt compound index: {}", name))
                .then();
    }

    /**
     * Creates a compound index on customerId and processedAt for customer history queries.
     *
     * @return Mono that completes when the index is created
     */
    private Mono<Void> createCustomerProcessedAtIndex() {
        LOGGER.debug("Creating compound index on customerId and processedAt");

        final Index customerProcessedAtIndex =
                new Index()
                        .on("customerId", Sort.Direction.ASC)
                        .on("processedAt", Sort.Direction.DESC)
                        .named("idx_customerId_processedAt");

        return mongoTemplate
                .indexOps(Order.class)
                .ensureIndex(customerProcessedAtIndex)
                .doOnSuccess(
                        name ->
                                LOGGER.debug(
                                        "Created customerId-processedAt compound index: {}", name))
                .then();
    }
}
