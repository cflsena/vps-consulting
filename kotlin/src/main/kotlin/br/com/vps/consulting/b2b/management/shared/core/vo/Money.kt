package br.com.vps.consulting.b2b.management.shared.core.vo

import java.math.BigDecimal
import java.math.RoundingMode

data class Money(val value: BigDecimal, val currency: String = "BRL") : ValueObject {

    init {
        require(value >= BigDecimal.ZERO) { "O valor não pode ser negativo: $value" }
    }
    operator fun plus(other: Money): Money {
        require(currency == other.currency) {
            "Não é possível operar com moedas diferentes: $currency vs ${other.currency}"
        }
        return Money(value + other.value, currency).defaultScale()
    }

    operator fun minus(other: Money): Money {
        require(currency == other.currency) {
            "Não é possível operar com moedas diferentes: $currency vs ${other.currency}"
        }
        return Money(value - other.value, currency).defaultScale()
    }

    operator fun times(factor: Int): Money =
        Money(value * BigDecimal.valueOf(factor.toLong()), currency).defaultScale()

    fun isGreaterThan(other: Money): Boolean {
        require(currency == other.currency) {
            "Não é possível comparar moedas diferentes: $currency vs ${other.currency}"
        }
        return value > other.value
    }

    fun isNegativeOrZero(): Boolean = value <= BigDecimal.ZERO

    companion object {
        fun of(amount: BigDecimal) = Money(amount)
        fun of(amount: Long) = Money(BigDecimal.valueOf(amount)).defaultScale()
        val ZERO = Money(BigDecimal.ZERO).defaultScale()
    }

}

fun Money.defaultScale(): Money {
    return Money(this.value.setScale(2, RoundingMode.HALF_EVEN), this.currency)
}
