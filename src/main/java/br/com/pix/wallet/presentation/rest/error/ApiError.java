package br.com.pix.wallet.presentation.rest.error;

import br.com.pix.wallet.domain.exception.DomainException;
import br.com.pix.wallet.domain.exception.NotFoundException;
import br.com.pix.wallet.domain.validation.Error;

import java.util.List;

public record ApiError(String message, List<Error> errors) {
    static ApiError from(final DomainException ex) {
        final String message = extractMessage(ex);
        return new ApiError(message, ex.getErrors());
    }

    static ApiError from(final NotFoundException ex) {
        return new ApiError(ex.getMessage(), List.of(new Error(ex.getMessage())));
    }

    private static String extractMessage(final DomainException ex) {
        if (ex.getMessage() != null && !ex.getMessage().isBlank()) {
            return ex.getMessage();
        }
        if (ex.getErrors() != null && !ex.getErrors().isEmpty()) {
            return ex.getErrors().get(0).message();
        }
        return "Unexpected error";
    }
}