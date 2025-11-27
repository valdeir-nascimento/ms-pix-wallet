package br.com.pix.wallet.domain.pix.webhook;

import java.util.Optional;

public interface PixWebhookEventGateway {
    PixWebhookEvent save(PixWebhookEvent event);

    Optional<PixWebhookEvent> findByEventId(String eventId);

    boolean existsByEventId(String eventId);
}