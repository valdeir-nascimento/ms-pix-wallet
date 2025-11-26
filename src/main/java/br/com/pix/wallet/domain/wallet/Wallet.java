package br.com.pix.wallet.domain.wallet;

import br.com.pix.wallet.domain.common.Money;
import br.com.pix.wallet.domain.core.AggregateRoot;
import br.com.pix.wallet.domain.validation.ValidationHandler;

public class Wallet extends AggregateRoot<WalletID> {

    private String ownerId;
    private Money currentBalance;
    private WalletStatus status;

    private Wallet(
        final WalletID id,
        final String ownerId,
        final Money currentBalance,
        final WalletStatus status
    ) {
        super(id);
        this.ownerId = ownerId;
        this.currentBalance = currentBalance;
        this.status = status;
    }

    public static Wallet newWallet(final String ownerId) {
        return new Wallet(
            WalletID.unique(),
            ownerId,
            Money.zero(),
            WalletStatus.ACTIVE
        );
    }

    public static Wallet with(
        final WalletID id,
        final String ownerId,
        final Money currentBalance,
        final WalletStatus status
    ) {
        return new Wallet(id, ownerId, currentBalance, status);
    }

    @Override
    public void validate(final ValidationHandler handler) {
        new WalletValidator(this, handler).validate();
    }

    public void deposit(final Money amount) {
        this.currentBalance = this.currentBalance.add(amount);
    }

    public void withdraw(final Money amount) {
        if (this.currentBalance.compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient balance");
        }
        this.currentBalance = this.currentBalance.subtract(amount);
    }

    public WalletID getId() {
        return id;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public Money getCurrentBalance() {
        return currentBalance;
    }

    public WalletStatus getStatus() {
        return status;
    }
}
