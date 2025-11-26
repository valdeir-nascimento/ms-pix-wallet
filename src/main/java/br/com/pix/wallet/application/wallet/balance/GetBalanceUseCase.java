package br.com.pix.wallet.application.wallet.balance;

import java.time.Instant;
import java.util.UUID;

public interface GetBalanceUseCase {
    GetBalanceOutput execute(final UUID walletId, final Instant at);
}
