package com.ltm.paypilot.payment_intent.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class CreatePaymentIntentRequest {
    @NotBlank(message = "merchantId is required")
    private String merchantId;

    @NotNull(message = "amount is required")
    @DecimalMin(value = "0.01", message = "amount must be greater than 0")
    private BigDecimal amount;

    @NotBlank(message = "currency is required")
    private String currency;

    private String referenceId;

    public String     getMerchantId()  { return merchantId; }
    public void       setMerchantId(String s) { this.merchantId = s; }

    public BigDecimal getAmount()      { return amount; }
    public void       setAmount(BigDecimal a) { this.amount = a; }

    public String     getCurrency()    { return currency; }
    public void       setCurrency(String c) { this.currency = c; }

    public String     getReferenceId() { return referenceId; }
    public void       setReferenceId(String r) { this.referenceId = r; }
}
