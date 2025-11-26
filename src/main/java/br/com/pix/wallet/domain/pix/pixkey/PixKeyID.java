package br.com.pix.wallet.domain.pix.pixkey;

import br.com.pix.wallet.domain.core.Identifier;

import java.util.UUID;

public class PixKeyID extends Identifier<UUID> {

    private final UUID value;

    private PixKeyID(final UUID value) {
        this.value = value;
    }

    public static PixKeyID from(final UUID value) {
        return new PixKeyID(value);
    }

    public static PixKeyID unique() {
        return new PixKeyID(UUID.randomUUID());
    }

    @Override
    public UUID getValue() {
        return value;
    }
}
