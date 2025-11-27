package br.com.pix.wallet.infrastructure.persistence.gateway;

import br.com.pix.wallet.GatewayTest;
import br.com.pix.wallet.domain.common.Money;
import br.com.pix.wallet.domain.pix.pixkey.PixKey;
import br.com.pix.wallet.domain.pix.pixkey.PixKeyGateway;
import br.com.pix.wallet.domain.pix.pixkey.PixKeyType;
import br.com.pix.wallet.domain.wallet.Wallet;
import br.com.pix.wallet.domain.wallet.WalletGateway;
import br.com.pix.wallet.domain.wallet.WalletID;
import br.com.pix.wallet.infrastructure.persistence.repository.PixKeyJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@GatewayTest
class PixKeyGatewayIT {

    @Autowired
    private PixKeyGateway pixKeyGateway;


    @Autowired
    private PixKeyJpaRepository pixKeyJpaRepository;

    @Autowired
    private WalletGateway walletGateway;

    @Test
    void givenValidPixKey_whenCallsSave_thenShouldPersistPixKey() {
        // given
        final var expectedWalletId = createWallet();
        final var expectedType = PixKeyType.EMAIL;
        final var expectedKeyValue = "user.test@example.com";

        final var pixKey = PixKey.newPixKey(
            expectedWalletId,
            expectedType,
            expectedKeyValue
        );

        assertEquals(0, pixKeyJpaRepository.count());

        // when
        final var actualPixKey = pixKeyGateway.save(pixKey);

        // then
        assertEquals(1, pixKeyJpaRepository.count());

        assertNotNull(actualPixKey.getId());
        assertEquals(expectedWalletId.getValue(), actualPixKey.getWalletId().getValue());
        assertEquals(expectedType, actualPixKey.getKeyType());
        assertEquals(expectedKeyValue, actualPixKey.getKeyValue());

        final var actualEntity = pixKeyJpaRepository.findById(actualPixKey.getId().getValue()).get();

        assertEquals(actualPixKey.getId().getValue(), actualEntity.getId());
        assertEquals(expectedWalletId.getValue(), actualEntity.getWalletId());
        assertEquals(expectedType, actualEntity.getKeyType());
        assertEquals(expectedKeyValue, actualEntity.getKeyValue());
        assertNotNull(actualEntity.getCreatedAt());
        assertNotNull(actualEntity.getUpdatedAt());
    }

    @Test
    void givenExistingPixKey_whenCallsSaveAgainWithSameId_thenShouldUpdatePixKey() {
        // given
        final var expectedWalletId = createWallet();

        final var originalKey = PixKey.newPixKey(
            expectedWalletId,
            PixKeyType.EMAIL,
            "original@example.com"
        );

        final var savedOriginal = pixKeyGateway.save(originalKey);
        final var expectedId = savedOriginal.getId();

        final var updatedKeyValue = "updated@example.com";
        final var updatedType = PixKeyType.PHONE;

        final var updatedPixKey = PixKey.with(
            expectedId,
            expectedWalletId,
            updatedType,
            updatedKeyValue
        );

        // when
        pixKeyGateway.save(updatedPixKey);

        // then
        final var actualEntity = pixKeyJpaRepository.findById(expectedId.getValue()).get();

        assertEquals(expectedId.getValue(), actualEntity.getId());
        assertEquals(expectedWalletId.getValue(), actualEntity.getWalletId());
        assertEquals(updatedType, actualEntity.getKeyType());
        assertEquals(updatedKeyValue, actualEntity.getKeyValue());
    }

    @Test
    void givenExistingPixKey_whenCallsExistsByKeyValue_thenShouldReturnTrue() {
        // given
        final var expectedWalletId = createWallet();
        final var expectedKeyValue = "exists@example.com";

        final var pixKey = PixKey.newPixKey(
            expectedWalletId,
            PixKeyType.EMAIL,
            expectedKeyValue
        );

        pixKeyGateway.save(pixKey);

        assertEquals(1, pixKeyJpaRepository.count());

        // when
        final var exists = pixKeyGateway.existsByKeyValue(expectedKeyValue);

        // then
        assertTrue(exists);
    }

    @Test
    void givenNonExistingKeyValue_whenCallsExistsByKeyValue_thenShouldReturnFalse() {
        // given
        assertEquals(0, pixKeyJpaRepository.count());

        final var nonExistingKeyValue = "non-existing@example.com";

        // when
        final var exists = pixKeyGateway.existsByKeyValue(nonExistingKeyValue);

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
