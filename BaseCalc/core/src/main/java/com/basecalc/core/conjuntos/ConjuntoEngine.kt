package com.basecalc.core.conjuntos

// ─── Tipos de resultado ───────────────────────────────────────────────────────

sealed class ConjuntoResult {
    data class Ok(
        val expression: String,
        val result: Set<String>,
        val notation: NotacaoFormal,
        val steps: List<Step>,
    ) : ConjuntoResult()

    data class Erro(val message: String) : ConjuntoResult()
}

data class NotacaoFormal(
    val extensao: String,          // {2, 3, 5}  ou  ∅
    val cardinalidade: Int,        // n(X) = 3
    val pertinencias: List<String>, // ["2 ∈ X", "3 ∈ X", ...]
    val subconjuntoDeU: Boolean,
    val vazio: Boolean,
)

data class Step(
    val operacao: String,   // "∪", "∩", "−", "ᶜ"
    val esquerda: Set<String>,
    val direita: Set<String>?,   // null para complementar
    val resultado: Set<String>,
)

data class MembershipResult(
    val elemento: String,
    val nomeConjunto: String,
    val conjunto: Set<String>,
    val pertence: Boolean,
    val notacao: String,           // "5 ∈ A"  ou  "12 ∉ A"
    val justificativa: String,
)

data class SubsetResult(
    val nomeA: String,
    val conjA: Set<String>,
    val nomeB: String,
    val conjB: Set<String>,
    val contido: Boolean,
    val notacao: String,           // "E ⊂ B"  ou  "C ⊄ A"
    val elementosFaltando: List<String>,
    val justificativa: String,
)

// ─── Motor ────────────────────────────────────────────────────────────────────

object ConjuntoEngine {

    // Conjuntos nomeados disponíveis
    private val KNOWN = setOf("U","A","B","C","D","E","F","G","H")

    // ── Operações primitivas ──────────────────────────────────────────────────

    fun uniao(a: Set<String>, b: Set<String>): Set<String> = a + b
    fun interseccao(a: Set<String>, b: Set<String>): Set<String> = a.intersect(b)
    fun diferenca(a: Set<String>, b: Set<String>): Set<String> = a - b
    fun complementar(a: Set<String>, u: Set<String>): Set<String> = u - a

    // ── Parse de string de conjunto: "1,3,5,7" → Set ─────────────────────────

    fun parseSet(s: String): Set<String> {
        if (s.isBlank()) return emptySet()
        return s.split(',').map { it.trim() }.filter { it.isNotEmpty() }.toSet()
    }

    fun formatSet(s: Set<String>): String {
        if (s.isEmpty()) return "∅"
        val sorted = sortSet(s)
        return "{${sorted.joinToString(", ")}}"
    }

    private fun sortSet(s: Set<String>): List<String> {
        val allNum = s.all { it.toDoubleOrNull() != null }
        return if (allNum) s.sortedBy { it.toDouble() } else s.sorted()
    }

    // ── Verificar pertinência ─────────────────────────────────────────────────

    fun verificarPertinencia(
        elemento: String,
        nomeConjunto: String,
        conjunto: Set<String>
    ): MembershipResult {
        val pertence = conjunto.contains(elemento)
        val sym = if (pertence) "∈" else "∉"
        val just = if (pertence)
            "O elemento $elemento pertence ao conjunto $nomeConjunto pois está em ${formatSet(conjunto)}."
        else
            "O elemento $elemento não pertence ao conjunto $nomeConjunto pois não está em ${formatSet(conjunto)}."
        return MembershipResult(
            elemento = elemento,
            nomeConjunto = nomeConjunto,
            conjunto = conjunto,
            pertence = pertence,
            notacao = "$elemento $sym $nomeConjunto",
            justificativa = just,
        )
    }

    // ── Verificar subconjunto ─────────────────────────────────────────────────

    fun verificarSubconjunto(
        nomeA: String, conjA: Set<String>,
        nomeB: String, conjB: Set<String>
    ): SubsetResult {
        val contido = conjA.all { it in conjB }
        val sym = if (contido) "⊂" else "⊄"
        val faltando = if (!contido) (conjA - conjB).let { sortSet(it) } else emptyList()
        val just = if (contido)
            "Todo elemento de $nomeA também é elemento de $nomeB. ∀x(x ∈ $nomeA → x ∈ $nomeB)"
        else
            "Os elementos ${faltando.joinToString(", ")} pertencem a $nomeA mas não a $nomeB."
        return SubsetResult(
            nomeA = nomeA, conjA = conjA,
            nomeB = nomeB, conjB = conjB,
            contido = contido,
            notacao = "$nomeA $sym $nomeB",
            elementosFaltando = faltando,
            justificativa = just,
        )
    }

