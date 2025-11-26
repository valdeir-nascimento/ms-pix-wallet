package br.com.pix.wallet.application.wallet.deposit;

import java.math.BigDecimal;
import java.util.UUID;

public record DepositOutput(UUID walletId, BigDecimal newBalance) {
    public static DepositOutput from(UUID walletId, BigDecimal newBalance) {
        return new DepositOutput(walletId, newBalance);
    }
}
