package br.com.pix.wallet.domain.pix.webhook;

import br.com.pix.wallet.domain.core.AggregateRoot;
import br.com.pix.wallet.domain.validation.ValidationHandler;

import java.time.Instant;

public class PixWebhookEvent extends AggregateRoot<PixWebhookEventID> {

    private final String eventId;
    private final String endToEndId;
    private final PixWebhookEventType type;
    private final Instant occurredAt;
    private final Instant processedAt;

    private PixWebhookEvent(
        final PixWebhookEventID id,
        final String eventId,
        final String endToEndId,
        final PixWebhookEventType type,
        final Instant occurredAt,
        final Instant processedAt
    ) {
        super(id);
        this.eventId = eventId;
        this.endToEndId = endToEndId;
        this.type = type;
        this.occurredAt = occurredAt;
        this.processedAt = processedAt;
    }

    public static PixWebhookEvent newEvent(
        final PixWebhookEventType type,
        final String eventId,
        final String endToEndId,
        final Instant occurredAt
    ) {
        return new PixWebhookEvent(
            PixWebhookEventID.unique(),
            eventId,
            endToEndId,
            type,
            occurredAt,
            Instant.now()
        );
    }

    public static PixWebhookEvent with(
        final PixWebhookEventID id,
        final String eventId,
        final String endToEndId,
        final PixWebhookEventType type,
        final Instant occurredAt,
        final Instant processedAt
    ) {
        return new PixWebhookEvent(
            id,
            eventId,
            endToEndId,
            type,
            occurredAt,
            processedAt
        );
    }

    @Override
    public void validate(final ValidationHandler handler) {
        new PixWebhookEventValidator(this, handler).validate();
    }

    public String getEventId() {
        return eventId;
    }

    public String getEndToEndId() {
        return endToEndId;
    }

    public PixWebhookEventType getType() {
        return type;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public Instant getProcessedAt() {
        return processedAt;
    }
}
