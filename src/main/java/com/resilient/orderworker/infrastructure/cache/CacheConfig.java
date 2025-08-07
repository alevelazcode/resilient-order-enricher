/*
 * Copyright (c) 2025 Resilient Order Enricher
 *
 * Licensed under the MIT License.
 */
package com.resilient.orderworker.infrastructure.cache;

import java.time.Duration;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Configuration for Redis-based caching to improve performance.
 *
 * <p>This configuration sets up:
 *
 * <ul>
 *   <li><strong>Redis Cache Manager</strong>: Central cache management using Redis
 *   <li><strong>JSON Serialization</strong>: Efficient object serialization with Jackson
 *   <li><strong>TTL Configuration</strong>: Configurable time-to-live for cached data
 *   <li><strong>Cache Names</strong>: Predefined cache regions for different data types
 * </ul>
 *
 * <p><strong>Cache Regions:</strong>
 *
 * <ul>
 *   <li>{@code customers}: Customer data from external API (TTL: 15 minutes)
 *   <li>{@code products}: Product data from external API (TTL: 30 minutes)
 *   <li>{@code orders}: Frequently accessed order data (TTL: 5 minutes)
 * </ul>
 *
 * <p><strong>Configuration Properties:</strong>
 *
 * <ul>
 *   <li>{@code spring.cache.enabled}: Enable/disable caching (default: true)
 *   <li>{@code spring.cache.redis.time-to-live}: Default TTL for cache entries
 * </ul>
 *
 * @author Alejandro Velazco
 * @version 1.0.0
 * @since 1.0.0
 */
@Configuration
@EnableCaching
@ConditionalOnProperty(name = "spring.cache.enabled", havingValue = "true", matchIfMissing = true)
public class CacheConfig {

    /** Cache region names for different types of data. */
    public static final String CUSTOMERS_CACHE = "customers";

    public static final String PRODUCTS_CACHE = "products";
    public static final String ORDERS_CACHE = "orders";

    /**
     * Creates a Redis-based cache manager with optimized configuration.
     *
     * <p>This cache manager provides:
     *
     * <ul>
     *   <li>JSON serialization for efficient storage
     *   <li>Configurable TTL per cache region
     *   <li>Null value caching disabled for better performance
     *   <li>Prefix-based key naming for organization
     * </ul>
     *
     * @param connectionFactory Redis connection factory
     * @return configured Redis cache manager
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration defaultConfig =
                RedisCacheConfiguration.defaultCacheConfig()
                        .entryTtl(Duration.ofMinutes(10)) // Default TTL
                        .disableCachingNullValues()
                        .serializeKeysWith(
                                RedisSerializationContext.SerializationPair.fromSerializer(
                                        new StringRedisSerializer()))
                        .serializeValuesWith(
                                RedisSerializationContext.SerializationPair.fromSerializer(
                                        new GenericJackson2JsonRedisSerializer()));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                // Customer cache - longer TTL as customer data changes less frequently
                .withCacheConfiguration(
                        CUSTOMERS_CACHE, defaultConfig.entryTtl(Duration.ofMinutes(15)))
                // Product cache - longer TTL as product data is relatively stable
                .withCacheConfiguration(
                        PRODUCTS_CACHE, defaultConfig.entryTtl(Duration.ofMinutes(30)))
                // Order cache - shorter TTL as orders are more dynamic
                .withCacheConfiguration(ORDERS_CACHE, defaultConfig.entryTtl(Duration.ofMinutes(5)))
                .build();
    }
}
