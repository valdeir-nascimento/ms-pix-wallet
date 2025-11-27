package br.com.pix.wallet.infrastructure.persistence.gateway;

import br.com.pix.wallet.GatewayTest;
import br.com.pix.wallet.domain.common.Money;
import br.com.pix.wallet.domain.exception.NotFoundException;
import br.com.pix.wallet.domain.wallet.Wallet;
import br.com.pix.wallet.domain.wallet.WalletGateway;
import br.com.pix.wallet.domain.wallet.WalletID;
import br.com.pix.wallet.domain.wallet.WalletStatus;
import br.com.pix.wallet.infrastructure.persistence.repository.WalletJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@GatewayTest
class WalletGatewayIT {

    @Autowired
    private WalletGateway walletGateway;

    @Autowired
    private WalletJpaRepository walletJpaRepository;

    @Test
    void givenValidWallet_whenCallsSave_thenShouldPersistNewWallet() {
        // given
        final var expectedOwnerId = UUID.randomUUID().toString();
        final var expectedBalance = Money.zero();
        final var expectedStatus = WalletStatus.ACTIVE;

        final var wallet = Wallet.newWallet(expectedOwnerId);

        assertEquals(0, walletJpaRepository.count());

        // when
        final var actualWallet = walletGateway.save(wallet);

        // then
        assertEquals(1, walletJpaRepository.count());

        assertNotNull(actualWallet.getId());
        assertEquals(expectedOwnerId, actualWallet.getOwnerId());
        assertEquals(expectedBalance.getAmount(), actualWallet.getCurrentBalance().getAmount());
        assertEquals(expectedStatus, actualWallet.getStatus());

        final var entity = walletJpaRepository.findById(actualWallet.getId().getValue()).orElseThrow();

        assertEquals(actualWallet.getId().getValue(), entity.getId());
        assertEquals(expectedOwnerId, entity.getOwnerId());
        assertEquals(expectedBalance.getAmount(), entity.getCurrentBalance());
        assertEquals(expectedStatus, entity.getStatus());
        assertNotNull(entity.getCreatedAt());
        assertNotNull(entity.getUpdatedAt());
    }

    @Test
    void givenExistingWallet_whenCallsSave_thenShouldUpdateWallet() {
        // given
        final var initialOwnerId = UUID.randomUUID().toString();
        final var wallet = Wallet.newWallet(initialOwnerId);

        final var persisted = walletGateway.save(wallet);

        final var expectedId = persisted.getId();
        final var expectedOwnerId = UUID.randomUUID().toString();
        final var expectedBalance = Money.of(BigDecimal.valueOf(150.0));
        final var expectedStatus = WalletStatus.ACTIVE;

        final var updatedWallet = Wallet.with(
            expectedId,
            expectedOwnerId,
            expectedBalance,
            expectedStatus
        );

        assertEquals(1, walletJpaRepository.count());

        // when
        final var actualWallet = walletGateway.save(updatedWallet);

        // then
        assertEquals(1, walletJpaRepository.count());

        assertEquals(expectedId.getValue(), actualWallet.getId().getValue());
        assertEquals(expectedOwnerId, actualWallet.getOwnerId());
        assertEquals(expectedBalance.getAmount(), actualWallet.getCurrentBalance().getAmount());
        assertEquals(expectedStatus, actualWallet.getStatus());

        final var entity = walletJpaRepository.findById(expectedId.getValue()).orElseThrow();

        assertEquals(expectedId.getValue(), entity.getId());
        assertEquals(expectedOwnerId, entity.getOwnerId());
        assertEquals(expectedBalance.getAmount(), entity.getCurrentBalance());
        assertEquals(expectedStatus, entity.getStatus());
    }

