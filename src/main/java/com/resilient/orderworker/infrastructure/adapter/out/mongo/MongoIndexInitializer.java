/*
 * Copyright (c) 2025 Resilient Order Enricher
 *
 * Licensed under the MIT License.
 */
package com.resilient.orderworker.infrastructure.adapter.out.mongo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
        name = "spring.data.mongodb.enabled",
        havingValue = "true",
        matchIfMissing = true)
public class MongoIndexInitializer {

    private static final Logger LOG = LoggerFactory.getLogger(MongoIndexInitializer.class);

    private final ReactiveMongoTemplate mongoTemplate;

    public MongoIndexInitializer(ReactiveMongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void ensureIndexes() {
        LOG.info("Ensuring MongoDB indexes on 'orders' collection");
        var ops = mongoTemplate.indexOps(OrderDocument.class);

        // Unique orderId enforces idempotency at the storage layer as a defence-in-depth
        // against duplicate sends — Spring Data does not honour @Indexed unless
        // spring.data.mongodb.auto-index-creation is true, so create it explicitly.
        ops.ensureIndex(
                        new Index()
                                .on("orderId", Sort.Direction.ASC)
                                .unique()
                                .named("idx_orderId_unique"))
                .then(
                        ops.ensureIndex(
                                new Index()
                                        .on("customerId", Sort.Direction.ASC)
                                        .named("idx_customerId")))
                .then(
                        ops.ensureIndex(
                                new Index().on("status", Sort.Direction.ASC).named("idx_status")))
                .then(
                        ops.ensureIndex(
                                new Index()
                                        .on("processedAt", Sort.Direction.DESC)
                                        .named("idx_processedAt_desc")))
                .then(
                        ops.ensureIndex(
                                new Index()
                                        .on("status", Sort.Direction.ASC)
                                        .on("processedAt", Sort.Direction.DESC)
                                        .named("idx_status_processedAt")))
                .then(
                        ops.ensureIndex(
                                new Index()
                                        .on("customerId", Sort.Direction.ASC)
                                        .on("processedAt", Sort.Direction.DESC)
                                        .named("idx_customerId_processedAt")))
                .doOnSuccess(name -> LOG.info("MongoDB indexes ready"))
                .doOnError(err -> LOG.error("Failed to create MongoDB indexes", err))
                .block();
    }
}
