package br.com.pix.wallet.infrastructure.config.security;

import br.com.pix.wallet.application.security.register.RegisterUserCommand;
import br.com.pix.wallet.application.security.register.RegisterUserUseCase;
import br.com.pix.wallet.domain.user.UserAccountGateway;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@Profile("!test & !test-integration & !test-e2e")
public class BootstrapAdminUser {

    private static final Logger LOG = LoggerFactory.getLogger(BootstrapAdminUser.class);

    private final PixWalletSecurityProperties securityProperties;
    private final UserAccountGateway userAccountGateway;
    private final RegisterUserUseCase registerUserUseCase;

    public BootstrapAdminUser(
        final PixWalletSecurityProperties securityProperties,
        final UserAccountGateway userAccountGateway,
        final RegisterUserUseCase registerUserUseCase
    ) {
        this.securityProperties = securityProperties;
        this.userAccountGateway = userAccountGateway;
        this.registerUserUseCase = registerUserUseCase;
    }

    @PostConstruct
    public void createAdminIfNecessary() {
        final var bootstrap = securityProperties.getBootstrapUser();
        if (!bootstrap.isEnabled() || bootstrap.getUsername() == null || bootstrap.getPassword() == null) {
            return;
        }

        if (userAccountGateway.existsByUsername(bootstrap.getUsername())) {
            return;
        }

        final var roles = bootstrap.getRoles().stream().map(Enum::name).collect(java.util.stream.Collectors.toSet());
        registerUserUseCase.execute(RegisterUserCommand.with(
            bootstrap.getUsername(),
            bootstrap.getPassword(),
            roles
        ));

        LOG.info("Bootstrap admin user '{}' created.", bootstrap.getUsername());
    }
}

