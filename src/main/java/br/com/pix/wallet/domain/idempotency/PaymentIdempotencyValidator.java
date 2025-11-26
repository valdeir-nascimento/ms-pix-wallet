package br.com.pix.wallet.domain.idempotency;

import br.com.pix.wallet.domain.validation.Error;
import br.com.pix.wallet.domain.validation.ValidationHandler;
import br.com.pix.wallet.domain.validation.Validator;

public class PaymentIdempotencyValidator extends Validator {

    private final PaymentIdempotency paymentIdempotency;

    public PaymentIdempotencyValidator(
        final PaymentIdempotency paymentIdempotency,
        final ValidationHandler handler
    ) {
        super(handler);
        this.paymentIdempotency = paymentIdempotency;
    }

    @Override
    public void validate() {

        if (paymentIdempotency.getKey() == null || paymentIdempotency.getKey().isBlank()) {
            validationHandler().append(Error.of("'key' must not be null or blank"));
        }

        if (paymentIdempotency.getStatus() == null) {
            validationHandler().append(Error.of("'status' must not be null"));
        }

        if (paymentIdempotency.getCreatedAt() == null) {
            validationHandler().append(Error.of("'createdAt' must not be null"));
        }

        if (paymentIdempotency.getUpdatedAt() == null) {
            validationHandler().append(Error.of("'updatedAt' must not be null"));
        }
    }
}
