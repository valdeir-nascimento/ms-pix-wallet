package br.com.pix.wallet.domain.pix.pixkey;

import br.com.pix.wallet.domain.exception.DomainException;
import br.com.pix.wallet.domain.validation.Error;

import java.util.regex.Pattern;

public enum PixKeyType {

    EMAIL {
        @Override
        public Error validate(final String value) {
            return isValid(value, EMAIL_REGEX) ? null
                : Error.of("'keyValue' is not a valid email");
        }
    },
    PHONE {
        @Override
        public Error validate(final String value) {
            return isValid(value, PHONE_REGEX) ? null
                : Error.of("'keyValue' is not a valid phone format");
        }
    },
    CPF {
        @Override
        public Error validate(final String value) {
            return isValidCPF(value) ? null
                : Error.of("'keyValue' is not a valid CPF");
        }
    },
    EVP {
        @Override
        public Error validate(final String value) {
            return isValid(value, EVP_REGEX) ? null
                : Error.of("'keyValue' is not a valid EVP format");
        }
    };

    private static final Pattern EMAIL_REGEX = Pattern.compile("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$");

    private static final Pattern PHONE_REGEX =
        Pattern.compile("^(\\+55)?[1-9]{2}9\\d{8}$");

    private static final Pattern EVP_REGEX = Pattern.compile("^[0-9a-fA-F-]{32,}$");

    public abstract Error validate(String value);

    protected static boolean isValid(final String value, final Pattern pattern) {
        return value != null && pattern.matcher(value).matches();
    }

    protected static boolean isValidCPF(final String cpf) {
        if (cpf == null || !cpf.matches("\\d{11}") || cpf.chars().distinct().count() == 1)
            return false;

        try {
            int sum = 0, weight = 10;
            for (int i = 0; i < 9; i++) sum += (cpf.charAt(i) - '0') * weight--;
            int check1 = 11 - (sum % 11);
            check1 = check1 > 9 ? 0 : check1;

            sum = 0;
            weight = 11;
            for (int i = 0; i < 10; i++) sum += (cpf.charAt(i) - '0') * weight--;
            int check2 = 11 - (sum % 11);
            check2 = check2 > 9 ? 0 : check2;

            return check1 == (cpf.charAt(9) - '0') && check2 == (cpf.charAt(10) - '0');
        } catch (Exception e) {
            return false;
        }
    }

    public static PixKeyType from(final String raw) {
        try {
            return PixKeyType.valueOf(raw.toUpperCase());
        } catch (Exception ex) {
            throw DomainException.with(Error.of("Invalid keyType: '%s'".formatted(raw)));
        }
    }
}
