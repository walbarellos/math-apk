package com.basecalc

import com.basecalc.core.model.CalcResult
import com.basecalc.core.logica.TabelaVerdade

/** Resultado de operação com conjuntos. */
data class ConjuntoResultado(
    val elementos: Set<String>,
    val passos: List<String>,
    val elementosA: Set<String>,
    val elementosB: Set<String>,
    val elementosApenasA: Set<String>,
    val elementosApenasB: Set<String>,
    val elementosComuns: Set<String>,
)

/** Passo de operação matricial. */
data class PassoMatriz(
    val descricao: String,
    val linhaDestaque: Int = -1,
    val colunaDestaque: Int = -1,
)

/** Estado do módulo de lógica proposicional. */
data class LogicaUiState(
    val formula: String = "",
    val tabela: TabelaVerdade? = null,
    val erro: String? = null,
)

/** Estado do módulo de conjuntos. */
data class ConjuntosUiState(
    val inputA: String = "",
    val inputB: String = "",
    val universo: String = "",
    val operacao: OperacaoConjunto = OperacaoConjunto.UNIAO,
    val resultado: ConjuntoResultado? = null,
)

/** Estado do módulo de matrizes. */
data class MatrizesUiState(
    val linhas: Int = 2,
    val colunas: Int = 2,
    val matrizA: List<List<String>> = List(2) { List(2) { "0" } },
    val matrizB: List<List<String>> = List(2) { List(2) { "0" } },
    val operacao: OperacaoMatriz = OperacaoMatriz.MULTIPLICACAO,
    val passos: List<PassoMatriz> = emptyList(),
    val resultado: List<List<String>> = emptyList(),
)

/** Estado do módulo de potenciação e radiciação. */
data class PotenciacaoUiState(
    val base: String = "",
    val expoente: String = "",
    val raiz: String = "",
    val indice: String = "",
    val resultado: String = "",
    val passos: List<String> = emptyList(),
)

/** Abas de navegação do app. */
enum class AppTab { CALCULADORA, LOGICA, CONJUNTOS, MATRIZES, POTENCIACAO, GRAFICO, HISTORICO }

/** Operações de teoria dos conjuntos. */
enum class OperacaoConjunto { UNIAO, INTERSECCAO, DIFERENCA, COMPLEMENTO }

/** Operações com matrizes. */
enum class OperacaoMatriz { MULTIPLICACAO, DETERMINANTE, TRANSPOSTA, INVERSA }

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

    /** Estado do módulo conjuntos. */
    val conjuntosState: ConjuntosUiState = ConjuntosUiState(),

    /** Estado do módulo matrizes. */
    val matrizesState: MatrizesUiState = MatrizesUiState(),

    /** Estado do módulo de lógica proposicional. */
    val logicaState: LogicaUiState = LogicaUiState(),

    /** Modo "cola discreta" - tema escuro OLED, fontes menores */
    val modoDiscreta: Boolean = false,

    /** Estado do módulo de potenciação/radiciação */
    val potenciacaoState: PotenciacaoUiState = PotenciacaoUiState(),
) {
    companion object {
        const val MAX_HISTORICO = 50
    }
}
