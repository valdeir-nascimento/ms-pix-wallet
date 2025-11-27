package br.com.pix.wallet.infrastructure.persistence.entity;

import br.com.pix.wallet.domain.ledger.LedgerEntry;
import br.com.pix.wallet.domain.ledger.LedgerEntryID;
import br.com.pix.wallet.domain.ledger.LedgerOperationType;
import br.com.pix.wallet.domain.common.Money;
import br.com.pix.wallet.domain.wallet.WalletID;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "ledger_entry")
public class LedgerEntryEntity {

    @Id
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    @Column(name = "wallet_id", nullable = false, columnDefinition = "uuid")
    private UUID walletId;

    @Column(name = "end_to_end_id")
    private String endToEndId;

    @Enumerated(EnumType.STRING)
    @Column(name = "operation_type", nullable = false)
    private LedgerOperationType operationType;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Column(name = "balance_after_operation", nullable = false)
    private BigDecimal balanceAfterOperation;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    protected LedgerEntryEntity() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getWalletId() {
        return walletId;
    }

    public void setWalletId(UUID walletId) {
        this.walletId = walletId;
    }

    public String getEndToEndId() {
        return endToEndId;
    }

    public void setEndToEndId(String endToEndId) {
        this.endToEndId = endToEndId;
    }

    public LedgerOperationType getOperationType() {
        return operationType;
    }

    public void setOperationType(LedgerOperationType operationType) {
        this.operationType = operationType;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getBalanceAfterOperation() {
        return balanceAfterOperation;
    }

    public void setBalanceAfterOperation(BigDecimal balanceAfterOperation) {
        this.balanceAfterOperation = balanceAfterOperation;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(Instant occurredAt) {
        this.occurredAt = occurredAt;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final LedgerEntryEntity that = (LedgerEntryEntity) o;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public static LedgerEntryEntity from(final LedgerEntry entry) {
        final var entity = new LedgerEntryEntity();

        entity.id = entry.getId() != null
            ? entry.getId().getValue()
            : UUID.randomUUID();

        entity.walletId = entry.getWalletId().getValue();
        entity.endToEndId = entry.getEndToEndId();
        entity.operationType = entry.getOperationType();
        entity.amount = entry.getAmount().getAmount();
        entity.balanceAfterOperation = entry.getBalanceAfterOperation().getAmount();
        entity.occurredAt = entry.getOccurredAt();
        return entity;
    }

    public LedgerEntry toAggregate() {
        return LedgerEntry.with(
            LedgerEntryID.from(this.id),
            WalletID.from(this.walletId),
            this.endToEndId,
            this.operationType,
            Money.of(this.amount),
            Money.of(this.balanceAfterOperation),
            this.occurredAt
        );
    }
}
