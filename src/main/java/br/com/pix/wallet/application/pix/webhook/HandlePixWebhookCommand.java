package br.com.pix.wallet.application.pix.webhook;

import java.time.Instant;

public record HandlePixWebhookCommand(
    String eventId,
    String endToEndId,
    String eventType,
    Instant occurredAt
) {

    public static HandlePixWebhookCommand with(
        final String eventId,
        final String endToEndId,
        final String eventType,
        final Instant occurredAt
    ) {
        return new HandlePixWebhookCommand(
            eventId,
            endToEndId,
            eventType,
            occurredAt
        );
    }
}
