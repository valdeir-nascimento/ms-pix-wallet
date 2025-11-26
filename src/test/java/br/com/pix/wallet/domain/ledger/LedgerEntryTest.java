package br.com.pix.wallet.domain.ledger;

import br.com.pix.wallet.domain.common.Money;
import br.com.pix.wallet.domain.validation.Error;
import br.com.pix.wallet.domain.validation.ValidationHandler;
import br.com.pix.wallet.domain.wallet.WalletID;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class LedgerEntryTest {

    @Test
    void givenValidParams_whenCallsWith_thenShouldCreateLedgerEntry() {
        // given
        final var expectedId = LedgerEntryID.unique();
        final var expectedWalletId = WalletID.unique();
        final var expectedEndToEndId = "2435eccd-7efd-45b5-8be0-39197092d03d";
        final var expectedOperationType = LedgerOperationType.PIX_CREDIT;
        final var expectedAmount = Money.of(BigDecimal.TEN);
        final var expectedBalanceAfterOperation = Money.of(BigDecimal.valueOf(20.0));
        final var expectedOccurredAt = Instant.now();

        // when
        final var actualEntry = LedgerEntry.with(
            expectedId,
            expectedWalletId,
            expectedEndToEndId,
            expectedOperationType,
            expectedAmount,
            expectedBalanceAfterOperation,
            expectedOccurredAt
        );

        // then
        assertNotNull(actualEntry);
        assertEquals(expectedId, actualEntry.getId());
        assertEquals(expectedWalletId, actualEntry.getWalletId());
        assertEquals(expectedEndToEndId, actualEntry.getEndToEndId());
        assertEquals(expectedOperationType, actualEntry.getOperationType());
        assertEquals(expectedAmount.getAmount(), actualEntry.getAmount().getAmount());
        assertEquals(expectedBalanceAfterOperation.getAmount(), actualEntry.getBalanceAfterOperation().getAmount());
        assertEquals(expectedOccurredAt, actualEntry.getOccurredAt());
    }

    @Test
    void givenValidParams_whenCallsDeposit_thenShouldCreateDepositEntry() {
        // given
        final var expectedWalletId = WalletID.unique();
        final var expectedAmount = Money.of(BigDecimal.TEN);
        final var expectedBalance = Money.of(BigDecimal.valueOf(20.0));
        final var expectedOperationType = LedgerOperationType.DEPOSIT;

        // when
        final var actualEntry = LedgerEntry.deposit(
            expectedWalletId,
            expectedAmount,
            expectedBalance
        );

        // then
        assertNotNull(actualEntry);
        assertNotNull(actualEntry.getId());
        assertEquals(expectedWalletId, actualEntry.getWalletId());
        assertNull(actualEntry.getEndToEndId());
        assertEquals(expectedOperationType, actualEntry.getOperationType());
        assertEquals(expectedAmount.getAmount(), actualEntry.getAmount().getAmount());
        assertEquals(expectedBalance.getAmount(), actualEntry.getBalanceAfterOperation().getAmount());
        assertNotNull(actualEntry.getOccurredAt());
    }

    @Test
    void givenValidParams_whenCallsWithdraw_thenShouldCreateWithdrawEntry() {
        // given
        final var expectedWalletId = WalletID.unique();
        final var expectedAmount = Money.of(BigDecimal.TEN);
        final var expectedBalance = Money.zero();
        final var expectedOperationType = LedgerOperationType.WITHDRAW;

        // when
        final var actualEntry = LedgerEntry.withdraw(
            expectedWalletId,
            expectedAmount,
            expectedBalance
        );

        // then
        assertNotNull(actualEntry);
        assertNotNull(actualEntry.getId());
        assertEquals(expectedWalletId, actualEntry.getWalletId());
        assertNull(actualEntry.getEndToEndId());
        assertEquals(expectedOperationType, actualEntry.getOperationType());
        assertEquals(expectedAmount.getAmount(), actualEntry.getAmount().getAmount());
        assertEquals(expectedBalance.getAmount(), actualEntry.getBalanceAfterOperation().getAmount());
        assertNotNull(actualEntry.getOccurredAt());
    }

    @Test
    void givenValidParams_whenCallsDebitPix_thenShouldCreateDebitPixEntry() {
        // given
        final var expectedWalletId = WalletID.unique();
        final var expectedEndToEndId = "2435eccd-7efd-45b5-8be0-39197092d03d";
        final var expectedAmount = Money.of(BigDecimal.TEN);
        final var expectedBalance = Money.zero();
        final var expectedOperationType = LedgerOperationType.PIX_DEBIT;

        // when
        final var actualEntry = LedgerEntry.debitPix(expectedWalletId, expectedEndToEndId, expectedAmount,
            expectedBalance);

        // then
        assertNotNull(actualEntry);
        assertNotNull(actualEntry.getId());
        assertEquals(expectedWalletId, actualEntry.getWalletId());
        assertEquals(expectedEndToEndId, actualEntry.getEndToEndId());
        assertEquals(expectedOperationType, actualEntry.getOperationType());
        assertEquals(expectedAmount.getAmount(), actualEntry.getAmount().getAmount());
        assertEquals(expectedBalance.getAmount(), actualEntry.getBalanceAfterOperation().getAmount());
        assertNotNull(actualEntry.getOccurredAt());
    }

    @Test
    void givenValidParams_whenCallsCreditPix_thenShouldCreateCreditPixEntry() {
        // given
        final var expectedWalletId = WalletID.unique();
        final var expectedEndToEndId = "2435eccd-7efd-45b5-8be0-39197092d03d";
        final var expectedAmount = Money.of(BigDecimal.TEN);
        final var expectedBalance = Money.of(BigDecimal.valueOf(20.0));
        final var expectedOperationType = LedgerOperationType.PIX_CREDIT;

        // when
        final var actualEntry = LedgerEntry.creditPix(
            expectedWalletId,
            expectedEndToEndId,
            expectedAmount,
            expectedBalance
        );

        // then
        assertNotNull(actualEntry);
        assertNotNull(actualEntry.getId());
        assertEquals(expectedWalletId, actualEntry.getWalletId());
        assertEquals(expectedEndToEndId, actualEntry.getEndToEndId());
        assertEquals(expectedOperationType, actualEntry.getOperationType());
        assertEquals(expectedAmount.getAmount(), actualEntry.getAmount().getAmount());
        assertEquals(expectedBalance.getAmount(), actualEntry.getBalanceAfterOperation().getAmount());
        assertNotNull(actualEntry.getOccurredAt());
    }

    @Test
    void givenValidParams_whenCallsRefund_thenShouldCreateRefundEntry() {
        // given
        final var expectedWalletId = WalletID.unique();
        final var expectedEndToEndId = "2435eccd-7efd-45b5-8be0-39197092d03d";
        final var expectedAmount = Money.of(BigDecimal.TEN);
        final var expectedBalance = Money.of(BigDecimal.valueOf(20.0));
        final var expectedOperationType = LedgerOperationType.REFUND;

        // when
        final var actualEntry = LedgerEntry.refund(
            expectedWalletId,
            expectedEndToEndId,
            expectedAmount,
            expectedBalance
        );

        // then
        assertNotNull(actualEntry);
        assertNotNull(actualEntry.getId());
        assertEquals(expectedWalletId, actualEntry.getWalletId());
        assertEquals(expectedEndToEndId, actualEntry.getEndToEndId());
        assertEquals(expectedOperationType, actualEntry.getOperationType());
        assertEquals(expectedAmount.getAmount(), actualEntry.getAmount().getAmount());
        assertEquals(expectedBalance.getAmount(), actualEntry.getBalanceAfterOperation().getAmount());
        assertNotNull(actualEntry.getOccurredAt());
    }

    @Test
    void givenValidLedgerEntry_whenCallsValidate_thenShouldValidate() {
        // given
        final var entry = LedgerEntry.deposit(WalletID.unique(), Money.of(BigDecimal.TEN), Money.of(BigDecimal.TEN));
        final var validationHandler = Mockito.mock(ValidationHandler.class);

        // when
        entry.validate(validationHandler);

        // then
        Mockito.verify(validationHandler, Mockito.times(0)).append(Mockito.any(Error.class));
    }
}
