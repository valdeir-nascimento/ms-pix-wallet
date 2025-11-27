package br.com.pix.wallet.infrastructure.persistence.entity;

import br.com.pix.wallet.domain.pix.pixkey.PixKey;
import br.com.pix.wallet.domain.pix.pixkey.PixKeyID;
import br.com.pix.wallet.domain.pix.pixkey.PixKeyType;
import br.com.pix.wallet.domain.wallet.WalletID;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "pix_key")
public class PixKeyEntity {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "wallet_id", nullable = false, columnDefinition = "uuid")
    private UUID walletId;

    @Enumerated(EnumType.STRING)
    @Column(name = "key_type", nullable = false)
    private PixKeyType keyType;

    @Column(name = "key_value", nullable = false, unique = true)
    private String keyValue;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    protected PixKeyEntity() {
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

    public PixKeyType getKeyType() {
        return keyType;
    }

    public void setKeyType(PixKeyType keyType) {
        this.keyType = keyType;
    }

    public String getKeyValue() {
        return keyValue;
    }

    public void setKeyValue(String keyValue) {
        this.keyValue = keyValue;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final PixKeyEntity that = (PixKeyEntity) o;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @PrePersist
    public void prePersist() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }

    public static PixKeyEntity from(final PixKey pixKey) {
        final var entity = new PixKeyEntity();
        entity.setId(pixKey.getId().getValue());
        entity.setWalletId(pixKey.getWalletId().getValue());
        entity.setKeyType(pixKey.getKeyType());
        entity.setKeyValue(pixKey.getKeyValue());
        return entity;
    }

    public PixKey toAggregate() {
        return PixKey.with(
            PixKeyID.from(this.id),
            WalletID.from(this.walletId),
            this.keyType,
            this.keyValue
        );
    }
}
