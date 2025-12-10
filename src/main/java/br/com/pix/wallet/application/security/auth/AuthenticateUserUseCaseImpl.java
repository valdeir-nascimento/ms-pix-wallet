package br.com.pix.wallet.application.security.auth;

import br.com.pix.wallet.domain.exception.DomainException;
import br.com.pix.wallet.domain.user.UserAccountGateway;
import br.com.pix.wallet.domain.validation.Error;
import br.com.pix.wallet.infrastructure.config.security.JwtTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthenticateUserUseCaseImpl implements AuthenticateUserUseCase {

    private final UserAccountGateway userAccountGateway;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthenticateUserUseCaseImpl(
        final UserAccountGateway userAccountGateway,
        final PasswordEncoder passwordEncoder,
        final JwtTokenProvider jwtTokenProvider
    ) {
        this.userAccountGateway = userAccountGateway;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public AuthenticateUserOutput execute(final AuthenticateUserCommand command) {
        final var account = userAccountGateway.findByUsername(command.username())
            .orElseThrow(() -> DomainException.with(Error.of("Invalid username or password")));

        if (!passwordEncoder.matches(command.password(), account.getPassword())) {
            throw DomainException.with(Error.of("Invalid username or password"));
        }

        final var tokenData = jwtTokenProvider.generateToken(account);
        return AuthenticateUserOutput.with(tokenData.token(), tokenData.expiresAt());
    }
}

