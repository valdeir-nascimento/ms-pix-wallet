package br.com.pix.wallet.infrastructure.persistence.entity;

import br.com.pix.wallet.domain.pix.transfer.PixTransfer;
import br.com.pix.wallet.domain.pix.transfer.PixTransferID;
import br.com.pix.wallet.domain.pix.transfer.PixTransferStatus;
import br.com.pix.wallet.domain.common.Money;
import br.com.pix.wallet.domain.wallet.WalletID;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "pix_transfer")
public class PixTransferEntity {

    @Id
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    @Column(name = "end_to_end_id", nullable = false, unique = true)
    private String endToEndId;

    @Column(name = "from_wallet_id", nullable = false, columnDefinition = "uuid")
    private UUID fromWalletId;

    @Column(name = "to_wallet_id", nullable = false, columnDefinition = "uuid")
    private UUID toWalletId;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PixTransferStatus status;

    @Column(name = "idempotency_key", nullable = false)
    private String idempotencyKey;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected PixTransferEntity() {
    }


    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getEndToEndId() {
        return endToEndId;
    }

    public void setEndToEndId(String endToEndId) {
        this.endToEndId = endToEndId;
    }

    public UUID getFromWalletId() {
        return fromWalletId;
    }

    public void setFromWalletId(UUID fromWalletId) {
        this.fromWalletId = fromWalletId;
    }

    public UUID getToWalletId() {
        return toWalletId;
    }

    public void setToWalletId(UUID toWalletId) {
        this.toWalletId = toWalletId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public PixTransferStatus getStatus() {
        return status;
    }

    public void setStatus(PixTransferStatus status) {
        this.status = status;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final PixTransferEntity that = (PixTransferEntity) o;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @PrePersist
    public void prePersist() {
        this.createdAt = Instant.now();
    }

    public static PixTransferEntity from(final PixTransfer transfer) {
        final var entity = new PixTransferEntity();
        entity.id = transfer.getId().getValue();
        entity.endToEndId = transfer.getEndToEndId();
        entity.fromWalletId = transfer.getFromWalletId().getValue();
        entity.toWalletId = transfer.getToWalletId().getValue();
        entity.amount = transfer.getAmount().getAmount();
        entity.status = transfer.getStatus();
        entity.idempotencyKey = transfer.getIdempotencyKey();
        entity.createdAt = transfer.getCreatedAt();
        return entity;
    }

    public PixTransfer toAggregate() {
        return PixTransfer.with(
            PixTransferID.from(this.id),
            WalletID.from(this.fromWalletId),
            WalletID.from(this.toWalletId),
            Money.of(this.amount),
            this.status,
            this.endToEndId,
            this.idempotencyKey,
            this.createdAt
        );
    }
}