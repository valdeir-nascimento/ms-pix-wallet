package br.com.pix.wallet.application.wallet.balance;

import br.com.pix.wallet.application.interfaces.LedgerGateway;
import br.com.pix.wallet.domain.wallet.WalletGateway;
import br.com.pix.wallet.domain.wallet.WalletID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Service
public class GetBalanceUseCaseImpl implements GetBalanceUseCase {

    private final WalletGateway walletGateway;
    private final LedgerGateway ledgerGateway;

    public GetBalanceUseCaseImpl(final WalletGateway walletGateway, final LedgerGateway ledgerGateway) {
        this.walletGateway = walletGateway;
        this.ledgerGateway = ledgerGateway;
    }

    @Override
    @Transactional(readOnly = true)
    public GetBalanceOutput execute(final UUID walletId, final Instant at) {
        final var wallet = walletGateway.findById(WalletID.from(walletId));

        if (at == null) {
            return GetBalanceOutput.from(wallet.getCurrentBalance().getAmount());
        }

        final var lastEntry = ledgerGateway.findLastEntryBefore(wallet.getId(), at);

        return lastEntry.map(ledgerEntry -> GetBalanceOutput.from(ledgerEntry.getBalanceAfterOperation().getAmount()))
            .orElseGet(() -> GetBalanceOutput.from(BigDecimal.ZERO));
    }
}
