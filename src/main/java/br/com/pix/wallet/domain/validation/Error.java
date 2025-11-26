package br.com.pix.wallet.domain.validation;

public record Error(String message) {
    public static Error of(String message) {
        return new Error(message);
    }
}
