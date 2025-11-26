package br.com.pix.wallet.domain.pix.pixkey;


import br.com.pix.wallet.domain.core.AggregateRoot;
import br.com.pix.wallet.domain.validation.ValidationHandler;
import br.com.pix.wallet.domain.wallet.WalletID;

public class PixKey extends AggregateRoot<PixKeyID> {

    private WalletID walletId;
    private PixKeyType keyType;
    private String keyValue;

    private PixKey(
        final PixKeyID id,
        final WalletID walletId,
        final PixKeyType keyType,
        final String keyValue
    ) {
        super(id);
        this.walletId = walletId;
        this.keyType = keyType;
        this.keyValue = keyValue;
    }

    public static PixKey newPixKey(
        final WalletID walletId,
        final PixKeyType keyType,
        final String keyValue
    ) {
        return new PixKey(
            PixKeyID.unique(),
            walletId,
            keyType,
            keyValue
        );
    }

    public static PixKey with(
        final PixKeyID id,
        final WalletID walletId,
        final PixKeyType keyType,
        final String keyValue
    ) {
        return new PixKey(
            id,
            walletId,
            keyType,
            keyValue
        );
    }

    @Override
    public void validate(final ValidationHandler handler) {
        new PixKeyValidator(this, handler).validate();
    }

    public WalletID getWalletId() {
        return walletId;
    }

    public PixKeyType getKeyType() {
        return keyType;
    }

    public String getKeyValue() {
        return keyValue;
    }
}
