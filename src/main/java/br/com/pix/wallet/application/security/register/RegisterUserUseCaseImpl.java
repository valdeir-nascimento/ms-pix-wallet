package br.com.pix.wallet.application.security.register;

import br.com.pix.wallet.domain.exception.DomainException;
import br.com.pix.wallet.domain.user.UserAccount;
import br.com.pix.wallet.domain.user.UserAccountGateway;
import br.com.pix.wallet.domain.user.UserRole;
import br.com.pix.wallet.domain.validation.Error;
import br.com.pix.wallet.domain.validation.handler.Notification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RegisterUserUseCaseImpl implements RegisterUserUseCase {

    private final UserAccountGateway userAccountGateway;
    private final PasswordEncoder passwordEncoder;

    public RegisterUserUseCaseImpl(
        final UserAccountGateway userAccountGateway,
        final PasswordEncoder passwordEncoder
    ) {
        this.userAccountGateway = userAccountGateway;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public RegisterUserOutput execute(final RegisterUserCommand command) {
        final var notification = Notification.create();

        if (command.roles() == null || command.roles().isEmpty()) {
            notification.append(Error.of("'roles' must contain at least one value"));
        }

        if (userAccountGateway.existsByUsername(command.username())) {
            notification.append(Error.of("Username already in use"));
        }

        if (notification.hasError()) {
            throw DomainException.with(notification.getErrors());
        }

        final var encodedPassword = passwordEncoder.encode(command.password());
        final Set<UserRole> roles = command.roles().stream()
            .map(UserRole::from)
            .collect(Collectors.toSet());

        final var account = UserAccount.newAccount(
            command.username(),
            encodedPassword,
            roles
        );

        account.validate(notification);

        if (notification.hasError()) {
            throw DomainException.with(notification.getErrors());
        }

        final var saved = userAccountGateway.save(account);
        return RegisterUserOutput.from(saved);
    }
}

