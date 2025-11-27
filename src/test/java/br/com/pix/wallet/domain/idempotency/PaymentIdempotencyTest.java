package br.com.pix.wallet.domain.idempotency;

import br.com.pix.wallet.domain.validation.Error;
import br.com.pix.wallet.domain.validation.ValidationHandler;
import br.com.pix.wallet.domain.validation.handler.Notification;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PaymentIdempotencyTest {

    @Test
    void givenValidKey_whenCallsStart_thenShouldCreateInProgressPaymentIdempotency() {
        // given
        final var expectedKey = "f16a06e9-97b5-4655-b2f1-a60f23e40bea";
        final var expectedStatus = PaymentIdempotencyStatus.IN_PROGRESS;

        // when
        final var actual = PaymentIdempotency.start(expectedKey);

        // then
        assertNotNull(actual);
        assertNull(actual.getId());
        assertEquals(expectedKey, actual.getKey());
        assertEquals(expectedStatus, actual.getStatus());
        assertNull(actual.getResponsePayload());
        assertNotNull(actual.getCreatedAt());
        assertNotNull(actual.getUpdatedAt());
    }

    @Test
    void givenValidParams_whenCallsWith_thenShouldCreatePaymentIdempotencyWithGivenData() {
        // given
        final var expectedId = PaymentIdempotencyID.from(1L);
        final var expectedKey = "f16a06e9-97b5-4655-b2f1-a60f23e40bea";
        final var expectedStatus = PaymentIdempotencyStatus.COMPLETED;
        final var expectedResponsePayload = """
            {
                "result" : "ok"
            }
            """;
        final var expectedCreatedAt = Instant.parse("2025-11-24T21:00:00Z");
        final var expectedUpdatedAt = Instant.parse("2025-11-24T21:05:00Z");

        // when
        final var actual = PaymentIdempotency.with(
            expectedId,
            expectedKey,
            expectedStatus,
            expectedResponsePayload,
            expectedCreatedAt,
            expectedUpdatedAt
        );

        // then
        assertNotNull(actual);
        assertEquals(expectedId, actual.getId());
        assertEquals(expectedKey, actual.getKey());
        assertEquals(expectedStatus, actual.getStatus());
        assertEquals(expectedResponsePayload, actual.getResponsePayload());
        assertEquals(expectedCreatedAt, actual.getCreatedAt());
        assertEquals(expectedUpdatedAt, actual.getUpdatedAt());
    }

    @Test
    void givenInProgressPaymentIdempotency_whenCallsComplete_thenShouldSetCompletedStatusAndUpdateFields() {
        // given
        final var initialKey = "f16a06e9-97b5-4655-b2f1-a60f23e40bea";
        final var initial = PaymentIdempotency.start(initialKey);
        final var previousUpdatedAt = initial.getUpdatedAt();
        final var expectedStatus = PaymentIdempotencyStatus.COMPLETED;
        final var expectedResponsePayload = """
            {
                "status" : "SUCCESS"
            }
            """;

        // when
        initial.complete(expectedResponsePayload);

        // then
        assertEquals(expectedStatus, initial.getStatus());
        assertEquals(expectedResponsePayload, initial.getResponsePayload());
        assertNotNull(initial.getUpdatedAt());
    }

    @Test
    void givenValidPaymentIdempotency_whenCallsValidate_thenShouldNotAppendErrors() {
        // given
        final var payment = PaymentIdempotency.start("f16a06e9-97b5-4655-b2f1-a60f23e40bea");
        final var validationHandler = mock(ValidationHandler.class);

        // when
        payment.validate(validationHandler);

        // then
        verify(validationHandler, never()).append(any(Error.class));
    }

    @Test
    void givenNullKey_whenCallsValidate_thenShouldReturnKeyError() {
        // given
        final var payment = PaymentIdempotency.with(
            PaymentIdempotencyID.from(1L),
            null,
            PaymentIdempotencyStatus.IN_PROGRESS,
            null,
            Instant.now(),
            Instant.now()
        );

        final var notification = Notification.create();

        // when
        payment.validate(notification);

        // then
        assertTrue(notification.hasError());
        assertEquals(1, notification.getErrors().size());
        assertEquals("'key' must not be null or blank", notification.getErrors().get(0).message());
    }

    @Test
    void givenBlankKey_whenCallsValidate_thenShouldReturnKeyError() {
        // given
        final var payment = PaymentIdempotency.with(
            PaymentIdempotencyID.from(1L),
            "   ",
            PaymentIdempotencyStatus.IN_PROGRESS,
            null,
            Instant.now(),
            Instant.now()
        );

        final var notification = Notification.create();

        // when
        payment.validate(notification);

        // then
        assertTrue(notification.hasError());
        assertEquals(1, notification.getErrors().size());
        assertEquals("'key' must not be null or blank", notification.getErrors().get(0).message());
    }

    @Test
    void givenNullStatus_whenCallsValidate_thenShouldReturnStatusError() {
        // given
        final var payment = PaymentIdempotency.with(
            PaymentIdempotencyID.from(1L),
            "idempotency-key",
            null,
            null,
            Instant.now(),
            Instant.now()
        );

        final var notification = Notification.create();

        // when
        payment.validate(notification);

        // then
        assertTrue(notification.hasError());
        assertEquals(1, notification.getErrors().size());
        assertEquals("'status' must not be null", notification.getErrors().get(0).message());
    }

    @Test
    void givenNullCreatedAt_whenCallsValidate_thenShouldReturnCreatedAtError() {
        // given
        final var payment = PaymentIdempotency.with(
            PaymentIdempotencyID.from(1L),
            "idempotency-key",
            PaymentIdempotencyStatus.IN_PROGRESS,
            null,
            null,
            Instant.now()
        );

        final var notification = Notification.create();

        // when
        payment.validate(notification);

        // then
        assertTrue(notification.hasError());
        assertEquals(1, notification.getErrors().size());
        assertEquals("'createdAt' must not be null", notification.getErrors().get(0).message());
    }

    @Test
    void givenNullUpdatedAt_whenCallsValidate_thenShouldReturnUpdatedAtError() {
        // given
        final var payment = PaymentIdempotency.with(
            PaymentIdempotencyID.from(1L),
            "idempotency-key",
            PaymentIdempotencyStatus.IN_PROGRESS,
            null,
            Instant.now(),
            null
        );

        final var notification = Notification.create();

        // when
        payment.validate(notification);

        // then
        assertTrue(notification.hasError());
        assertEquals(1, notification.getErrors().size());
        assertEquals("'updatedAt' must not be null", notification.getErrors().get(0).message());
    }

    @Test
    void givenMultipleInvalidFields_whenCallsValidate_thenShouldReturnAllErrors() {
        // given
        final var payment = PaymentIdempotency.with(
            PaymentIdempotencyID.from(1L),
            "   ",
            null,
            null,
            null,
            null
        );

        final var notification = Notification.create();

        // when
        payment.validate(notification);

        // then
        assertTrue(notification.hasError());
        assertEquals(4, notification.getErrors().size());
        assertEquals("'key' must not be null or blank", notification.getErrors().get(0).message());
        assertEquals("'status' must not be null", notification.getErrors().get(1).message());
        assertEquals("'createdAt' must not be null", notification.getErrors().get(2).message());
        assertEquals("'updatedAt' must not be null", notification.getErrors().get(3).message());
    }
}
