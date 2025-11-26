package br.com.pix.wallet.application.wallet.deposit;

import br.com.pix.wallet.application.interfaces.LedgerGateway;
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
public class DepositUseCaseImpl implements DepositUseCase {

    private final WalletGateway walletGateway;
    private final LedgerGateway ledgerGateway;

    public DepositUseCaseImpl(final WalletGateway walletGateway, final LedgerGateway ledgerGateway) {
        this.walletGateway = walletGateway;
        this.ledgerGateway = ledgerGateway;
    }

    @Override
    @Transactional
    public DepositOutput execute(final DepositCommand command) {
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

        wallet.deposit(amount);

        final var saved = walletGateway.save(wallet);

        final var ledger = LedgerEntry.deposit(
            saved.getId(),
            amount,
            saved.getCurrentBalance()
        );

        ledger.validate(notification);

        if (notification.hasError()) {
            throw DomainException.with(notification.getErrors());
        }

        ledgerGateway.save(ledger);

        return DepositOutput.from(
            saved.getId().getValue(),
            saved.getCurrentBalance().getAmount()
        );
    }
}
