package com.basecalc.core.model

import com.basecalc.core.number.ConjuntoNumerico

/**
 * Resultado completo da conversão de um número racional para uma base específica.
 *
 * @param base        Número da base (2–16).
 * @param label       Rótulo legível (ex.: "Base 2 — Binário").
 * @param intPart     Representação da parte inteira (pode ter sinal "−").
 * @param fracPart    Dígitos da parte fracionária antes da repetição.
 * @param repeatPart  Bloco que se repete (dízima periódica), vazio se termina.
 * @param repeats     Indica se há dízima periódica.
 * @param steps       Passos de divisão sucessiva da parte inteira.
 * @param fracSteps   Passos de multiplicação sucessiva da parte fracionária.
 * @param valid       false quando a base é inválida ou ocorre outro erro.
 * @param error       Mensagem de erro quando [valid] == false.
 * @param conjunto    Conjunto numérico (N, Z, Q, I, R).
 */
data class BaseEntry(
    val base: Int,
    val label: String,
    val intPart: String,
    val fracPart: String,
    val repeatPart: String,
    val repeats: Boolean,
    val steps: List<DivisionStep>,
    val fracSteps: List<FracStep> = emptyList(),
    val valid: Boolean,
    val error: String? = null,
    val additionalInfo: List<String> = emptyList(),
    val conjunto: ConjuntoNumerico = ConjuntoNumerico.REAL,
) {
    /** Monta a representação legível com vírgula e indicação de período. */
    fun displayWithComma(): String = buildString {
        append(intPart)
        if (fracPart.isNotEmpty()) {
            append(',')
            append(fracPart)
        }
        if (repeats && repeatPart.isNotEmpty()) {
            append('(')
            append(repeatPart)
            append("...)")
        }
    }
}
