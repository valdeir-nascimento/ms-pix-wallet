package br.com.pix.wallet.infrastructure.persistence.gateway;

import br.com.pix.wallet.domain.user.UserAccount;
import br.com.pix.wallet.domain.user.UserAccountGateway;
import br.com.pix.wallet.infrastructure.persistence.entity.UserAccountEntity;
import br.com.pix.wallet.infrastructure.persistence.repository.UserAccountJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
public class UserAccountGatewayImpl implements UserAccountGateway {

    private final UserAccountJpaRepository repository;

    public UserAccountGatewayImpl(final UserAccountJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public UserAccount save(final UserAccount account) {
        final var saved = repository.save(UserAccountEntity.from(account));
        return saved.toAggregate();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserAccount> findByUsername(final String username) {
        return repository.findByUsername(username).map(UserAccountEntity::toAggregate);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByUsername(final String username) {
        return repository.existsByUsername(username);
    }
}

