package br.com.pix.wallet.domain.idempotency;

import br.com.pix.wallet.domain.core.AggregateRoot;
import br.com.pix.wallet.domain.validation.ValidationHandler;

import java.time.Instant;

public class PaymentIdempotency extends AggregateRoot<PaymentIdempotencyID> {

    private final String key;
    private PaymentIdempotencyStatus status;
    private String responsePayload;
    private final Instant createdAt;
    private Instant updatedAt;

    private PaymentIdempotency(
        final PaymentIdempotencyID id,
        final String key,
        final PaymentIdempotencyStatus status,
        final String responsePayload,
        final Instant createdAt,
        final Instant updatedAt
    ) {
        super(id);
        this.key = key;
        this.status = status;
        this.responsePayload = responsePayload;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static PaymentIdempotency start(final String key) {
        return new PaymentIdempotency(
            null,
            key,
            PaymentIdempotencyStatus.IN_PROGRESS,
            null,
            Instant.now(),
            Instant.now()
        );
    }

    public static PaymentIdempotency with(
        final PaymentIdempotencyID id,
        final String key,
        final PaymentIdempotencyStatus status,
        final String responsePayload,
        final Instant createdAt,
        final Instant updatedAt
    ) {
        return new PaymentIdempotency(
            id,
            key,
            status,
            responsePayload,
            createdAt,
            updatedAt
        );
    }

    public void complete(final String responsePayload) {
        this.status = PaymentIdempotencyStatus.COMPLETED;
        this.responsePayload = responsePayload;
        this.updatedAt = Instant.now();
    }

    @Override
    public void validate(final ValidationHandler handler) {
        new PaymentIdempotencyValidator(this, handler).validate();
    }

    public String getKey() {
        return key;
    }

    public PaymentIdempotencyStatus getStatus() {
        return status;
    }

    public String getResponsePayload() {
        return responsePayload;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
