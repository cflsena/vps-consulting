package br.com.vps.consulting.b2b.management.shared.core.vo

import java.math.BigDecimal

data class Money(val amount: BigDecimal, val currency: String = "BRL") : ValueObject {

    operator fun plus(other: Money): Money {
        require(currency == other.currency) {
            "Não é possível operar com moedas diferentes: $currency vs ${other.currency}"
        }
        return Money(amount + other.amount, currency)
    }

    operator fun minus(other: Money): Money {
        require(currency == other.currency) {
            "Não é possível operar com moedas diferentes: $currency vs ${other.currency}"
        }
        return Money(amount - other.amount, currency)
    }

    operator fun times(factor: Int): Money = Money(amount * BigDecimal.valueOf(factor.toLong()), currency)

    fun isGreaterThan(other: Money): Boolean {
        require(currency == other.currency) {
            "Não é possível comparar moedas diferentes: $currency vs ${other.currency}"
        }
        return amount > other.amount
    }

    fun isNegativeOrZero(): Boolean = amount <= BigDecimal.ZERO

    companion object {
        fun of(amount: BigDecimal) = Money(amount)
        fun of(amount: Long) = Money(BigDecimal.valueOf(amount))
    }

}
