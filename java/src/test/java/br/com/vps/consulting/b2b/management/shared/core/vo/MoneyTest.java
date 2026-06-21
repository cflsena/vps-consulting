package br.com.vps.consulting.b2b.management.shared.core.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MoneyTest {

    @Test
    @DisplayName("Should create Money with explicit value and currency")
    void shouldCreateWithAmountAndCurrency() {
        final var money = new Money(new BigDecimal("100.00"), "USD");
        assertThat(money.value()).isEqualByComparingTo("100.00");
        assertThat(money.currency()).isEqualTo("USD");
    }

    @Test
    @DisplayName("Should create Money with default BRL currency via of(BigDecimal)")
    void shouldCreateWithDefaultCurrencyFromBigDecimal() {
        final var money = Money.of(new BigDecimal("50.00"));
        assertThat(money.currency()).isEqualTo(Money.DEFAULT_CURRENCY);
        assertThat(money.value()).isEqualByComparingTo("50.00");
    }

    @Test
    @DisplayName("Should create Money with default BRL currency via of(String)")
    void shouldCreateWithDefaultCurrencyFromString() {
        final var money = Money.of("75.50");
        assertThat(money.currency()).isEqualTo(Money.DEFAULT_CURRENCY);
        assertThat(money.value()).isEqualByComparingTo("75.50");
    }

    @Test
    @DisplayName("Should allow zero value")
    void shouldAllowZeroAmount() {
        final var money = new Money(BigDecimal.ZERO, "BRL");
        assertThat(money.isZero()).isTrue();
    }

    @Test
    @DisplayName("Should reject null value")
    void shouldRejectNullAmount() {
        assertThrows(NullPointerException.class, () -> new Money(null, "BRL"));
    }

    @Test
    @DisplayName("Should reject null currency")
    void shouldRejectNullCurrency() {
        assertThrows(NullPointerException.class, () -> new Money(new BigDecimal("10.00"), null));
    }

    @Test
    @DisplayName("Should reject negative value")
    void shouldRejectNegativeAmount() {
        assertThatThrownBy(() -> new Money(new BigDecimal("-0.01"), "BRL"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot be negative");
    }

    @Test
    @DisplayName("Should add two Money values with same currency")
    void shouldAddTwoMoneyWithSameCurrency() {
        final var a = new Money(new BigDecimal("30.00"), "BRL");
        final var b = new Money(new BigDecimal("20.00"), "BRL");
        final var result = a.add(b);
        assertThat(result.value()).isEqualByComparingTo("50.00");
        assertThat(result.currency()).isEqualTo("BRL");
    }

    @Test
    @DisplayName("Should reject addition of Money with different currencies")
    void shouldRejectAdditionWithDifferentCurrencies() {
        final var brl = new Money(new BigDecimal("10.00"), "BRL");
        final var usd = new Money(new BigDecimal("10.00"), "USD");
        assertThatThrownBy(() -> brl.add(usd))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Currency mismatch");
    }

    @Test
    @DisplayName("Should multiply Money by positive quantity")
    void shouldMultiplyByPositiveQuantity() {
        final var price = new Money(new BigDecimal("15.00"), "BRL");
        final var result = price.multiply(3);
        assertThat(result.value()).isEqualByComparingTo("45.00");
        assertThat(result.currency()).isEqualTo("BRL");
    }

    @Test
    @DisplayName("Should return zero when multiplying by zero")
    void shouldReturnZeroWhenMultiplyingByZero() {
        final var price = new Money(new BigDecimal("20.00"), "BRL");
        final var result = price.multiply(0);
        assertThat(result.isZero()).isTrue();
    }

    @Test
    @DisplayName("Should reject negative quantity in multiply")
    void shouldRejectNegativeQuantityInMultiply() {
        final var price = new Money(new BigDecimal("10.00"), "BRL");
        assertThatThrownBy(() -> price.multiply(-1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Quantity cannot be negative");
    }

    @Test
    @DisplayName("Should return true when value is greater than other")
    void shouldReturnTrueWhenIsGreaterThan() {
        final var larger = new Money(new BigDecimal("100.00"), "BRL");
        final var smaller = new Money(new BigDecimal("50.00"), "BRL");
        assertThat(larger.isGreaterThan(smaller)).isTrue();
    }

    @Test
    @DisplayName("Should return false when value is not greater than other")
    void shouldReturnFalseWhenNotGreaterThan() {
        final var a = new Money(new BigDecimal("50.00"), "BRL");
        final var b = new Money(new BigDecimal("50.00"), "BRL");
        assertThat(a.isGreaterThan(b)).isFalse();
    }

    @Test
    @DisplayName("Should reject isGreaterThan comparison with different currency")
    void shouldRejectIsGreaterThanWithDifferentCurrency() {
        var brl = new Money(new BigDecimal("100.00"), "BRL");
        var usd = new Money(new BigDecimal("50.00"), "USD");
        assertThatThrownBy(() -> brl.isGreaterThan(usd))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Currency mismatch");
    }

    @Test
    @DisplayName("Should return true for isZero when value is zero")
    void shouldReturnTrueForIsZero() {
        var money = new Money(BigDecimal.ZERO, "BRL");
        assertThat(money.isZero()).isTrue();
    }

    @Test
    @DisplayName("Should return false for isZero when value is positive")
    void shouldReturnFalseForIsZeroWhenPositive() {
        var money = new Money(new BigDecimal("0.01"), "BRL");
        assertThat(money.isZero()).isFalse();
    }
}
