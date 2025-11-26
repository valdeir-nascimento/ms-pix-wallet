package br.com.pix.wallet.domain.pix.pixkey;

import br.com.pix.wallet.domain.validation.Error;
import br.com.pix.wallet.domain.validation.ValidationHandler;
import br.com.pix.wallet.domain.wallet.WalletID;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

class PixKeyTest {

    @Test
    void givenValidParams_whenCallsNewPixKey_thenShouldCreatePixKey() {
        // given
        final var expectedWalletId = WalletID.unique();
        final var expectedKeyType = PixKeyType.EMAIL;
        final var expectedKeyValue = "test@example.com";

        // when
        final var actualPixKey = PixKey.newPixKey(expectedWalletId, expectedKeyType, expectedKeyValue);

        // then
        assertNotNull(actualPixKey);
        assertNotNull(actualPixKey.getId());
        assertEquals(expectedWalletId, actualPixKey.getWalletId());
        assertEquals(expectedKeyType, actualPixKey.getKeyType());
        assertEquals(expectedKeyValue, actualPixKey.getKeyValue());
    }

    @Test
    void givenValidParams_whenCallsWith_thenShouldCreatePixKey() {
        // given
        final var expectedId = PixKeyID.unique();
        final var expectedWalletId = WalletID.unique();
        final var expectedKeyType = PixKeyType.CPF;
        final var expectedKeyValue = "12345678900";

        // when
        final var actualPixKey = PixKey.with(expectedId, expectedWalletId, expectedKeyType, expectedKeyValue);

        // then
        assertNotNull(actualPixKey);
        assertEquals(expectedId, actualPixKey.getId());
        assertEquals(expectedWalletId, actualPixKey.getWalletId());
        assertEquals(expectedKeyType, actualPixKey.getKeyType());
        assertEquals(expectedKeyValue, actualPixKey.getKeyValue());
    }

    @Test
    void givenValidPixKey_whenCallsValidate_thenShouldValidate() {
        // given
        final var pixKey = PixKey.newPixKey(WalletID.unique(), PixKeyType.EMAIL, "test@example.com");
        final var validationHandler = mock(ValidationHandler.class);

        // when
        pixKey.validate(validationHandler);

        // then
        verify(validationHandler, times(0)).append(any(Error.class));
    }

    @Test
    void givenInvalidEmail_whenCallsValidate_thenShouldReturnError() {
        // given
        final var pixKey = PixKey.newPixKey(WalletID.unique(), PixKeyType.EMAIL, "invalid-email");
        final var validationHandler = mock(ValidationHandler.class);
        final var expectedErrorMessage = "'keyValue' is not a valid email";

        // when
        pixKey.validate(validationHandler);

        // then
        verify(validationHandler, times(1))
            .append((Error) argThat(error -> expectedErrorMessage.equals(((Error) error).message())));
    }

    @Test
    void givenInvalidPhone_whenCallsValidate_thenShouldReturnError() {
        // given
        final var pixKey = PixKey.newPixKey(WalletID.unique(), PixKeyType.PHONE, "123");
        final var validationHandler = mock(ValidationHandler.class);
        final var expectedErrorMessage = "'keyValue' is not a valid phone format";

        // when
        pixKey.validate(validationHandler);

        // then
        verify(validationHandler, times(1))
            .append((Error) argThat(error -> expectedErrorMessage.equals(((Error) error).message())));
    }

    @Test
    void givenInvalidCpf_whenCallsValidate_thenShouldReturnError() {
        // given
        final var pixKey = PixKey.newPixKey(WalletID.unique(), PixKeyType.CPF, "12345678900"); // Invalid CPF logic
        // check
        final var validationHandler = mock(ValidationHandler.class);
        final var expectedErrorMessage = "'keyValue' is not a valid CPF";

        // when
        pixKey.validate(validationHandler);

        // then
        verify(validationHandler, times(1))
            .append((Error) argThat(error -> expectedErrorMessage.equals(((Error) error).message())));
    }

    @Test
    void givenInvalidEvp_whenCallsValidate_thenShouldReturnError() {
        // given
        final var pixKey = PixKey.newPixKey(WalletID.unique(), PixKeyType.EVP, "invalid-evp");
        final var validationHandler = mock(ValidationHandler.class);
        final var expectedErrorMessage = "'keyValue' is not a valid EVP format";

        // when
        pixKey.validate(validationHandler);

        // then
        verify(validationHandler, times(1))
            .append((Error) argThat(error -> expectedErrorMessage.equals(((Error) error).message())));
    }

    @Test
    void givenNullWalletId_whenCallsValidate_thenShouldReturnError() {
        // given
        final var pixKey = PixKey.newPixKey(null, PixKeyType.EMAIL, "test@example.com");
        final var validationHandler = mock(ValidationHandler.class);
        final var expectedErrorMessage = "'walletId' must not be null";

        // when
        pixKey.validate(validationHandler);

        // then
        verify(validationHandler, times(1))
            .append((Error) argThat(error -> expectedErrorMessage.equals(((Error) error).message())));
    }

    @Test
    void givenNullKeyType_whenCallsValidate_thenShouldReturnError() {
        // given
        final var pixKey = PixKey.newPixKey(WalletID.unique(), null, "test@example.com");
        final var validationHandler = mock(ValidationHandler.class);
        final var expectedErrorMessage = "'keyType' must not be null";

        // when
        pixKey.validate(validationHandler);

        // then
        verify(validationHandler, times(1))
            .append((Error) argThat(error -> expectedErrorMessage.equals(((Error) error).message())));
    }

    @Test
    void givenNullKeyValue_whenCallsValidate_thenShouldReturnError() {
        // given
        final var pixKey = PixKey.newPixKey(WalletID.unique(), PixKeyType.EMAIL, null);
        final var validationHandler = mock(ValidationHandler.class);
        final var expectedErrorMessage = "'keyValue' must not be null or blank";

        // when
        pixKey.validate(validationHandler);

        // then
        verify(validationHandler, times(1))
            .append((Error) argThat(error -> expectedErrorMessage.equals(((Error) error).message())));
    }
}
