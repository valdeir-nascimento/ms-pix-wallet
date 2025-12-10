package br.com.pix.wallet.domain.user;

public enum UserRole {
    ADMIN,
    OPERATOR;

    public static UserRole from(final String value) {
        return UserRole.valueOf(value.toUpperCase());
    }
}

