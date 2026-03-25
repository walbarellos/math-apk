package com.basecalc.core.engine

import com.basecalc.core.conversion.BaseConverter
import com.basecalc.core.conversion.BaseToDecimalConverter
import com.basecalc.core.model.CalcResult
import com.basecalc.core.parser.ExpressionParser

/** Implementação padrão do motor. */
class BaseCalcEngineImpl(
    private val basesAlvo: List<Int> = listOf(2, 3, 4, 5, 6, 7, 8, 9, 10, 16),
    private val maxFractionDigits: Int = 16,
    private val maxFractionDigitsDecimal: Int = 20
) : BaseCalcEngine {

    override fun evaluateExpression(expression: String): CalcResult {
        return try {
            val parser = ExpressionParser(expression)
            val valor = parser.parse()

            val bases = basesAlvo.map { base ->
                val limite = if (base == 10) maxFractionDigitsDecimal else maxFractionDigits
                BaseConverter(base).convert(valor, limite)
            }

            val decimalDisplay = bases.firstOrNull { it.base == 10 }?.displayWithComma().orEmpty()

            CalcResult(
                ok = true,
                expression = expression,
                value = valor,
                decimalDisplay = decimalDisplay,
                bases = bases
            )
        } catch (e: Exception) {
            CalcResult(
                ok = false,
                error = e.message ?: "Erro desconhecido",
                expression = expression
            )
        }
    }

    override fun convertFromBase(digits: String, base: Int) =
        BaseToDecimalConverter.convert(digits, base)
}
