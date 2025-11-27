package br.com.pix.wallet.application.pix.webhook;

import br.com.pix.wallet.domain.pix.webhook.PixWebhookEvent;

import java.time.Instant;

public record HandlePixWebhookOutput(
    String webhookEventId,
    String eventId,
    String endToEndId,
    String eventType,
    Instant occurredAt,
    Instant processedAt
) {

    public static HandlePixWebhookOutput from(final PixWebhookEvent event) {
        return new HandlePixWebhookOutput(
            event.getId().getValue().toString(),
            event.getEventId(),
            event.getEndToEndId(),
            event.getType().name(),
            event.getOccurredAt(),
            event.getProcessedAt()
        );
    }
}
