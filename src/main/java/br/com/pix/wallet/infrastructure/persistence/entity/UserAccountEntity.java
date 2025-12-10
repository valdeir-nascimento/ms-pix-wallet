package br.com.pix.wallet.infrastructure.persistence.entity;

import br.com.pix.wallet.domain.user.UserAccount;
import br.com.pix.wallet.domain.user.UserAccountID;
import br.com.pix.wallet.domain.user.UserRole;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "user_account")
public class UserAccountEntity {

    @Id
    @Column(name = "id", nullable = false, columnDefinition = "uuid")
    private UUID id;

    @Column(name = "username", nullable = false, unique = true)
    private String username;

    @Column(name = "password", nullable = false)
    private String password;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_account_roles", joinColumns = @JoinColumn(name = "user_account_id"))
    @Column(name = "role", nullable = false)
    @Enumerated(EnumType.STRING)
    private Set<UserRole> roles = new HashSet<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public UserAccountEntity() {
    }

    public static UserAccountEntity from(final UserAccount account) {
        final var entity = new UserAccountEntity();
        entity.id = account.getId().getValue();
        entity.username = account.getUsername();
        entity.password = account.getPassword();
        entity.roles = new HashSet<>(account.getRoles());
        entity.createdAt = account.getCreatedAt();
        entity.updatedAt = account.getUpdatedAt();
        return entity;
    }

    public UserAccount toAggregate() {
        return UserAccount.with(
            UserAccountID.from(this.id),
            this.username,
            this.password,
            this.roles,
            this.createdAt,
            this.updatedAt
        );
    }
}

