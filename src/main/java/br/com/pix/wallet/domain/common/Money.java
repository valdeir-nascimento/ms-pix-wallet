package br.com.pix.wallet.domain.common;

import br.com.pix.wallet.domain.core.ValueObject;

import java.math.BigDecimal;
import java.util.Objects;

public final class Money extends ValueObject {

    private final BigDecimal amount;

    private Money(final BigDecimal amount) {
        this.amount = amount;
    }

    public static Money of(final BigDecimal value) {
        Objects.requireNonNull(value, "'value' must not be null");
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("'value' must be >= 0");
        }
        return new Money(value);
    }

    public static Money zero() {
        return new Money(BigDecimal.ZERO);
    }

    public Money add(final Money other) {
        return Money.of(this.amount.add(other.amount));
    }

    public Money subtract(final Money other) {
        final var result = this.amount.subtract(other.amount);
        if (result.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Insufficient amount");
        }
        return Money.of(result);
    }

    public int compareTo(final Money other) {
        return this.amount.compareTo(other.amount);
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public boolean isZero() {
        return this.amount.compareTo(BigDecimal.ZERO) == 0;
    }

    public boolean isNegative() {
        return this.amount.compareTo(BigDecimal.ZERO) < 0;
    }

    public boolean isZeroOrNegative() {
        return isZero() || isNegative();
    }

    public boolean isPositive() {
        return this.amount.compareTo(BigDecimal.ZERO) > 0;
    }

}
