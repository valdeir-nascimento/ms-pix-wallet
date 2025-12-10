package br.com.pix.wallet.application.security.auth;

public record AuthenticateUserCommand(
    String username,
    String password
) {

    public static AuthenticateUserCommand with(
        final String username,
        final String password
    ) {
        return new AuthenticateUserCommand(username, password);
    }
}

