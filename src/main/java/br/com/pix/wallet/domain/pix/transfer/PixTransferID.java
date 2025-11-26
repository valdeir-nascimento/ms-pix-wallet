package br.com.pix.wallet.domain.pix.transfer;

import br.com.pix.wallet.domain.core.Identifier;

import java.util.UUID;

public class PixTransferID extends Identifier<UUID> {

    private final UUID value;

    private PixTransferID(final UUID value) {
        this.value = value;
    }

    public static PixTransferID from(final UUID value) {
        return new PixTransferID(value);
    }

    public static PixTransferID unique() {
        return new PixTransferID(UUID.randomUUID());
    }

    @Override
    public UUID getValue() {
        return value;
    }
}
