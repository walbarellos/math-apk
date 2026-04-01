// ============================================================
// ARQUIVO: LogicaEngine.kt
// CORREÇÕES A APLICAR (3 mudanças cirúrgicas)
// ============================================================

// ── CORREÇÃO 1 ───────────────────────────────────────────────
// PROBLEMA: isLetter() captura 'v' antes da regra de OR.
// ONDE: função tokenizar(), dentro do while(i < input.length)
//
// SUBSTITUIR o bloco inteiro do when{} por este (ordem correta):

fun tokenizar(input: String): List<Token> {
    val tokens = mutableListOf<Token>()
    var i = 0
    while (i < input.length) {
        when {
            input[i].isWhitespace() -> i++

            // ── Operadores ASCII e Unicode PRIMEIRO (antes de variáveis) ──

            // Negação
            input[i] in "¬~!" -> { tokens += Token.Neg; i++ }

            // Conjunção
            input[i] in "∧&" -> { tokens += Token.And; i++ }

            // Disjunção unicode e pipe
            input[i] == '∨' -> { tokens += Token.Or; i++ }
            input[i] == '|' -> { tokens += Token.Or; i++ }

            // 'v' isolado como ∨ — checar ANTES do bloco de variáveis
            input[i] == 'v' && (i == 0 || !Character.isLetterOrDigit(input[i - 1])) &&
                    (i + 1 >= input.length || !Character.isLetterOrDigit(input[i + 1])) -> {
                tokens += Token.Or; i++
            }

            // XOR
            input[i] == '⊕' -> { tokens += Token.Xor; i++ }

            // Implicação unicode e ASCII (-> precisa checar 2 chars)
            input[i] == '→' -> { tokens += Token.Impl; i++ }
            input[i] == '-' && i + 1 < input.length && input[i + 1] == '>' -> {
                tokens += Token.Impl; i += 2
            }

            // Bicondicional unicode e ASCII (<-> são 3 chars)
            input[i] == '↔' -> { tokens += Token.Bic; i++ }
            input[i] == '<' && i + 2 < input.length
                    && input[i + 1] == '-' && input[i + 2] == '>' -> {
                tokens += Token.Bic; i += 3
            }

            // Parênteses
            input[i] == '(' -> { tokens += Token.LP; i++ }
            input[i] == ')' -> { tokens += Token.RP; i++ }

            // ── Variáveis POR ÚLTIMO ──
            input[i].isLetter() -> {
                var j = i + 1
                while (j < input.length && (input[j].isLetterOrDigit() || input[j] == '_')) j++
                tokens += Token.Var(input.substring(i, j))
                i = j
            }

            else -> i++ // ignora caracteres desconhecidos
        }
    }
    return tokens
}

// ── CORREÇÃO 2 ───────────────────────────────────────────────
// PROBLEMA: Tabela gera linhas na ordem FF→VV, testes esperam VV→FF.
// ONDE: função avaliar(), no bloco que monta atribuicoes
//
// SUBSTITUIR:
//   val atribuicoes: List<Map<String, Boolean>> = (0 until numLinhas).map { i ->
//       variaveis.mapIndexed { idx, v ->
//           v to ((i shr (n - 1 - idx)) and 1 == 1)    // ← ERRADO
//       }.toMap()
//   }
//
// POR:
val atribuicoes: List<Map<String, Boolean>> = (0 until numLinhas).map { i ->
    // Iterar de numLinhas-1 downTo 0 para gerar VV, VF, FV, FF (padrão BR)
    val row = numLinhas - 1 - i
    variaveis.mapIndexed { idx, v ->
        v to (((row shr (n - 1 - idx)) and 1) == 1)
    }.toMap()
}

// ── CORREÇÃO 3 ───────────────────────────────────────────────
// PROBLEMA: TabelaVerdade não tem campo ok, quebrando o teste.
// ONDE: data class TabelaVerdade
//
// SUBSTITUIR:
//   data class TabelaVerdade(
//       val variaveis: List<String>,
//       val colunas: List<ColunaLogica>,
//       val tipo: TipoFormula,
//       val passos: List<String>,
//   )
//
// POR:
data class TabelaVerdade(
    val variaveis: List<String>,
    val colunas: List<ColunaLogica>,
    val tipo: TipoFormula,
    val passos: List<String>,
    val ok: Boolean = true,   // ← campo adicionado
)
