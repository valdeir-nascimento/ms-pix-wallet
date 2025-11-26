package br.com.pix.wallet.domain.ledger;

import br.com.pix.wallet.domain.common.Money;
import br.com.pix.wallet.domain.validation.Error;
import br.com.pix.wallet.domain.validation.ValidationHandler;
import br.com.pix.wallet.domain.validation.Validator;

public class LedgerEntryValidator extends Validator {

    private final LedgerEntry entry;

    public LedgerEntryValidator(final LedgerEntry entry, final ValidationHandler handler) {
        super(handler);
        this.entry = entry;
    }

    @Override
    public void validate() {

        if (entry.getWalletId() == null) {
            validationHandler().append(Error.of("'walletId' must not be null"));
        }

        if (entry.getOperationType() == null) {
            validationHandler().append(Error.of("'operationType' must not be null"));
        }

        final Money amount = entry.getAmount();
        if (amount == null) {
            validationHandler().append(Error.of("'amount' must not be null"));
        } else if (amount.getAmount() == null || amount.getAmount().signum() <= 0) {
            validationHandler().append(Error.of("'amount' must be greater than zero"));
        }

        if (entry.getBalanceAfterOperation() == null) {
            validationHandler().append(Error.of("'balanceAfterOperation' must not be null"));
        }

        if (entry.getOccurredAt() == null) {
            validationHandler().append(Error.of("'occurredAt' must not be null"));
        }

        // Regra extra: para operações de PIX, endToEndId precisa existir
        if (isPixOperation(entry.getOperationType())
            && (entry.getEndToEndId() == null || entry.getEndToEndId().isBlank())) {
            validationHandler().append(Error.of("'endToEndId' must not be null or blank for PIX operations"));
        }
    }

    private boolean isPixOperation(final LedgerOperationType type) {
        return type == LedgerOperationType.PIX_DEBIT
            || type == LedgerOperationType.PIX_CREDIT
            || type == LedgerOperationType.REFUND;
    }
}
