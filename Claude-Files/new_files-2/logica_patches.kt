// ═══════════════════════════════════════════════════════════════════════════
// PATCH 1: CalcUiState.kt
// ═══════════════════════════════════════════════════════════════════════════

// 1a. Trocar:
enum class AppTab { CALCULADORA, CONJUNTOS, MATRIZES, GRAFICO, HISTORICO }
// Por:
enum class AppTab { CALCULADORA, LOGICA, CONJUNTOS, MATRIZES, GRAFICO, HISTORICO }

// 1b. Adicionar antes dos data classes de Conjuntos:

import com.basecalc.core.logica.TabelaVerdade

data class LogicaUiState(
    val formula: String = "",
    val tabela: TabelaVerdade? = null,
    val erro: String? = null,
)

// 1c. Adicionar no data class CalcUiState (após matrizesState):

    /** Estado do módulo de lógica proposicional. */
    val logicaState: LogicaUiState = LogicaUiState(),


// ═══════════════════════════════════════════════════════════════════════════
// PATCH 2: CalcViewModel.kt
// ═══════════════════════════════════════════════════════════════════════════

// Adicionar import no topo:
import com.basecalc.core.logica.LogicaEngine

// Adicionar ao final da classe, após as funções de Matrizes:

    // ─── Lógica Proposicional ─────────────────────────────────────────────────

    fun setFormulaLogica(formula: String) {
        _uiState.value = _uiState.value.copy(
            logicaState = _uiState.value.logicaState.copy(
                formula = formula,
                erro = null,
                tabela = null,
            )
        )
    }

    fun calcularLogica() {
        val formula = _uiState.value.logicaState.formula
        if (formula.isBlank()) return

        viewModelScope.launch {
            val resultado = withContext(Dispatchers.Default) {
                try {
                    LogicaEngine.avaliar(formula) to null
                } catch (e: IllegalArgumentException) {
                    null to (e.message ?: "Erro de sintaxe")
                } catch (e: Exception) {
                    null to "Erro inesperado: ${e.message}"
                }
            }
            _uiState.value = _uiState.value.copy(
                logicaState = _uiState.value.logicaState.copy(
                    tabela = resultado.first,
                    erro = resultado.second,
                )
            )
        }
    }


// ═══════════════════════════════════════════════════════════════════════════
// PATCH 3: CalculatorScreen.kt
// ═══════════════════════════════════════════════════════════════════════════

// 3a. Adicionar import:
import com.basecalc.ui.logica.LogicaScreen

// 3b. No rotuloAba():
private fun rotuloAba(aba: AppTab) = when (aba) {
    AppTab.CALCULADORA -> "Calcular"
    AppTab.LOGICA      -> "Lógica"      // ← NOVO
    AppTab.CONJUNTOS   -> "Conjuntos"
    AppTab.MATRIZES    -> "Matrizes"
    AppTab.GRAFICO     -> "Gráfico"
    AppTab.HISTORICO   -> "Histórico"
}

// 3c. No when(aba) do AnimatedContent, adicionar case:
AppTab.LOGICA -> LogicaScreen(viewModel = viewModel)
