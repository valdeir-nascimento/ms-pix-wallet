package br.com.pix.wallet.domain.pix.transfer;

import br.com.pix.wallet.domain.common.Money;
import br.com.pix.wallet.domain.core.AggregateRoot;
import br.com.pix.wallet.domain.validation.ValidationHandler;
import br.com.pix.wallet.domain.wallet.WalletID;

import java.time.Instant;

public class PixTransfer extends AggregateRoot<PixTransferID> {

    private final WalletID fromWalletId;
    private final WalletID toWalletId;
    private final Money amount;
    private final PixTransferStatus status;
    private final String endToEndId;
    private final String idempotencyKey;
    private final Instant createdAt;

    private PixTransfer(
        final PixTransferID id,
        final WalletID fromWalletId,
        final WalletID toWalletId,
        final Money amount,
        final PixTransferStatus status,
        final String endToEndId,
        final String idempotencyKey,
        final Instant createdAt
    ) {
        super(id);
        this.fromWalletId = fromWalletId;
        this.toWalletId = toWalletId;
        this.amount = amount;
        this.status = status;
        this.endToEndId = endToEndId;
        this.idempotencyKey = idempotencyKey;
        this.createdAt = createdAt;
    }

    public static PixTransfer newTransfer(
        final WalletID fromWalletId,
        final WalletID toWalletId,
        final Money amount,
        final String idempotencyKey,
        final String endToEndId
    ) {
        return new PixTransfer(
            PixTransferID.unique(),
            fromWalletId,
            toWalletId,
            amount,
            PixTransferStatus.PENDING,
            endToEndId,
            idempotencyKey,
            Instant.now()
        );
    }

    public static PixTransfer with(
        final PixTransferID id,
        final WalletID fromWalletId,
        final WalletID toWalletId,
        final Money amount,
        final PixTransferStatus status,
        final String endToEndId,
        final String idempotencyKey,
        final Instant createdAt
    ) {
        return new PixTransfer(
            id,
            fromWalletId,
            toWalletId,
            amount,
            status,
            endToEndId,
            idempotencyKey,
            createdAt
        );
    }

    @Override
    public void validate(final ValidationHandler handler) {
        new PixTransferValidator(this, handler).validate();
    }

    public WalletID getFromWalletId() {
        return fromWalletId;
    }

    public WalletID getToWalletId() {
        return toWalletId;
    }

    public Money getAmount() {
        return amount;
    }

    public PixTransferStatus getStatus() {
        return status;
    }

    public String getEndToEndId() {
        return endToEndId;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
