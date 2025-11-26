package br.com.pix.wallet.domain.wallet;

public interface WalletGateway {
    Wallet findById(WalletID id);

    Wallet findByIdWithLock(WalletID id);

    Wallet save(Wallet wallet);

    boolean existsByOwnerId(String ownerId);
}