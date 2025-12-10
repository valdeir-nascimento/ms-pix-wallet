package br.com.pix.wallet.application.security.register;

public interface RegisterUserUseCase {

    RegisterUserOutput execute(RegisterUserCommand command);
}

