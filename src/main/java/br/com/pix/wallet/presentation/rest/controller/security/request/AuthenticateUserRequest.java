package br.com.pix.wallet.presentation.rest.controller.security.request;

import jakarta.validation.constraints.NotBlank;

public record AuthenticateUserRequest(

    @NotBlank(message = "'username' must not be blank")
    String username,

    @NotBlank(message = "'password' must not be blank")
    String password
) {
}

