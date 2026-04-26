package com.ltm.paypilot.payment_intent.repository;

import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simulates a distributed Redis cache with a ConcurrentHashMap.
 *
 * Stores: idempotency key → serialized JSON response body.
 * In production this would be backed by Redis with a TTL.
 */
@Component
public class IdempotencyStore {
    /** Holds { idempotencyKey → cached JSON body }. */
    private final ConcurrentHashMap<String, String> cache = new ConcurrentHashMap<>();

    public void put(String key, String jsonBody) {
        cache.put(key, jsonBody);
    }

    public Optional<String> get(String key) {
        return Optional.ofNullable(cache.get(key));
    }

    public boolean contains(String key) {
        return cache.containsKey(key);
    }
}
