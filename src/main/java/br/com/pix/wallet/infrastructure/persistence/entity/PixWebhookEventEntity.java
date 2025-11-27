package br.com.pix.wallet.infrastructure.persistence.entity;

import br.com.pix.wallet.domain.pix.webhook.PixWebhookEvent;
import br.com.pix.wallet.domain.pix.webhook.PixWebhookEventID;
import br.com.pix.wallet.domain.pix.webhook.PixWebhookEventType;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "pix_webhook_event")
public class PixWebhookEventEntity {

    @Id
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    @Column(name = "event_id", nullable = false, unique = true)
    private String eventId;

    @Column(name = "end_to_end_id", nullable = false)
    private String endToEndId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    private PixWebhookEventType eventType;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @Column(name = "processed_at", nullable = false)
    private Instant processedAt;

    protected PixWebhookEventEntity() {
    }

    public UUID getId() {
        return id;
    }

    public String getEventId() {
        return eventId;
    }

    public String getEndToEndId() {
        return endToEndId;
    }

    public PixWebhookEventType getEventType() {
        return eventType;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public Instant getProcessedAt() {
        return processedAt;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final PixWebhookEventEntity that = (PixWebhookEventEntity) o;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public static PixWebhookEventEntity from(final PixWebhookEvent event) {
        final var entity = new PixWebhookEventEntity();
        entity.id = event.getId().getValue();
        entity.eventId = event.getEventId();
        entity.endToEndId = event.getEndToEndId();
        entity.eventType = event.getType();
        entity.occurredAt = event.getOccurredAt();
        entity.processedAt = event.getProcessedAt();
        return entity;
    }

    public PixWebhookEvent toAggregate() {
        return PixWebhookEvent.with(
            PixWebhookEventID.from(this.id),
            this.eventId,
            this.endToEndId,
            this.eventType,
            this.occurredAt,
            this.processedAt
        );
    }
}
