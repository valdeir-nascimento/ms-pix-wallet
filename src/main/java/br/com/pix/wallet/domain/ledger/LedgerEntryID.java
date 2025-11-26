package br.com.pix.wallet.domain.ledger;

import br.com.pix.wallet.domain.core.Identifier;

import java.util.UUID;

public class LedgerEntryID extends Identifier<UUID> {

    private final UUID value;

    private LedgerEntryID(final UUID value) {
        this.value = value;
    }

    public static LedgerEntryID from(final UUID value) {
        return new LedgerEntryID(value);
    }

    public static LedgerEntryID unique() {
        return new LedgerEntryID(UUID.randomUUID());
    }

    @Override
    public UUID getValue() {
        return value;
    }
}
