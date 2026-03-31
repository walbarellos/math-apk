package com.basecalc.core.conversion

/** Fornece rótulos descritivos para cada base. */
object BaseLabelProvider {

    fun labelFor(base: Int): String {
        return when (base) {
            2 -> "Base 2 — Binário"
            3 -> "Base 3"
            4 -> "Base 4"
            5 -> "Base 5"
            6 -> "Base 6"
            7 -> "Base 7"
            8 -> "Base 8 — Octal"
            9 -> "Base 9"
            10 -> "Base 10 — Decimal"
            16 -> "Base 16 — Hexadecimal"
            else -> "Base $base"
        }
    }
}
