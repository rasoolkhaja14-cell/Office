package com.ltm.paypilot.payment_intent.dto;

import com.ltm.paypilot.payment_intent.domain.PaymentIntent;
import com.ltm.paypilot.payment_intent.domain.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Read-only response view of a PaymentIntent.
 */
public class PaymentIntentResponse {
    private final String        intentId;
    private final String        merchantId;
    private final BigDecimal amount;
    private final String        currency;
    private final String        referenceId;
    private final PaymentStatus status;
    private final Instant createdAt;

    private PaymentIntentResponse(PaymentIntent pi) {
        this.intentId    = pi.getIntentId();
        this.merchantId  = pi.getMerchantId();
        this.amount      = pi.getAmount();
        this.currency    = pi.getCurrency();
        this.referenceId = pi.getReferenceId();
        this.status      = pi.getStatus();
        this.createdAt   = pi.getCreatedAt();
    }

    public static PaymentIntentResponse from(PaymentIntent pi) {
        return new PaymentIntentResponse(pi);
    }

    public String        getIntentId()    { return intentId; }
    public String        getMerchantId()  { return merchantId; }
    public BigDecimal    getAmount()      { return amount; }
    public String        getCurrency()    { return currency; }
    public String        getReferenceId() { return referenceId; }
    public PaymentStatus getStatus()      { return status; }
    public Instant       getCreatedAt()   { return createdAt; }
}
