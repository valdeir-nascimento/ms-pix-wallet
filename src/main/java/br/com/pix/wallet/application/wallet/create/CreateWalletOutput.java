package br.com.pix.wallet.application.wallet.create;

import java.util.UUID;

public record CreateWalletOutput(UUID walletId, String ownerId) {
    public static CreateWalletOutput from(UUID walletId, String ownerId) {
        return new CreateWalletOutput(walletId, ownerId);
    }
}