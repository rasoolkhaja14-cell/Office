package com.ltm.paypilot.payment_intent.service;

import com.ltm.paypilot.payment_intent.domain.PaymentIntent;
import com.ltm.paypilot.payment_intent.domain.PaymentStatus;
import com.ltm.paypilot.payment_intent.dto.CreatePaymentIntentRequest;
import com.ltm.paypilot.payment_intent.dto.PaymentIntentResponse;
import com.ltm.paypilot.payment_intent.repository.IdempotencyStore;
import com.ltm.paypilot.payment_intent.repository.PaymentIntentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

/**
 * Business logic for PaymentIntent creation, retrieval, and authorization.
 * Idempotency is handled at the service layer (simpler than interceptor approach).
 */
@Service
public class PaymentIntentService {
    private static final Logger log = LoggerFactory.getLogger(PaymentIntentService.class);

    private final PaymentIntentRepository repository;
    private final IdempotencyStore idempotencyStore;
    private final ProviderAdapter  providerAdapter;
    private final ObjectMapper objectMapper;

    public PaymentIntentService(PaymentIntentRepository repository,
                                IdempotencyStore idempotencyStore,
                                ProviderAdapter providerAdapter,
                                ObjectMapper objectMapper) {
        this.repository       = repository;
        this.idempotencyStore = idempotencyStore;
        this.providerAdapter  = providerAdapter;
        this.objectMapper     = objectMapper;
    }

    // ── Create with idempotency ───────────────────────────────────────────────
    public PaymentIntentResponse createPaymentIntent(CreatePaymentIntentRequest request,
                                                     String idempotencyKey) {
        // Check idempotency store first
        if (idempotencyStore.contains(idempotencyKey)) {
            String cached = idempotencyStore.get(idempotencyKey).orElseThrow();
            try {
                log.info("Idempotency hit for key={}", idempotencyKey);
                return objectMapper.readValue(cached, PaymentIntentResponse.class);
            } catch (JsonProcessingException e) {
                throw new DomainException("IDEMPOTENCY_ERROR", "Cached response is corrupted");
            }
        }

        // New request — validate and create
        if (request.getAmount().signum() <= 0) {
            throw new DomainException("INVALID_AMOUNT", "Amount must be greater than 0");
        }

        String intentId = UUID.randomUUID().toString();
        PaymentIntent intent = new PaymentIntent(
                intentId,
                request.getMerchantId(),
                request.getAmount(),
                request.getCurrency(),
                request.getReferenceId()
        );
        repository.save(intent);

        PaymentIntentResponse response = PaymentIntentResponse.from(intent);

        // Cache the response for future duplicate requests
        try {
            idempotencyStore.put(idempotencyKey, objectMapper.writeValueAsString(response));
        } catch (JsonProcessingException e) {
            log.warn("Failed to cache idempotency response for key={}", idempotencyKey);
        }

        return response;
    }

    // ── Retrieve ──────────────────────────────────────────────────────────────
    public PaymentIntentResponse getPaymentIntent(String intentId) {
        return repository.findById(intentId)
                .map(PaymentIntentResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "PaymentIntent not found: " + intentId));
    }

    // ── Authorize (S1-5 hook) ─────────────────────────────────────────────────
    public PaymentIntentResponse authorize(String intentId) {
        PaymentIntent intent = repository.findById(intentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "PaymentIntent not found: " + intentId));

        ProviderAuthRequest authRequest = new ProviderAuthRequest(
                intentId,
                intent.getMerchantId(),
                intent.getAmount(),
                intent.getCurrency(),
                false
        );

        ProviderAuthResult result = providerAdapter.authorize(authRequest);

        PaymentStatus newStatus = switch (result.getStatus()) {
            case SUCCESS  -> PaymentStatus.AUTHORIZED;
            case DECLINED -> PaymentStatus.FAILED;
            case ERROR    -> PaymentStatus.FAILED;
        };

        intent.setStatus(newStatus);
        repository.save(intent);

        log.info("Authorized intentId={}, providerTxnId={}, status={}",
                intentId, result.getProviderTxnId(), newStatus);

        return PaymentIntentResponse.from(intent);
    }
}
