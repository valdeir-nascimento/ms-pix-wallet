package br.com.pix.wallet.application.wallet.deposit;

import java.math.BigDecimal;

public record DepositCommand(String walletId, BigDecimal amount) {
    public static DepositCommand with(final String walletId, final BigDecimal amount) {
        return new DepositCommand(walletId, amount);
    }
}
