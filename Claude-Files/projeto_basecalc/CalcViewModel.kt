package com.basecalc

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.basecalc.core.engine.BaseCalcEngine
import com.basecalc.core.engine.BaseCalcEngineImpl
import com.basecalc.data.AppContainer
import com.basecalc.data.HistoryEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ViewModel da calculadora multi-base.
 *
 * Responsabilidades:
 * - Manter o estado da UI via [uiState].
 * - Processar entradas do teclado ([onKey], [onEquals]).
 * - Delegar avaliação ao [BaseCalcEngine] em background.
 * - Gerenciar histórico de cálculos e persistência.
 */
class CalcViewModel(
    private val engine: BaseCalcEngine = BaseCalcEngineImpl(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalcUiState())
    val uiState: StateFlow<CalcUiState> = _uiState

    private val operadores = setOf("+", "-", "*", "/", "%", "^")
    private val cacheLock = Any()
    private val cache = object : LinkedHashMap<String, com.basecalc.core.model.CalcResult>(50, 0.75f, true) {
        override fun removeEldestEntry(
            eldest: MutableMap.MutableEntry<String, com.basecalc.core.model.CalcResult>?
        ): Boolean = size > 50
    }

    init {
        carregarHistorico()
    }

    private fun carregarHistorico() {
        try {
            val repository = AppContainer.getHistoryRepository()
            val historyEntries = repository.loadHistory()
            val history = historyEntries.map { HistoricoItem(it.expression, it.result) }
            _uiState.value = _uiState.value.copy(history = history)
        } catch (e: Exception) {
            // Silently fail if repository not available
        }
    }

    // ─── Entradas do teclado ──────────────────────────────────────────────────

    fun onKey(key: String) {
        val atual = _uiState.value

        when (key) {
            "AC" -> {
                _uiState.value = atual.copy(
                    expression = "0",
                    error = null,
                    showStepsForBase = null,
                )
                calcular("0")
                return
            }

            "⌫" -> {
                val novaExpr = atual.expression.dropLast(1).ifEmpty { "0" }
                _uiState.value = atual.copy(expression = novaExpr, error = null)
                calcular(novaExpr)
                return
            }
        }

        val novaExpr = construirExpressao(atual.expression, key)
        _uiState.value = atual.copy(expression = novaExpr, error = null)
    }

    fun onEquals() {
        val expr = _uiState.value.expression
        if (expr.isNotBlank()) calcular(expr)
    }

    // ─── Controle de UI ───────────────────────────────────────────────────────

    fun toggleSteps(base: Int) {
        val atual = _uiState.value
        _uiState.value = atual.copy(
            showStepsForBase = if (atual.showStepsForBase == base) null else base,
        )
    }

    fun navegarParaAba(aba: AppTab) {
        _uiState.value = _uiState.value.copy(activeTab = aba)
    }

    fun setHapticsEnabled(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(hapticsEnabled = enabled)
    }

    fun setReduceMotion(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(reduceMotion = enabled)
    }

    fun setColorMode(mode: ColorMode) {
        _uiState.value = _uiState.value.copy(colorMode = mode)
    }

    fun setInputBase(base: Int) {
        if (base !in 2..16) return
        _uiState.value = _uiState.value.copy(inputBase = base)
        calcular(_uiState.value.expression)
    }

    /** Restaura uma expressão do histórico e navega para a aba calculadora. */
    fun restaurarDoHistorico(item: HistoricoItem) {
        _uiState.value = _uiState.value.copy(
            expression = item.expressao,
            activeTab = AppTab.CALCULADORA,
        )
        calcular(item.expressao)
    }

    fun limparHistorico() {
        _uiState.value = _uiState.value.copy(history = emptyList())
        try {
            AppContainer.getHistoryRepository().clearHistory()
        } catch (e: Exception) {
            // Silently fail
        }
    }

    private fun persistirHistorico(history: List<HistoricoItem>) {
        try {
            val entries = history.map { HistoryEntry(it.expressao, it.resultadoDecimal) }
            AppContainer.getHistoryRepository().saveHistory(entries)
        } catch (e: Exception) {
            // Silently fail
        }
    }

    // ─── Cálculo assíncrono ───────────────────────────────────────────────────

    /**
     * Avalia [expr] em Dispatchers.Default (não trava a UI).
     * Salva no histórico apenas quando o resultado é válido e a expressão não é "0".
     */
    private fun calcular(expr: String) {
        viewModelScope.launch {
            val inputBase = _uiState.value.inputBase
            val cacheKey = "$inputBase:$expr"
            val cached = synchronized(cacheLock) { cache[cacheKey] }
            val resultado = cached ?: withContext(Dispatchers.Default) {
                engine.evaluateExpression(expr, inputBase)
            }
            if (cached == null) {
                synchronized(cacheLock) { cache[cacheKey] = resultado }
            }
            val atual = _uiState.value

            val novoHistorico = if (resultado.ok && expr != "0" && resultado.decimalDisplay.isNotEmpty()) {
                val novo = HistoricoItem(
                    expressao = expr,
                    resultadoDecimal = resultado.decimalDisplay,
                )
                (listOf(novo) + atual.history)
                    .distinctBy { it.expressao }
                    .take(CalcUiState.MAX_HISTORICO)
            } else {
                atual.history
            }

            _uiState.value = atual.copy(
                result = resultado,
                error = if (!resultado.ok) resultado.error else null,
                showStepsForBase = null,
                history = novoHistorico,
            )
            
            persistirHistorico(novoHistorico)
        }
    }

    // ─── Lógica de montagem da expressão ─────────────────────────────────────

    /**
     * Anexa [tecla] à expressão [atual] respeitando as regras:
     * - "0" isolado é substituído por dígito (nunca "07").
     * - Operador no final substitui operador anterior (evita "+-", "**" etc.).
     * - Vírgula e ponto são ambos aceitos como separador decimal.
     */
    private fun construirExpressao(atual: String, tecla: String): String {
        if (atual == "0" && tecla !in operadores) {
            return if (tecla == "." || tecla == ",") "0$tecla" else tecla
        }
        if (tecla in operadores && atual.lastOrNull()?.toString() in operadores) {
            return atual.dropLast(1) + tecla
        }
        return atual + tecla
    }
}
