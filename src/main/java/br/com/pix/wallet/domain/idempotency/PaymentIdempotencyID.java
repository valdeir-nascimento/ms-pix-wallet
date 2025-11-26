package br.com.pix.wallet.domain.idempotency;

import br.com.pix.wallet.domain.core.Identifier;

public class PaymentIdempotencyID extends Identifier<Long> {

    private final Long value;

    private PaymentIdempotencyID(final Long value) {
        this.value = value;
    }

    public static PaymentIdempotencyID from(final Long value) {
        return new PaymentIdempotencyID(value);
    }

    @Override
    public Long getValue() {
        return value;
    }
}