    @Test
    void givenExistingWallet_whenCallsFindById_thenShouldReturnWallet() {
        // given
        final var expectedOwnerId = UUID.randomUUID().toString();
        final var expectedWallet = Wallet.newWallet(expectedOwnerId);

        final var saved = walletGateway.save(expectedWallet);

        final var expectedId = saved.getId();
        final var expectedBalance = saved.getCurrentBalance();
        final var expectedStatus = saved.getStatus();

        assertEquals(1, walletJpaRepository.count());

        // when
        final var actualWallet = walletGateway.findById(expectedId);

        // then
        assertEquals(expectedId.getValue(), actualWallet.getId().getValue());
        assertEquals(expectedOwnerId, actualWallet.getOwnerId());
        assertEquals(expectedBalance.getAmount(), actualWallet.getCurrentBalance().getAmount());
        assertEquals(expectedStatus, actualWallet.getStatus());
    }

    @Test
    void givenNonExistingWalletId_whenCallsFindById_thenShouldThrowNotFoundException() {
        // given
        final var unknownId = WalletID.unique();

        // when / then
        assertThrows(NotFoundException.class, () -> walletGateway.findById(unknownId));
    }

    @Test
    void givenExistingWallet_whenCallsExistsByOwnerId_thenShouldReturnTrue() {
        // given
        final var expectedOwnerId = UUID.randomUUID().toString();
        final var expectedWallet = Wallet.newWallet(expectedOwnerId);

        walletGateway.save(expectedWallet);

        assertEquals(1, walletJpaRepository.count());

        // when
        final var exists = walletGateway.existsByOwnerId(expectedOwnerId);

        // then
        assertTrue(exists);
    }

    @Test
    void givenNoWalletForOwner_whenCallsExistsByOwnerId_thenShouldReturnFalse() {
        // given
        assertEquals(0, walletJpaRepository.count());
        final var unknownOwnerId = "unknown-" + UUID.randomUUID();

        // when
        final var exists = walletGateway.existsByOwnerId(unknownOwnerId);

        // then
        assertFalse(exists);
    }

    @Test
    void givenExistingWallet_whenCallsFindByIdWithLock_thenShouldReturnWallet() {
        // given
        final var expectedOwnerId = UUID.randomUUID().toString();
        final var wallet = Wallet.newWallet(expectedOwnerId);

        final var saved = walletGateway.save(wallet);
        final var expectedId = saved.getId();

        assertEquals(1, walletJpaRepository.count());

        // when
        final var actualWallet = walletGateway.findByIdWithLock(expectedId);

        // then
        assertEquals(expectedId.getValue(), actualWallet.getId().getValue());
        assertEquals(expectedOwnerId, actualWallet.getOwnerId());
    }

    @Test
    void givenNonExistingWallet_whenCallsFindByIdWithLock_thenShouldThrowNotFoundException() {
        // given
        final var nonExistingId = WalletID.unique();
        assertEquals(0, walletJpaRepository.count());

        // when
        final var actualException = assertThrows(NotFoundException.class, () -> walletGateway.findByIdWithLock(nonExistingId));

        // then
        assertTrue(actualException.getMessage().contains("not found"));
        assertTrue(actualException.getMessage().contains(nonExistingId.getValue().toString()));
    }

    @Test
    void givenExistingWallet_whenCallsFindByIdWithLockAndUpdateBalance_thenShouldPersistUpdatedBalance() {
        // given
        final var expectedOwnerId = UUID.randomUUID().toString();
        final var initialWallet = Wallet.newWallet(expectedOwnerId);

        final var saved = walletGateway.save(initialWallet);
        final var expectedWalletId = saved.getId();

        assertEquals(1, walletJpaRepository.count());

        final var lockedWallet = walletGateway.findByIdWithLock(expectedWalletId);

        lockedWallet.deposit(Money.of(BigDecimal.valueOf(100)));

        final var updatedWallet = walletGateway.save(lockedWallet);

        // then
        assertEquals(expectedWalletId.getValue(), updatedWallet.getId().getValue());
        assertEquals(expectedOwnerId, updatedWallet.getOwnerId());
        assertEquals(0, updatedWallet.getCurrentBalance().getAmount().compareTo(BigDecimal.valueOf(100)));


        final var entity = walletJpaRepository.findById(expectedWalletId.getValue()).orElseThrow();

        assertEquals(BigDecimal.valueOf(100), entity.getCurrentBalance());
    }

}
