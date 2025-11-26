package br.com.pix.wallet.application.wallet.deposit;

public interface DepositUseCase {
    DepositOutput execute(DepositCommand command);
}