package br.com.pix.wallet.application.wallet.withdraw;

public interface WithdrawUseCase {
    WithdrawOutput execute(WithdrawCommand command);
}