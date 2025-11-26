package br.com.pix.wallet.application.wallet.balance;

import br.com.pix.wallet.domain.ledger.LedgerGateway;
import br.com.pix.wallet.domain.common.Money;
import br.com.pix.wallet.domain.ledger.LedgerEntry;
import br.com.pix.wallet.domain.wallet.Wallet;
import br.com.pix.wallet.domain.wallet.WalletGateway;
import br.com.pix.wallet.domain.wallet.WalletID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetBalanceUseCaseImplTest {

    @InjectMocks
    private GetBalanceUseCaseImpl useCase;

    @Mock
    private WalletGateway walletGateway;

    @Mock
    private LedgerGateway ledgerGateway;

    @BeforeEach
    void cleanUp() {
        reset(walletGateway, ledgerGateway);
    }

    @Test
    void givenExistingWalletAndNullInstant_whenCallsExecute_thenShouldReturnCurrentWalletBalance() {
        // given
        final var expectedWalletId = UUID.randomUUID();
        final var expectedAmount = BigDecimal.valueOf(150.00);
        final var expectedMoney = Money.of(expectedAmount);

        final var wallet = mock(Wallet.class);
        when(wallet.getCurrentBalance()).thenReturn(expectedMoney);
        when(walletGateway.findById(any(WalletID.class))).thenReturn(wallet);

        // when
        final var actualOutput = useCase.execute(expectedWalletId, null);

        // then
        assertEquals(expectedAmount, actualOutput.currentBalance());

        verify(walletGateway, times(1))
            .findById(argThat(id -> expectedWalletId.equals(id.getValue())));

        verifyNoInteractions(ledgerGateway);
    }

    @Test
    void givenExistingWalletAndInstant_whenLedgerHasLastEntry_thenShouldReturnLastEntryBalance() {
        // given
        final var expectedWalletId = UUID.randomUUID();
        final var queryInstant = Instant.now();
        final var expectedLedgerBalance = BigDecimal.valueOf(320.50);
        final var ledgerBalanceMoney = Money.of(expectedLedgerBalance);

        final var wallet = mock(Wallet.class);
        final var ledgerEntry = mock(LedgerEntry.class);

        when(wallet.getId()).thenReturn(WalletID.from(expectedWalletId));
        when(walletGateway.findById(any(WalletID.class))).thenReturn(wallet);

        when(ledgerEntry.getBalanceAfterOperation()).thenReturn(ledgerBalanceMoney);

        when(ledgerGateway.findLastEntryBefore(any(WalletID.class), eq(queryInstant)))
            .thenReturn(Optional.of(ledgerEntry));

        // when
        final var actualOutput = useCase.execute(expectedWalletId, queryInstant);

        // then
        assertEquals(expectedLedgerBalance, actualOutput.currentBalance());

        verify(walletGateway, times(1))
            .findById(argThat(id -> expectedWalletId.equals(id.getValue())));

        verify(ledgerGateway, times(1))
            .findLastEntryBefore(any(WalletID.class), eq(queryInstant));
    }

    @Test
    void givenExistingWalletAndInstant_whenLedgerHasNoEntry_thenShouldReturnZeroBalance() {
        // given
        final var expectedWalletId = UUID.randomUUID();
        final var queryInstant = Instant.now();
        final var expectedBalance = BigDecimal.ZERO;

        final var wallet = mock(Wallet.class);
        when(wallet.getId()).thenReturn(WalletID.from(expectedWalletId));
        when(walletGateway.findById(any(WalletID.class))).thenReturn(wallet);

        when(ledgerGateway.findLastEntryBefore(any(WalletID.class), eq(queryInstant)))
            .thenReturn(Optional.empty());

        // when
        final var actualOutput = useCase.execute(expectedWalletId, queryInstant);

        // then
        assertEquals(expectedBalance, actualOutput.currentBalance());

        verify(walletGateway, times(1))
            .findById(argThat(id -> expectedWalletId.equals(id.getValue())));

        verify(ledgerGateway, times(1))
            .findLastEntryBefore(any(WalletID.class), eq(queryInstant));
    }
}
