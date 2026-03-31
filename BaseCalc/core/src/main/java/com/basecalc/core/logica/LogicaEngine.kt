package com.basecalc.core.logica

// ─── Tipos de resultado ───────────────────────────────────────────────────────

enum class TipoFormula(val label: String, val emoji: String) {
    TAUTOLOGIA("Tautologia", "✓"),
    CONTRADICAO("Contradição", "✗"),
    CONTINGENCIA("Contingência", "~"),
}

data class ColunaLogica(
    val expressao: String,
    val valores: List<Boolean>,
    val isVariavel: Boolean = false,
    val isResposta: Boolean = false,
)

data class TabelaVerdade(
    val variaveis: List<String>,
    val colunas: List<ColunaLogica>,
    val tipo: TipoFormula,
    val passos: List<String>,
)

// ─── Tokens ───────────────────────────────────────────────────────────────────

sealed class Token {
    data class Var(val nome: String) : Token()
    object Neg : Token() { override fun toString() = "¬" }
    object And : Token() { override fun toString() = "∧" }
    object Or  : Token() { override fun toString() = "∨" }
    object Xor : Token() { override fun toString() = "⊕" }
    object Impl : Token() { override fun toString() = "→" }
    object Bic  : Token() { override fun toString() = "↔" }
    object LP   : Token() { override fun toString() = "(" }
    object RP   : Token() { override fun toString() = ")" }
}

fun tokenizar(input: String): List<Token> {
    val tokens = mutableListOf<Token>()
    var i = 0
    while (i < input.length) {
        when {
            input[i].isWhitespace() -> i++
            input[i].isLetter() -> {
                // variáveis: p, q, r, p1, q2...
                var j = i + 1
                while (j < input.length && (input[j].isLetterOrDigit() || input[j] == '_')) j++
                tokens += Token.Var(input.substring(i, j))
                i = j
            }
            input[i] in "¬~!" -> { tokens += Token.Neg; i++ }
            input[i] in "∧&" -> { tokens += Token.And; i++ }
            input[i] == '∨' -> { tokens += Token.Or; i++ }
            input[i] == '⊕' -> { tokens += Token.Xor; i++ }
            input[i] == '→' -> { tokens += Token.Impl; i++ }
            input[i] == '↔' -> { tokens += Token.Bic; i++ }
            input[i] == '(' -> { tokens += Token.LP; i++ }
            input[i] == ')' -> { tokens += Token.RP; i++ }
            // Alternativas ASCII
            input[i] == '-' && i + 1 < input.length && input[i + 1] == '>' -> { tokens += Token.Impl; i += 2 }
            input[i] == '<' && i + 2 < input.length && input[i+1] == '-' && input[i+2] == '>' -> { tokens += Token.Bic; i += 3 }
            input[i] == '|' -> { tokens += Token.Or; i++ }
            input[i] == 'v' && (i == 0 || !input[i-1].isLetter()) -> { tokens += Token.Or; i++ }  // 'v' isolado como ∨
            else -> i++
        }
    }
    return tokens
}

// ─── AST ─────────────────────────────────────────────────────────────────────

sealed class Expr {
    abstract fun eval(env: Map<String, Boolean>): Boolean
    abstract fun label(): String

    data class Var(val nome: String) : Expr() {
        override fun eval(env: Map<String, Boolean>) = env[nome] ?: false
        override fun label() = nome
    }

    data class Neg(val expr: Expr) : Expr() {
        override fun eval(env: Map<String, Boolean>) = !expr.eval(env)
        override fun label() = if (expr is Var) "¬${expr.label()}" else "¬(${expr.label()})"
    }

    data class And(val esq: Expr, val dir: Expr) : Expr() {
        override fun eval(env: Map<String, Boolean>) = esq.eval(env) && dir.eval(env)
        override fun label() = "${par(esq)} ∧ ${par(dir)}"
    }

    data class Or(val esq: Expr, val dir: Expr) : Expr() {
        override fun eval(env: Map<String, Boolean>) = esq.eval(env) || dir.eval(env)
        override fun label() = "${par(esq)} ∨ ${par(dir)}"
    }

    data class Xor(val esq: Expr, val dir: Expr) : Expr() {
        override fun eval(env: Map<String, Boolean>) = esq.eval(env) xor dir.eval(env)
        override fun label() = "${par(esq)} ⊕ ${par(dir)}"
    }

