package br.com.pix.wallet.domain.ledger;

import br.com.pix.wallet.domain.core.AggregateRoot;
import br.com.pix.wallet.domain.validation.ValidationHandler;
import br.com.pix.wallet.domain.common.Money;
import br.com.pix.wallet.domain.wallet.WalletID;

import java.time.Instant;

public class LedgerEntry extends AggregateRoot<LedgerEntryID> {

    private final WalletID walletId;
    private final String endToEndId;
    private final LedgerOperationType operationType;
    private final Money amount;
    private final Money balanceAfterOperation;
    private final Instant occurredAt;

    private LedgerEntry(
        final LedgerEntryID id,
        final WalletID walletId,
        final String endToEndId,
        final LedgerOperationType operationType,
        final Money amount,
        final Money balanceAfterOperation,
        final Instant occurredAt
    ) {
        super(id);
        this.walletId = walletId;
        this.endToEndId = endToEndId;
        this.operationType = operationType;
        this.amount = amount;
        this.balanceAfterOperation = balanceAfterOperation;
        this.occurredAt = occurredAt;
    }

    public static LedgerEntry with(
        final LedgerEntryID id,
        final WalletID walletId,
        final String endToEndId,
        final LedgerOperationType operationType,
        final Money amount,
        final Money balanceAfterOperation,
        final Instant occurredAt
    ) {
        return new LedgerEntry(
            id,
            walletId,
            endToEndId,
            operationType,
            amount,
            balanceAfterOperation,
            occurredAt
        );
    }

    public static LedgerEntry deposit(
        final WalletID walletId,
        final Money amount,
        final Money newBalance
    ) {
        return new LedgerEntry(
            LedgerEntryID.unique(),
            walletId,
            null,
            LedgerOperationType.DEPOSIT,
            amount,
            newBalance,
            Instant.now()
        );
    }

    public static LedgerEntry withdraw(
        final WalletID walletId,
        final Money amount,
        final Money newBalance
    ) {
        return new LedgerEntry(
            LedgerEntryID.unique(),
            walletId,
            null,
            LedgerOperationType.WITHDRAW,
            amount,
            newBalance,
            Instant.now()
        );
    }

    public static LedgerEntry debitPix(
        final WalletID walletId,
        final String endToEndId,
        final Money amount,
        final Money newBalance
    ) {
        return new LedgerEntry(
            LedgerEntryID.unique(),
            walletId,
            endToEndId,
            LedgerOperationType.PIX_DEBIT,
            amount,
            newBalance,
            Instant.now()
        );
    }

    public static LedgerEntry creditPix(
        final WalletID walletId,
        final String endToEndId,
        final Money amount,
        final Money newBalance
    ) {
        return new LedgerEntry(
            LedgerEntryID.unique(),
            walletId,
            endToEndId,
            LedgerOperationType.PIX_CREDIT,
            amount,
            newBalance,
            Instant.now()
        );
    }

    public static LedgerEntry refund(
        final WalletID walletId,
        final String endToEndId,
        final Money amount,
        final Money newBalance
    ) {
        return new LedgerEntry(
            LedgerEntryID.unique(),
            walletId,
            endToEndId,
            LedgerOperationType.REFUND,
            amount,
            newBalance,
            Instant.now()
        );
    }

    @Override
    public void validate(final ValidationHandler handler) {
        new LedgerEntryValidator(this, handler).validate();
    }

    public WalletID getWalletId() {
        return walletId;
    }

    public String getEndToEndId() {
        return endToEndId;
    }

    public LedgerOperationType getOperationType() {
        return operationType;
    }

    public Money getAmount() {
        return amount;
    }

    public Money getBalanceAfterOperation() {
        return balanceAfterOperation;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }
}
