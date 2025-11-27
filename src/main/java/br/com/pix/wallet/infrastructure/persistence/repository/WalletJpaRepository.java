package br.com.pix.wallet.infrastructure.persistence.repository;

import br.com.pix.wallet.infrastructure.persistence.entity.WalletEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface WalletJpaRepository extends JpaRepository<WalletEntity, UUID> {

    boolean existsByOwnerId(String ownerId);

    Optional<WalletEntity> findByOwnerId(String ownerId);

    @Query(
        value = "SELECT * FROM wallet WHERE id = :id FOR UPDATE",
        nativeQuery = true
    )
    Optional<WalletEntity> findByIdForUpdate(@Param("id") UUID id);
}









