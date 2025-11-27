package br.com.pix.wallet.infrastructure.persistence.gateway;

import br.com.pix.wallet.GatewayTest;
import br.com.pix.wallet.domain.common.Money;
import br.com.pix.wallet.domain.pix.transfer.PixTransfer;
import br.com.pix.wallet.domain.pix.transfer.PixTransferGateway;
import br.com.pix.wallet.domain.pix.transfer.PixTransferStatus;
import br.com.pix.wallet.domain.wallet.Wallet;
import br.com.pix.wallet.domain.wallet.WalletGateway;
import br.com.pix.wallet.domain.wallet.WalletID;
import br.com.pix.wallet.infrastructure.persistence.repository.PixTransferJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@GatewayTest
class PixTransferGatewayIT {

    @Autowired
    private PixTransferGateway pixTransferGateway;

    @Autowired
    private PixTransferJpaRepository pixTransferJpaRepository;

    @Autowired
    private WalletGateway walletGateway;

    @Test
    void givenValidPixTransfer_whenCallsSave_thenShouldPersistTransfer() {
        // given
        final var expectedFromWallet = createWallet();
        final var expectedToWallet = createWallet();
        final var expectedAmount = Money.of(BigDecimal.valueOf(150.00));
        final var expectedStatus = PixTransferStatus.PENDING;
        final var expectedEndToEndId = UUID.randomUUID().toString();
        final var expectedIdempotencyKey = UUID.randomUUID().toString();

        final var transfer = PixTransfer.newTransfer(
            expectedFromWallet,
            expectedToWallet,
            expectedAmount,
            expectedIdempotencyKey,
            expectedEndToEndId
        );

        assertEquals(0, pixTransferJpaRepository.count());

        // when
        final var actualTransfer = pixTransferGateway.save(transfer);

        // then
        assertEquals(1, pixTransferJpaRepository.count());

        assertNotNull(actualTransfer.getId());
        assertEquals(expectedFromWallet.getValue(), actualTransfer.getFromWalletId().getValue());
        assertEquals(expectedToWallet.getValue(), actualTransfer.getToWalletId().getValue());
        assertEquals(expectedAmount.getAmount(), actualTransfer.getAmount().getAmount());
        assertEquals(expectedStatus, actualTransfer.getStatus());
        assertEquals(expectedEndToEndId, actualTransfer.getEndToEndId());
        assertEquals(expectedIdempotencyKey, actualTransfer.getIdempotencyKey());
        assertNotNull(actualTransfer.getCreatedAt());

        final var actualEntity = pixTransferJpaRepository.findById(actualTransfer.getId().getValue()).get();

        assertEquals(actualTransfer.getId().getValue(), actualEntity.getId());
        assertEquals(expectedFromWallet.getValue(), actualEntity.getFromWalletId());
        assertEquals(expectedToWallet.getValue(), actualEntity.getToWalletId());
        assertEquals(expectedAmount.getAmount(), actualEntity.getAmount());
        assertEquals(expectedStatus, actualEntity.getStatus());
        assertEquals(expectedEndToEndId, actualEntity.getEndToEndId());
        assertEquals(expectedIdempotencyKey, actualEntity.getIdempotencyKey());
        assertNotNull(actualEntity.getCreatedAt());
    }

    @Test
    void givenExistingPixTransfer_whenCallsSaveWithSameId_thenShouldUpdateStoredTransfer() {
        // given
        final var expectedWalletFrom = createWallet();
        final var expectedWalletTo = createWallet();

        final var expectedOriginalAmount = Money.of(BigDecimal.valueOf(100.00));
        final var expectedOriginalEndToEndId = UUID.randomUUID().toString();
        final var expectedOriginalIdempotencyKey = UUID.randomUUID().toString();

        final var originalTransfer = PixTransfer.newTransfer(
            expectedWalletFrom,
            expectedWalletTo,
            expectedOriginalAmount,
            expectedOriginalIdempotencyKey,
            expectedOriginalEndToEndId
        );

        final var savedOriginal = pixTransferGateway.save(originalTransfer);

        final var expectedId = savedOriginal.getId();
        final var expectedCreatedAt = savedOriginal.getCreatedAt();

        final var updatedAmount = Money.of(BigDecimal.valueOf(250.00));
        final var updatedStatus = PixTransferStatus.CONFIRMED;
        final var updatedIdempotencyKey = UUID.randomUUID().toString();

        final var updatedTransfer = PixTransfer.with(
            expectedId,
            expectedWalletFrom,
            expectedWalletTo,
            updatedAmount,
            updatedStatus,
            expectedOriginalEndToEndId,
            updatedIdempotencyKey,
            expectedCreatedAt
        );

        // when
        pixTransferGateway.save(updatedTransfer);

        // then
        final var actualEntity = pixTransferJpaRepository.findById(expectedId.getValue()).get();

        assertEquals(expectedId.getValue(), actualEntity.getId());
        assertEquals(expectedWalletFrom.getValue(), actualEntity.getFromWalletId());
        assertEquals(expectedWalletTo.getValue(), actualEntity.getToWalletId());
        assertEquals(updatedAmount.getAmount(), actualEntity.getAmount());
        assertEquals(updatedStatus, actualEntity.getStatus());
        assertEquals(expectedOriginalEndToEndId, actualEntity.getEndToEndId());
        assertEquals(updatedIdempotencyKey, actualEntity.getIdempotencyKey());
        assertEquals(expectedCreatedAt, actualEntity.getCreatedAt());
    }

