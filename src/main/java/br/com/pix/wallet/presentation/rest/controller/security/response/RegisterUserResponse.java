package br.com.pix.wallet.presentation.rest.controller.security.response;

import br.com.pix.wallet.application.security.register.RegisterUserOutput;
import br.com.pix.wallet.domain.user.UserRole;

import java.util.Set;

public record RegisterUserResponse(
    String userId,
    String username,
    Set<UserRole> roles
) {

    public static RegisterUserResponse from(final RegisterUserOutput output) {
        return new RegisterUserResponse(output.userId(), output.username(), output.roles());
    }
}

