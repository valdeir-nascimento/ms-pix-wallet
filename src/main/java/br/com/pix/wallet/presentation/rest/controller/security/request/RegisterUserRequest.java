package br.com.pix.wallet.presentation.rest.controller.security.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.Set;

public record RegisterUserRequest(

    @NotBlank(message = "'username' must not be blank")
    String username,

    @NotBlank(message = "'password' must not be blank")
    String password,

    @NotEmpty(message = "'roles' must not be empty")
    Set<String> roles
) {
}

