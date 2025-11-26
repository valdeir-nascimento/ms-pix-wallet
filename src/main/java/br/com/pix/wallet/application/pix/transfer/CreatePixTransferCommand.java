package br.com.pix.wallet.application.pix.transfer;

import java.math.BigDecimal;

public record CreatePixTransferCommand(
    String fromWalletId,
    String toWalletId,
    BigDecimal amount,
    String idempotencyKey,
    String endToEndId
) {

    public static CreatePixTransferCommand with(
        final String fromWalletId,
        final String toWalletId,
        final BigDecimal amount,
        final String idempotencyKey,
        final String endToEndId
    ) {
        return new CreatePixTransferCommand(
            fromWalletId,
            toWalletId,
            amount,
            idempotencyKey,
            endToEndId
        );
    }
}
