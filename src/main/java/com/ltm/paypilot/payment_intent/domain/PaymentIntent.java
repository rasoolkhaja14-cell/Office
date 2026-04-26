package com.ltm.paypilot.payment_intent.domain;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Atomic unit of a payment request.
 */
public class PaymentIntent {
    private final String       intentId;
    private final String       merchantId;
    private final BigDecimal   amount;
    private final String       currency;
    private final String       referenceId;
    private PaymentStatus      status;
    private final Instant createdAt;

    public PaymentIntent(String intentId, String merchantId,
                         BigDecimal amount, String currency, String referenceId) {
        this.intentId    = intentId;
        this.merchantId  = merchantId;
        this.amount      = amount;
        this.currency    = currency;
        this.referenceId = referenceId;
        this.status      = PaymentStatus.CREATED;
        this.createdAt   = Instant.now();
    }

    public String       getIntentId()    { return intentId; }
    public String       getMerchantId()  { return merchantId; }
    public BigDecimal   getAmount()      { return amount; }
    public String       getCurrency()    { return currency; }
    public String       getReferenceId() { return referenceId; }
    public PaymentStatus getStatus()    { return status; }
    public Instant      getCreatedAt()  { return createdAt; }

    public void setStatus(PaymentStatus status) { this.status = status; }
}
