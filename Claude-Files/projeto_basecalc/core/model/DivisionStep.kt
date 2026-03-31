package com.basecalc.core.model

import java.math.BigInteger

/**
 * Passo de divisão sucessiva para conversão de base.
 */
data class DivisionStep(
    val dividendo: BigInteger,
    val base: Int,
    val quociente: BigInteger,
    val resto: Int,
    val digito: Char
)
