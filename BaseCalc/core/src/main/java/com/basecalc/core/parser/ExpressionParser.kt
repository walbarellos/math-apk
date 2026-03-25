package com.basecalc.core.parser

import com.basecalc.core.number.Rational
import java.math.BigInteger

/**
 * Parser de expressão aritmética simples.
 * Suporta +, -, *, /, %, parênteses e números decimais.
 */
class ExpressionParser(private val texto: String) {

    private var index = 0

    fun parse(): Rational {
        if (texto.isBlank()) {
            throw IllegalArgumentException("Expressão vazia")
        }
        val result = parseExpression()
        skipWhitespace()
        if (index < texto.length) {
            throw IllegalArgumentException("Caractere inesperado: '${texto[index]}'")
        }
        return result
    }

    private fun parseExpression(): Rational {
        var left = parseTerm()
        while (true) {
            skipWhitespace()
            when (peekChar()) {
                '+' -> {
                    index++
                    left = left + parseTerm()
                }
                '-' -> {
                    index++
                    left = left - parseTerm()
                }
                else -> return left
            }
        }
    }

    private fun parseTerm(): Rational {
        var left = parseFactor()
        while (true) {
            skipWhitespace()
            when (peekChar()) {
                '*' -> {
                    index++
                    left = left * parseFactor()
                }
                '/' -> {
                    index++
                    val right = parseFactor()
                    if (right.numerador == BigInteger.ZERO) {
                        throw IllegalArgumentException("Divisão por zero")
                    }
                    left = left / right
                }
                '%' -> {
                    index++
                    val right = parseFactor()
                    left = moduloInteiro(left, right)
                }
                else -> return left
            }
        }
    }

    private fun parseFactor(): Rational {
        skipWhitespace()
        return when (peekChar()) {
            '+' -> {
                index++
                parseFactor()
            }
            '-' -> {
                index++
                parseFactor().negate()
            }
            '(' -> {
                index++
                val value = parseExpression()
                skipWhitespace()
                if (peekChar() != ')') {
                    throw IllegalArgumentException("Falta ')' na expressão")
                }
                index++
                value
            }
            else -> parseNumber()
        }
    }

    private fun parseNumber(): Rational {
        skipWhitespace()
        var sawDigit = false

        val integerPart = StringBuilder()
        while (peekChar() in '0'..'9') {
            integerPart.append(texto[index])
            index++
            sawDigit = true
        }

        var fractionalPart = StringBuilder()
        if (peekChar() == '.' || peekChar() == ',') {
            index++
            while (peekChar() in '0'..'9') {
                fractionalPart.append(texto[index])
                index++
                sawDigit = true
            }
        }

        if (!sawDigit) {
            throw IllegalArgumentException("Número esperado")
        }

        val intValue = if (integerPart.isNotEmpty()) {
            BigInteger(integerPart.toString())
        } else {
            BigInteger.ZERO
        }

        val scale = fractionalPart.length
        if (scale == 0) {
            return Rational.of(intValue, BigInteger.ONE)
        }

        val fracValue = BigInteger(fractionalPart.toString())
        val denominator = BigInteger.TEN.pow(scale)
        val numerator = intValue * denominator + fracValue
        return Rational.of(numerator, denominator)
    }

    private fun moduloInteiro(left: Rational, right: Rational): Rational {
        val leftInt = left.toBigIntegerExact()
        val rightInt = right.toBigIntegerExact()
        if (leftInt == null || rightInt == null) {
            throw IllegalArgumentException("Módulo só é válido para inteiros")
        }
        if (rightInt == BigInteger.ZERO) {
            throw IllegalArgumentException("Módulo por zero")
        }
        return Rational.of(leftInt.remainder(rightInt), BigInteger.ONE)
    }

    private fun skipWhitespace() {
        while (index < texto.length && texto[index].isWhitespace()) {
            index++
        }
    }

    private fun peekChar(): Char? {
        return if (index < texto.length) texto[index] else null
    }
}
