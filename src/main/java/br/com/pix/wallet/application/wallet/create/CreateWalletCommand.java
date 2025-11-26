package br.com.pix.wallet.application.wallet.create;

public record CreateWalletCommand(String ownerId) {
    public static CreateWalletCommand with(String ownerId) {
        return new CreateWalletCommand(ownerId);
    }
}
