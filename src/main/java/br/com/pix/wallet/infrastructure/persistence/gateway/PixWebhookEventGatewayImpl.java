package br.com.pix.wallet.infrastructure.persistence.gateway;

import br.com.pix.wallet.domain.pix.webhook.PixWebhookEventGateway;
import br.com.pix.wallet.domain.pix.webhook.PixWebhookEvent;
import br.com.pix.wallet.infrastructure.persistence.entity.PixWebhookEventEntity;
import br.com.pix.wallet.infrastructure.persistence.repository.PixWebhookEventJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
public class PixWebhookEventGatewayImpl implements PixWebhookEventGateway {

    private final PixWebhookEventJpaRepository repository;

    public PixWebhookEventGatewayImpl(final PixWebhookEventJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public PixWebhookEvent save(final PixWebhookEvent event) {
        final var entity = PixWebhookEventEntity.from(event);
        final var saved = repository.save(entity);
        return saved.toAggregate();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PixWebhookEvent> findByEventId(final String eventId) {
        return repository.findByEventId(eventId)
            .map(PixWebhookEventEntity::toAggregate);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEventId(final String eventId) {
        return repository.existsByEventId(eventId);
    }
}
