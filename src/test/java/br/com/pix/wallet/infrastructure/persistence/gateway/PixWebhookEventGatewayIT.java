package br.com.pix.wallet.infrastructure.persistence.gateway;

import br.com.pix.wallet.GatewayTest;
import br.com.pix.wallet.domain.pix.webhook.PixWebhookEvent;
import br.com.pix.wallet.domain.pix.webhook.PixWebhookEventGateway;
import br.com.pix.wallet.domain.pix.webhook.PixWebhookEventType;
import br.com.pix.wallet.infrastructure.persistence.entity.PixWebhookEventEntity;
import br.com.pix.wallet.infrastructure.persistence.repository.PixWebhookEventJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@GatewayTest
class PixWebhookEventGatewayIT {

    @Autowired
    private PixWebhookEventGateway pixWebhookEventGateway;

    @Autowired
    private PixWebhookEventJpaRepository pixWebhookEventJpaRepository;

    @Test
    void givenValidPixWebhookEvent_whenCallsSave_thenShouldPersistEvent() {
        // given
        final var expectedType = PixWebhookEventType.CREDIT_CONFIRMED;
        final var expectedEventId = UUID.randomUUID().toString();
        final var expectedEndToEndId = UUID.randomUUID().toString();
        final var expectedOccurredAt = Instant.now().minusSeconds(60);

        final var event = PixWebhookEvent.newEvent(
            expectedType,
            expectedEventId,
            expectedEndToEndId,
            expectedOccurredAt
        );

        assertEquals(0, pixWebhookEventJpaRepository.count());

        // when
        final var actualEvent = pixWebhookEventGateway.save(event);

        // then
        assertEquals(1, pixWebhookEventJpaRepository.count());

        assertNotNull(actualEvent.getId());
        assertEquals(expectedEventId, actualEvent.getEventId());
        assertEquals(expectedEndToEndId, actualEvent.getEndToEndId());
        assertEquals(expectedType, actualEvent.getType());
        assertEquals(expectedOccurredAt, actualEvent.getOccurredAt());
        assertNotNull(actualEvent.getProcessedAt());

        final var actualEntity = pixWebhookEventJpaRepository.findById(actualEvent.getId().getValue()).get();

        assertEquals(actualEvent.getId().getValue(), actualEntity.getId());
        assertEquals(expectedEventId, actualEntity.getEventId());
        assertEquals(expectedEndToEndId, actualEntity.getEndToEndId());
        assertEquals(expectedType, actualEntity.getEventType());
        assertEquals(expectedOccurredAt, actualEntity.getOccurredAt());
        assertEquals(actualEvent.getProcessedAt(), actualEntity.getProcessedAt());
    }

    @Test
    void givenExistingPixWebhookEvent_whenCallsSaveWithSameId_thenShouldUpdateStoredEvent() {
        // given
        final var expectedType = PixWebhookEventType.CREDIT_CONFIRMED;
        final var expectedEventId = UUID.randomUUID().toString();
        final var expectedEndToEndId = UUID.randomUUID().toString();
        final var expectedOccurredAt = Instant.now().minusSeconds(120);

        final var originalEvent = PixWebhookEvent.newEvent(
            expectedType,
            expectedEventId,
            expectedEndToEndId,
            expectedOccurredAt
        );

        final var savedOriginal = pixWebhookEventGateway.save(originalEvent);

        final var expectedId = savedOriginal.getId();
        final var expectedProcessedAt = savedOriginal.getProcessedAt();

        final var updatedType = PixWebhookEventType.CREDIT_CONFIRMED;
        final var updatedEndToEndId = UUID.randomUUID().toString();

        final var updatedEvent = PixWebhookEvent.with(
            expectedId,
            expectedEventId,
            updatedEndToEndId,
            updatedType,
            expectedOccurredAt,
            expectedProcessedAt
        );

        // when
        pixWebhookEventGateway.save(updatedEvent);

        // then
        assertEquals(1, pixWebhookEventJpaRepository.count());

        final var actualEntity = pixWebhookEventJpaRepository.findById(expectedId.getValue()).get();

        assertEquals(expectedId.getValue(), actualEntity.getId());
        assertEquals(expectedEventId, actualEntity.getEventId());
        assertEquals(updatedEndToEndId, actualEntity.getEndToEndId());
        assertEquals(updatedType, actualEntity.getEventType());
        assertEquals(expectedOccurredAt, actualEntity.getOccurredAt());
        assertEquals(expectedProcessedAt, actualEntity.getProcessedAt());
    }

