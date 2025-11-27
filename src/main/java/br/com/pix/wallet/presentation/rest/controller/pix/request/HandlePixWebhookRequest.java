package br.com.pix.wallet.presentation.rest.controller.pix.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record HandlePixWebhookRequest(

    @NotBlank(message = "'eventId' must not be blank")
    String eventId,

    @NotBlank(message = "'endToEndId' must not be blank")
    String endToEndId,

    @NotBlank(message = "'eventType' must not be blank")
    String eventType,

    @NotNull(message = "'occurredAt' must not be null")
    Instant occurredAt
) {
}
