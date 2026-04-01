package com.basecalc.conjuntos

import androidx.lifecycle.ViewModel
import com.basecalc.core.conjuntos.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

// ─── Modos da tela ────────────────────────────────────────────────────────────

enum class ModoConjunto { OPERACAO, PERTINENCIA, SUBCONJUNTO }

// ─── Estado da UI ─────────────────────────────────────────────────────────────

data class ConjuntoUiState(
    val conjuntos: Map<String, String> = defaultSets(),
    val tokens: List<String> = emptyList(),
    val modo: ModoConjunto = ModoConjunto.OPERACAO,

    // Resultado de operação
    val resultadoOp: ConjuntoResult? = null,

    // Resultado de pertinência
    val resultadoMember: MembershipResult? = null,

    // Resultado de subconjunto
    val resultadoSubset: SubsetResult? = null,

    // Histórico de operações
    val historico: List<ConjuntoResult.Ok> = emptyList(),

    val erro: String? = null,
)

fun defaultSets() = mapOf(
    "U" to "0,1,2,3,4,5,6,7,8,9,10,11",
    "A" to "1,3,5,7,9,11",
    "B" to "0,2,4,6,8,10",
    "C" to "2,3,5,7,11",
    "D" to "0,3,6,9",
    "E" to "0,4,8",
)

class ConjuntoViewModel : ViewModel() {

    private val _state = MutableStateFlow(ConjuntoUiState())
    val state: StateFlow<ConjuntoUiState> = _state

    // ── Definição dos conjuntos ───────────────────────────────────────────────

    fun setConjunto(nome: String, valor: String) {
        _state.value = _state.value.copy(
            conjuntos = _state.value.conjuntos + (nome to valor)
        )
    }

    // ── Construção da expressão ───────────────────────────────────────────────

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

    // ── Calcular operação ─────────────────────────────────────────────────────

    fun calcular() {
        val st = _state.value
        val expr = st.tokens.joinToString("")
        if (expr.isBlank()) {
            _state.value = st.copy(erro = "Monte uma expressão primeiro")
            return
        }
        val sets = st.conjuntos.mapValues { ConjuntoEngine.parseSet(it.value) }
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

    // ── Verificar pertinência ─────────────────────────────────────────────────

    fun verificarPertinencia(elemento: String, nomeConjunto: String) {
        val st = _state.value
        val sets = st.conjuntos.mapValues { ConjuntoEngine.parseSet(it.value) }
        val conj = sets[nomeConjunto] ?: run {
            _state.value = st.copy(erro = "Conjunto $nomeConjunto não definido")
            return
        }
        val r = ConjuntoEngine.verificarPertinencia(elemento.trim(), nomeConjunto, conj)
        _state.value = st.copy(resultadoMember = r, erro = null)
    }

    // ── Verificar subconjunto ─────────────────────────────────────────────────

    fun verificarSubconjunto(nomeA: String, nomeB: String) {
        val st = _state.value
        val sets = st.conjuntos.mapValues { ConjuntoEngine.parseSet(it.value) }
        val cA = sets[nomeA] ?: run {
            _state.value = st.copy(erro = "Conjunto $nomeA não definido"); return
        }
        val cB = sets[nomeB] ?: run {
            _state.value = st.copy(erro = "Conjunto $nomeB não definido"); return
        }
        val r = ConjuntoEngine.verificarSubconjunto(nomeA, cA, nomeB, cB)
        _state.value = st.copy(resultadoSubset = r, erro = null)
    }

    // ── Carregar exercício direto ──────────────────────────────────────────────

    fun rodarExercicio(expr: String) {
        _state.value = _state.value.copy(tokens = emptyList())
        val toks = tokenizarSimples(expr)
        _state.value = _state.value.copy(tokens = toks)
        calcular()
    }

    private fun tokenizarSimples(s: String): List<String> {
        val result = mutableListOf<String>()
        var i = 0
        while (i < s.length) {
            when {
                s[i].isWhitespace() -> i++
                s[i] in "∪∩−ᶜ()".toList() -> { result += s[i].toString(); i++ }
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
