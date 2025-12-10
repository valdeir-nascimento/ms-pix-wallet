package br.com.pix.wallet.application.security.auth;

public interface AuthenticateUserUseCase {

    AuthenticateUserOutput execute(AuthenticateUserCommand command);
}

