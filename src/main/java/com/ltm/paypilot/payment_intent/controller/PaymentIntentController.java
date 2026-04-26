package com.ltm.paypilot.payment_intent.controller;

import com.ltm.paypilot.payment_intent.dto.CreatePaymentIntentRequest;
import com.ltm.paypilot.payment_intent.dto.PaymentIntentResponse;
import com.ltm.paypilot.payment_intent.service.PaymentIntentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Payment Intent endpoints:
 *
 * POST /api/v1/payments/intents                         → create payment intent (requires Idempotency-Key header)
 * GET  /api/v1/payments/intents/{intentId}              → retrieve payment intent
 * POST /api/v1/payments/intents/{intentId}/authorize    → authorize via provider adapter
 */
@RestController
@RequestMapping("/api/v1/payments/intents")
public class PaymentIntentController {
    private static final String IDEMPOTENCY_HEADER = "Idempotency-Key";

    private final PaymentIntentService service;

    public PaymentIntentController(PaymentIntentService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<?> create(
            @RequestHeader(value = IDEMPOTENCY_HEADER, required = false) String idempotencyKey,
            @Valid @RequestBody CreatePaymentIntentRequest request) {

        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            ErrorResponse error = ErrorResponse.builder()
                    .code("MISSING_IDEMPOTENCY_KEY")
                    .message("Idempotency-Key header is required")
                    .build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        PaymentIntentResponse response = service.createPaymentIntent(request, idempotencyKey);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{intentId}")
    public ResponseEntity<PaymentIntentResponse> get(@PathVariable String intentId) {
        return ResponseEntity.ok(service.getPaymentIntent(intentId));
    }

    @PostMapping("/{intentId}/authorize")
    public ResponseEntity<PaymentIntentResponse> authorize(@PathVariable String intentId) {
        return ResponseEntity.ok(service.authorize(intentId));
    }
}
