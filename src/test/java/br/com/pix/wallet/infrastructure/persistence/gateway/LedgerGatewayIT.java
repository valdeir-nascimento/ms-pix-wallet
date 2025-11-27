package br.com.pix.wallet.infrastructure.persistence.gateway;

import br.com.pix.wallet.GatewayTest;
import br.com.pix.wallet.domain.common.Money;
import br.com.pix.wallet.domain.ledger.LedgerEntry;
import br.com.pix.wallet.domain.ledger.LedgerGateway;
import br.com.pix.wallet.domain.ledger.LedgerOperationType;
import br.com.pix.wallet.domain.wallet.Wallet;
import br.com.pix.wallet.domain.wallet.WalletGateway;
import br.com.pix.wallet.domain.wallet.WalletID;
import br.com.pix.wallet.infrastructure.persistence.entity.LedgerEntryEntity;
import br.com.pix.wallet.infrastructure.persistence.repository.LedgerEntryJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@GatewayTest
class LedgerGatewayIT {

    @Autowired
    private LedgerGateway ledgerGateway;

    @Autowired
    private LedgerEntryJpaRepository ledgerEntryJpaRepository;

    @Autowired
    private WalletGateway walletGateway;

    @Test
    void givenAValidLedgerEntry_whenCallsSave_thenShouldPersistEntry() {
        // given
        final var expectedWalletId = createWallet();
        final var expectedAmount = Money.of(BigDecimal.valueOf(100.00));
        final var expectedBalance = Money.of(BigDecimal.valueOf(100.00));
        final var expectedOperation = LedgerOperationType.DEPOSIT;

        final var entry = LedgerEntry.deposit(
            expectedWalletId,
            expectedAmount,
            expectedBalance
        );

        assertEquals(0, ledgerEntryJpaRepository.count());

        // when
        final var actualEntry = ledgerGateway.save(entry);

        // then
        assertEquals(1, ledgerEntryJpaRepository.count());

        assertNotNull(actualEntry.getId());
        assertNotNull(actualEntry.getWalletId());
        assertEquals(expectedOperation, actualEntry.getOperationType());
        assertEquals(expectedAmount.getAmount(), actualEntry.getAmount().getAmount());
        assertEquals(expectedBalance.getAmount(), actualEntry.getBalanceAfterOperation().getAmount());
        assertEquals(entry.getOccurredAt(), actualEntry.getOccurredAt());
        assertNull(actualEntry.getEndToEndId());

        final var actualEntity = ledgerEntryJpaRepository.findById(entry.getId().getValue()).get();

        assertEquals(entry.getId().getValue(), actualEntity.getId());
        assertEquals(expectedWalletId.getValue(), actualEntity.getWalletId());
        assertEquals(expectedAmount.getAmount(), actualEntity.getAmount());
    }

    @Test
    void givenPrePersistedEntries_whenCallsFindByWalletId_thenShouldReturnListOfEntries() {
        // given
        final var expectedWalletOne = createWallet();
        final var expectedWalletTwo = createWallet();

        final var expectedLedgerOne = LedgerEntry.deposit(
            expectedWalletOne,
            Money.of(BigDecimal.TEN),
            Money.of(BigDecimal.TEN)
        );

        final var expectedLedgerTwo = LedgerEntry.withdraw(
            expectedWalletOne,
            Money.of(BigDecimal.ONE),
            Money.of(BigDecimal.valueOf(9))
        );

        final var expectedLedgerThree = LedgerEntry.deposit(
            expectedWalletTwo,
            Money.of(BigDecimal.valueOf(50)),
            Money.of(BigDecimal.valueOf(50))
        );

        ledgerEntryJpaRepository.saveAll(List.of(
            LedgerEntryEntity.from(expectedLedgerOne),
            LedgerEntryEntity.from(expectedLedgerTwo),
            LedgerEntryEntity.from(expectedLedgerThree)
        ));

        assertEquals(3, ledgerEntryJpaRepository.count());

        // when
        final var actualEntries = ledgerGateway.findByWalletId(expectedWalletOne);

        // then
        assertEquals(2, actualEntries.size());

        assertTrue(
            actualEntries.stream()
                .anyMatch(it -> it.getId().getValue().equals(expectedLedgerOne.getId().getValue())),
            "Should contain entryOne in result list"
        );

        assertTrue(
            actualEntries.stream()
                .anyMatch(it -> it.getId().getValue().equals(expectedLedgerTwo.getId().getValue())),
            "Should contain entryTwo in result list"
        );
    }

    @Test
    void givenUnknownWalletId_whenCallsFindByWalletId_thenShouldReturnEmptyList() {
        // given
        assertEquals(0, ledgerEntryJpaRepository.count());

        // when
        final var actualEntries = ledgerGateway.findByWalletId(WalletID.unique());

        // then
        assertTrue(actualEntries.isEmpty());
    }