    @Test
    void givenExistingPixTransfer_whenCallsFindByEndToEndId_thenShouldReturnTransfer() {
        // given
        final var expectedWalletFrom = createWallet();
        final var expectedWalletTo = createWallet();
        final var expectedAmount = Money.of(BigDecimal.valueOf(80.00));
        final var expectedEndToEndId = UUID.randomUUID().toString();
        final var expectedIdempotencyKey = UUID.randomUUID().toString();

        final var transfer = PixTransfer.newTransfer(
            expectedWalletFrom,
            expectedWalletTo,
            expectedAmount,
            expectedIdempotencyKey,
            expectedEndToEndId
        );

        final var saved = pixTransferGateway.save(transfer);

        // when
        final var actualOptional = pixTransferGateway.findByEndToEndId(expectedEndToEndId);

        // then
        assertTrue(actualOptional.isPresent());

        final var actual = actualOptional.get();

        assertEquals(saved.getId().getValue(), actual.getId().getValue());
        assertEquals(expectedWalletFrom.getValue(), actual.getFromWalletId().getValue());
        assertEquals(expectedWalletTo.getValue(), actual.getToWalletId().getValue());
        assertEquals(expectedAmount.getAmount(), actual.getAmount().getAmount());
        assertEquals(expectedEndToEndId, actual.getEndToEndId());
        assertEquals(expectedIdempotencyKey, actual.getIdempotencyKey());
    }

    @Test
    void givenNonExistingEndToEndId_whenCallsFindByEndToEndId_thenShouldReturnEmpty() {
        // given
        assertEquals(0, pixTransferJpaRepository.count());

        final var nonExistingEndToEndId = UUID.randomUUID().toString();

        // when
        final var actualResult = pixTransferGateway.findByEndToEndId(nonExistingEndToEndId);

        // then
        assertTrue(actualResult.isEmpty());
    }

    @Test
    void givenExistingPixTransfer_whenCallsExistsByIdempotencyKey_thenShouldReturnTrue() {
        // given
        final var expectedWalletFrom = createWallet();
        final var expectedWalletTo = createWallet();
        final var amount = Money.of(BigDecimal.valueOf(60.00));
        final var endToEndId = UUID.randomUUID().toString();
        final var expectedIdempotencyKey = UUID.randomUUID().toString();

        final var transfer = PixTransfer.newTransfer(
            expectedWalletFrom,
            expectedWalletTo,
            amount,
            expectedIdempotencyKey,
            endToEndId
        );

        pixTransferGateway.save(transfer);

        assertEquals(1, pixTransferJpaRepository.count());

        // when
        final var exists = pixTransferGateway.existsByIdempotencyKey(expectedIdempotencyKey);

        // then
        assertTrue(exists);
    }

    @Test
    void givenNonExistingIdempotencyKey_whenCallsExistsByIdempotencyKey_thenShouldReturnFalse() {
        // given
        assertEquals(0, pixTransferJpaRepository.count());

        final var nonExistingKey = UUID.randomUUID().toString();

        // when
        final var exists = pixTransferGateway.existsByIdempotencyKey(nonExistingKey);

        // then
        assertFalse(exists);
    }

    private WalletID createWallet() {
        final var wallet = Wallet.newWallet(UUID.randomUUID().toString());
        wallet.deposit(Money.of(BigDecimal.ZERO));
        final var saved = walletGateway.save(wallet);
        return saved.getId();
    }
}
