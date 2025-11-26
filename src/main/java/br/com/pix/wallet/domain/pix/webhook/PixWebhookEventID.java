package br.com.pix.wallet.domain.pix.webhook;

import br.com.pix.wallet.domain.core.Identifier;

import java.util.UUID;

public class PixWebhookEventID extends Identifier<UUID> {

    private final UUID value;

    private PixWebhookEventID(final UUID value) {
        this.value = value;
    }

    public static PixWebhookEventID unique() {
        return new PixWebhookEventID(UUID.randomUUID());
    }

    public static PixWebhookEventID from(final UUID value) {
        return new PixWebhookEventID(value);
    }

    public static PixWebhookEventID from(final String value) {
        return new PixWebhookEventID(UUID.fromString(value));
    }

    @Override
    public UUID getValue() {
        return value;
    }
}
