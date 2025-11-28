package br.com.pix.wallet.application.pix.transfer;

import br.com.pix.wallet.application.UseCaseTest;
import br.com.pix.wallet.application.metrics.ApplicationMetrics;
import br.com.pix.wallet.domain.common.Money;
import br.com.pix.wallet.domain.exception.DomainException;
import br.com.pix.wallet.domain.exception.NotFoundException;
import br.com.pix.wallet.domain.ledger.LedgerGateway;
import br.com.pix.wallet.domain.pix.transfer.PixTransferGateway;
import br.com.pix.wallet.domain.wallet.Wallet;
import br.com.pix.wallet.domain.wallet.WalletGateway;
import br.com.pix.wallet.domain.wallet.WalletID;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CreatePixTransferUseCaseImplTest extends UseCaseTest {

    @InjectMocks
    private CreatePixTransferUseCaseImpl createPixTransferUseCase;

    @Mock
    private WalletGateway walletGateway;

    @Mock
    private LedgerGateway ledgerGateway;

    @Mock
    private PixTransferGateway pixTransferGateway;

    @Mock
    private ApplicationMetrics applicationMetrics;

    @Override
    protected List<Object> getMocks() {
        return List.of(walletGateway, ledgerGateway, pixTransferGateway, applicationMetrics);
    }

    @Test
    void givenValidCommand_whenCreatePixTransfer_thenReturnOutput() {
        // given
        final var expectedFromWalletId = WalletID.unique();
        final var expectedToWalletId = WalletID.unique();
        final var expectedAmount = BigDecimal.valueOf(100.00);
        final var expectedIdempotencyKey = UUID.randomUUID().toString();
        final var expectedEndToEndId = "E12345678202311261234567890AB";

        final var command = CreatePixTransferCommand.with(
            expectedFromWalletId.getValue().toString(),
            expectedToWalletId.getValue().toString(),
            expectedAmount,
            expectedIdempotencyKey,
            expectedEndToEndId
        );
        mockTimer();

        final var fromWallet = mock(Wallet.class);
        final var toWallet = mock(Wallet.class);
        final var currentBalance = Money.of(BigDecimal.valueOf(500.00));
        final var newBalance = Money.of(BigDecimal.valueOf(400.00));

        when(fromWallet.getId()).thenReturn(expectedFromWalletId);
        when(toWallet.getId()).thenReturn(expectedToWalletId);
        when(fromWallet.getCurrentBalance()).thenReturn(currentBalance, newBalance);
        when(toWallet.getCurrentBalance()).thenReturn(Money.of(BigDecimal.valueOf(200.00)));

        when(pixTransferGateway.existsByIdempotencyKey(expectedIdempotencyKey)).thenReturn(false);
        when(walletGateway.findByIdWithLock(any())).thenReturn(fromWallet, toWallet);
        when(walletGateway.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(ledgerGateway.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(pixTransferGateway.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        final var output = createPixTransferUseCase.execute(command);

        // then
        assertNotNull(output);
        assertNotNull(output.transferId());
        verify(pixTransferGateway).existsByIdempotencyKey(expectedIdempotencyKey);
        verify(walletGateway, times(2)).findByIdWithLock(any());
        verify(fromWallet).withdraw(any());
        verify(toWallet).deposit(any());
        verify(walletGateway, times(2)).save(any());
        verify(ledgerGateway, times(2)).save(any());
        verify(pixTransferGateway).save(any());
    }

    @Test
    void givenDuplicateIdempotencyKey_whenCreatePixTransfer_thenThrowDomainException() {
        // given
        final var expectedFromWalletId = WalletID.unique();
        final var expectedToWalletId = WalletID.unique();
        final var expectedAmount = BigDecimal.valueOf(100.00);
        final var duplicateIdempotencyKey = UUID.randomUUID().toString();
        final var expectedEndToEndId = "E12345678202311261234567890AB";

        final var command = CreatePixTransferCommand.with(
            expectedFromWalletId.getValue().toString(),
            expectedToWalletId.getValue().toString(),
            expectedAmount,
            duplicateIdempotencyKey,
            expectedEndToEndId);

        when(pixTransferGateway.existsByIdempotencyKey(duplicateIdempotencyKey)).thenReturn(true);
        mockTimer();

        // when
        final var exception = assertThrows(DomainException.class, () -> createPixTransferUseCase.execute(command));

        // then
        assertNotNull(exception);
        assertEquals(1, exception.getErrors().size());
        assertEquals("Transfer already processed for this idempotency key", exception.getErrors().get(0).message());
        verify(pixTransferGateway).existsByIdempotencyKey(duplicateIdempotencyKey);
        verify(walletGateway, never()).findByIdWithLock(any());
        verify(walletGateway, never()).save(any());
        verify(ledgerGateway, never()).save(any());
        verify(pixTransferGateway, never()).save(any());
    }

    @Test
    void givenInsufficientBalance_whenCreatePixTransfer_thenThrowDomainException() {
        // given
        final var expectedFromWalletId = WalletID.unique();
        final var expectedToWalletId = WalletID.unique();
        final var expectedAmount = BigDecimal.valueOf(1000.00);
        final var expectedIdempotencyKey = UUID.randomUUID().toString();
        final var expectedEndToEndId = "E12345678202311261234567890AB";

        final var command = CreatePixTransferCommand.with(
            expectedFromWalletId.getValue().toString(),
            expectedToWalletId.getValue().toString(),
            expectedAmount,
            expectedIdempotencyKey,
            expectedEndToEndId
        );

        final var fromWallet = mock(Wallet.class);
        final var toWallet = mock(Wallet.class);
        final var insufficientBalance = Money.of(BigDecimal.valueOf(50.00));

        when(fromWallet.getCurrentBalance()).thenReturn(insufficientBalance);
        when(pixTransferGateway.existsByIdempotencyKey(expectedIdempotencyKey)).thenReturn(false);
        when(walletGateway.findByIdWithLock(any())).thenReturn(fromWallet, toWallet);
        mockTimer();

        // when
        final var exception = assertThrows(DomainException.class, () -> createPixTransferUseCase.execute(command));

        // then
        assertNotNull(exception);
        assertEquals(1, exception.getErrors().size());
        assertEquals("Insufficient balance in source wallet", exception.getErrors().get(0).message());
        verify(pixTransferGateway).existsByIdempotencyKey(expectedIdempotencyKey);
        verify(walletGateway, times(2)).findByIdWithLock(any());
        verify(fromWallet, never()).withdraw(any());
        verify(toWallet, never()).deposit(any());
        verify(walletGateway, never()).save(any());
        verify(ledgerGateway, never()).save(any());
        verify(pixTransferGateway, never()).save(any());
    }

    @Test
    void givenInvalidFromWalletId_whenCreatePixTransfer_thenThrowNotFoundException() {
        // given
        final var invalidFromWalletId = UUID.randomUUID().toString();
        final var expectedToWalletId = WalletID.unique();
        final var expectedAmount = BigDecimal.valueOf(100.00);
        final var expectedIdempotencyKey = UUID.randomUUID().toString();
        final var expectedEndToEndId = "E12345678202311261234567890AB";

        final var command = CreatePixTransferCommand.with(
            invalidFromWalletId,
            expectedToWalletId.getValue().toString(),
            expectedAmount,
            expectedIdempotencyKey,
            expectedEndToEndId
        );

        when(pixTransferGateway.existsByIdempotencyKey(expectedIdempotencyKey)).thenReturn(false);
        when(walletGateway.findByIdWithLock(any())).thenThrow(NotFoundException.class);
        mockTimer();

        // when
        final var exception = assertThrows(NotFoundException.class, () -> createPixTransferUseCase.execute(command));

        // then
        assertNotNull(exception);
        verify(pixTransferGateway).existsByIdempotencyKey(expectedIdempotencyKey);
        verify(walletGateway).findByIdWithLock(any());
        verify(walletGateway, never()).save(any());
        verify(ledgerGateway, never()).save(any());
        verify(pixTransferGateway, never()).save(any());
    }

    @Test
    void givenInvalidToWalletId_whenCreatePixTransfer_thenThrowNotFoundException() {
        // given
        final var expectedFromWalletId = WalletID.unique();
        final var invalidToWalletId = UUID.randomUUID().toString();
        final var expectedAmount = BigDecimal.valueOf(100.00);
        final var expectedIdempotencyKey = UUID.randomUUID().toString();
        final var expectedEndToEndId = "E12345678202311261234567890AB";

        final var command = CreatePixTransferCommand.with(
            expectedFromWalletId.getValue().toString(),
            invalidToWalletId,
            expectedAmount,
            expectedIdempotencyKey,
            expectedEndToEndId
        );

        final var fromWallet = mock(Wallet.class);

        when(pixTransferGateway.existsByIdempotencyKey(expectedIdempotencyKey)).thenReturn(false);
        when(walletGateway.findByIdWithLock(any()))
            .thenReturn(fromWallet)
            .thenThrow(NotFoundException.class);
        mockTimer();

        // when
        final var exception = assertThrows(NotFoundException.class, () -> createPixTransferUseCase.execute(command));

        // then
        assertNotNull(exception);
        verify(pixTransferGateway).existsByIdempotencyKey(expectedIdempotencyKey);
        verify(walletGateway, times(2)).findByIdWithLock(any());
        verify(walletGateway, never()).save(any());
        verify(ledgerGateway, never()).save(any());
        verify(pixTransferGateway, never()).save(any());
    }

    private void mockTimer() {
        when(applicationMetrics.startPixTransferTimer()).thenReturn(Timer.start(new SimpleMeterRegistry()));
    }
}
