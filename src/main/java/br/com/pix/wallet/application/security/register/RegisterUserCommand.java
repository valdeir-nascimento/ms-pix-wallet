package br.com.pix.wallet.application.security.register;

import java.util.Set;

public record RegisterUserCommand(
    String username,
    String password,
    Set<String> roles
) {

    public static RegisterUserCommand with(
        final String username,
        final String password,
        final Set<String> roles
    ) {
        return new RegisterUserCommand(username, password, roles);
    }
}

