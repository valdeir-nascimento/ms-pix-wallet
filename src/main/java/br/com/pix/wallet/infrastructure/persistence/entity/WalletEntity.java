package br.com.pix.wallet.infrastructure.persistence.entity;

import br.com.pix.wallet.domain.common.Money;
import br.com.pix.wallet.domain.wallet.Wallet;
import br.com.pix.wallet.domain.wallet.WalletID;
import br.com.pix.wallet.domain.wallet.WalletStatus;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "wallet")
public class WalletEntity {

    @Id
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    @Column(name = "owner_id", nullable = false, unique = true)
    private String ownerId;

    @Column(name = "current_balance", nullable = false)
    private BigDecimal currentBalance;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private WalletStatus status;

    @Version
    private Long version;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    protected WalletEntity() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(final UUID id) {
        this.id = id;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(final String ownerId) {
        this.ownerId = ownerId;
    }

    public BigDecimal getCurrentBalance() {
        return currentBalance;
    }

    public void setCurrentBalance(final BigDecimal currentBalance) {
        this.currentBalance = currentBalance;
    }

    public WalletStatus getStatus() {
        return status;
    }

    public void setStatus(final WalletStatus status) {
        this.status = status;
    }

    public Long getVersion() {
        return version;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final WalletEntity that = (WalletEntity) o;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @PrePersist
    public void onPrePersist() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
        if (this.currentBalance == null) {
            this.currentBalance = BigDecimal.ZERO;
        }
        if (this.status == null) {
            this.status = WalletStatus.ACTIVE;
        }
    }

    @PreUpdate
    public void onPreUpdate() {
        this.updatedAt = Instant.now();
    }

    public static WalletEntity from(final Wallet wallet) {
        final var entity = new WalletEntity();
        if (wallet.getId() != null) {
            entity.id = wallet.getId().getValue();
        }
        entity.ownerId = wallet.getOwnerId();
        entity.currentBalance = wallet.getCurrentBalance().getAmount();
        entity.status = wallet.getStatus();
        return entity;
    }

    public Wallet toAggregate() {
        return Wallet.with(
            WalletID.from(this.id),
            this.ownerId,
            Money.of(this.currentBalance),
            this.status
        );
    }

    public WalletEntity updateFrom(final Wallet wallet) {
        this.ownerId = wallet.getOwnerId();
        this.currentBalance = wallet.getCurrentBalance().getAmount();
        this.status = wallet.getStatus();
        return this;
    }
}
