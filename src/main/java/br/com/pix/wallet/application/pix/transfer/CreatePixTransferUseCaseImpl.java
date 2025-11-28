package br.com.pix.wallet.application.pix.transfer;

import br.com.pix.wallet.application.metrics.ApplicationMetrics;
import br.com.pix.wallet.domain.common.Money;
import br.com.pix.wallet.domain.exception.DomainException;
import br.com.pix.wallet.domain.ledger.LedgerEntry;
import br.com.pix.wallet.domain.ledger.LedgerGateway;
import br.com.pix.wallet.domain.pix.transfer.PixTransfer;
import br.com.pix.wallet.domain.pix.transfer.PixTransferGateway;
import br.com.pix.wallet.domain.validation.Error;
import br.com.pix.wallet.domain.validation.handler.Notification;
import br.com.pix.wallet.domain.wallet.WalletGateway;
import br.com.pix.wallet.domain.wallet.WalletID;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreatePixTransferUseCaseImpl implements CreatePixTransferUseCase {

    private final WalletGateway walletGateway;
    private final LedgerGateway ledgerGateway;
    private final PixTransferGateway pixTransferGateway;
    private final ApplicationMetrics applicationMetrics;

    public CreatePixTransferUseCaseImpl(
        final WalletGateway walletGateway,
        final LedgerGateway ledgerGateway,
        final PixTransferGateway pixTransferGateway,
        final ApplicationMetrics applicationMetrics
    ) {
        this.walletGateway = walletGateway;
        this.ledgerGateway = ledgerGateway;
        this.pixTransferGateway = pixTransferGateway;
        this.applicationMetrics = applicationMetrics;
    }

    @Override
    @Transactional
    public CreatePixTransferOutput execute(final CreatePixTransferCommand command) {
        final Timer.Sample sample = applicationMetrics.startPixTransferTimer();
        var success = false;

        try {
            final var notification = Notification.create();

            final var fromId = WalletID.from(command.fromWalletId());
            final var toId = WalletID.from(command.toWalletId());
            final var amount = Money.of(command.amount());

            if (pixTransferGateway.existsByIdempotencyKey(command.idempotencyKey())) {
                notification.append(Error.of("Transfer already processed for this idempotency key"));
                throw DomainException.with(notification.getErrors());
            }

            final var fromWallet = walletGateway.findByIdWithLock(fromId);
            final var toWallet = walletGateway.findByIdWithLock(toId);

            if (fromWallet.getCurrentBalance().compareTo(amount) < 0) {
                notification.append(Error.of("Insufficient balance in source wallet"));
                throw DomainException.with(notification.getErrors());
            }

            fromWallet.withdraw(amount);
            toWallet.deposit(amount);

            final var updatedFromWallet = walletGateway.save(fromWallet);
            final var updatedToWallet = walletGateway.save(toWallet);

            final var debitEntry = LedgerEntry.debitPix(
                updatedFromWallet.getId(),
                command.endToEndId(),
                amount,
                updatedFromWallet.getCurrentBalance()
            );

            final var creditEntry = LedgerEntry.creditPix(
                updatedToWallet.getId(),
                command.endToEndId(),
                amount,
                updatedToWallet.getCurrentBalance()
            );

            debitEntry.validate(notification);
            creditEntry.validate(notification);

            if (notification.hasError()) {
                throw DomainException.with(notification.getErrors());
            }

            ledgerGateway.save(debitEntry);
            ledgerGateway.save(creditEntry);

            final var transfer = PixTransfer.newTransfer(
                updatedFromWallet.getId(),
                updatedToWallet.getId(),
                amount,
                command.idempotencyKey(),
                command.endToEndId()
            );

            transfer.validate(notification);

            if (notification.hasError()) {
                throw DomainException.with(notification.getErrors());
            }

            final var savedTransfer = pixTransferGateway.save(transfer);
            success = true;
            return CreatePixTransferOutput.from(savedTransfer);
        } finally {
            applicationMetrics.recordPixTransferOutcome(sample, success, command.amount());
        }
    }
}
