package br.com.pix.wallet.domain.ledger;

import br.com.pix.wallet.domain.wallet.WalletID;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface LedgerGateway {
    LedgerEntry save(LedgerEntry entry);

    List<LedgerEntry> findByWalletId(WalletID walletId);

    List<LedgerEntry> findByWalletIdAndOccurredAtBefore(WalletID walletId, Instant at);

    Optional<LedgerEntry> findLastEntryBefore(WalletID walletID, Instant before);
}