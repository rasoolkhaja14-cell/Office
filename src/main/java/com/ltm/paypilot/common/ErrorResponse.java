package com.ltm.paypilot.common;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;

/**
 * Shared error payload used by all endpoints.
 * Format: { timestamp, traceId, code, message, details[] }
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    private final Instant timestamp;
    private final String traceId;
    private final String code;
    private final String message;
    private final List<String> details;

    private ErrorResponse(Builder builder) {
        this.timestamp = builder.timestamp;
        this.traceId   = builder.traceId;
        this.code      = builder.code;
        this.message   = builder.message;
        this.details   = builder.details;
    }

    public Instant getTimestamp() { return timestamp; }
    public String getTraceId()    { return traceId; }
    public String getCode()       { return code; }
    public String getMessage()    { return message; }
    public List<String> getDetails() { return details; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Instant timestamp = Instant.now();
        private String traceId;
        private String code;
        private String message;
        private List<String> details;

        public Builder traceId(String traceId)      { this.traceId = traceId; return this; }
        public Builder code(String code)            { this.code = code; return this; }
        public Builder message(String message)      { this.message = message; return this; }
        public Builder details(List<String> details){ this.details = details; return this; }
        public ErrorResponse build()                { return new ErrorResponse(this); }
    }
}
