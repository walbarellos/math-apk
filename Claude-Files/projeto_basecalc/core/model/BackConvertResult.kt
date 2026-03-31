package com.basecalc.core.model

import com.basecalc.core.number.Rational

/** Resultado da validação e conversão de uma base para decimal. */
data class BackConvertResult(
    val ok: Boolean,
    val value: Rational = Rational.ZERO,
    val error: String? = null
)
