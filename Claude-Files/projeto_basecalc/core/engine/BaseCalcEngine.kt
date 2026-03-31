package com.basecalc.core.engine

import com.basecalc.core.model.BackConvertResult
import com.basecalc.core.model.CalcResult

/** Contrato principal do motor da calculadora. */
interface BaseCalcEngine {

    fun evaluateExpression(expression: String, sourceBase: Int = 10): CalcResult

    fun convertFromBase(digits: String, base: Int): BackConvertResult
}