    @Test
    void givenExistingPixWebhookEvent_whenCallsFindByEventId_thenShouldReturnEvent() {
        // given
        final var expectedType = PixWebhookEventType.CREDIT_CONFIRMED;
        final var expectedEventId = UUID.randomUUID().toString();
        final var expectedEndToEndId = UUID.randomUUID().toString();
        final var expectedOccurredAt = Instant.now().minusSeconds(90);

        final var event = PixWebhookEvent.newEvent(
            expectedType,
            expectedEventId,
            expectedEndToEndId,
            expectedOccurredAt
        );

        final var saved = pixWebhookEventGateway.save(event);

        // when
        final var actualOptional = pixWebhookEventGateway.findByEventId(expectedEventId);

        // then
        assertTrue(actualOptional.isPresent());

        final var actual = actualOptional.get();

        assertEquals(saved.getId().getValue(), actual.getId().getValue());
        assertEquals(expectedEventId, actual.getEventId());
        assertEquals(expectedEndToEndId, actual.getEndToEndId());
        assertEquals(expectedType, actual.getType());
        assertEquals(expectedOccurredAt, actual.getOccurredAt());
        assertEquals(saved.getProcessedAt(), actual.getProcessedAt());
    }

    @Test
    void givenNonExistingEventId_whenCallsFindByEventId_thenShouldReturnEmpty() {
        // given
        assertEquals(0, pixWebhookEventJpaRepository.count());

        final var nonExistingEventId = UUID.randomUUID().toString();

        // when
        final var actualOptional = pixWebhookEventGateway.findByEventId(nonExistingEventId);

        // then
        assertTrue(actualOptional.isEmpty());
    }

    @Test
    void givenExistingPixWebhookEvent_whenCallsExistsByEventId_thenShouldReturnTrue() {
        // given
        final var expectedEventId = UUID.randomUUID().toString();

        final var event = PixWebhookEvent.newEvent(
            PixWebhookEventType.DEBIT_CONFIRMED,
            expectedEventId,
            UUID.randomUUID().toString(),
            Instant.now().minusSeconds(30)
        );

        pixWebhookEventGateway.save(event);

        assertEquals(1, pixWebhookEventJpaRepository.count());

        // when
        final var exists = pixWebhookEventGateway.existsByEventId(expectedEventId);

        // then
        assertTrue(exists);
    }

    @Test
    void givenNonExistingEventId_whenCallsExistsByEventId_thenShouldReturnFalse() {
        // given
        assertEquals(0, pixWebhookEventJpaRepository.count());

        final var nonExistingEventId = UUID.randomUUID().toString();

        // when
        final var exists = pixWebhookEventGateway.existsByEventId(nonExistingEventId);

        // then
        assertFalse(exists);
    }

    @Test
    void givenPersistedEntity_whenConvertsToAggregate_thenShouldMatchFieldValues() {
        // given
        final var expectedEventId = UUID.randomUUID().toString();
        final var expectedEndToEndId = UUID.randomUUID().toString();
        final var expectedType = PixWebhookEventType.CREDIT_CONFIRMED;
        final var expectedOccurredAt = Instant.now().minusSeconds(200);
        final var expectedProcessedAt = Instant.now();

        final var expectedEvent = PixWebhookEvent.newEvent(
            expectedType,
            expectedEventId,
            expectedEndToEndId,
            expectedOccurredAt
        );

        final var saved = pixWebhookEventJpaRepository.save(PixWebhookEventEntity.from(expectedEvent));

        final var expectedId = saved.getId();

        // when
        final var loaded = pixWebhookEventJpaRepository.findById(saved.getId()).get();

        final var aggregate = loaded.toAggregate();

        // then
        assertEquals(expectedId, aggregate.getId().getValue());
        assertEquals(expectedEventId, aggregate.getEventId());
        assertEquals(expectedEndToEndId, aggregate.getEndToEndId());
        assertEquals(expectedType, aggregate.getType());
        assertEquals(expectedOccurredAt, aggregate.getOccurredAt());
        assertEquals(expectedProcessedAt, aggregate.getProcessedAt());
    }
}
