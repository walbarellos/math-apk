package com.basecalc.core.model

import com.basecalc.core.number.Rational

/** Resultado completo da avaliação de uma expressão. */
data class CalcResult(
    val ok: Boolean,
    val error: String? = null,
    val expression: String = "",
    val value: Rational = Rational.ZERO,
    val decimalDisplay: String = "",
    val bases: List<BaseEntry> = emptyList(),
    val operationVisual: OperationVisual? = null
)
