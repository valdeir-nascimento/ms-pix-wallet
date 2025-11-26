package br.com.pix.wallet.application.pix.transfer;

import br.com.pix.wallet.domain.pix.transfer.PixTransfer;
import br.com.pix.wallet.domain.pix.transfer.PixTransferStatus;

import java.math.BigDecimal;

public record CreatePixTransferOutput(
    String transferId,
    String endToEndId,
    String fromWalletId,
    String toWalletId,
    BigDecimal amount,
    PixTransferStatus status
) {

    public static CreatePixTransferOutput from(final PixTransfer transfer) {
        return new CreatePixTransferOutput(
            transfer.getId().getValue().toString(),
            transfer.getEndToEndId(),
            transfer.getFromWalletId().getValue().toString(),
            transfer.getToWalletId().getValue().toString(),
            transfer.getAmount().getAmount(),
            transfer.getStatus()
        );
    }
}
