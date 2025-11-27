package br.com.pix.wallet.presentation.rest.controller.wallet.request;

import jakarta.validation.constraints.NotBlank;

public record CreateWalletRequest(@NotBlank String ownerId) {
}