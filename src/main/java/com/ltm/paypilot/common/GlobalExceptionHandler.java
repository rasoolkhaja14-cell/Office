package com.ltm.paypilot.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

import static tools.jackson.databind.jsonFormatVisitors.JsonValueFormat.UUID;

/**
 * Catches all exceptions application-wide and maps them to the shared error
 * payload: { timestamp, traceId, code, message, details[] }.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ── Bean Validation (@Valid failures) ────────────────────────────────────
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        List<String> details = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.toList());

        ErrorResponse body = ErrorResponse.builder()
                .traceId(traceId())
                .code("VALIDATION_ERROR")
                .message("Request validation failed")
                .details(details)
                .build();

        log.warn("Validation error: {}", details);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    // ── 404 Not Found ─────────────────────────────────────────────────────────
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        ErrorResponse body = ErrorResponse.builder()
                .traceId(traceId())
                .code("NOT_FOUND")
                .message(ex.getMessage())
                .build();

        log.warn("Not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    // ── 422 Domain / Business Rule Violation ──────────────────────────────────
    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ErrorResponse> handleDomain(DomainException ex) {
        ErrorResponse body = ErrorResponse.builder()
                .traceId(traceId())
                .code(ex.getCode())
                .message(ex.getMessage())
                .build();

        log.warn("Domain violation [{}]: {}", ex.getCode(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(body);
    }

    // ── 500 Catch-all ─────────────────────────────────────────────────────────
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAll(Exception ex) {
        String tid = traceId();
        ErrorResponse body = ErrorResponse.builder()
                .traceId(tid)
                .code("INTERNAL_ERROR")
                .message("An unexpected error occurred")
                .build();

        log.error("Unhandled exception [traceId={}]", tid, ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    // ── Helper ────────────────────────────────────────────────────────────────
    private String traceId() {
        String tid = MDC.get("traceId");
        return (tid != null && !tid.isBlank()) ? tid : UUID.randomUUID().toString();
    }
}
