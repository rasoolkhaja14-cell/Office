package com.ltm.paypilot.payment_intent.repository;

import com.ltm.paypilot.payment_intent.domain.PaymentIntent;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class PaymentIntentRepository {
    private final ConcurrentHashMap<String, PaymentIntent> store = new ConcurrentHashMap<>();

    public PaymentIntent save(PaymentIntent pi) {
        store.put(pi.getIntentId(), pi);
        return pi;
    }

    public Optional<PaymentIntent> findById(String intentId) {
        return Optional.ofNullable(store.get(intentId));
    }
}
