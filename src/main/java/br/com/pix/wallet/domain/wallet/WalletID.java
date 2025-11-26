package br.com.pix.wallet.domain.wallet;

import br.com.pix.wallet.domain.core.Identifier;
import br.com.pix.wallet.domain.exception.DomainException;
import br.com.pix.wallet.domain.validation.Error;

import java.util.UUID;

public class WalletID extends Identifier<UUID> {

    private final UUID value;

    private WalletID(final UUID value) {
        this.value = value;
    }

    public static WalletID from(final UUID value) {
        return new WalletID(value);
    }

    public static WalletID unique() {
        return new WalletID(UUID.randomUUID());
    }

    public static WalletID from(final String id) {
        try {
            return new WalletID(UUID.fromString(id));
        } catch (Exception e) {
            throw DomainException.with(Error.of("Invalid UUID format for WalletID: " + id));
        }
    }

    @Override
    public UUID getValue() {
        return value;
    }
}