    @Test
    void givenPrePersistedEntriesWithDifferentTimes_whenCallsFindByWalletIdAndOccurredAtBefore_thenShouldReturnCorrectEntries() {
        final var expectedWalletId = createWallet();
        final var now = Instant.now();

        final var entryOld = LedgerEntry.deposit(
            expectedWalletId,
            Money.of(BigDecimal.TEN),
            Money.of(BigDecimal.TEN)
        );
        final var entityOld = LedgerEntryEntity.from(entryOld);
        entityOld.setOccurredAt(now.minus(2, ChronoUnit.HOURS));

        final var entryMid = LedgerEntry.deposit(
            expectedWalletId,
            Money.of(BigDecimal.TEN),
            Money.of(BigDecimal.valueOf(20))
        );
        final var entityMid = LedgerEntryEntity.from(entryMid);
        entityMid.setOccurredAt(now.minus(1, ChronoUnit.HOURS));

        final var entryNew = LedgerEntry.deposit(
            expectedWalletId,
            Money.of(BigDecimal.TEN),
            Money.of(BigDecimal.valueOf(30))
        );
        final var entityNew = LedgerEntryEntity.from(entryNew);
        entityNew.setOccurredAt(now);

        ledgerEntryJpaRepository.saveAll(List.of(entityOld, entityMid, entityNew));

        final var cutoffTime = now.minus(30, ChronoUnit.MINUTES);

        // when
        final var actualEntries = ledgerGateway.findByWalletIdAndOccurredAtBefore(expectedWalletId, cutoffTime);

        // then
        assertEquals(2, actualEntries.size());

        assertTrue(
            actualEntries.stream()
                .anyMatch(e -> e.getId().getValue().equals(entryOld.getId().getValue())),
            "Result list should contain entryOld"
        );

        assertTrue(
            actualEntries.stream()
                .anyMatch(e -> e.getId().getValue().equals(entryMid.getId().getValue())),
            "Result list should contain entryMid"
        );

        assertFalse(
            actualEntries.stream()
                .anyMatch(e -> e.getId().getValue().equals(entryNew.getId().getValue())),
            "Result list should not contain entryNew"
        );
    }


    @Test
    void givenEmptyLedger_whenCallsFindLastEntryBefore_thenShouldReturnEmpty() {
        // given
        final var expectedWalletId = WalletID.unique();

        // when
        final var actualEntry = ledgerGateway.findLastEntryBefore(expectedWalletId, Instant.now());

        // then
        assertTrue(actualEntry.isEmpty());
    }

    @Test
    void givenEntriesButNoneBeforeDate_whenCallsFindLastEntryBefore_thenShouldReturnEmpty() {
        // given
        final var expectedWalletId = createWallet();
        final var now = Instant.now();

        final var entry = LedgerEntry.deposit(
            expectedWalletId,
            Money.of(BigDecimal.TEN),
            Money.of(BigDecimal.TEN)
        );

        final var entity = LedgerEntryEntity.from(entry);

        entity.setOccurredAt(now);

        ledgerEntryJpaRepository.save(entity);

        final var searchBefore = now.minus(1, ChronoUnit.HOURS);

        // when
        final var actualEntry = ledgerGateway.findLastEntryBefore(expectedWalletId, searchBefore);

        // then
        assertTrue(actualEntry.isEmpty());
    }

    @Test
    void givenPixTransactions_whenCallsSave_thenShouldPersistEndToEndIdAndType() {
        // given
        final var expectedWalletId = createWallet();
        final var expectedEndToEndId = UUID.randomUUID().toString();
        final var expectedAmount = Money.of(BigDecimal.valueOf(50.00));
        final var expectedBalance = Money.of(BigDecimal.valueOf(150.00));

        final var entry = LedgerEntry.creditPix(
            expectedWalletId,
            expectedEndToEndId,
            expectedAmount,
            expectedBalance
        );

        // when
        final var actualEntry = ledgerGateway.save(entry);

        // then
        assertNotNull(actualEntry.getId());
        assertEquals(expectedEndToEndId, actualEntry.getEndToEndId());
        assertEquals(LedgerOperationType.PIX_CREDIT, actualEntry.getOperationType());

        final var entity = ledgerEntryJpaRepository.findById(actualEntry.getId().getValue()).get();
        assertEquals(expectedEndToEndId, entity.getEndToEndId());
    }

    private WalletID createWallet() {
        final var wallet = Wallet.newWallet(UUID.randomUUID().toString());
        final var saved = walletGateway.save(wallet);
        return saved.getId();
    }
}