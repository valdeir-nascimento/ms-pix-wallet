package br.com.pix.wallet.application.wallet.create;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.pix.wallet.domain.exception.DomainException;
import br.com.pix.wallet.domain.wallet.Wallet;
import br.com.pix.wallet.domain.wallet.WalletGateway;

@ExtendWith(MockitoExtension.class)
class CreateWalletUseCaseImplTest {

    @InjectMocks
    private CreateWalletUseCaseImpl useCase;

    @Mock
    private WalletGateway walletGateway;

    @BeforeEach
    void cleanUp() {
        reset(walletGateway);
    }

    @Test
    void givenValidCommand_whenCallsExecute_thenShouldCreateWallet() {
        // given
        final var expectedOwnerId = UUID.randomUUID().toString();
        final var command = CreateWalletCommand.with(expectedOwnerId);

        when(walletGateway.save(any(Wallet.class))).thenAnswer(returnsFirstArg());

        // when
        final var actualOutput = useCase.execute(command);

        // then
        assertNotNull(actualOutput);
        assertNotNull(actualOutput.walletId());
        assertEquals(expectedOwnerId, actualOutput.ownerId());

        verify(walletGateway, times(1)).save(argThat(wallet -> expectedOwnerId.equals(wallet.getOwnerId()) &&
            wallet.getId() != null));
    }

    @Test
    void givenInvalidCommand_whenCallsExecute_thenShouldThrowException() {
        // given
        final var command = CreateWalletCommand.with(null);
        final var expectedErrorMessage = "'ownerId' cannot be null or blank";

        // when
        final var actualException = assertThrows(DomainException.class, () -> useCase.execute(command));

        // then
        assertEquals(expectedErrorMessage, actualException.getErrors().get(0).message());

        verify(walletGateway, times(0)).save(any());
    }

    @Test
    void givenExistingOwnerId_whenCallsExecute_thenShouldThrowDomainException() {
        // given
        final var expectedOwnerId = UUID.randomUUID().toString();
        final var expectedErrorMessage = "Owner ID already has a wallet";
        final var command = CreateWalletCommand.with(expectedOwnerId);

        when(walletGateway.existsByOwnerId(expectedOwnerId)).thenReturn(true);

        // when
        final var actualException = assertThrows(DomainException.class, () -> useCase.execute(command));

        // then
        assertEquals(expectedErrorMessage, actualException.getErrors().get(0).message());

        verify(walletGateway, times(1)).existsByOwnerId(expectedOwnerId);
        verify(walletGateway, never()).save(any());
    }
}
