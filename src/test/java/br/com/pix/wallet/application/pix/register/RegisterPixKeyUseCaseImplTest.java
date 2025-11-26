package br.com.pix.wallet.application.pix.register;

import br.com.pix.wallet.application.UseCaseTest;
import br.com.pix.wallet.domain.exception.DomainException;
import br.com.pix.wallet.domain.exception.NotFoundException;
import br.com.pix.wallet.domain.pix.pixkey.PixKey;
import br.com.pix.wallet.domain.pix.pixkey.PixKeyGateway;
import br.com.pix.wallet.domain.pix.pixkey.PixKeyType;
import br.com.pix.wallet.domain.wallet.Wallet;
import br.com.pix.wallet.domain.wallet.WalletGateway;
import br.com.pix.wallet.domain.wallet.WalletID;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RegisterPixKeyUseCaseImplTest extends UseCaseTest {

    @InjectMocks
    private RegisterPixKeyUseCaseImpl registerPixKeyUseCase;

    @Mock
    private PixKeyGateway pixKeyGateway;

    @Mock
    private WalletGateway walletGateway;

    @Override
    protected List<Object> getMocks() {
        return List.of(pixKeyGateway, walletGateway);
    }

    @Test
    void givenValidCommand_whenRegisterPixKey_thenReturnOutput() {
        // given
        final var expectedWalletId = WalletID.unique();
        final var expectedKeyType = PixKeyType.EMAIL;
        final var expectedKeyValue = "test@example.com";

        final var command = RegisterPixKeyCommand.with(
            expectedWalletId.getValue().toString(),
            expectedKeyType.name(),
            expectedKeyValue
        );

        final var wallet = mock(Wallet.class);
        when(walletGateway.findById(any())).thenReturn(wallet);
        when(pixKeyGateway.existsByKeyValue(expectedKeyValue)).thenReturn(false);
        when(pixKeyGateway.save(any())).thenAnswer(invocation -> {
            final var pixKey = (PixKey) invocation.getArgument(0);
            return pixKey;
        });

        // when
        final var output = registerPixKeyUseCase.execute(command);

        // then
        assertNotNull(output);
        assertNotNull(output.pixKeyId());
        verify(walletGateway).findById(any());
        verify(pixKeyGateway).existsByKeyValue(expectedKeyValue);
        verify(pixKeyGateway).save(any());
    }

    @Test
    void givenExistingPixKey_whenRegisterPixKey_thenThrowDomainException() {
        // given
        final var expectedWalletId = WalletID.unique();
        final var expectedKeyType = PixKeyType.EMAIL;
        final var expectedKeyValue = "existing@example.com";

        final var command = RegisterPixKeyCommand.with(
            expectedWalletId.getValue().toString(),
            expectedKeyType.name(),
            expectedKeyValue
        );

        final var wallet = mock(Wallet.class);
        when(walletGateway.findById(any())).thenReturn(wallet);
        when(pixKeyGateway.existsByKeyValue(expectedKeyValue)).thenReturn(true);

        // when
        final var exception = assertThrows(DomainException.class, () -> registerPixKeyUseCase.execute(command));

        // then
        assertNotNull(exception);
        assertEquals(1, exception.getErrors().size());
        assertEquals("Pix key already registered", exception.getErrors().get(0).message());
        verify(walletGateway).findById(any());
        verify(pixKeyGateway).existsByKeyValue(expectedKeyValue);
        verify(pixKeyGateway, never()).save(any());
    }

    @Test
    void givenInvalidWalletId_whenRegisterPixKey_thenThrowNotFoundException() {
        // given
        final var invalidWalletId = UUID.randomUUID().toString();
        final var expectedKeyType = PixKeyType.CPF;
        final var expectedKeyValue = "17833570028";

        final var command = RegisterPixKeyCommand.with(
            invalidWalletId,
            expectedKeyType.name(),
            expectedKeyValue
        );

        when(walletGateway.findById(any())).thenThrow(NotFoundException.class);

        // when
        final var exception = assertThrows(NotFoundException.class, () -> registerPixKeyUseCase.execute(command));

        // then
        assertNotNull(exception);
        verify(walletGateway).findById(any());
        verify(pixKeyGateway, never()).existsByKeyValue(any());
        verify(pixKeyGateway, never()).save(any());
    }

    @Test
    void givenInvalidPixKeyValue_whenRegisterPixKey_thenThrowDomainException() {
        // given
        final var expectedWalletId = WalletID.unique();
        final var expectedKeyType = PixKeyType.EMAIL;
        final var invalidKeyValue = "";

        final var command = RegisterPixKeyCommand.with(
            expectedWalletId.getValue().toString(),
            expectedKeyType.name(),
            invalidKeyValue
        );

        final var wallet = mock(Wallet.class);
        when(walletGateway.findById(any())).thenReturn(wallet);
        when(pixKeyGateway.existsByKeyValue(invalidKeyValue)).thenReturn(false);

        // when
        final var exception = assertThrows(DomainException.class, () -> registerPixKeyUseCase.execute(command));

        // then
        assertNotNull(exception);
        assertTrue(exception.getErrors().size() > 0);
        verify(walletGateway).findById(any());
        verify(pixKeyGateway).existsByKeyValue(invalidKeyValue);
        verify(pixKeyGateway, never()).save(any());
    }

    @Test
    void givenInvalidEmailFormat_whenRegisterPixKey_thenThrowDomainException() {
        // given
        final var expectedWalletId = WalletID.unique();
        final var expectedKeyType = PixKeyType.EMAIL;
        final var invalidEmail = "invalid-email-format";

        final var command = RegisterPixKeyCommand.with(
            expectedWalletId.getValue().toString(),
            expectedKeyType.name(),
            invalidEmail
        );

        final var wallet = mock(Wallet.class);
        when(walletGateway.findById(any())).thenReturn(wallet);
        when(pixKeyGateway.existsByKeyValue(invalidEmail)).thenReturn(false);

        // when
        final var exception = assertThrows(DomainException.class, () -> registerPixKeyUseCase.execute(command));

        // then
        assertNotNull(exception);
        assertTrue(exception.getErrors().size() > 0);
        verify(walletGateway).findById(any());
        verify(pixKeyGateway).existsByKeyValue(invalidEmail);
        verify(pixKeyGateway, never()).save(any());
    }

    @Test
    void givenInvalidCpfFormat_whenRegisterPixKey_thenThrowDomainException() {
        // given
        final var expectedWalletId = WalletID.unique();
        final var expectedKeyType = PixKeyType.CPF;
        final var invalidCpf = "123";

        final var command = RegisterPixKeyCommand.with(
            expectedWalletId.getValue().toString(),
            expectedKeyType.name(),
            invalidCpf
        );

        final var wallet = mock(Wallet.class);
        when(walletGateway.findById(any())).thenReturn(wallet);
        when(pixKeyGateway.existsByKeyValue(invalidCpf)).thenReturn(false);

        // when
        final var exception = assertThrows(DomainException.class, () -> registerPixKeyUseCase.execute(command));

        // then
        assertNotNull(exception);
        assertTrue(exception.getErrors().size() > 0);
        verify(walletGateway).findById(any());
        verify(pixKeyGateway).existsByKeyValue(invalidCpf);
        verify(pixKeyGateway, never()).save(any());
    }

    @Test
    void givenInvalidPhoneFormat_whenRegisterPixKey_thenThrowDomainException() {
        // given
        final var expectedWalletId = WalletID.unique();
        final var expectedKeyType = PixKeyType.PHONE;
        final var invalidPhone = "123";
        final var command = RegisterPixKeyCommand.with(expectedWalletId.getValue().toString(), expectedKeyType.name(), invalidPhone);

        final var wallet = mock(Wallet.class);
        when(walletGateway.findById(any())).thenReturn(wallet);
        when(pixKeyGateway.existsByKeyValue(invalidPhone)).thenReturn(false);

        // when
        final var exception = assertThrows(DomainException.class, () -> registerPixKeyUseCase.execute(command));

        // then
        assertNotNull(exception);
        assertTrue(exception.getErrors().size() > 0);
        verify(walletGateway).findById(any());
        verify(pixKeyGateway).existsByKeyValue(invalidPhone);
        verify(pixKeyGateway, never()).save(any());
    }

    @Test
    void givenValidCpfKey_whenRegisterPixKey_thenReturnOutput() {
        // given
        final var expectedWalletId = WalletID.unique();
        final var expectedKeyType = PixKeyType.CPF;
        final var expectedKeyValue = "17833570028";
        final var command = RegisterPixKeyCommand.with(expectedWalletId.getValue().toString(), expectedKeyType.name(), expectedKeyValue);

        final var wallet = mock(Wallet.class);
        when(walletGateway.findById(any())).thenReturn(wallet);
        when(pixKeyGateway.existsByKeyValue(expectedKeyValue)).thenReturn(false);
        when(pixKeyGateway.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        final var output = registerPixKeyUseCase.execute(command);

        // then
        assertNotNull(output);
        assertNotNull(output.pixKeyId());
        verify(walletGateway).findById(any());
        verify(pixKeyGateway).existsByKeyValue(expectedKeyValue);
        verify(pixKeyGateway).save(any());
    }

    @Test
    void givenValidPhoneKey_whenRegisterPixKey_thenReturnOutput() {
        // given
        final var expectedWalletId = WalletID.unique();
        final var expectedKeyType = PixKeyType.PHONE;
        final var expectedKeyValue = "+5511987654321";

        final var command = RegisterPixKeyCommand.with(
            expectedWalletId.getValue().toString(),
            expectedKeyType.name(),
            expectedKeyValue
        );

        final var wallet = mock(Wallet.class);
        when(walletGateway.findById(any())).thenReturn(wallet);
        when(pixKeyGateway.existsByKeyValue(expectedKeyValue)).thenReturn(false);
        when(pixKeyGateway.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        final var output = registerPixKeyUseCase.execute(command);

        // then
        assertNotNull(output);
        assertNotNull(output.pixKeyId());
        verify(walletGateway).findById(any());
        verify(pixKeyGateway).existsByKeyValue(expectedKeyValue);
        verify(pixKeyGateway).save(any());
    }

    @Test
    void givenEvpKey_whenRegisterPixKey_thenReturnOutput() {
        // given
        final var expectedWalletId = WalletID.unique();
        final var expectedKeyType = PixKeyType.EVP;
        final var expectedKeyValue = UUID.randomUUID().toString().replace("-", "");

        final var command = RegisterPixKeyCommand.with(
            expectedWalletId.getValue().toString(),
            expectedKeyType.name(),
            expectedKeyValue
        );

        final var wallet = mock(Wallet.class);
        when(walletGateway.findById(any())).thenReturn(wallet);
        when(pixKeyGateway.existsByKeyValue(expectedKeyValue)).thenReturn(false);
        when(pixKeyGateway.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        final var output = registerPixKeyUseCase.execute(command);

        // then
        assertNotNull(output);
        assertNotNull(output.pixKeyId());
        verify(walletGateway).findById(any());
        verify(pixKeyGateway).existsByKeyValue(expectedKeyValue);
        verify(pixKeyGateway).save(any());
    }
}
