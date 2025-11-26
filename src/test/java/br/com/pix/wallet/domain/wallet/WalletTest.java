package br.com.pix.wallet.domain.wallet;

import br.com.pix.wallet.domain.common.Money;
import br.com.pix.wallet.domain.validation.Error;
import br.com.pix.wallet.domain.validation.ValidationHandler;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class WalletTest {

    @Test
    void givenValidOwnerId_whenCallsNewWallet_thenShouldCreateWallet() {
        // given
        final var expectedOwnerId = "da686494-cac2-4d2f-a454-5b5cdb9f6b35";
        final var expectedBalance = Money.zero();
        final var expectedStatus = WalletStatus.ACTIVE;

        // when
        final var actualWallet = Wallet.newWallet(expectedOwnerId);

        // then
        assertNotNull(actualWallet);
        assertNotNull(actualWallet.getId());
        assertEquals(expectedOwnerId, actualWallet.getOwnerId());
        assertEquals(expectedBalance.getAmount(), actualWallet.getCurrentBalance().getAmount());
        assertEquals(expectedStatus, actualWallet.getStatus());
    }

    @Test
    void givenValidParams_whenCallsWith_thenShouldCreateWallet() {
        // given
        final var expectedId = WalletID.unique();
        final var expectedOwnerId = "da686494-cac2-4d2f-a454-5b5cdb9f6b35";
        final var expectedBalance = Money.of(BigDecimal.TEN);
        final var expectedStatus = WalletStatus.ACTIVE;

        // when
        final var actualWallet = Wallet.with(expectedId, expectedOwnerId, expectedBalance, expectedStatus);

        // then
        assertNotNull(actualWallet);
        assertEquals(expectedId, actualWallet.getId());
        assertEquals(expectedOwnerId, actualWallet.getOwnerId());
        assertEquals(expectedBalance.getAmount(), actualWallet.getCurrentBalance().getAmount());
        assertEquals(expectedStatus, actualWallet.getStatus());
    }

    @Test
    void givenValidAmount_whenCallsDeposit_thenShouldIncreaseBalance() {
        // given
        final var expectedOwnerId = "da686494-cac2-4d2f-a454-5b5cdb9f6b35";
        final var initialBalance = Money.zero();
        final var depositAmount = Money.of(BigDecimal.valueOf(50.0));
        final var expectedBalance = initialBalance.add(depositAmount);

        final var wallet = Wallet.newWallet(expectedOwnerId);

        // when
        wallet.deposit(depositAmount);

        // then
        assertEquals(expectedBalance.getAmount(), wallet.getCurrentBalance().getAmount());
    }

    @Test
    void givenValidAmount_whenCallsWithdraw_thenShouldDecreaseBalance() {
        // given
        final var expectedOwnerId = "da686494-cac2-4d2f-a454-5b5cdb9f6b35";
        final var initialBalance = Money.of(BigDecimal.valueOf(100.0));
        final var withdrawAmount = Money.of(BigDecimal.valueOf(50.0));
        final var expectedBalance = initialBalance.subtract(withdrawAmount);

        final var wallet = Wallet.with(WalletID.unique(), expectedOwnerId, initialBalance, WalletStatus.ACTIVE);

        // when
        wallet.withdraw(withdrawAmount);

        // then
        assertEquals(expectedBalance.getAmount(), wallet.getCurrentBalance().getAmount());
    }

    @Test
    void givenAmountGreaterThanBalance_whenCallsWithdraw_thenShouldThrowException() {
        // given
        final var expectedOwnerId = "da686494-cac2-4d2f-a454-5b5cdb9f6b35";
        final var initialBalance = Money.of(BigDecimal.valueOf(10.0));
        final var withdrawAmount = Money.of(BigDecimal.valueOf(50.0));
        final var expectedErrorMessage = "Insufficient balance";

        final var wallet = Wallet.with(WalletID.unique(), expectedOwnerId, initialBalance, WalletStatus.ACTIVE);

        // when
        final var exception = assertThrows(IllegalStateException.class, () -> {
            wallet.withdraw(withdrawAmount);
        });

        // then
        assertEquals(expectedErrorMessage, exception.getMessage());
    }

    @Test
    void givenValidWallet_whenCallsValidate_thenShouldValidate() {
        // given
        final var wallet = Wallet.newWallet("da686494-cac2-4d2f-a454-5b5cdb9f6b35");
        final var validationHandler = Mockito.mock(ValidationHandler.class);

        // when
        wallet.validate(validationHandler);

        // then
        Mockito.verify(validationHandler, Mockito.times(0)).append(Mockito.any(Error.class));
    }
}
