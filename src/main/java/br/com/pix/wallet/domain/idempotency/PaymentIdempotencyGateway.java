package br.com.pix.wallet.domain.idempotency;

import java.util.Optional;

public interface PaymentIdempotencyGateway {

    Optional<PaymentIdempotency> findByKeyAndScope(String key, String scope);

    PaymentIdempotency save(PaymentIdempotency paymentIdempotency);
}
