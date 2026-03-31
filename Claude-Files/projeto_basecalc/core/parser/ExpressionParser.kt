package com.basecalc.core.parser

import com.basecalc.core.number.Rational
import java.math.BigInteger

/**
 * Parser de expressão aritmética simples.
 * Suporta +, -, *, /, %, parênteses e números na [basePadrao] (2–16).
 */
class ExpressionParser(private val texto: String, private val basePadrao: Int = 10) {

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
        var left = parsePower()
        while (true) {
            skipWhitespace()
            when (peekChar()) {
                '*' -> {
                    index++
                    left = left * parsePower()
                }
                '/' -> {
                    index++
                    val right = parsePower()
                    if (right.numerador == BigInteger.ZERO) {
                        throw IllegalArgumentException("Divisão por zero")
                    }
                    left = left / right
                }
                '%' -> {
                    index++
                    val right = parsePower()
                    left = moduloInteiro(left, right)
                }
                else -> return left
            }
        }
    }

    private fun parsePower(): Rational {
        var left = parseFactor()
        while (true) {
            skipWhitespace()
            if (peekChar() == '^') {
                index++
                val right = parseFactor()
                val expInt = right.toBigIntegerExact()?.toInt() 
                    ?: throw IllegalArgumentException("Expoente deve ser inteiro")
                
                if (expInt < 0) {
                    // Suporte básico a expoente negativo: 1 / (base ^ |exp|)
                    val power = left.numerador.pow(-expInt)
                    val basePower = left.denominador.pow(-expInt)
                    left = Rational.of(basePower, power)
                } else {
                    val num = left.numerador.pow(expInt)
                    val den = left.denominador.pow(expInt)
                    left = Rational.of(num, den)
                }
            } else {
                return left
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
        while (peekChar()?.let { isDigitForBase(it, basePadrao) } == true) {
            integerPart.append(texto[index])
            index++
            sawDigit = true
        }

        var fractionalPart = StringBuilder()
        if (peekChar() == '.' || peekChar() == ',') {
            index++
            while (peekChar()?.let { isDigitForBase(it, basePadrao) } == true) {
                fractionalPart.append(texto[index])
                index++
                sawDigit = true
            }
        }

        if (!sawDigit) {
            throw IllegalArgumentException("Número esperado (Base $basePadrao)")
        }

        val intValue = if (integerPart.isNotEmpty()) {
            BigInteger(integerPart.toString(), basePadrao)
        } else {
            BigInteger.ZERO
        }

        val scale = fractionalPart.length
        if (scale == 0) {
            return Rational.of(intValue, BigInteger.ONE)
        }

        val fracValue = BigInteger(fractionalPart.toString(), basePadrao)
        val denominator = BigInteger.valueOf(basePadrao.toLong()).pow(scale)
        val numerator = intValue * denominator + fracValue
        return Rational.of(numerator, denominator)
    }

    private fun isDigitForBase(c: Char, base: Int): Boolean {
        val value = when {
            c in '0'..'9' -> c - '0'
            c in 'A'..'F' -> c - 'A' + 10
            c in 'a'..'f' -> c - 'a' + 10
            else -> -1
        }
        return value >= 0 && value < base
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
