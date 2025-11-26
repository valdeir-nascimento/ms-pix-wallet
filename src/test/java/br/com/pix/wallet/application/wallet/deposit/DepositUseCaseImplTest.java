package br.com.pix.wallet.application.wallet.deposit;

import br.com.pix.wallet.domain.ledger.LedgerGateway;
import br.com.pix.wallet.domain.common.Money;
import br.com.pix.wallet.domain.exception.DomainException;
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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DepositUseCaseImplTest {

    @InjectMocks
    private DepositUseCaseImpl useCase;

    @Mock
    private WalletGateway walletGateway;

    @Mock
    private LedgerGateway ledgerGateway;

    @BeforeEach
    void cleanUp() {
        reset(walletGateway, ledgerGateway);
    }

    @Test
    void givenValidCommand_whenCallsExecute_thenShouldDepositMoney() {
        // given
        final var expectedWalletId = UUID.randomUUID();
        final var expectedAmount = BigDecimal.valueOf(100.00);
        final var command = DepositCommand.with(expectedWalletId.toString(), expectedAmount);

        final var wallet = mock(Wallet.class);
        when(wallet.getId()).thenReturn(WalletID.from(expectedWalletId));
        when(wallet.getCurrentBalance()).thenReturn(Money.of(expectedAmount));

        when(walletGateway.findByIdWithLock(any(WalletID.class))).thenReturn(wallet);
        when(walletGateway.save(any(Wallet.class))).thenAnswer(returnsFirstArg());
        when(ledgerGateway.save(any(LedgerEntry.class))).thenAnswer(returnsFirstArg());

        // when
        final var actualOutput = useCase.execute(command);

        // then
        assertNotNull(actualOutput);
        assertEquals(expectedWalletId, actualOutput.walletId());
        assertEquals(expectedAmount, actualOutput.newBalance());

        verify(walletGateway, times(1)).findByIdWithLock(argThat(id -> expectedWalletId.equals(id.getValue())));
        verify(wallet, times(1)).deposit(argThat(money -> expectedAmount.equals(money.getAmount())));
        verify(walletGateway, times(1)).save(wallet);
        verify(ledgerGateway, times(1)).save(any(LedgerEntry.class));
    }

    @Test
    void givenInvalidAmount_whenCallsExecute_thenShouldThrowException() {
        // given
        final var expectedWalletId = UUID.randomUUID();
        final var expectedAmount = BigDecimal.valueOf(-10.00);
        final var command = DepositCommand.with(expectedWalletId.toString(), expectedAmount);
        final var expectedErrorMessage = "'amount' must be greater than zero";

        // when
        final var actualException = assertThrows(DomainException.class, () -> useCase.execute(command));

        // then
        assertEquals(expectedErrorMessage, actualException.getErrors().get(0).message());

        verify(walletGateway, times(0)).findByIdWithLock(any());
        verify(walletGateway, times(0)).save(any());
        verify(ledgerGateway, times(0)).save(any());
    }

    @Test
    void givenNonExistingWallet_whenCallsExecute_thenShouldThrowException() {
        // given
        final var expectedWalletId = UUID.randomUUID();
        final var expectedAmount = BigDecimal.valueOf(100.00);
        final var command = DepositCommand.with(expectedWalletId.toString(), expectedAmount);
        final var expectedErrorMessage = "Wallet with ID %s was not found".formatted(expectedWalletId);

        when(walletGateway.findByIdWithLock(any(WalletID.class)))
                .thenThrow(DomainException.with(br.com.pix.wallet.domain.validation.Error.of(expectedErrorMessage)));

        // when
        final var actualException = assertThrows(DomainException.class, () -> useCase.execute(command));

        // then
        assertEquals(expectedErrorMessage, actualException.getErrors().get(0).message());

        verify(walletGateway, times(1)).findByIdWithLock(argThat(id -> expectedWalletId.equals(id.getValue())));
        verify(walletGateway, times(0)).save(any());
        verify(ledgerGateway, times(0)).save(any());
    }
}
