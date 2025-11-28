package br.com.pix.wallet.application.wallet.withdraw;

import br.com.pix.wallet.application.metrics.ApplicationMetrics;
import br.com.pix.wallet.domain.ledger.LedgerGateway;
import br.com.pix.wallet.domain.wallet.WalletGateway;
import br.com.pix.wallet.domain.exception.DomainException;
import br.com.pix.wallet.domain.ledger.LedgerEntry;
import br.com.pix.wallet.domain.validation.Error;
import br.com.pix.wallet.domain.validation.handler.Notification;
import br.com.pix.wallet.domain.common.Money;
import br.com.pix.wallet.domain.wallet.WalletID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class WithdrawUseCaseImpl implements WithdrawUseCase {

    private final WalletGateway walletGateway;
    private final LedgerGateway ledgerGateway;
    private final ApplicationMetrics applicationMetrics;

    public WithdrawUseCaseImpl(
        final WalletGateway walletGateway,
        final LedgerGateway ledgerGateway,
        final ApplicationMetrics applicationMetrics
    ) {
        this.walletGateway = walletGateway;
        this.ledgerGateway = ledgerGateway;
        this.applicationMetrics = applicationMetrics;
    }

    @Override
    @Transactional
    public WithdrawOutput execute(final WithdrawCommand command) {
        try {
            final var notification = Notification.create();

            if (command.amount() == null || command.amount().compareTo(BigDecimal.ZERO) <= 0) {
                notification.append(Error.of("'amount' must be greater than zero"));
            }

            if (notification.hasError()) {
                throw DomainException.with(notification.getErrors());
            }

            final var walletId = WalletID.from(command.walletId());
            final var wallet = walletGateway.findByIdWithLock(walletId);

            final var amount = Money.of(command.amount());

            try {
                wallet.withdraw(amount);
            } catch (final IllegalStateException ex) {
                notification.append(Error.of(ex.getMessage()));
            }

            if (notification.hasError()) {
                throw DomainException.with(notification.getErrors());
            }

            final var saved = walletGateway.save(wallet);

            final var ledger = LedgerEntry.withdraw(
                saved.getId(),
                amount,
                saved.getCurrentBalance()
            );
            ledger.validate(notification);

            if (notification.hasError()) {
                throw DomainException.with(notification.getErrors());
            }

            ledgerGateway.save(ledger);
            applicationMetrics.recordWithdrawOperation(true, command.amount());

            return WithdrawOutput.from(
                saved.getId().getValue(),
                saved.getCurrentBalance().getAmount()
            );
        } catch (DomainException ex) {
            applicationMetrics.recordWithdrawOperation(false, command.amount());
            throw ex;
        }
    }
}
