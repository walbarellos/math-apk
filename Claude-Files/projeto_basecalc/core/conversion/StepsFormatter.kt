package com.basecalc.core.conversion

import com.basecalc.core.model.DivisionStep
import com.basecalc.core.model.FracStep
import com.basecalc.core.util.DigitSymbols

/**
 * Formata os passos de conversão de base para exibição ao estudante.
 *
 * Parte inteira: divisões sucessivas (lidas de baixo para cima).
 * Parte fracionária: multiplicações sucessivas (lidas de cima para baixo).
 */
object StepsFormatter {

    /**
     * @param passosInteiros  Passos de divisão da parte inteira.
     * @param passosFracao    Passos de multiplicação da parte fracionária.
     */
    fun format(
        passosInteiros: List<DivisionStep>,
        passosFracao: List<FracStep> = emptyList(),
    ): String {
        if (passosInteiros.isEmpty() && passosFracao.isEmpty()) return ""

        return buildString {
            // ─── Parte inteira ─────────────────────────────────────────────
            if (passosInteiros.isNotEmpty()) {
                appendLine("Divisões sucessivas (parte inteira):")
                passosInteiros.forEachIndexed { i, passo ->
                    append("  ${passo.dividendo} ÷ ${passo.base} = ${passo.quociente}")
                    append("  resto ${passo.resto}")
                    if (i == passosInteiros.lastIndex) append("  ← lido primeiro")
                    appendLine()
                }
                appendLine("  (ler restos de baixo pra cima)")
            }

            // ─── Parte fracionária ─────────────────────────────────────────
            if (passosFracao.isNotEmpty()) {
                if (passosInteiros.isNotEmpty()) appendLine()
                appendLine("Multiplicações sucessivas (parte fracionária):")
                passosFracao.forEach { passo ->
                    val digitoChar = DigitSymbols.digitToChar(passo.digito)
                    append("  ${passo.restoAnterior}/${passo.denominador}")
                    append(" × ${passo.base} = ${passo.produto}/${passo.denominador}")
                    appendLine("  → dígito $digitoChar")
                }
                append("  (ler dígitos de cima pra baixo)")
            }
        }
    }
}
