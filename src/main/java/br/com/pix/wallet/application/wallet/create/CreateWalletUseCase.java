package br.com.pix.wallet.application.wallet.create;

public interface CreateWalletUseCase {
    CreateWalletOutput execute(CreateWalletCommand command);
}