package br.com.pix.wallet.application.security.auth;

import br.com.pix.wallet.domain.exception.DomainException;
import br.com.pix.wallet.domain.user.UserAccount;
import br.com.pix.wallet.domain.user.UserAccountGateway;
import br.com.pix.wallet.domain.user.UserRole;
import br.com.pix.wallet.infrastructure.config.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticateUserUseCaseImplTest {

    @InjectMocks
    private AuthenticateUserUseCaseImpl useCase;

    @Mock
    private UserAccountGateway userAccountGateway;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void cleanUp() {
        reset(userAccountGateway, passwordEncoder, jwtTokenProvider);
    }

    @Test
    void givenValidCredentials_whenExecute_thenReturnToken() {
        // given
        final var command = AuthenticateUserCommand.with("operator", "secret");
        final var account = UserAccount.newAccount("operator", "hashed", Set.of(UserRole.OPERATOR));

        when(userAccountGateway.findByUsername("operator")).thenReturn(java.util.Optional.of(account));
        when(passwordEncoder.matches("secret", "hashed")).thenReturn(true);
        when(jwtTokenProvider.generateToken(account)).thenReturn(new JwtTokenProvider.TokenData("token", java.time.Instant.now()));

        // when
        final var output = useCase.execute(command);

        // then
        assertNotNull(output);
        assertNotNull(output.accessToken());
        verify(jwtTokenProvider, times(1)).generateToken(account);
    }

    @Test
    void givenInvalidPassword_whenExecute_thenThrowException() {
        // given
        final var command = AuthenticateUserCommand.with("operator", "secret");
        final var account = UserAccount.newAccount("operator", "hashed", Set.of(UserRole.OPERATOR));

        when(userAccountGateway.findByUsername("operator")).thenReturn(java.util.Optional.of(account));
        when(passwordEncoder.matches("secret", "hashed")).thenReturn(false);

        // when / then
        assertThrows(DomainException.class, () -> useCase.execute(command));
        verify(jwtTokenProvider, times(0)).generateToken(any());
    }
}

