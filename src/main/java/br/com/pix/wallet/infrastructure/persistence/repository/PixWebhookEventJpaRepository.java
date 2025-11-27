package br.com.pix.wallet.infrastructure.persistence.repository;

import br.com.pix.wallet.infrastructure.persistence.entity.PixWebhookEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PixWebhookEventJpaRepository extends JpaRepository<PixWebhookEventEntity, UUID> {

    Optional<PixWebhookEventEntity> findByEventId(String eventId);

    boolean existsByEventId(String eventId);
}