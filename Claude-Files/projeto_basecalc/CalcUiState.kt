package com.basecalc

import com.basecalc.core.model.CalcResult

/** Abas de navegação do app. */
enum class AppTab { CALCULADORA, GRAFICO, HISTORICO }

/** Modo de cores da interface. */
enum class ColorMode { PADRAO, ALTO_CONTRASTE, DALTONISMO }

/**
 * Entrada no histórico de cálculos.
 * @param expressao  Expressão original digitada.
 * @param resultadoDecimal  Resultado em base 10, já formatado.
 */
data class HistoricoItem(
    val expressao: String,
    val resultadoDecimal: String,
)

/**
 * Estado completo e imutável da interface.
 * Toda mudança produz uma nova cópia via [copy].
 */
data class CalcUiState(

    /** Texto atual da expressão na tela. */
    val expression: String = "0",

    /** Resultado da última avaliação (null = ainda não calculou). */
    val result: CalcResult? = null,

    /** Mensagem de erro da última avaliação, se houver. */
    val error: String? = null,

    /** Base cujos passos estão expandidos (null = nenhum). */
    val showStepsForBase: Int? = null,

    /** Aba visível no momento. */
    val activeTab: AppTab = AppTab.CALCULADORA,

    /** Histórico das últimas [MAX_HISTORICO] expressões avaliadas. */
    val history: List<HistoricoItem> = emptyList(),

    /** Feedback tátil ao tocar nas teclas. */
    val hapticsEnabled: Boolean = true,

    /** Reduz animações para dispositivos lentos. */
    val reduceMotion: Boolean = false,

    /** Modo de cores selecionado pelo usuário. */
    val colorMode: ColorMode = ColorMode.PADRAO,

    /** Base de entrada para os números digitados (2–16). */
    val inputBase: Int = 10,
) {
    companion object {
        const val MAX_HISTORICO = 50
    }
}
