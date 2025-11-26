package br.com.pix.wallet.domain.pix.transfer;

import br.com.pix.wallet.domain.validation.Error;
import br.com.pix.wallet.domain.validation.ValidationHandler;
import br.com.pix.wallet.domain.validation.Validator;

public class PixTransferValidator extends Validator {

    private final PixTransfer transfer;

    public PixTransferValidator(final PixTransfer transfer, final ValidationHandler handler) {
        super(handler);
        this.transfer = transfer;
    }

    @Override
    public void validate() {

        if (transfer.getFromWalletId() == null) {
            validationHandler().append(Error.of("'fromWalletId' must not be null"));
        }

        if (transfer.getToWalletId() == null) {
            validationHandler().append(Error.of("'toWalletId' must not be null"));
        }

        if (transfer.getFromWalletId() != null
            && transfer.getToWalletId() != null
            && transfer.getFromWalletId().equals(transfer.getToWalletId())) {
            validationHandler().append(Error.of("'fromWalletId' and 'toWalletId' must be different"));
        }

        if (transfer.getAmount() == null || transfer.getAmount().isZeroOrNegative()) {
            validationHandler().append(Error.of("'amount' must be greater than zero"));
        }

        if (transfer.getIdempotencyKey() == null || transfer.getIdempotencyKey().isBlank()) {
            validationHandler().append(Error.of("'idempotencyKey' must not be null or blank"));
        }

        if (transfer.getEndToEndId() == null || transfer.getEndToEndId().isBlank()) {
            validationHandler().append(Error.of("'endToEndId' must not be null or blank"));
        }
    }
}
