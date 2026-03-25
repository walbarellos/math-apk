package com.basecalc.core.util

/** Mapeia dígitos para bases até 16. */
object DigitSymbols {

    private const val SYMBOLS = "0123456789ABCDEF"

    fun digitToChar(digit: Int): Char {
        if (digit < 0 || digit >= SYMBOLS.length) return '?'
        return SYMBOLS[digit]
    }

    fun charToDigit(char: Char): Int {
        return when {
            char in '0'..'9' -> char - '0'
            char in 'A'..'F' -> 10 + (char - 'A')
            char in 'a'..'f' -> 10 + (char - 'a')
            else -> -1
        }
    }

    fun isValidForBase(char: Char, base: Int): Boolean {
        val digit = charToDigit(char)
        return digit in 0 until base
    }
}
