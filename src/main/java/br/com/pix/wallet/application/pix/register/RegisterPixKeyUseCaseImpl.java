package br.com.pix.wallet.application.pix.register;

import br.com.pix.wallet.domain.pix.pixkey.PixKeyGateway;
import br.com.pix.wallet.domain.wallet.WalletGateway;
import br.com.pix.wallet.domain.exception.DomainException;
import br.com.pix.wallet.domain.pix.pixkey.PixKey;
import br.com.pix.wallet.domain.pix.pixkey.PixKeyType;
import br.com.pix.wallet.domain.validation.Error;
import br.com.pix.wallet.domain.validation.handler.Notification;
import br.com.pix.wallet.domain.wallet.WalletID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegisterPixKeyUseCaseImpl implements RegisterPixKeyUseCase {

    private final PixKeyGateway pixKeyGateway;
    private final WalletGateway walletGateway;

    public RegisterPixKeyUseCaseImpl(final PixKeyGateway pixKeyGateway, final WalletGateway walletGateway) {
        this.pixKeyGateway = pixKeyGateway;
        this.walletGateway = walletGateway;
    }

    @Override
    @Transactional
    public RegisterPixKeyOutput execute(final RegisterPixKeyCommand command) {
        final var walletId = WalletID.from(command.walletId());
        final var keyType = PixKeyType.from(command.keyType());

        walletGateway.findById(walletId);

        final var notification = Notification.create();

        if (pixKeyGateway.existsByKeyValue(command.keyValue())) {
            notification.append(Error.of("Pix key already registered"));
        }

        if (notification.hasError()) {
            throw DomainException.with(notification.getErrors());
        }

        final var pixKey = PixKey.newPixKey(
            walletId,
            keyType,
            command.keyValue()
        );

        pixKey.validate(notification);

        if (notification.hasError()) {
            throw DomainException.with(notification.getErrors());
        }

        final var saved = pixKeyGateway.save(pixKey);

        return RegisterPixKeyOutput.from(saved);
    }
}
