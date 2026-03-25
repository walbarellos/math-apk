package com.basecalc.core.conversion

import com.basecalc.core.model.BackConvertResult
import com.basecalc.core.number.Rational
import com.basecalc.core.util.DigitSymbols
import java.math.BigInteger

/** Converte uma string em base N para decimal exato (Rational). */
object BaseToDecimalConverter {

    fun convert(digits: String, base: Int): BackConvertResult {
        if (base < 2 || base > 16) {
            return BackConvertResult(ok = false, error = "Base inválida")
        }

        val raw = digits.trim()
        if (raw.isEmpty()) {
            return BackConvertResult(ok = false, error = "Entrada vazia")
        }

        var index = 0
        var negative = false
        if (raw[0] == '+' || raw[0] == '-') {
            negative = raw[0] == '-'
            index++
        }

        val normalized = raw.substring(index).replace(',', '.')
        val parts = normalized.split('.')
        if (parts.size > 2) {
            return BackConvertResult(ok = false, error = "Número com separador inválido")
        }

        val intPart = parts[0]
        val fracPart = if (parts.size == 2) parts[1] else ""

        if (intPart.isEmpty() && fracPart.isEmpty()) {
            return BackConvertResult(ok = false, error = "Número inválido")
        }

        var integerValue = BigInteger.ZERO
        for (ch in intPart) {
            val digit = DigitSymbols.charToDigit(ch)
            if (digit !in 0 until base) {
                return BackConvertResult(ok = false, error = "Dígito '$ch' inválido para base $base")
            }
            integerValue = integerValue * BigInteger.valueOf(base.toLong()) + BigInteger.valueOf(digit.toLong())
        }

        var fractionalNumerator = BigInteger.ZERO
        var denominator = BigInteger.ONE
        if (fracPart.isNotEmpty()) {
            for (ch in fracPart) {
                val digit = DigitSymbols.charToDigit(ch)
                if (digit !in 0 until base) {
                    return BackConvertResult(ok = false, error = "Dígito '$ch' inválido para base $base")
                }
                fractionalNumerator = fractionalNumerator * BigInteger.valueOf(base.toLong()) + BigInteger.valueOf(digit.toLong())
            }
            denominator = BigInteger.valueOf(base.toLong()).pow(fracPart.length)
        }

        val numerator = integerValue * denominator + fractionalNumerator
        val result = Rational.of(numerator, denominator)
        return BackConvertResult(ok = true, value = if (negative) result.negate() else result)
    }
}
