package br.com.pix.wallet.application.pix.register;

public interface RegisterPixKeyUseCase {
    RegisterPixKeyOutput execute(RegisterPixKeyCommand command);
}