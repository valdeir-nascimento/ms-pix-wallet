package br.com.pix.wallet.application.wallet.balance;

import java.math.BigDecimal;

public record GetBalanceOutput(BigDecimal currentBalance) {
    public static GetBalanceOutput from(BigDecimal currentBalance) {
        return new GetBalanceOutput(currentBalance);
    }
}
