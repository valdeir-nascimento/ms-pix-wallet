package br.com.pix.wallet.domain.pix.pixkey;

import br.com.pix.wallet.domain.validation.Error;
import br.com.pix.wallet.domain.validation.ValidationHandler;
import br.com.pix.wallet.domain.validation.Validator;

public class PixKeyValidator extends Validator {

    private final PixKey key;

    public PixKeyValidator(final PixKey key, final ValidationHandler handler) {
        super(handler);
        this.key = key;
    }

    @Override
    public void validate() {

        if (key.getWalletId() == null) {
            validationHandler().append(Error.of("'walletId' must not be null"));
        }

        if (key.getKeyType() == null) {
            validationHandler().append(Error.of("'keyType' must not be null"));
        }

        if (key.getKeyValue() == null || key.getKeyValue().isBlank()) {
            validationHandler().append(Error.of("'keyValue' must not be null or blank"));
            return;
        }

        if (key.getKeyType() != null) {
            final var error = key.getKeyType().validate(key.getKeyValue());
            if (error != null)
                validationHandler().append(error);
        }
    }
}
