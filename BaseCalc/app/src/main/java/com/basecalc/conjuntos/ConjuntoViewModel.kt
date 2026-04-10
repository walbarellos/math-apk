package com.basecalc.conjuntos

import androidx.lifecycle.ViewModel
import com.basecalc.core.conjuntos.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

enum class ModoConjunto { OPERACAO, PERTINENCIA, SUBCONJUNTO, EXERCICIO1, PROVA, VENN }

data class ConjuntoUiState(
    val conjuntos: Map<String, String> = defaultSets(),
    val tokens: List<String> = emptyList(),
    val modo: ModoConjunto = ModoConjunto.OPERACAO,
    val resultadoOp: ConjuntoResult? = null,
    val resultadoMember: MembershipResult? = null,
    val resultadoSubset: SubsetResult? = null,
    val historico: List<ConjuntoResult.Ok> = emptyList(),
    val erro: String? = null,
    val vennTotal: Int = 0,
    val vennA: Int = 0,
    val vennB: Int = 0,
    val vennC: Int = 0,
    val vennAB: Int = 0,
    val vennAC: Int = 0,
    val vennBC: Int = 0,
    val vennABC: Int = 0,
    val vennResultado: Map<String, Int> = emptyMap(),
)

fun defaultSets() = mapOf(
    "U" to "0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15",
    "A" to "1,3,5,7,9,11,13,15",
    "B" to "0,2,4,6,8,10,12,14",
    "C" to "2,3,5,7,11,13",
    "D" to "0,4,8,12",
    "E" to "0,3,6,9,12,15",
)

class ConjuntoViewModel : ViewModel() {

    private val _state = MutableStateFlow(ConjuntoUiState())
    val state: StateFlow<ConjuntoUiState> = _state
    private val operadoresChar = setOf('∪', '∩', '−', 'ᶜ', '(', ')')
    private var parsedCacheSource: Map<String, String>? = null
    private var parsedCacheValue: Map<String, Set<String>> = emptyMap()

    private fun getParsedSets(conjuntos: Map<String, String>): Map<String, Set<String>> {
        val source = parsedCacheSource
        if (source != null && source == conjuntos) return parsedCacheValue
        val parsed = conjuntos.mapValues { ConjuntoEngine.parseSet(it.value) }
        parsedCacheSource = HashMap(conjuntos)
        parsedCacheValue = parsed
        return parsed
    }

    fun setVenn(total: Int, a: Int, b: Int, c: Int, ab: Int, ac: Int, bc: Int, abc: Int) {
        val soABC = abc
        val soAB = ab - abc
        val soAC = ac - abc
        val soBC = bc - abc
        val soA = a - soAB - soAC - abc
        val soB = b - soAB - soBC - abc
        val soC = c - soAC - soBC - abc
        val fora = total - (soA + soB + soC + soAB + soAC + soBC + soABC)

        _state.value = _state.value.copy(
            vennTotal = total, vennA = a, vennB = b, vennC = c,
            vennAB = ab, vennAC = ac, vennBC = bc, vennABC = abc,
            vennResultado = mapOf(
                "soA" to soA, "soB" to soB, "soC" to soC,
                "soAB" to soAB, "soAC" to soAC, "soBC" to soBC,
                "soABC" to soABC, "fora" to fora
            )
        )
    }

    fun setConjunto(nome: String, valor: String) {
        _state.value = _state.value.copy(
            conjuntos = _state.value.conjuntos + (nome to valor)
        )
    }

    fun addToken(t: String) {
        _state.value = _state.value.copy(
            tokens = _state.value.tokens + t,
            erro = null,
        )
    }

    fun backspace() {
        val toks = _state.value.tokens
        if (toks.isNotEmpty())
            _state.value = _state.value.copy(tokens = toks.dropLast(1))
    }

    fun clearExpression() {
        _state.value = _state.value.copy(tokens = emptyList(), resultadoOp = null, erro = null)
    }

    fun setModo(m: ModoConjunto) {
        _state.value = _state.value.copy(modo = m, erro = null)
    }

    fun calcular() {
        val st = _state.value
        val expr = st.tokens.joinToString("")
        if (expr.isBlank()) {
            _state.value = st.copy(erro = "Monte uma expressão primeiro")
            return
        }
        val sets = getParsedSets(st.conjuntos)
        val result = ConjuntoEngine.avaliar(expr, sets)
        val newHistorico = if (result is ConjuntoResult.Ok)
            (listOf(result) + st.historico).take(20)
        else st.historico
        _state.value = st.copy(
            resultadoOp = result,
            historico = newHistorico,
            erro = if (result is ConjuntoResult.Erro) result.message else null,
            tokens = emptyList(),
        )
    }

    fun verificarPertinencia(elemento: String, nomeConjunto: String) {
        val st = _state.value
        val sets = getParsedSets(st.conjuntos)
        val conj = sets[nomeConjunto] ?: run {
            _state.value = st.copy(erro = "Conjunto $nomeConjunto não definido")
            return
        }
        val r = ConjuntoEngine.verificarPertinencia(elemento.trim(), nomeConjunto, conj)
        _state.value = st.copy(resultadoMember = r, erro = null)
    }

    fun verificarSubconjunto(nomeA: String, nomeB: String) {
        val st = _state.value
        val sets = getParsedSets(st.conjuntos)
        val cA = sets[nomeA] ?: run {
            _state.value = st.copy(erro = "Conjunto $nomeA não definido"); return
        }
        val cB = sets[nomeB] ?: run {
            _state.value = st.copy(erro = "Conjunto $nomeB não definido"); return
        }
        val r = ConjuntoEngine.verificarSubconjunto(nomeA, cA, nomeB, cB)
        _state.value = st.copy(resultadoSubset = r, erro = null)
    }

    fun rodarExercicio(expr: String) {
        val st = _state.value
        val toks = tokenizarSimples(expr)
        val sets = getParsedSets(st.conjuntos)
        val result = ConjuntoEngine.avaliar(toks.joinToString(""), sets)
        val newHistorico = if (result is ConjuntoResult.Ok)
            (listOf(result) + st.historico).take(20)
        else st.historico

        _state.value = st.copy(
            tokens = emptyList(),
            resultadoOp = result,
            historico = newHistorico,
            erro = if (result is ConjuntoResult.Erro) result.message else null,
        )
    }

    private fun tokenizarSimples(s: String): List<String> {
        val result = mutableListOf<String>()
        var i = 0
        while (i < s.length) {
            when {
                s[i].isWhitespace() -> i++
                s[i] in operadoresChar -> { result += s[i].toString(); i++ }
                s[i].isLetter() -> {
                    var j = i + 1
                    while (j < s.length && s[j].isLetterOrDigit()) j++
                    result += s.substring(i, j); i = j
                }
                else -> i++
            }
        }
        return result
    }
}
