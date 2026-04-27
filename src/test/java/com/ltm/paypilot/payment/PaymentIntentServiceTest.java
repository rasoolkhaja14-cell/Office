package com.ltm.paypilot.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ltm.paypilot.common.ResourceNotFoundException;
import com.ltm.paypilot.payment_intent.dto.CreatePaymentIntentRequest;
import com.ltm.paypilot.payment_intent.dto.PaymentIntentResponse;
import com.ltm.paypilot.payment_intent.repository.IdempotencyStore;
import com.ltm.paypilot.payment_intent.repository.PaymentIntentRepository;
import com.ltm.paypilot.payment_intent.service.PaymentIntentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * S1-3 — Unit tests for PaymentIntentService (idempotency + creation).
 */
public class PaymentIntentServiceTest {
    private PaymentIntentService service;
    private ProviderAdapter mockAdapter;

    @BeforeEach
    void setUp() {
        ObjectMapper mapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        mockAdapter = Mockito.mock(ProviderAdapter.class);
        service     = new PaymentIntentService(
                new PaymentIntentRepository(),
                new IdempotencyStore(),
                mockAdapter,
                mapper
        );
    }

    // ── createPaymentIntent ───────────────────────────────────────────────────

    @Test
    @DisplayName("createPaymentIntent: first call stores and returns a fresh intent")
    void create_firstCall_storesFreshIntent() {
        PaymentIntentResponse res = service.createPaymentIntent(request("500.00"), "key-001");

        assertThat(res.getIntentId()).isNotBlank();
        assertThat(res.getAmount()).isEqualByComparingTo("500.00");
        assertThat(res.getStatus().name()).isEqualTo("CREATED");
    }

    @Test
    @DisplayName("createPaymentIntent: duplicate key returns identical response (same intentId)")
    void create_duplicateKey_returnsCachedResponse() {
        PaymentIntentResponse first  = service.createPaymentIntent(request("200.00"), "key-dup");
        PaymentIntentResponse second = service.createPaymentIntent(request("200.00"), "key-dup");

        assertThat(second.getIntentId()).isEqualTo(first.getIntentId());
        assertThat(second.getAmount()).isEqualByComparingTo(first.getAmount());
    }

    @Test
    @DisplayName("createPaymentIntent: different keys produce different intents")
    void create_differentKeys_produceDifferentIntents() {
        PaymentIntentResponse r1 = service.createPaymentIntent(request("100.00"), "key-A");
        PaymentIntentResponse r2 = service.createPaymentIntent(request("100.00"), "key-B");

        assertThat(r1.getIntentId()).isNotEqualTo(r2.getIntentId());
    }

    // ── getPaymentIntent ──────────────────────────────────────────────────────

    @Test
    @DisplayName("getPaymentIntent: retrieves created intent by id")
    void get_existingIntent_returnsIt() {
        PaymentIntentResponse created = service.createPaymentIntent(request("300.00"), "key-get");
        PaymentIntentResponse fetched = service.getPaymentIntent(created.getIntentId());

        assertThat(fetched.getIntentId()).isEqualTo(created.getIntentId());
    }

    @Test
    @DisplayName("getPaymentIntent: unknown id throws ResourceNotFoundException")
    void get_unknownId_throws() {
        assertThatThrownBy(() -> service.getPaymentIntent("ghost-id"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── authorize ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("authorize: adapter SUCCESS maps to AUTHORIZED status")
    void authorize_success_setsAuthorized() {
        Mockito.when(mockAdapter.authorize(any()))
                .thenReturn(ProviderAuthResult.success("txn-001"));

        PaymentIntentResponse created    = service.createPaymentIntent(request("100.00"), "key-auth");
        PaymentIntentResponse authorized = service.authorize(created.getIntentId());

        assertThat(authorized.getStatus().name()).isEqualTo("AUTHORIZED");
    }

    @Test
    @DisplayName("authorize: adapter DECLINED maps to FAILED status")
    void authorize_declined_setsFailed() {
        Mockito.when(mockAdapter.authorize(any()))
                .thenReturn(ProviderAuthResult.declined("txn-002", "Declined"));

        PaymentIntentResponse created = service.createPaymentIntent(request("100.00"), "key-fail");
        PaymentIntentResponse result  = service.authorize(created.getIntentId());

        assertThat(result.getStatus().name()).isEqualTo("FAILED");
    }

    // ── helper ────────────────────────────────────────────────────────────────
    private CreatePaymentIntentRequest request(String amount) {
        CreatePaymentIntentRequest r = new CreatePaymentIntentRequest();
        r.setMerchantId("merchant-001");
        r.setAmount(new BigDecimal(amount));
        r.setCurrency("INR");
        r.setReferenceId("REF-" + System.nanoTime());
        return r;
    }
}
