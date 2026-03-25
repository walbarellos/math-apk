package com.basecalc.core.model

import java.math.BigInteger

/**
 * Passo da conversão da parte fracionária pelo método das multiplicações sucessivas.
 *
 * Algoritmo: a cada passo, [restoAnterior] / [denominador] é multiplicado por [base].
 * O dígito extraído é a parte inteira do produto; o novo resto é a parte fracionária.
 *
 * @param restoAnterior  Numerador do resto antes da multiplicação (inteiro ≥ 0).
 * @param denominador    Denominador comum — não muda ao longo da conversão.
 * @param base           Base de destino.
 * @param produto        [restoAnterior] × [base] (numerador antes de dividir).
 * @param digito         Parte inteira de [produto] / [denominador] = dígito extraído.
 * @param restoApos      [produto] mod [denominador] = numerador do novo resto.
 */
data class FracStep(
    val restoAnterior: BigInteger,
    val denominador: BigInteger,
    val base: Int,
    val produto: BigInteger,
    val digito: Int,
    val restoApos: BigInteger,
)
