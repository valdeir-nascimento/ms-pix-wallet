package br.com.pix.wallet.application.wallet.withdraw;

import java.math.BigDecimal;
import java.util.UUID;

public record WithdrawOutput(UUID walletId, BigDecimal newBalance) {
    public static WithdrawOutput from(UUID walletId, BigDecimal newBalance) {
        return new WithdrawOutput(walletId, newBalance);
    }
}
