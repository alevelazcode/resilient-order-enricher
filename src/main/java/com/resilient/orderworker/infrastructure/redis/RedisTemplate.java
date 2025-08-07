/*
 * Copyright (c) 2025 Resilient Order Enricher
 *
 * Licensed under the MIT License.
 */
package com.resilient.orderworker.infrastructure.redis;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.redisson.api.RBucket;
import org.redisson.api.RSet;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Redis template wrapper for common Redis operations using Redisson.
 *
 * <p>This template provides simplified methods for:
 *
 * <ul>
 *   <li><strong>Key-Value Operations</strong>: Get, set, delete operations
 *   <li><strong>Set Operations</strong>: Add/remove from sets, get set members
 *   <li><strong>TTL Management</strong>: Set expiration times for keys
 *   <li><strong>Type Safety</strong>: Generic methods for type-safe operations
 * </ul>
 *
 * <p>The template wraps Redisson client operations and provides a consistent interface for Redis
 * interactions throughout the application.
 *
 * @author Alejandro Velazco
 * @version 1.0.0
 * @since 1.0.0
 * @see RedissonClient
 */
@Component
@ConditionalOnProperty(name = "spring.redis.enabled", havingValue = "true", matchIfMissing = true)
public class RedisTemplate {

    private final RedissonClient redissonClient;

    /**
     * Constructs a new RedisTemplate with the provided Redisson client.
     *
     * @param redissonClient the Redisson client for Redis operations
     */
    public RedisTemplate(final RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    /**
     * Sets a value for the given key.
     *
     * @param key the Redis key
     * @param value the value to store
     * @param <T> the type of the value
     */
    public <T> void set(final String key, final T value) {
        final RBucket<T> bucket = redissonClient.getBucket(key);
        bucket.set(value);
    }

    /**
     * Sets a value for the given key with TTL.
     *
     * @param key the Redis key
     * @param value the value to store
     * @param ttl time to live
     * @param timeUnit time unit for TTL
     * @param <T> the type of the value
     */
    public <T> void set(final String key, final T value, final long ttl, final TimeUnit timeUnit) {
        final RBucket<T> bucket = redissonClient.getBucket(key);
        bucket.set(value, Duration.of(ttl, timeUnit.toChronoUnit()));
    }

    /**
     * Gets a value for the given key.
     *
     * @param key the Redis key
     * @param type the expected type of the value
     * @param <T> the type of the value
     * @return the value or null if not found
     */
    public <T> T get(final String key, final Class<T> type) {
        final RBucket<T> bucket = redissonClient.getBucket(key);
        return bucket.get();
    }

    /**
     * Deletes a key from Redis.
     *
     * @param key the Redis key to delete
     * @return true if the key was deleted, false if it didn't exist
     */
    public boolean delete(final String key) {
        final RBucket<Object> bucket = redissonClient.getBucket(key);
        return bucket.delete();
    }

    /**
     * Checks if a key exists in Redis.
     *
     * @param key the Redis key
     * @return true if the key exists
     */
    public boolean exists(final String key) {
        final RBucket<Object> bucket = redissonClient.getBucket(key);
        return bucket.isExists();
    }

    /**
     * Adds a value to a Redis set.
     *
     * @param setKey the Redis set key
     * @param value the value to add
     * @param <T> the type of the value
     * @return true if the value was added, false if it already existed
     */
    public <T> boolean addToSet(final String setKey, final T value) {
        final RSet<T> set = redissonClient.getSet(setKey);
        return set.add(value);
    }

    /**
     * Removes a value from a Redis set.
     *
     * @param setKey the Redis set key
     * @param value the value to remove
     * @param <T> the type of the value
     * @return true if the value was removed, false if it didn't exist
     */
    public <T> boolean removeFromSet(final String setKey, final T value) {
        final RSet<T> set = redissonClient.getSet(setKey);
        return set.remove(value);
    }

    /**
     * Gets all members of a Redis set.
     *
     * @param setKey the Redis set key
     * @param <T> the type of the set members
     * @return list of set members
     */
    public <T> List<T> getSetMembers(final String setKey) {
        final RSet<T> set = redissonClient.getSet(setKey);
        return set.stream().toList();
    }

    /**
     * Checks if a value exists in a Redis set.
     *
     * @param setKey the Redis set key
     * @param value the value to check
     * @param <T> the type of the value
     * @return true if the value exists in the set
     */
    public <T> boolean isSetMember(final String setKey, final T value) {
        final RSet<T> set = redissonClient.getSet(setKey);
        return set.contains(value);
    }

    /**
     * Gets the size of a Redis set.
     *
     * @param setKey the Redis set key
     * @return the number of members in the set
     */
    public int getSetSize(final String setKey) {
        final RSet<Object> set = redissonClient.getSet(setKey);
        return set.size();
    }

    /**
     * Sets TTL for an existing key.
     *
     * @param key the Redis key
     * @param ttl time to live
     * @param timeUnit time unit for TTL
     * @return true if TTL was set successfully
     */
    public boolean expire(final String key, final long ttl, final TimeUnit timeUnit) {
        final RBucket<Object> bucket = redissonClient.getBucket(key);
        return bucket.expire(Duration.of(ttl, timeUnit.toChronoUnit()));
    }

    /**
     * Gets the TTL for a key.
     *
     * @param key the Redis key
     * @return TTL in milliseconds, -1 if no TTL, -2 if key doesn't exist
     */
    public long getTtl(final String key) {
        final RBucket<Object> bucket = redissonClient.getBucket(key);
        return bucket.remainTimeToLive();
    }
}
