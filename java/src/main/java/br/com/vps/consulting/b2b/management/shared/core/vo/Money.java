package br.com.vps.consulting.b2b.management.shared.core.vo;

import java.math.BigDecimal;
import java.util.Objects;

public record Money(BigDecimal value, String currency) implements ValueObject {

    public static final String DEFAULT_CURRENCY = "BRL";

    public Money {
        Objects.requireNonNull(value, "value é obrigatório");
        Objects.requireNonNull(currency, "currency é obrigatório");
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Money value cannot be negative: " + value);
        }
    }

    public static Money of(final BigDecimal amount) {
        return new Money(amount, DEFAULT_CURRENCY);
    }

    public static Money of(final String amount) {
        return new Money(new BigDecimal(amount), DEFAULT_CURRENCY);
    }

    public Money add(final Money other) {
        assertSameCurrency(other);
        return new Money(this.value.add(other.value), this.currency);
    }

    public Money multiply(final int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative: " + quantity);
        }
        return new Money(this.value.multiply(BigDecimal.valueOf(quantity)), this.currency);
    }

    public boolean isGreaterThan(final Money other) {
        assertSameCurrency(other);
        return this.value.compareTo(other.value) > 0;
    }

    public boolean isZero() {
        return this.value.compareTo(BigDecimal.ZERO) == 0;
    }

    private void assertSameCurrency(final Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("Currency mismatch: " + this.currency + " vs " + other.currency);
        }
    }

}
