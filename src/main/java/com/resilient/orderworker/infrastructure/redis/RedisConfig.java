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
import org.redisson.config.SingleServerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Redis configuration for distributed locks and caching.
 *
 * <p>Enable or disable by setting {@code redisson.enabled=false} in {@code application.yml}.
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

    /** Interval between retry attempts (default 1 s). */
    @Value("${spring.redis.retry-interval:1s}")
    private Duration retryInterval;

    /** Number of retry attempts before giving up (default 3). */
    @Value("${spring.redis.retry-attempts:3}")
    private int retryAttempts;

    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();

        SingleServerConfig single =
                config.useSingleServer()
                        .setAddress(String.format("redis://%s:%d", redisHost, redisPort))
                        .setTimeout((int) timeout.toMillis()) // expects int (ms)
                        .setRetryAttempts(retryAttempts)
                        .setRetryInterval((int) retryInterval.toMillis()); // expects int (ms)

        if (!redisPassword.isBlank()) {
            single.setPassword(redisPassword);
        }

        return Redisson.create(config);
    }
}
