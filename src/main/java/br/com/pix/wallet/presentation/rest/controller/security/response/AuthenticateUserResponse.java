package br.com.pix.wallet.presentation.rest.controller.security.response;

import br.com.pix.wallet.application.security.auth.AuthenticateUserOutput;

import java.time.Instant;

public record AuthenticateUserResponse(
    String accessToken,
    Instant expiresAt
) {

    public static AuthenticateUserResponse from(final AuthenticateUserOutput output) {
        return new AuthenticateUserResponse(output.accessToken(), output.expiresAt());
    }
}

