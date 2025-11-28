package br.com.pix.wallet.application.wallet.create;

import br.com.pix.wallet.application.metrics.ApplicationMetrics;
import br.com.pix.wallet.domain.validation.Error;
import br.com.pix.wallet.domain.wallet.WalletGateway;
import br.com.pix.wallet.domain.exception.DomainException;
import br.com.pix.wallet.domain.validation.handler.Notification;
import br.com.pix.wallet.domain.wallet.Wallet;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class CreateWalletUseCaseImpl implements CreateWalletUseCase {

    private final WalletGateway walletGateway;
    private final ApplicationMetrics applicationMetrics;

    public CreateWalletUseCaseImpl(
        final WalletGateway walletGateway,
        final ApplicationMetrics applicationMetrics
    ) {
        this.walletGateway = walletGateway;
        this.applicationMetrics = applicationMetrics;
    }

    @Override
    @Transactional
    public CreateWalletOutput execute(final CreateWalletCommand command) {
        try {
            final var notification = Notification.create();

            if (walletGateway.existsByOwnerId(command.ownerId())) {
                notification.append(Error.of("Owner ID already has a wallet"));
            }

            if (notification.hasError()) {
                throw DomainException.with(notification.getErrors());
            }

            final var wallet = Wallet.newWallet(command.ownerId());

            wallet.validate(notification);

            if (notification.hasError()) {
                throw DomainException.with(notification.getErrors());
            }

            final var saved = walletGateway.save(wallet);
            applicationMetrics.recordWalletCreation(true);

            return CreateWalletOutput.from(saved.getId().getValue(), saved.getOwnerId());
        } catch (DomainException ex) {
            applicationMetrics.recordWalletCreation(false);
            throw ex;
        }
    }
}
