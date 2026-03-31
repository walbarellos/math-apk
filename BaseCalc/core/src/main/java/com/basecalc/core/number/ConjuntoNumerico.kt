package com.basecalc.core.number

import java.math.BigInteger

enum class ConjuntoNumerico(val simbolo: String, val nome: String) {
    NATURAL("N", "Natural"),
    INTEIRO("Z", "Inteiro"),
    RACIONAL("Q", "Racional"),
    IRRACIONAL("I", "Irracional"),
    REAL("R", "Real");

    fun Contem(outro: ConjuntoNumerico): Boolean {
        return when {
            this == REAL -> true
            this == RACIONAL -> outro in listOf(NATURAL, INTEIRO, RACIONAL)
            this == INTEIRO -> outro in listOf(NATURAL, INTEIRO)
            this == NATURAL -> outro == NATURAL
            else -> false
        }
    }
}

object ConjuntoClassifier {

    fun classificar(numero: Rational): ConjuntoNumerico {
        return when {
            numero.isNatural() -> ConjuntoNumerico.NATURAL
            numero.isInteiro() -> ConjuntoNumerico.INTEIRO
            else -> ConjuntoNumerico.RACIONAL
        }
    }

    private fun Rational.isNatural(): Boolean {
        if (!isInteger()) return false
        return numerador >= BigInteger.ZERO
    }

    private fun Rational.isInteiro(): Boolean {
        return isInteger()
    }
}
