package br.com.vps.consulting.b2b.management.shared.core.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MoneyTest {

    @Test
    @DisplayName("Given an explicit value and currency, when constructing Money, should create it with those fields")
    void shouldCreateWithAmountAndCurrency() {
        final var money = new Money(new BigDecimal("100.00"), "USD");
        assertThat(money.value()).isEqualByComparingTo("100.00");
        assertThat(money.currency()).isEqualTo("USD");
    }

    @Test
    @DisplayName("Given a BigDecimal, when of(BigDecimal) is called, should create Money with the default BRL currency")
    void shouldCreateWithDefaultCurrencyFromBigDecimal() {
        final var money = Money.of(new BigDecimal("50.00"));
        assertThat(money.currency()).isEqualTo(Money.DEFAULT_CURRENCY);
        assertThat(money.value()).isEqualByComparingTo("50.00");
    }

    @Test
    @DisplayName("Given a String value, when of(String) is called, should create Money with the default BRL currency")
    void shouldCreateWithDefaultCurrencyFromString() {
        final var money = Money.of("75.50");
        assertThat(money.currency()).isEqualTo(Money.DEFAULT_CURRENCY);
        assertThat(money.value()).isEqualByComparingTo("75.50");
    }

    @Test
    @DisplayName("Given a zero value, when constructing Money, should allow it")
    void shouldAllowZeroAmount() {
        final var money = new Money(BigDecimal.ZERO, "BRL");
        assertThat(money.isZero()).isTrue();
    }

    @Test
    @DisplayName("Given a null value, when constructing Money, should reject it with NullPointerException")
    void shouldRejectNullAmount() {
        assertThrows(NullPointerException.class, () -> new Money(null, "BRL"));
    }

    @Test
    @DisplayName("Given a null currency, when constructing Money, should reject it with NullPointerException")
    void shouldRejectNullCurrency() {
        assertThrows(NullPointerException.class, () -> new Money(new BigDecimal("10.00"), null));
    }

    @Test
    @DisplayName("Given a negative value, when constructing Money, should throw IllegalArgumentException")
    void shouldRejectNegativeAmount() {
        assertThatThrownBy(() -> new Money(new BigDecimal("-0.01"), "BRL"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot be negative");
    }

    @Test
    @DisplayName("Given two Money values with the same currency, when add is called, should sum their values")
    void shouldAddTwoMoneyWithSameCurrency() {
        final var a = new Money(new BigDecimal("30.00"), "BRL");
        final var b = new Money(new BigDecimal("20.00"), "BRL");
        final var result = a.add(b);
        assertThat(result.value()).isEqualByComparingTo("50.00");
        assertThat(result.currency()).isEqualTo("BRL");
    }

    @Test
    @DisplayName("Given two Money values with different currencies, when add is called, should throw IllegalArgumentException")
    void shouldRejectAdditionWithDifferentCurrencies() {
        final var brl = new Money(new BigDecimal("10.00"), "BRL");
        final var usd = new Money(new BigDecimal("10.00"), "USD");
        assertThatThrownBy(() -> brl.add(usd))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Currency mismatch");
    }

    @Test
    @DisplayName("Given a positive quantity, when multiply is called, should multiply the Money value by it")
    void shouldMultiplyByPositiveQuantity() {
        final var price = new Money(new BigDecimal("15.00"), "BRL");
        final var result = price.multiply(3);
        assertThat(result.value()).isEqualByComparingTo("45.00");
        assertThat(result.currency()).isEqualTo("BRL");
    }

    @Test
    @DisplayName("Given a zero quantity, when multiply is called, should return zero")
    void shouldReturnZeroWhenMultiplyingByZero() {
        final var price = new Money(new BigDecimal("20.00"), "BRL");
        final var result = price.multiply(0);
        assertThat(result.isZero()).isTrue();
    }

    @Test
    @DisplayName("Given a negative quantity, when multiply is called, should throw IllegalArgumentException")
    void shouldRejectNegativeQuantityInMultiply() {
        final var price = new Money(new BigDecimal("10.00"), "BRL");
        assertThatThrownBy(() -> price.multiply(-1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Quantity cannot be negative");
    }

    @Test
    @DisplayName("Given a larger value, when isGreaterThan is called, should return true")
    void shouldReturnTrueWhenIsGreaterThan() {
        final var larger = new Money(new BigDecimal("100.00"), "BRL");
        final var smaller = new Money(new BigDecimal("50.00"), "BRL");
        assertThat(larger.isGreaterThan(smaller)).isTrue();
    }

    @Test
    @DisplayName("Given an equal value, when isGreaterThan is called, should return false")
    void shouldReturnFalseWhenNotGreaterThan() {
        final var a = new Money(new BigDecimal("50.00"), "BRL");
        final var b = new Money(new BigDecimal("50.00"), "BRL");
        assertThat(a.isGreaterThan(b)).isFalse();
    }

    @Test
    @DisplayName("Given values with different currencies, when isGreaterThan is called, should throw IllegalArgumentException")
    void shouldRejectIsGreaterThanWithDifferentCurrency() {
        var brl = new Money(new BigDecimal("100.00"), "BRL");
        var usd = new Money(new BigDecimal("50.00"), "USD");
        assertThatThrownBy(() -> brl.isGreaterThan(usd))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Currency mismatch");
    }

    @Test
    @DisplayName("Given a zero value, when isZero is called, should return true")
    void shouldReturnTrueForIsZero() {
        var money = new Money(BigDecimal.ZERO, "BRL");
        assertThat(money.isZero()).isTrue();
    }

    @Test
    @DisplayName("Given a positive value, when isZero is called, should return false")
    void shouldReturnFalseForIsZeroWhenPositive() {
        var money = new Money(new BigDecimal("0.01"), "BRL");
        assertThat(money.isZero()).isFalse();
    }
}
