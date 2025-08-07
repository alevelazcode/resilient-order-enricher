/*
 * Copyright (c) 2025 Resilient Order Enricher
 *
 * Licensed under the MIT License.
 */
package com.resilient.orderworker.infrastructure.redis;

import java.time.Duration;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.EqualJitterDelay;
import org.redisson.config.SingleServerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Redis configuration for distributed locks and caching using Redisson.
 *
 * <p>This configuration provides:
 *
 * <ul>
 *   <li><strong>Single Server Mode</strong>: Connects to a single Redis instance
 *   <li><strong>Retry Strategy</strong>: Uses EqualJitterDelay for connection retries
 *   <li><strong>Timeout Configuration</strong>: Configurable response timeouts
 *   <li><strong>Password Authentication</strong>: Optional password-based auth
 * </ul>
 *
 * <p>Enable or disable by setting {@code redisson.enabled=false} in {@code application.yml}.
 *
 * @author Alejandro Velazco
 * @version 1.0.0
 * @since 1.0.0
 */
@Configuration
@ConditionalOnProperty(name = "redisson.enabled", havingValue = "true", matchIfMissing = true)
public class RedisConfig {

    @Value("${spring.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.redis.port:6379}")
    private int redisPort;

    @Value("${spring.redis.password:}")
    private String redisPassword;

    /** Max time to wait for a Redis response (e.g. 3s, 1500ms). */
    @Value("${spring.redis.timeout:3s}")
    private Duration timeout;

    /** Number of retry attempts before giving up (default 3). */
    @Value("${spring.redis.retry-attempts:3}")
    private int retryAttempts;

    /**
     * Creates and configures a RedissonClient for distributed operations.
     *
     * <p>This client is configured with:
     *
     * <ul>
     *   <li>Single server mode for Redis connection
     *   <li>Configurable timeout and retry settings
     *   <li>EqualJitterDelay retry strategy for better resilience
     *   <li>Optional password authentication
     * </ul>
     *
     * @return configured RedissonClient instance
     */
    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();

        SingleServerConfig single =
                config.useSingleServer()
                        .setAddress(String.format("redis://%s:%d", redisHost, redisPort))
                        .setTimeout((int) timeout.toMillis()) // expects int (ms)
                        .setRetryAttempts(retryAttempts)
                        .setRetryDelay(
                                new EqualJitterDelay(Duration.ofSeconds(1), Duration.ofSeconds(2)));

        if (!redisPassword.isBlank()) {
            single.setPassword(redisPassword);
        }

        return Redisson.create(config);
    }
}
