package br.com.pix.wallet.presentation.rest.controller.pix.request;

public record PixWebhookRequest(
    String endToEndId,
    String eventId,
    String eventType, // CONFIRMED, REJECTED
    String occurredAt
) {
}