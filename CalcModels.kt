package com.basecalc

/** Resultado de uma base individual. */
data class BaseEntry(
    val base: Int,
    val label: String,
    val valid: Boolean,
    val intPart: String,
    val fracPart: String,       // vazio se inteiro
    val repeats: Boolean,
    val repeatPart: String,     // bloco periódico
    val steps: String           // string formatada para sala de aula
) {
    /** Exibe o número completo na base */
    val display: String get() = buildString {
        append(intPart)
        if (fracPart.isNotEmpty()) {
            append(",")          // vírgula decimal (BR)
            append(fracPart)
            if (repeats && repeatPart.isNotEmpty()) append("(${repeatPart}...)")
        }
    }
}

/** Resultado completo de uma expressão. */
data class CalcResult(
    val ok: Boolean,
    val error: String = "",
    val value: Double = 0.0,
    val decimal: String = "0",
    val bases: List<BaseEntry> = emptyList()
)

/** Estado da UI da calculadora. */
data class CalcUiState(
    val expression: String = "",
    val result: CalcResult? = null,
    val showStepsForBase: Int? = null,   // qual base está expandida
    val error: String? = null
)
