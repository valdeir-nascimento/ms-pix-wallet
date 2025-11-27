package br.com.pix.wallet.infrastructure.persistence.repository;

import br.com.pix.wallet.infrastructure.persistence.entity.LedgerEntryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LedgerEntryJpaRepository extends JpaRepository<LedgerEntryEntity, UUID> {

    @Query("""
            SELECT e 
            FROM LedgerEntryEntity e 
            WHERE e.walletId = :walletId 
            ORDER BY e.occurredAt ASC
        """)
    List<LedgerEntryEntity> findAllByWallet(UUID walletId);

    @Query("""
            SELECT e
            FROM LedgerEntryEntity e
            WHERE e.walletId = :walletId 
                AND e.occurredAt <= :at
            ORDER BY e.occurredAt ASC
        """)
    List<LedgerEntryEntity> findAllBefore(UUID walletId, Instant at);

    @Query("""
            SELECT e 
            FROM LedgerEntryEntity e 
            WHERE e.walletId = :walletId 
              AND e.occurredAt <= :before
            ORDER BY e.occurredAt DESC
        """)
    Optional<LedgerEntryEntity> findLastBefore(UUID walletId, Instant before);
}