    data class Impl(val ant: Expr, val con: Expr) : Expr() {
        override fun eval(env: Map<String, Boolean>) = !ant.eval(env) || con.eval(env)
        override fun label() = "${par(ant)} → ${par(con)}"
    }

    data class Bic(val esq: Expr, val dir: Expr) : Expr() {
        override fun eval(env: Map<String, Boolean>): Boolean {
            val a = esq.eval(env); val b = dir.eval(env)
            return (a && b) || (!a && !b)
        }
        override fun label() = "${par(esq)} ↔ ${par(dir)}"
    }
}

private fun par(e: Expr): String = when (e) {
    is Expr.Var, is Expr.Neg -> e.label()
    else -> "(${e.label()})"
}

// ─── Parser (descida recursiva) ───────────────────────────────────────────────

class Parser(private val tokens: List<Token>) {
    private var pos = 0

    fun parse(): Expr {
        val e = parseBic()
        if (pos < tokens.size) throw IllegalArgumentException("Token inesperado na posição $pos: ${tokens[pos]}")
        return e
    }

    // Precedência (menor → maior):
    // ↔  →  ∨  ⊕  ∧  ¬  átomo

    private fun parseBic(): Expr {
        var esq = parseImpl()
        while (peek() is Token.Bic) { consume(); esq = Expr.Bic(esq, parseImpl()) }
        return esq
    }

    private fun parseImpl(): Expr {
        val esq = parseOr()
        return if (peek() is Token.Impl) { consume(); Expr.Impl(esq, parseImpl()) } else esq  // assoc. direita
    }

    private fun parseOr(): Expr {
        var esq = parseXor()
        while (peek() is Token.Or) { consume(); esq = Expr.Or(esq, parseXor()) }
        return esq
    }

    private fun parseXor(): Expr {
        var esq = parseAnd()
        while (peek() is Token.Xor) { consume(); esq = Expr.Xor(esq, parseAnd()) }
        return esq
    }

    private fun parseAnd(): Expr {
        var esq = parseNeg()
        while (peek() is Token.And) { consume(); esq = Expr.And(esq, parseNeg()) }
        return esq
    }

    private fun parseNeg(): Expr =
        if (peek() is Token.Neg) { consume(); Expr.Neg(parseNeg()) } else parseAtom()

    private fun parseAtom(): Expr = when (val t = peek()) {
        is Token.Var -> { consume(); Expr.Var(t.nome) }
        is Token.LP -> {
            consume()
            val e = parseBic()
            if (peek() !is Token.RP) throw IllegalArgumentException("Esperado ')'")
            consume()
            e
        }
        else -> throw IllegalArgumentException("Esperado variável ou '(', encontrou: $t na posição $pos")
    }

    private fun peek() = tokens.getOrNull(pos)
    private fun consume() = tokens[pos++]
}

// ─── Coleta sub-expressões (post-order, sem duplicatas) ───────────────────────

private fun coletarSubExprs(expr: Expr): List<Expr> {
    val vistos = linkedSetOf<String>()
    val lista = mutableListOf<Expr>()

    fun visitar(e: Expr) {
        when (e) {
            is Expr.Var -> { /* variáveis são colunas separadas */ }
            is Expr.Neg -> {
                visitar(e.expr)
                if (vistos.add(e.label())) lista.add(e)
            }
            is Expr.And -> { visitar(e.esq); visitar(e.dir); if (vistos.add(e.label())) lista.add(e) }
            is Expr.Or  -> { visitar(e.esq); visitar(e.dir); if (vistos.add(e.label())) lista.add(e) }
            is Expr.Xor -> { visitar(e.esq); visitar(e.dir); if (vistos.add(e.label())) lista.add(e) }
            is Expr.Impl -> { visitar(e.ant); visitar(e.con); if (vistos.add(e.label())) lista.add(e) }
            is Expr.Bic  -> { visitar(e.esq); visitar(e.dir); if (vistos.add(e.label())) lista.add(e) }
        }
    }
    visitar(expr)
    return lista
}

