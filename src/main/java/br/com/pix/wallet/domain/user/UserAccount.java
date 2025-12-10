package br.com.pix.wallet.domain.user;

import br.com.pix.wallet.domain.core.AggregateRoot;
import br.com.pix.wallet.domain.validation.ValidationHandler;

import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class UserAccount extends AggregateRoot<UserAccountID> {

    private final String username;
    private final String password;
    private final Set<UserRole> roles;
    private final Instant createdAt;
    private final Instant updatedAt;

    private UserAccount(
        final UserAccountID id,
        final String username,
        final String password,
        final Set<UserRole> roles,
        final Instant createdAt,
        final Instant updatedAt
    ) {
        super(id);
        this.username = username;
        this.password = password;
        this.roles = Collections.unmodifiableSet(new HashSet<>(roles));
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static UserAccount newAccount(
        final String username,
        final String password,
        final Set<UserRole> roles
    ) {
        final var now = Instant.now();
        return new UserAccount(
            UserAccountID.unique(),
            username,
            password,
            roles,
            now,
            now
        );
    }

    public static UserAccount with(
        final UserAccountID id,
        final String username,
        final String password,
        final Set<UserRole> roles,
        final Instant createdAt,
        final Instant updatedAt
    ) {
        return new UserAccount(id, username, password, roles, createdAt, updatedAt);
    }

    @Override
    public void validate(final ValidationHandler handler) {
        new UserAccountValidator(this, handler).validate();
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public Set<UserRole> getRoles() {
        return roles;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}

