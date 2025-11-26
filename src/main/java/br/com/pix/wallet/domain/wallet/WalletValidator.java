package br.com.pix.wallet.domain.wallet;

import br.com.pix.wallet.domain.validation.Error;
import br.com.pix.wallet.domain.validation.ValidationHandler;
import br.com.pix.wallet.domain.validation.Validator;

public class WalletValidator extends Validator {

    private final Wallet wallet;

    public WalletValidator(final Wallet wallet, final ValidationHandler handler) {
        super(handler);
        this.wallet = wallet;
    }

    @Override
    public void validate() {
        if (wallet.getOwnerId() == null || wallet.getOwnerId().isBlank()) {
            validationHandler().append(Error.of("'ownerId' cannot be null or blank"));
        }

        if (wallet.getCurrentBalance() == null) {
            validationHandler().append(Error.of("'currentBalance' cannot be null"));
        }

        if (wallet.getStatus() == null) {
            validationHandler().append(Error.of("'status' cannot be null"));
        }
    }
}
