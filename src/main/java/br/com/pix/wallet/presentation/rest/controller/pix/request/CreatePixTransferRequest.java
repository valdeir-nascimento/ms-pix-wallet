package br.com.pix.wallet.presentation.rest.controller.pix.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record CreatePixTransferRequest(

    @NotBlank(message = "'fromWalletId' must not be blank")
    String fromWalletId,

    @NotBlank(message = "'toWalletId' must not be blank")
    String toWalletId,

    @NotNull(message = "'amount' must not be null")
    @Positive(message = "'amount' must be greater than zero")
    BigDecimal amount,

    @NotBlank(message = "'idempotencyKey' must not be blank")
    String idempotencyKey,

    @NotBlank(message = "'endToEndId' must not be blank")
    String endToEndId
) {
}
