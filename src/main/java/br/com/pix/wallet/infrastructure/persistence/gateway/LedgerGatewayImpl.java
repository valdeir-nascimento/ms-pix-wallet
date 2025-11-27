package br.com.pix.wallet.infrastructure.persistence.gateway;

import br.com.pix.wallet.domain.ledger.LedgerGateway;
import br.com.pix.wallet.domain.ledger.LedgerEntry;
import br.com.pix.wallet.domain.wallet.WalletID;
import br.com.pix.wallet.infrastructure.persistence.entity.LedgerEntryEntity;
import br.com.pix.wallet.infrastructure.persistence.repository.LedgerEntryJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Component
public class LedgerGatewayImpl implements LedgerGateway {

    private final LedgerEntryJpaRepository ledgerEntryJpaRepository;

    public LedgerGatewayImpl(final LedgerEntryJpaRepository ledgerEntryJpaRepository) {
        this.ledgerEntryJpaRepository = ledgerEntryJpaRepository;
    }

    @Override
    @Transactional
    public LedgerEntry save(final LedgerEntry entry) {
        final var entity = LedgerEntryEntity.from(entry);
        final var saved = ledgerEntryJpaRepository.save(entity);
        return saved.toAggregate();
    }

    @Override
    @Transactional(readOnly = true)
    public List<LedgerEntry> findByWalletId(final WalletID walletId) {
        return ledgerEntryJpaRepository.findAllByWallet(walletId.getValue())
            .stream()
            .map(LedgerEntryEntity::toAggregate)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<LedgerEntry> findByWalletIdAndOccurredAtBefore(final WalletID walletId, final Instant at) {
        return ledgerEntryJpaRepository.findAllBefore(walletId.getValue(), at)
            .stream()
            .map(LedgerEntryEntity::toAggregate)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<LedgerEntry> findLastEntryBefore(final WalletID walletID, final Instant before) {
        return ledgerEntryJpaRepository.findLastBefore(walletID.getValue(), before)
            .map(LedgerEntryEntity::toAggregate);
    }
}
