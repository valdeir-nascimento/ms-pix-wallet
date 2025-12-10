package br.com.pix.wallet.domain.user;

import br.com.pix.wallet.domain.core.Identifier;

import java.util.Objects;
import java.util.UUID;

public class UserAccountID extends Identifier<UUID> {

    private final UUID value;

    private UserAccountID(final UUID value) {
        this.value = value;
    }

    public static UserAccountID unique() {
        return new UserAccountID(UUID.randomUUID());
    }

    public static UserAccountID from(final UUID value) {
        return new UserAccountID(Objects.requireNonNull(value));
    }

    public static UserAccountID from(final String value) {
        return new UserAccountID(UUID.fromString(value));
    }

    @Override
    public UUID getValue() {
        return value;
    }
}

