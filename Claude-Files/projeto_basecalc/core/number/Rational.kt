package com.basecalc.core.number

import java.math.BigInteger

/**
 * Número racional exato (numerador/denominador), sempre reduzido e com denominador positivo.
 */
data class Rational private constructor(
    val numerador: BigInteger,
    val denominador: BigInteger
) : Comparable<Rational> {

    companion object {
        val ZERO: Rational = Rational(BigInteger.ZERO, BigInteger.ONE)
        val ONE: Rational = Rational(BigInteger.ONE, BigInteger.ONE)

        fun of(numerador: BigInteger, denominador: BigInteger): Rational {
            require(denominador != BigInteger.ZERO) { "Denominador não pode ser zero" }

            if (numerador == BigInteger.ZERO) {
                return ZERO
            }

            var n = numerador
            var d = denominador
            if (d.signum() < 0) {
                n = n.negate()
                d = d.negate()
            }
            val gcd = n.gcd(d)
            return Rational(n / gcd, d / gcd)
        }

        fun fromLong(value: Long): Rational {
            return of(BigInteger.valueOf(value), BigInteger.ONE)
        }
    }

    fun isInteger(): Boolean = denominador == BigInteger.ONE

    fun toBigIntegerExact(): BigInteger? {
        return if (isInteger()) numerador else null
    }

    fun negate(): Rational = of(numerador.negate(), denominador)

    operator fun plus(other: Rational): Rational {
        val n = numerador * other.denominador + other.numerador * denominador
        val d = denominador * other.denominador
        return of(n, d)
    }

    operator fun minus(other: Rational): Rational {
        val n = numerador * other.denominador - other.numerador * denominador
        val d = denominador * other.denominador
        return of(n, d)
    }

    operator fun times(other: Rational): Rational {
        return of(numerador * other.numerador, denominador * other.denominador)
    }

    operator fun div(other: Rational): Rational {
        require(other.numerador != BigInteger.ZERO) { "Divisão por zero" }
        return of(numerador * other.denominador, denominador * other.numerador)
    }

    override fun compareTo(other: Rational): Int {
        val left = numerador * other.denominador
        val right = other.numerador * denominador
        return left.compareTo(right)
    }
}
