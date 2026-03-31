package com.basecalc

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.basecalc.core.engine.BaseCalcEngine
import com.basecalc.core.engine.BaseCalcEngineImpl
import com.basecalc.core.logica.LogicaEngine
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

    fun toggleModoDiscreta() {
        _uiState.value = _uiState.value.copy(
            modoDiscreta = !_uiState.value.modoDiscreta
        )
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

    // ─── Conjuntos ────────────────────────────────────────────────────────────────

    fun setInputConjuntoA(v: String) {
        _uiState.value = _uiState.value.copy(
            conjuntosState = _uiState.value.conjuntosState.copy(inputA = v)
        )
    }

    fun setInputConjuntoB(v: String) {
        _uiState.value = _uiState.value.copy(
            conjuntosState = _uiState.value.conjuntosState.copy(inputB = v)
        )
    }

    fun setUniverso(v: String) {
        _uiState.value = _uiState.value.copy(
            conjuntosState = _uiState.value.conjuntosState.copy(universo = v)
        )
    }

    fun setOperacaoConjunto(op: OperacaoConjunto) {
        _uiState.value = _uiState.value.copy(
            conjuntosState = _uiState.value.conjuntosState.copy(operacao = op)
        )
    }

    fun calcularConjuntos() {
        val s = _uiState.value.conjuntosState
        val a = parseConjunto(s.inputA)
        val b = parseConjunto(s.inputB)
        val u = if (s.universo.isBlank()) null else parseConjunto(s.universo)

        val resultado = when (s.operacao) {
            OperacaoConjunto.UNIAO -> {
                val r = a union b
                ConjuntoResultado(
                    elementos = r,
                    elementosA = a, elementosB = b,
                    elementosComuns = a intersect b,
                    elementosApenasA = a - b, elementosApenasB = b - a,
                    passos = listOf(
                        "A = ${a.formatConjunto()}",
                        "B = ${b.formatConjunto()}",
                        "A ∪ B = todos os elementos de A e B sem repetição",
                        "A ∪ B = ${r.formatConjunto()}",
                    )
                )
            }
            OperacaoConjunto.INTERSECCAO -> {
                val r = a intersect b
                ConjuntoResultado(
                    elementos = r,
                    elementosA = a, elementosB = b,
                    elementosComuns = r,
                    elementosApenasA = a - b, elementosApenasB = b - a,
                    passos = listOf(
                        "A = ${a.formatConjunto()}",
                        "B = ${b.formatConjunto()}",
                        "A ∩ B = elementos que estão em A E em B",
                        "Comuns: ${r.formatConjunto()}",
                        "A ∩ B = ${r.formatConjunto()}",
                    )
                )
            }
            OperacaoConjunto.DIFERENCA -> {
                val r = a - b
                ConjuntoResultado(
                    elementos = r,
                    elementosA = a, elementosB = b,
                    elementosComuns = a intersect b,
                    elementosApenasA = r, elementosApenasB = b - a,
                    passos = listOf(
                        "A = ${a.formatConjunto()}",
                        "B = ${b.formatConjunto()}",
                        "A - B = elementos de A que NÃO estão em B",
                        "Remover de A: ${(a intersect b).formatConjunto()}",
                        "A - B = ${r.formatConjunto()}",
                    )
                )
            }
            OperacaoConjunto.COMPLEMENTO -> {
                val universo = u ?: (a union b)
                val r = universo - a
                ConjuntoResultado(
                    elementos = r,
                    elementosA = a, elementosB = b,
                    elementosComuns = emptySet(),
                    elementosApenasA = a, elementosApenasB = emptySet(),
                    passos = listOf(
                        "U = ${universo.formatConjunto()}",
                        "A = ${a.formatConjunto()}",
                        "A' = U - A = elementos de U que não estão em A",
                        "A' = ${r.formatConjunto()}",
                    )
                )
            }
        }

        _uiState.value = _uiState.value.copy(
            conjuntosState = s.copy(resultado = resultado)
        )
    }

    private fun parseConjunto(input: String): Set<String> =
        input.split(",", ";", " ")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .toSortedSet()

    private fun Set<String>.formatConjunto(): String =
        if (isEmpty()) "∅" else "{ ${sorted().joinToString(", ")} }"

    // ─── Matrizes ─────────────────────────────────────────────────────────────────

    fun setCelulaMatrizA(row: Int, col: Int, value: String) {
        val s = _uiState.value.matrizesState
        val nova = s.matrizA.mapIndexed { r, linha ->
            if (r == row) linha.mapIndexed { c, v -> if (c == col) value else v } else linha
        }
        _uiState.value = _uiState.value.copy(matrizesState = s.copy(matrizA = nova))
    }

    fun setCelulaMatrizB(row: Int, col: Int, value: String) {
        val s = _uiState.value.matrizesState
        val nova = s.matrizB.mapIndexed { r, linha ->
            if (r == row) linha.mapIndexed { c, v -> if (c == col) value else v } else linha
        }
        _uiState.value = _uiState.value.copy(matrizesState = s.copy(matrizB = nova))
    }

    fun setDimensaoMatriz(linhas: Int, colunas: Int) {
        val s = _uiState.value.matrizesState
        fun resize(m: List<List<String>>) = List(linhas) { r ->
            List(colunas) { c -> m.getOrNull(r)?.getOrNull(c) ?: "0" }
        }
        _uiState.value = _uiState.value.copy(
            matrizesState = s.copy(
                linhas = linhas, colunas = colunas,
                matrizA = resize(s.matrizA),
                matrizB = resize(s.matrizB),
            )
        )
    }

    fun setOperacaoMatriz(op: OperacaoMatriz) {
        _uiState.value = _uiState.value.copy(
            matrizesState = _uiState.value.matrizesState.copy(operacao = op)
        )
    }

    fun calcularMatriz() {
        val s = _uiState.value.matrizesState
        val a = s.matrizA.toDoubleMatrix()
        val b = s.matrizB.toDoubleMatrix()

        val (resultado, passos) = when (s.operacao) {
            OperacaoMatriz.MULTIPLICACAO -> multiplicarMatrizes(a, b)
            OperacaoMatriz.DETERMINANTE -> calcularDeterminante(a)
            OperacaoMatriz.TRANSPOSTA -> transporMatriz(a)
            OperacaoMatriz.INVERSA -> inverterMatriz(a)
        }

        _uiState.value = _uiState.value.copy(
            matrizesState = s.copy(
                resultado = resultado.map { linha -> linha.map { it.formatNum() } },
                passos = passos,
            )
        )
    }

    private fun multiplicarMatrizes(
        a: List<List<Double>>,
        b: List<List<Double>>,
    ): Pair<List<List<Double>>, List<PassoMatriz>> {
        val linhas = a.size
        val colunas = b[0].size
        val k = b.size
        val passos = mutableListOf<PassoMatriz>()
        val resultado = List(linhas) { r ->
            List(colunas) { c ->
                var soma = 0.0
                val termos = (0 until k).map { i ->
                    soma += a[r][i] * b[i][c]
                    "${a[r][i].formatNum()}×${b[i][c].formatNum()}"
                }
                passos.add(
                    PassoMatriz(
                        descricao = "C[$r][$c] = ${termos.joinToString(" + ")} = ${soma.formatNum()}",
                        linhaDestaque = r,
                        colunaDestaque = c,
                    )
                )
                soma
            }
        }
        return resultado to passos
    }

    private fun calcularDeterminante(
        a: List<List<Double>>,
    ): Pair<List<List<Double>>, List<PassoMatriz>> {
        val passos = mutableListOf<PassoMatriz>()
        val det = detRecursivo(a, passos)
        return listOf(listOf(det)) to passos
    }

    private fun detRecursivo(m: List<List<Double>>, passos: MutableList<PassoMatriz>): Double {
        if (m.size == 1) return m[0][0]
        if (m.size == 2) {
            val d = m[0][0] * m[1][1] - m[0][1] * m[1][0]
            passos.add(PassoMatriz("det = (${m[0][0].formatNum()}×${m[1][1].formatNum()}) - (${m[0][1].formatNum()}×${m[1][0].formatNum()}) = ${d.formatNum()}"))
            return d
        }
        var det = 0.0
        for (col in m[0].indices) {
            val cofator = m.drop(1).map { row -> row.filterIndexed { c, _ -> c != col } }
            val sinal = if (col % 2 == 0) 1.0 else -1.0
            val subDet = detRecursivo(cofator, passos)
            val contribuicao = sinal * m[0][col] * subDet
            passos.add(
                PassoMatriz(
                    "Laplace col $col: ${if (sinal > 0) "+" else "-"}${m[0][col].formatNum()} × det submatriz = ${contribuicao.formatNum()}",
                    colunaDestaque = col,
                )
            )
            det += contribuicao
        }
        return det
    }

    private fun transporMatriz(
        a: List<List<Double>>,
    ): Pair<List<List<Double>>, List<PassoMatriz>> {
        val t = List(a[0].size) { c -> List(a.size) { r -> a[r][c] } }
        val passos = listOf(PassoMatriz("Transposta: A[i][j] → Aᵀ[j][i] para todo i,j"))
        return t to passos
    }

    private fun inverterMatriz(
        a: List<List<Double>>,
    ): Pair<List<List<Double>>, List<PassoMatriz>> {
        val n = a.size
        val passos = mutableListOf<PassoMatriz>()
        val m = Array(n) { r -> DoubleArray(2 * n) { c -> if (c < n) a[r][c] else if (c - n == r) 1.0 else 0.0 } }
        for (col in 0 until n) {
            val pivot = m[col][col]
            if (pivot == 0.0) {
                passos.add(PassoMatriz("Matriz singular — inversa não existe"))
                return List(n) { List(n) { Double.NaN } } to passos
            }
            passos.add(PassoMatriz("Pivô [$col][$col] = ${pivot.formatNum()} → dividir linha $col por ${pivot.formatNum()}"))
            for (c in 0 until 2 * n) m[col][c] /= pivot
            for (r in 0 until n) {
                if (r == col) continue
                val fator = m[r][col]
                if (fator == 0.0) continue
                passos.add(PassoMatriz("L$r = L$r - (${fator.formatNum()}) × L$col"))
                for (c in 0 until 2 * n) m[r][c] -= fator * m[col][c]
            }
        }
        val inv = List(n) { r -> List(n) { c -> m[r][c + n] } }
        return inv to passos
    }

    private fun List<List<String>>.toDoubleMatrix(): List<List<Double>> =
        map { row -> row.map { it.toDoubleOrNull() ?: 0.0 } }

    private fun Double.formatNum(): String =
        if (this == toLong().toDouble()) toLong().toString()
        else "%.4f".format(this).trimEnd('0').trimEnd('.')

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
}