    // ── Avaliar expressão ─────────────────────────────────────────────────────
    // Suporta: ∪ ∩ − ᶜ ( )
    // Exemplo: "(A ∪ B) − Cᶜ"

    fun avaliar(expressao: String, conjuntos: Map<String, Set<String>>): ConjuntoResult {
        return try {
            val tokens = tokenizar(expressao)
            val parser = ExprParser(tokens, conjuntos)
            val (result, steps) = parser.parseExpr()
            val u = conjuntos["U"] ?: emptySet()
            val notation = NotacaoFormal(
                extensao = formatSet(result),
                cardinalidade = result.size,
                pertinencias = if (result.size <= 12)
                    sortSet(result).map { "$it ∈ ($expressao)" }
                else emptyList(),
                subconjuntoDeU = result.all { it in u },
                vazio = result.isEmpty(),
            )
            ConjuntoResult.Ok(
                expression = expressao,
                result = result,
                notation = notation,
                steps = steps,
            )
        } catch (e: Exception) {
            ConjuntoResult.Erro(e.message ?: "Expressão inválida")
        }
    }

    // ── Tokenizer ─────────────────────────────────────────────────────────────

    private sealed class Token {
        data class Nome(val name: String) : Token()
        object LParen : Token()
        object RParen : Token()
        object Uniao : Token()
        object Inter : Token()
        object Diff  : Token()
        object Comp  : Token()
    }

    private fun tokenizar(expr: String): List<Token> {
        val result = mutableListOf<Token>()
        var i = 0
        while (i < expr.length) {
            when {
                expr[i].isWhitespace() -> i++
                expr[i] == '(' -> { result += Token.LParen; i++ }
                expr[i] == ')' -> { result += Token.RParen; i++ }
                expr[i] == '∪' -> { result += Token.Uniao; i++ }
                expr[i] == '∩' -> { result += Token.Inter; i++ }
                expr[i] == '−' || expr[i] == '-' -> { result += Token.Diff; i++ }
                expr[i] == 'ᶜ' || (expr[i] == '^' && i+1 < expr.length && expr[i+1] == 'c') -> {
                    result += Token.Comp
                    i += if (expr[i] == 'ᶜ') 1 else 2
                }
                expr[i].isLetter() -> {
                    var j = i + 1
                    while (j < expr.length && expr[j].isLetterOrDigit() && expr[j] != 'ᶜ') j++
                    val name = expr.substring(i, j)
                    result += Token.Nome(name)
                    i = j
                }
                else -> i++
            }
        }
        return result
    }

    // ── Parser recursivo descendente ──────────────────────────────────────────
    // Precedência: ᶜ (mais alta) > ∩ > − > ∪ (mais baixa)

    private class ExprParser(
        private val tokens: List<Token>,
        private val conj: Map<String, Set<String>>,
    ) {
        private var pos = 0
        private val steps = mutableListOf<Step>()

        private fun peek() = tokens.getOrNull(pos)
        private fun consume() = tokens[pos++]

        fun parseExpr(): Pair<Set<String>, List<Step>> {
            val result = parseUnion()
            return Pair(result, steps)
        }

        private fun parseUnion(): Set<String> {
            var left = parseDiff()
            while (peek() == Token.Uniao) {
                consume()
                val right = parseDiff()
                val prev = left
                left = prev + right
                steps += Step("∪", prev, right, left)
            }
            return left
        }

        private fun parseDiff(): Set<String> {
            var left = parseInter()
            while (peek() == Token.Diff) {
                consume()
                val right = parseInter()
                val prev = left
                left = prev - right
                steps += Step("−", prev, right, left)
            }
            return left
        }

        private fun parseInter(): Set<String> {
            var left = parseComp()
            while (peek() == Token.Inter) {
                consume()
                val right = parseComp()
                val prev = left
                left = prev.intersect(right)
                steps += Step("∩", prev, right, left)
            }
            return left
        }

        private fun parseComp(): Set<String> {
            var atom = parseAtom()
            while (peek() == Token.Comp) {
                consume()
                val u = conj["U"] ?: emptySet()
                val prev = atom
                atom = u - atom
                steps += Step("ᶜ", prev, null, atom)
            }
            return atom
        }

        private fun parseAtom(): Set<String> {
            return when (val t = peek()) {
                is Token.LParen -> {
                    consume()
                    val inner = parseUnion()
                    if (peek() == Token.RParen) consume()
                    inner
                }
                is Token.Nome -> {
                    consume()
                    conj[t.name] ?: throw IllegalArgumentException(
                        "Conjunto '${t.name}' não definido"
                    )
                }
                else -> throw IllegalArgumentException(
                    "Token inesperado: $t na posição $pos"
                )
            }
        }
    }
}
