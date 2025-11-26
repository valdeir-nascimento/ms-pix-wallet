package br.com.pix.wallet.domain.pix.transfer;

import br.com.pix.wallet.domain.common.Money;
import br.com.pix.wallet.domain.validation.Error;
import br.com.pix.wallet.domain.validation.ValidationHandler;
import br.com.pix.wallet.domain.validation.handler.Notification;
import br.com.pix.wallet.domain.wallet.WalletID;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PixTransferTest {

    @Test
    void givenValidParams_whenCallsNewTransfer_thenShouldCreatePendingTransfer() {
        // given
        final var expectedFromWalletId = WalletID.unique();
        final var expectedToWalletId = WalletID.unique();
        final var expectedAmount = Money.of(BigDecimal.valueOf(150.0));
        final var expectedStatus = PixTransferStatus.PENDING;
        final var expectedEndToEndId = "f16a06e9-97b5-4655-b2f1-a60f23e40bea";
        final var expectedIdempotencyKey = "73fedf90-7154-4cbd-8337-4f09172331e2";

        // when
        final var actualTransfer = PixTransfer.newTransfer(
            expectedFromWalletId,
            expectedToWalletId,
            expectedAmount,
            expectedIdempotencyKey,
            expectedEndToEndId
        );

        // then
        assertNotNull(actualTransfer);
        assertNotNull(actualTransfer.getId());
        assertEquals(expectedFromWalletId, actualTransfer.getFromWalletId());
        assertEquals(expectedToWalletId, actualTransfer.getToWalletId());
        assertEquals(expectedAmount, actualTransfer.getAmount());
        assertEquals(expectedStatus, actualTransfer.getStatus());
        assertEquals(expectedEndToEndId, actualTransfer.getEndToEndId());
        assertEquals(expectedIdempotencyKey, actualTransfer.getIdempotencyKey());
        assertNotNull(actualTransfer.getCreatedAt());
    }

    @Test
    void givenValidParams_whenCallsWith_thenShouldCreateTransferWithGivenData() {
        // given
        final var expectedId = PixTransferID.unique();
        final var expectedFromWalletId = WalletID.unique();
        final var expectedToWalletId = WalletID.unique();
        final var expectedAmount = Money.of(BigDecimal.valueOf(200.0));
        final var expectedStatus = PixTransferStatus.CONFIRMED;
        final var expectedEndToEndId = "c8f34e2b-7ad7-4afb-9d56-2c5d0e2d0b91";
        final var expectedIdempotencyKey = "a94f9c0d-9a1f-4e89-8c2b-37a2c9dd02bf";
        final var expectedCreatedAt = Instant.now();

        // when
        final var actualTransfer = PixTransfer.with(
            expectedId,
            expectedFromWalletId,
            expectedToWalletId,
            expectedAmount,
            expectedStatus,
            expectedEndToEndId,
            expectedIdempotencyKey,
            expectedCreatedAt
        );

        // then
        assertNotNull(actualTransfer);
        assertEquals(expectedId, actualTransfer.getId());
        assertEquals(expectedFromWalletId, actualTransfer.getFromWalletId());
        assertEquals(expectedToWalletId, actualTransfer.getToWalletId());
        assertEquals(expectedAmount, actualTransfer.getAmount());
        assertEquals(expectedStatus, actualTransfer.getStatus());
        assertEquals(expectedEndToEndId, actualTransfer.getEndToEndId());
        assertEquals(expectedIdempotencyKey, actualTransfer.getIdempotencyKey());
        assertEquals(expectedCreatedAt, actualTransfer.getCreatedAt());
    }

    @Test
    void givenValidTransfer_whenCallsValidate_thenShouldNotAppendErrors() {
        // given
        final var fromWalletId = WalletID.unique();
        final var toWalletId = WalletID.unique();
        final var amount = Money.of(BigDecimal.valueOf(100.0));
        final var idempotencyKey = "9d1e1b3d-0dac-4c40-8d41-3e5f7a19aabc";
        final var endToEndId = "f16a06e9-97b5-4655-b2f1-a60f23e40bea";

        final var transfer = PixTransfer.newTransfer(
            fromWalletId,
            toWalletId,
            amount,
            idempotencyKey,
            endToEndId
        );

        final var validationHandler = mock(ValidationHandler.class);

        // when
        transfer.validate(validationHandler);

        // then
        verify(validationHandler, never()).append(any(Error.class));
    }

    @Test
    void givenNullFromWalletId_whenCallsValidate_thenShouldReturnError() {
        // given
        final var toWalletId = WalletID.unique();
        final var amount = Money.of(BigDecimal.ONE);
        final var idempotencyKey = "9d1e1b3d-0dac-4c40-8d41-3e5f7a19aabc";
        final var endToEndId = "f16a06e9-97b5-4655-b2f1-a60f23e40bea";

        final var transfer = createTransfer(
            null,
            toWalletId,
            amount,
            idempotencyKey,
            endToEndId
        );

        final var notification = Notification.create();

        // when
        transfer.validate(notification);

        // then
        assertTrue(notification.hasError());
        assertEquals(1, notification.getErrors().size());
        assertEquals("'fromWalletId' must not be null", notification.getErrors().get(0).message());
    }

    @Test
    void givenNullToWalletId_whenCallsValidate_thenShouldReturnError() {
        // given
        final var fromWalletId = WalletID.unique();
        final var amount = Money.of(BigDecimal.ONE);
        final var idempotencyKey = "9d1e1b3d-0dac-4c40-8d41-3e5f7a19aabc";
        final var endToEndId = "f16a06e9-97b5-4655-b2f1-a60f23e40bea";

        final var transfer = createTransfer(
            fromWalletId,
            null,
            amount,
            idempotencyKey,
            endToEndId
        );

        final var notification = Notification.create();

        // when
        transfer.validate(notification);

        // then
        assertTrue(notification.hasError());
        assertEquals(1, notification.getErrors().size());
        assertEquals("'toWalletId' must not be null", notification.getErrors().get(0).message());
    }

    @Test
    void givenSameWalletIds_whenCallsValidate_thenShouldReturnError() {
        // given
        final var walletId = WalletID.unique();
        final var amount = Money.of(BigDecimal.ONE);
        final var idempotencyKey = "9d1e1b3d-0dac-4c40-8d41-3e5f7a19aabc";
        final var endToEndId = "f16a06e9-97b5-4655-b2f1-a60f23e40bea";

        final var transfer = createTransfer(
            walletId,
            walletId,
            amount,
            idempotencyKey,
            endToEndId
        );

        final var notification = Notification.create();

        // when
        transfer.validate(notification);

        // then
        assertTrue(notification.hasError());
        assertEquals(1, notification.getErrors().size());
        assertEquals("'fromWalletId' and 'toWalletId' must be different", notification.getErrors().get(0).message());
    }

    @Test
    void givenNullAmount_whenCallsValidate_thenShouldReturnError() {
        // given
        final var fromWalletId = WalletID.unique();
        final var toWalletId = WalletID.unique();
        final Money amount = null;
        final var idempotencyKey = "9d1e1b3d-0dac-4c40-8d41-3e5f7a19aabc";
        final var endToEndId = "f16a06e9-97b5-4655-b2f1-a60f23e40bea";

        final var transfer = createTransfer(
            fromWalletId,
            toWalletId,
            amount,
            idempotencyKey,
            endToEndId
        );

        final var notification = Notification.create();

        // when
        transfer.validate(notification);

        // then
        assertTrue(notification.hasError());
        assertEquals(1, notification.getErrors().size());
        assertEquals("'amount' must be greater than zero", notification.getErrors().get(0).message());
    }

    @Test
    void givenZeroAmount_whenCallsValidate_thenShouldReturnError() {
        // given
        final var fromWalletId = WalletID.unique();
        final var toWalletId = WalletID.unique();
        final var amount = Money.zero();
        final var idempotencyKey = "9d1e1b3d-0dac-4c40-8d41-3e5f7a19aabc";
        final var endToEndId = "f16a06e9-97b5-4655-b2f1-a60f23e40bea";

        final var transfer = createTransfer(
            fromWalletId,
            toWalletId,
            amount,
            idempotencyKey,
            endToEndId
        );

        final var notification = Notification.create();

        // when
        transfer.validate(notification);

        // then
        assertTrue(notification.hasError());
        assertEquals(1, notification.getErrors().size());
        assertEquals("'amount' must be greater than zero", notification.getErrors().get(0).message());
    }

    @Test
    void givenNegativeAmount_whenCallsValidate_thenShouldReturnError() {
        // given
        final var fromWalletId = WalletID.unique();
        final var toWalletId = WalletID.unique();
        final var amount = Money.zero();
        final var idempotencyKey = "9d1e1b3d-0dac-4c40-8d41-3e5f7a19aabc";
        final var endToEndId = "f16a06e9-97b5-4655-b2f1-a60f23e40bea";

        final var transfer = createTransfer(
            fromWalletId,
            toWalletId,
            amount,
            idempotencyKey,
            endToEndId
        );

        final var notification = Notification.create();

        // when
        transfer.validate(notification);

        // then
        assertTrue(notification.hasError());
        assertEquals(1, notification.getErrors().size());
        assertEquals("'amount' must be greater than zero", notification.getErrors().get(0).message());
    }

    @Test
    void givenNullIdempotencyKey_whenCallsValidate_thenShouldReturnError() {
        // given
        final var fromWalletId = WalletID.unique();
        final var toWalletId = WalletID.unique();
        final var amount = Money.of(BigDecimal.ONE);
        final String idempotencyKey = null;
        final var endToEndId = "f16a06e9-97b5-4655-b2f1-a60f23e40bea";

        final var transfer = createTransfer(
            fromWalletId,
            toWalletId,
            amount,
            idempotencyKey,
            endToEndId
        );

        final var notification = Notification.create();

        // when
        transfer.validate(notification);

        // then
        assertTrue(notification.hasError());
        assertEquals(1, notification.getErrors().size());
        assertEquals("'idempotencyKey' must not be null or blank", notification.getErrors().get(0).message());
    }

    @Test
    void givenBlankIdempotencyKey_whenCallsValidate_thenShouldReturnError() {
        // given
        final var fromWalletId = WalletID.unique();
        final var toWalletId = WalletID.unique();
        final var amount = Money.of(BigDecimal.ONE);
        final var idempotencyKey = "   ";
        final var endToEndId = "f16a06e9-97b5-4655-b2f1-a60f23e40bea";

        final var transfer = createTransfer(
            fromWalletId,
            toWalletId,
            amount,
            idempotencyKey,
            endToEndId
        );

        final var notification = Notification.create();

        // when
        transfer.validate(notification);

        // then
        assertTrue(notification.hasError());
        assertEquals(1, notification.getErrors().size());
        assertEquals("'idempotencyKey' must not be null or blank",
            notification.getErrors().get(0).message());
    }

    @Test
    void givenNullEndToEndId_whenCallsValidate_thenShouldReturnError() {
        // given
        final var fromWalletId = WalletID.unique();
        final var toWalletId = WalletID.unique();
        final var amount = Money.of(BigDecimal.ONE);
        final var idempotencyKey = "9d1e1b3d-0dac-4c40-8d41-3e5f7a19aabc";
        final String endToEndId = null;


        final var transfer = createTransfer(
            fromWalletId,
            toWalletId,
            amount,
            idempotencyKey,
            endToEndId
        );

        final var notification = Notification.create();

        // when
        transfer.validate(notification);

        // then
        assertTrue(notification.hasError());
        assertEquals(1, notification.getErrors().size());
        assertEquals("'endToEndId' must not be null or blank", notification.getErrors().get(0).message());
    }

    @Test
    void givenBlankEndToEndId_whenCallsValidate_thenShouldReturnError() {
        // given
        final var fromWalletId = WalletID.unique();
        final var toWalletId = WalletID.unique();
        final var amount = Money.of(BigDecimal.ONE);
        final var idempotencyKey = "9d1e1b3d-0dac-4c40-8d41-3e5f7a19aabc";
        final var endToEndId = "   ";

        final var transfer = createTransfer(
            fromWalletId,
            toWalletId,
            amount,
            idempotencyKey,
            endToEndId
        );

        final var notification = Notification.create();

        // when
        transfer.validate(notification);

        // then
        assertTrue(notification.hasError());
        assertEquals(1, notification.getErrors().size());
        assertEquals("'endToEndId' must not be null or blank", notification.getErrors().get(0).message());
    }

    private PixTransfer createTransfer(
        final WalletID fromWalletId,
        final WalletID toWalletId,
        final Money amount,
        final String idempotencyKey,
        final String endToEndId
    ) {
        return PixTransfer.with(
            PixTransferID.unique(),
            fromWalletId,
            toWalletId,
            amount,
            PixTransferStatus.PENDING,
            endToEndId,
            idempotencyKey,
            Instant.now()
        );
    }
}
