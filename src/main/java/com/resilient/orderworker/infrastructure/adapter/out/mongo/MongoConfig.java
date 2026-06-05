/*
 * Copyright (c) 2025 Resilient Order Enricher
 *
 * Licensed under the MIT License.
 */
package com.resilient.orderworker.infrastructure.adapter.out.mongo;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;

@Configuration
@ConditionalOnProperty(
        name = "spring.data.mongodb.enabled",
        havingValue = "true",
        matchIfMissing = true)
@EnableReactiveMongoRepositories(basePackageClasses = SpringDataOrderRepository.class)
public class MongoConfig {}
