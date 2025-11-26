package br.com.pix.wallet.domain.pix.webhook;

import br.com.pix.wallet.domain.exception.DomainException;
import br.com.pix.wallet.domain.validation.Error;

public enum PixWebhookEventType {
    CREDIT_CONFIRMED,
    DEBIT_CONFIRMED,
    REFUND_PROCESSED;

    public static PixWebhookEventType from(final String value) {
        try {
            return PixWebhookEventType.valueOf(value.toUpperCase());
        } catch (Exception ex) {
            throw DomainException.with(Error.of("Invalid webhook event type: '%s'".formatted(value)));
        }
    }
}
