/*
 * Copyright (c) 2025 Resilient Order Enricher
 *
 * Licensed under the MIT License.
 */
package com.resilient.orderworker.infrastructure.adapter.out.redis;

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

@Configuration
@ConditionalOnProperty(name = "redisson.enabled", havingValue = "true", matchIfMissing = true)
public class RedisConfig {

    @Value("${spring.data.redis.host:localhost}")
    private String host;

    @Value("${spring.data.redis.port:6379}")
    private int port;

    @Value("${spring.data.redis.password:}")
    private String password;

    @Value("${spring.data.redis.timeout:3s}")
    private Duration timeout;

    @Value("${spring.data.redis.retry-attempts:3}")
    private int retryAttempts;

    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient() {
        Config config = new Config();
        SingleServerConfig single =
                config.useSingleServer()
                        .setAddress(String.format("redis://%s:%d", host, port))
                        .setTimeout((int) timeout.toMillis())
                        .setRetryAttempts(retryAttempts)
                        .setRetryDelay(
                                new EqualJitterDelay(Duration.ofSeconds(1), Duration.ofSeconds(2)));
        if (password != null && !password.isBlank()) {
            single.setPassword(password);
        }
        return Redisson.create(config);
    }
}
