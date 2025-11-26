package br.com.pix.wallet.domain.pix.webhook;

import br.com.pix.wallet.domain.validation.Error;
import br.com.pix.wallet.domain.validation.ValidationHandler;
import br.com.pix.wallet.domain.validation.handler.Notification;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PixWebhookEventTest {

    @Test
    void givenValidParams_whenCallsNewEvent_thenShouldCreateEvent() {
        // given
        final var expectedType = PixWebhookEventType.CREDIT_CONFIRMED;
        final var expectedEventId = "da686494-cac2-4d2f-a454-5b5cdb9f6b35";
        final var expectedEndToEndId = "f16a06e9-97b5-4655-b2f1-a60f23e40bea";
        final var expectedOccurredAt = Instant.now();

        // when
        final var actualEvent = PixWebhookEvent.newEvent(
            expectedType,
            expectedEventId,
            expectedEndToEndId,
            expectedOccurredAt
        );

        // then
        assertNotNull(actualEvent);
        assertNotNull(actualEvent.getId());
        assertEquals(expectedType, actualEvent.getType());
        assertEquals(expectedEventId, actualEvent.getEventId());
        assertEquals(expectedEndToEndId, actualEvent.getEndToEndId());
        assertEquals(expectedOccurredAt, actualEvent.getOccurredAt());
        assertNotNull(actualEvent.getProcessedAt());
    }

    @Test
    void givenValidParams_whenCallsWith_thenShouldCreateEventWithGivenData() {
        // given
        final var expectedId = PixWebhookEventID.unique();
        final var expectedType = PixWebhookEventType.REFUND_PROCESSED;
        final var expectedEventId = "da686494-cac2-4d2f-a454-5b5cdb9f6b35";
        final var expectedEndToEndId = "f16a06e9-97b5-4655-b2f1-a60f23e40bea";
        final var expectedOccurredAt = Instant.parse("2025-11-24T21:10:00Z");
        final var expectedProcessedAt = Instant.parse("2025-11-24T21:10:05Z");

        // when
        final var actualEvent = PixWebhookEvent.with(
            expectedId,
            expectedEventId,
            expectedEndToEndId,
            expectedType,
            expectedOccurredAt,
            expectedProcessedAt
        );

        // then
        assertNotNull(actualEvent);
        assertEquals(expectedId, actualEvent.getId());
        assertEquals(expectedType, actualEvent.getType());
        assertEquals(expectedEventId, actualEvent.getEventId());
        assertEquals(expectedEndToEndId, actualEvent.getEndToEndId());
        assertEquals(expectedOccurredAt, actualEvent.getOccurredAt());
        assertEquals(expectedProcessedAt, actualEvent.getProcessedAt());
    }

    @Test
    void givenValidEvent_whenCallsValidate_thenShouldNotAppendErrors() {
        // given
        final var event = PixWebhookEvent.newEvent(
            PixWebhookEventType.CREDIT_CONFIRMED,
            "da686494-cac2-4d2f-a454-5b5cdb9f6b35",
            "f16a06e9-97b5-4655-b2f1-a60f23e40bea",
            Instant.now()
        );

        final var validationHandler = mock(ValidationHandler.class);

        // when
        event.validate(validationHandler);

        // then
        verify(validationHandler, never()).append(any(Error.class));
    }

    @Test
    void givenNullEventId_whenCallsValidate_thenShouldReturnError() {
        // given
        final var event = PixWebhookEvent.with(
            PixWebhookEventID.unique(),
            null,
            "f16a06e9-97b5-4655-b2f1-a60f23e40bea",
            PixWebhookEventType.CREDIT_CONFIRMED,
            Instant.now(),
            Instant.now()
        );

        final var notification = Notification.create();

        // when
        event.validate(notification);

        // then
        assertTrue(notification.hasError());
        assertEquals(1, notification.getErrors().size());
        assertEquals("'eventId' must not be null or blank", notification.getErrors().get(0).message());
    }

    @Test
    void givenBlankEventId_whenCallsValidate_thenShouldReturnError() {
        // given
        final var event = PixWebhookEvent.with(
            PixWebhookEventID.unique(),
            "   ",
            "f16a06e9-97b5-4655-b2f1-a60f23e40bea",
            PixWebhookEventType.CREDIT_CONFIRMED,
            Instant.now(),
            Instant.now()
        );

        final var notification = Notification.create();

        // when
        event.validate(notification);

        // then
        assertTrue(notification.hasError());
        assertEquals(1, notification.getErrors().size());
        assertEquals("'eventId' must not be null or blank", notification.getErrors().get(0).message());
    }

    @Test
    void givenNullEndToEndId_whenCallsValidate_thenShouldReturnError() {
        // given
        final var event = PixWebhookEvent.with(
            PixWebhookEventID.unique(),
            "da686494-cac2-4d2f-a454-5b5cdb9f6b35",
            null,
            PixWebhookEventType.CREDIT_CONFIRMED,
            Instant.now(),
            Instant.now()
        );

        final var notification = Notification.create();

        // when
        event.validate(notification);

        // then
        assertTrue(notification.hasError());
        assertEquals(1, notification.getErrors().size());
        assertEquals("'endToEndId' must not be null or blank", notification.getErrors().get(0).message());
    }

    @Test
    void givenBlankEndToEndId_whenCallsValidate_thenShouldReturnError() {
        // given
        final var event = PixWebhookEvent.with(
            PixWebhookEventID.unique(),
            "da686494-cac2-4d2f-a454-5b5cdb9f6b35",
            "   ",
            PixWebhookEventType.CREDIT_CONFIRMED,
            Instant.now(),
            Instant.now()
        );

        final var notification = Notification.create();

        // when
        event.validate(notification);

        // then
        assertTrue(notification.hasError());
        assertEquals(1, notification.getErrors().size());
        assertEquals("'endToEndId' must not be null or blank", notification.getErrors().get(0).message());
    }

    @Test
    void givenNullType_whenCallsValidate_thenShouldReturnError() {
        // given
        final var event = PixWebhookEvent.with(
            PixWebhookEventID.unique(),
            "da686494-cac2-4d2f-a454-5b5cdb9f6b35",
            "f16a06e9-97b5-4655-b2f1-a60f23e40bea",
            null,
            Instant.now(),
            Instant.now()
        );

        final var notification = Notification.create();

        // when
        event.validate(notification);

        // then
        assertTrue(notification.hasError());
        assertEquals(1, notification.getErrors().size());
        assertEquals("'type' must not be null", notification.getErrors().get(0).message());
    }

    @Test
    void givenNullOccurredAt_whenCallsValidate_thenShouldReturnError() {
        // given
        final var event = PixWebhookEvent.with(
            PixWebhookEventID.unique(),
            "da686494-cac2-4d2f-a454-5b5cdb9f6b35",
            "f16a06e9-97b5-4655-b2f1-a60f23e40bea",
            PixWebhookEventType.CREDIT_CONFIRMED,
            null,
            Instant.now()
        );

        final var notification = Notification.create();

        // when
        event.validate(notification);

        // then
        assertTrue(notification.hasError());
        assertEquals(1, notification.getErrors().size());
        assertEquals("'occurredAt' must not be null", notification.getErrors().get(0).message());
    }

    @Test
    void givenNullProcessedAt_whenCallsValidate_thenShouldReturnError() {
        // given
        final var event = PixWebhookEvent.with(
            PixWebhookEventID.unique(),
            "da686494-cac2-4d2f-a454-5b5cdb9f6b35",
            "f16a06e9-97b5-4655-b2f1-a60f23e40bea",
            PixWebhookEventType.CREDIT_CONFIRMED,
            Instant.now(),
            null
        );

        final var notification = Notification.create();

        // when
        event.validate(notification);

        // then
        assertTrue(notification.hasError());
        assertEquals(1, notification.getErrors().size());
        assertEquals("'processedAt' must not be null", notification.getErrors().get(0).message());
    }
}
