package br.com.pix.wallet.application.security.register;

import br.com.pix.wallet.domain.exception.DomainException;
import br.com.pix.wallet.domain.user.UserAccount;
import br.com.pix.wallet.domain.user.UserAccountGateway;
import br.com.pix.wallet.domain.user.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegisterUserUseCaseImplTest {

    @InjectMocks
    private RegisterUserUseCaseImpl useCase;

    @Mock
    private UserAccountGateway userAccountGateway;

    @Mock
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setup() {
        reset(userAccountGateway, passwordEncoder);
    }

    @Test
    void givenValidCommand_whenExecute_thenRegistersUser() {
        // given
        final var command = RegisterUserCommand.with("operator", "secret", Set.of("OPERATOR"));

        when(userAccountGateway.existsByUsername("operator")).thenReturn(false);
        when(passwordEncoder.encode("secret")).thenReturn("encoded");
        when(userAccountGateway.save(any(UserAccount.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        final var output = useCase.execute(command);

        // then
        assertNotNull(output);
        assertEquals("operator", output.username());
        assertEquals(Set.of(UserRole.OPERATOR), output.roles());

        verify(userAccountGateway, times(1)).existsByUsername("operator");
        verify(passwordEncoder, times(1)).encode("secret");
        verify(userAccountGateway, times(1)).save(any(UserAccount.class));
    }

    @Test
    void givenExistingUsername_whenExecute_thenThrowsException() {
        // given
        final var command = RegisterUserCommand.with("operator", "secret", Set.of("OPERATOR"));
        when(userAccountGateway.existsByUsername("operator")).thenReturn(true);

        // when
        final var exception = assertThrows(DomainException.class, () -> useCase.execute(command));

        // then
        assertEquals("Username already in use", exception.getErrors().get(0).message());
        verify(userAccountGateway, times(1)).existsByUsername("operator");
        verify(userAccountGateway, times(0)).save(ArgumentMatchers.any());
    }
}

