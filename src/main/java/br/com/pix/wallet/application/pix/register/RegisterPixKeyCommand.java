package br.com.pix.wallet.application.pix.register;

public record RegisterPixKeyCommand(
    String walletId,
    String keyType,
    String keyValue
) {
    public static RegisterPixKeyCommand with(
        final String walletId,
        final String keyType,
        final String keyValue
    ) {
        return new RegisterPixKeyCommand(walletId, keyType, keyValue);
    }
}
