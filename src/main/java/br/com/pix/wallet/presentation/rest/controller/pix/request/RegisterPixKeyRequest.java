package br.com.pix.wallet.presentation.rest.controller.pix.request;

import jakarta.validation.constraints.NotBlank;

public record RegisterPixKeyRequest(
    @NotBlank String keyType, // EMAIL, PHONE, EVP
    @NotBlank String keyValue
) {
}