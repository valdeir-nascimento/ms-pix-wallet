package br.com.pix.wallet.infrastructure.persistence.gateway;

import br.com.pix.wallet.domain.wallet.WalletGateway;
import br.com.pix.wallet.domain.exception.NotFoundException;
import br.com.pix.wallet.domain.wallet.Wallet;
import br.com.pix.wallet.domain.wallet.WalletID;
import br.com.pix.wallet.infrastructure.persistence.entity.WalletEntity;
import br.com.pix.wallet.infrastructure.persistence.repository.WalletJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class WalletGatewayImpl implements WalletGateway {

    private final WalletJpaRepository walletJpaRepository;

    public WalletGatewayImpl(final WalletJpaRepository walletJpaRepository) {
        this.walletJpaRepository = walletJpaRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Wallet findById(final WalletID id) {
        return walletJpaRepository.findById(id.getValue())
            .map(WalletEntity::toAggregate)
            .orElseThrow(() -> NotFoundException.with(Wallet.class, id.getValue()));
    }

    @Override
    @Transactional
    public Wallet findByIdWithLock(final WalletID id) {
        return walletJpaRepository.findByIdForUpdate(id.getValue())
            .map(WalletEntity::toAggregate)
            .orElseThrow(() -> NotFoundException.with(Wallet.class, id.getValue()));
    }

    @Override
    @Transactional
    public Wallet save(final Wallet wallet) {
        final var entity = walletJpaRepository
            .findById(wallet.getId().getValue())
            .map(e -> e.updateFrom(wallet))
            .orElseGet(() -> WalletEntity.from(wallet));  // cria a entidade e deixa o Hibernate persistir
        return walletJpaRepository.save(entity).toAggregate();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByOwnerId(final String ownerId) {
        return walletJpaRepository.existsByOwnerId(ownerId);
    }
}
