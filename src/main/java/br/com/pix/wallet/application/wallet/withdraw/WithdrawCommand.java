package br.com.pix.wallet.application.wallet.withdraw;

import java.math.BigDecimal;
import java.util.UUID;

public record WithdrawCommand(UUID walletId, BigDecimal amount) {
    public static WithdrawCommand with(final UUID walletId, final BigDecimal amount) {
        return new WithdrawCommand(walletId, amount);
    }
}
