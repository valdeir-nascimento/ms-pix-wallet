package br.com.pix.wallet.domain.pix.transfer;

import java.util.Optional;

public interface PixTransferGateway {
    PixTransfer save(PixTransfer transfer);

    Optional<PixTransfer> findByEndToEndId(String endToEndId);

    boolean existsByIdempotencyKey(String idempotencyKey);
}