private fun extrairVariaveis(expr: Expr): List<String> {
    val vars = linkedSetOf<String>()
    fun v(e: Expr) {
        when (e) {
            is Expr.Var  -> vars.add(e.nome)
            is Expr.Neg  -> v(e.expr)
            is Expr.And  -> { v(e.esq); v(e.dir) }
            is Expr.Or   -> { v(e.esq); v(e.dir) }
            is Expr.Xor  -> { v(e.esq); v(e.dir) }
            is Expr.Impl -> { v(e.ant); v(e.con) }
            is Expr.Bic  -> { v(e.esq); v(e.dir) }
        }
    }
    v(expr)
    return vars.sorted()
}

// ─── Engine pública ───────────────────────────────────────────────────────────

object LogicaEngine {

    /**
     * Avalia [formula] e retorna a tabela-verdade completa com todas as colunas
     * intermediárias em ordem de resolução (seguindo precedência da aula).
     *
     * Lança [IllegalArgumentException] com mensagem legível em caso de erro de sintaxe.
     */
    fun avaliar(formula: String): TabelaVerdade {
        val tokens = tokenizar(formula)
        if (tokens.isEmpty()) throw IllegalArgumentException("Fórmula vazia")
        val ast = Parser(tokens).parse()
        val variaveis = extrairVariaveis(ast)
        if (variaveis.isEmpty()) throw IllegalArgumentException("Nenhuma variável encontrada")

        val n = variaveis.size
        val numLinhas = 1 shl n   // 2^n

        // Atribuições: linha i → mapa variável→valor
        val atribuicoes: List<Map<String, Boolean>> = (0 until numLinhas).map { i ->
            variaveis.mapIndexed { idx, v ->
                v to ((i shr (n - 1 - idx)) and 1 == 1)
            }.toMap()
        }

        val colunas = mutableListOf<ColunaLogica>()

        // 1. Colunas das variáveis (sempre primeiro)
        variaveis.forEach { v ->
            colunas += ColunaLogica(
                expressao = v,
                valores = atribuicoes.map { it[v]!! },
                isVariavel = true,
            )
        }

        // 2. Colunas das sub-expressões em post-order
        val subExprs = coletarSubExprs(ast)
        val labelRaiz = ast.label()

        subExprs.forEach { sub ->
            colunas += ColunaLogica(
                expressao = sub.label(),
                valores = atribuicoes.map { sub.eval(it) },
                isResposta = sub.label() == labelRaiz,
            )
        }

        // Garante que a raiz sempre está como coluna resposta
        if (colunas.none { it.isResposta }) {
            colunas += ColunaLogica(
                expressao = labelRaiz,
                valores = atribuicoes.map { ast.eval(it) },
                isResposta = true,
            )
        }

        val valoresResposta = colunas.last { it.isResposta }.valores
        val tipo = when {
            valoresResposta.all { it }   -> TipoFormula.TAUTOLOGIA
            valoresResposta.none { it }  -> TipoFormula.CONTRADICAO
            else                         -> TipoFormula.CONTINGENCIA
        }

        val passos = gerarPassos(colunas, variaveis, tipo)

        return TabelaVerdade(variaveis, colunas, tipo, passos)
    }

    private fun gerarPassos(
        colunas: List<ColunaLogica>,
        variaveis: List<String>,
        tipo: TipoFormula,
    ): List<String> = buildList {
        add("Passo 1 — Preencher variáveis: ${variaveis.joinToString(", ")} (${1 shl variaveis.size} combinações)")

        val negacoesSimples = colunas.filter {
            !it.isVariavel && it.expressao.startsWith("¬") &&
                it.expressao.drop(1).trimStart('(').trimEnd(')') in variaveis
        }
        if (negacoesSimples.isNotEmpty()) {
            add("Passo 2 — Resolver negações diretas: ${negacoesSimples.joinToString(", ") { it.expressao }}")
        }

        val intermediarias = colunas.filter { !it.isVariavel && !it.isResposta && it !in negacoesSimples }
        intermediarias.forEachIndexed { idx, col ->
            add("Passo ${idx + 3} — Calcular: ${col.expressao}")
        }

        val resposta = colunas.last { it.isResposta }
        add("Coluna Resposta: ${resposta.expressao}")
        add("Resultado: ${tipo.label} ${tipo.emoji} — ${
            when (tipo) {
                TipoFormula.TAUTOLOGIA   -> "sempre Verdadeiro"
                TipoFormula.CONTRADICAO  -> "sempre Falso"
                TipoFormula.CONTINGENCIA -> "depende dos valores"
            }
        }")
    }
}
