package br.com.vps.consulting.b2b.management.shared.core.vo

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class MoneyTest {

    @Test
    fun `should create Money with default BRL currency via of(BigDecimal)`() {
        val money = Money.of(BigDecimal("100.00"))

        assertThat(money.value).isEqualByComparingTo("100.00")
        assertThat(money.currency).isEqualTo("BRL")
    }

    @Test
    fun `should create Money with default BRL currency via of(Long)`() {
        val money = Money.of(100L)

        assertThat(money.value).isEqualByComparingTo("100")
        assertThat(money.currency).isEqualTo("BRL")
    }

    @Test
    fun `should add two Money values with same currency`() {
        val result = Money.of(BigDecimal("10.00")) + Money.of(BigDecimal("5.00"))

        assertThat(result.value).isEqualByComparingTo("15.00")
    }

    @Test
    fun `should reject addition of Money with different currencies`() {
        val brl = Money(BigDecimal("10.00"), "BRL")
        val usd = Money(BigDecimal("5.00"), "USD")

        assertThatThrownBy { brl + usd }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("moedas diferentes")
    }

    @Test
    fun `should subtract two Money values with same currency`() {
        val result = Money.of(BigDecimal("10.00")) - Money.of(BigDecimal("4.00"))

        assertThat(result.value).isEqualByComparingTo("6.00")
    }

    @Test
    fun `should reject subtraction with different currencies`() {
        val brl = Money(BigDecimal("10.00"), "BRL")
        val usd = Money(BigDecimal("5.00"), "USD")

        assertThatThrownBy { brl - usd }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("moedas diferentes")
    }

    @Test
    fun `should multiply Money by a positive factor`() {
        val result = Money.of(BigDecimal("10.00")) * 3

        assertThat(result.value).isEqualByComparingTo("30.00")
    }

    @Test
    fun `should return zero-value Money when multiplying by zero`() {
        val result = Money.of(BigDecimal("10.00")) * 0

        assertThat(result.value).isEqualByComparingTo("0")
    }

    @Test
    fun `should return true when isGreaterThan and value is strictly greater with same currency`() {
        val greater = Money.of(BigDecimal("10.00"))
        val smaller = Money.of(BigDecimal("5.00"))

        assertThat(greater.isGreaterThan(smaller)).isTrue()
    }

    @Test
    fun `should return false when isGreaterThan and values are equal`() {
        val first = Money.of(BigDecimal("10.00"))
        val second = Money.of(BigDecimal("10.00"))

        assertThat(first.isGreaterThan(second)).isFalse()
    }

    @Test
    fun `should reject isGreaterThan comparison with different currency`() {
        val brl = Money(BigDecimal("10.00"), "BRL")
        val usd = Money(BigDecimal("5.00"), "USD")

        assertThatThrownBy { brl.isGreaterThan(usd) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("moedas diferentes")
    }

    @Test
    fun `should return true for isNegativeOrZero when value is zero`() {
        assertThat(Money.of(BigDecimal.ZERO).isNegativeOrZero()).isTrue()
    }

    @Test
    fun `should return true for isNegativeOrZero when value is negative`() {
        assertThat(Money.of(BigDecimal("-1.00")).isNegativeOrZero()).isTrue()
    }

    @Test
    fun `should return false for isNegativeOrZero when value is positive`() {
        assertThat(Money.of(BigDecimal("0.01")).isNegativeOrZero()).isFalse()
    }

}
