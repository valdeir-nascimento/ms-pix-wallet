package br.com.pix.wallet.application.security.auth;

import java.time.Instant;

public record AuthenticateUserOutput(
    String accessToken,
    Instant expiresAt
) {

    public static AuthenticateUserOutput with(
        final String token,
        final Instant expiresAt
    ) {
        return new AuthenticateUserOutput(token, expiresAt);
    }
}

