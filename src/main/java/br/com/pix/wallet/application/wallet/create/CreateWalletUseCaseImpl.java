package br.com.pix.wallet.application.wallet.create;

import br.com.pix.wallet.domain.wallet.WalletGateway;
import br.com.pix.wallet.domain.exception.DomainException;
import br.com.pix.wallet.domain.validation.handler.Notification;
import br.com.pix.wallet.domain.wallet.Wallet;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class CreateWalletUseCaseImpl implements CreateWalletUseCase {

    private final WalletGateway walletGateway;

    public CreateWalletUseCaseImpl(final WalletGateway walletGateway) {
        this.walletGateway = walletGateway;
    }

    @Override
    @Transactional
    public CreateWalletOutput execute(final CreateWalletCommand command) {
        final var notification = Notification.create();

        final var wallet = Wallet.newWallet(command.ownerId());

        wallet.validate(notification);

        if (notification.hasError()) {
            throw DomainException.with(notification.getErrors());
        }

        final var saved = walletGateway.save(wallet);

        return CreateWalletOutput.from(saved.getId().getValue(), saved.getOwnerId());
    }
}
