package br.com.pix.wallet.application.pix.register;

import br.com.pix.wallet.domain.pix.pixkey.PixKey;

public record RegisterPixKeyOutput(
    String pixKeyId,
    String walletId,
    String keyType,
    String keyValue
) {

    public static RegisterPixKeyOutput from(final PixKey pixKey) {
        return new RegisterPixKeyOutput(
            pixKey.getId().getValue().toString(),
            pixKey.getWalletId().getValue().toString(),
            pixKey.getKeyType().name(),
            pixKey.getKeyValue()
        );
    }
}
