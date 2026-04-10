package com.basecalc.core.logica

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class LogicaEngineTest {

    // ─── Testes de Tokenização ─────────────────────────────────────────────────

    @Test
    fun `tokeniza negacao com simbolo unicode`() {
        val tokens = tokenizar("¬p")
        assertEquals(2, tokens.size)
        assertTrue(tokens[0] is Token.Neg)
        assertTrue(tokens[1] is Token.Var)
    }

    @Test
    fun `tokeniza negacao com til`() {
        val tokens = tokenizar("~p")
        assertEquals(2, tokens.size)
        assertTrue(tokens[0] is Token.Neg)
    }

    @Test
    fun `tokeniza negacao com exclamacao`() {
        val tokens = tokenizar("!p")
        assertEquals(2, tokens.size)
        assertTrue(tokens[0] is Token.Neg)
    }

    @Test
    fun `tokeniza conectivos AND`() {
        val tokens = tokenizar("p ∧ q")
        assertTrue(tokens[1] is Token.And)
    }

    @Test
    fun `tokeniza conectivo OR com pipe`() {
        val tokens = tokenizar("p|q")
        assertTrue(tokens[1] is Token.Or)
    }

    @Test
    fun `tokeniza implicacao com seta`() {
        val tokens = tokenizar("p→q")
        assertTrue(tokens[1] is Token.Impl)
    }

    @Test
    fun `tokeniza implicacao com ASCII`() {
        val tokens = tokenizar("p->q")
        assertTrue(tokens[1] is Token.Impl)
    }

    @Test
    fun `tokeniza bicondicional com ASCII`() {
        val tokens = tokenizar("p<->q")
        assertEquals(3, tokens.size)
        assertTrue(tokens[1] is Token.Bic)
    }

    @Test
    fun `ignora espacos`() {
        val tokens = tokenizar("p ∧ q")
        assertEquals(3, tokens.size)
    }

    // ─── Testes de Parser ───────────────────────────────────────────────────────

    @Test
    fun `parse variavel simples`() {
        val ast = Parser(tokenizar("p")).parse()
        assertTrue(ast is Expr.Var)
    }

    @Test
    fun `parse negacao`() {
        val ast = Parser(tokenizar("¬p")).parse()
        assertTrue(ast is Expr.Neg)
    }

    @Test
    fun `parse conjuncao`() {
        val ast = Parser(tokenizar("p∧q")).parse()
        assertTrue(ast is Expr.And)
    }

    @Test
    fun `parse disjuncao`() {
        val ast = Parser(tokenizar("p∨q")).parse()
        assertTrue(ast is Expr.Or)
    }

    @Test
    fun `parse implicacao`() {
        val ast = Parser(tokenizar("p→q")).parse()
        assertTrue(ast is Expr.Impl)
    }

    @Test
    fun `parse bicondicional`() {
        val ast = Parser(tokenizar("p↔q")).parse()
        assertTrue(ast is Expr.Bic)
    }

    @Test
    fun `parse expressao com parenteses`() {
        val ast = Parser(tokenizar("(p∨q)∧r")).parse()
        assertTrue(ast is Expr.And)
    }

    @Test
    fun `parse expressao aninhada`() {
        val ast = Parser(tokenizar("((p∧q)∨r)")).parse()
        assertTrue(ast is Expr.Or)
    }

    // ─── Testes de Precedência ──────────────────────────────────────────────────

    @Test
    fun `precedencia NOT antes de AND`() {
        val ast = Parser(tokenizar("¬p∧q")).parse()
        assertTrue(ast is Expr.And)
        val and = ast as Expr.And
        assertTrue(and.esq is Expr.Neg)
    }

    @Test
    fun `precedencia AND antes de OR`() {
        val ast = Parser(tokenizar("p∧q∨r")).parse()
        assertTrue(ast is Expr.Or)
    }

    @Test
    fun `precedencia OR antes de IMPL`() {
        val ast = Parser(tokenizar("p∨q→r")).parse()
        assertTrue(ast is Expr.Impl)
    }

    @Test
    fun `precedencia IMPL antes de BIC`() {
        val ast = Parser(tokenizar("p→q↔r")).parse()
        assertTrue(ast is Expr.Bic)
    }

    // ─── Testes de Avaliação ───────────────────────────────────────────────────

    @Test
    fun `avalia negacao verdadeira`() {
        val ast = Parser(tokenizar("¬p")).parse()
        assertFalse(ast.eval(mapOf("p" to true)))
    }

    @Test
    fun `avalia negacao falsa`() {
        val ast = Parser(tokenizar("¬p")).parse()
        assertTrue(ast.eval(mapOf("p" to false)))
    }

    @Test
    fun `avalia AND ambos verdade`() {
        val ast = Parser(tokenizar("p∧q")).parse()
        assertTrue(ast.eval(mapOf("p" to true, "q" to true)))
    }

    @Test
    fun `avalia AND um falso`() {
        val ast = Parser(tokenizar("p∧q")).parse()
        assertFalse(ast.eval(mapOf("p" to true, "q" to false)))
    }

    @Test
    fun `avalia OR um verdadeiro`() {
        val ast = Parser(tokenizar("p∨q")).parse()
        assertTrue(ast.eval(mapOf("p" to false, "q" to true)))
    }

    @Test
    fun `avalia OR ambos falsos`() {
        val ast = Parser(tokenizar("p∨q")).parse()
        assertFalse(ast.eval(mapOf("p" to false, "q" to false)))
    }

    @Test
    fun `avalia XOR verdadeiro`() {
        val ast = Parser(tokenizar("p⊕q")).parse()
        assertTrue(ast.eval(mapOf("p" to true, "q" to false)))
    }

    @Test
    fun `avalia XOR falso`() {
        val ast = Parser(tokenizar("p⊕q")).parse()
        assertFalse(ast.eval(mapOf("p" to true, "q" to true)))
    }

    @Test
    fun `avalia IMPL verdadeiro`() {
        val ast = Parser(tokenizar("p→q")).parse()
        assertTrue(ast.eval(mapOf("p" to false, "q" to true)))
    }

    @Test
    fun `avalia IMPL falso`() {
        val ast = Parser(tokenizar("p→q")).parse()
        assertFalse(ast.eval(mapOf("p" to true, "q" to false)))
    }

    @Test
    fun `avalia BIC verdadeiro`() {
        val ast = Parser(tokenizar("p↔q")).parse()
        assertTrue(ast.eval(mapOf("p" to true, "q" to true)))
    }

    @Test
    fun `avalia BIC falso`() {
        val ast = Parser(tokenizar("p↔q")).parse()
        assertFalse(ast.eval(mapOf("p" to true, "q" to false)))
    }

    // ─── Testes de Tabela-Verdade ──────────────────────────────────────────────

    @Test
    fun `tabela verdade simples`() {
        val result = LogicaEngine.avaliar("p∧q")
        assertEquals(2, result.variaveis.size)
        assertEquals(listOf("p", "q"), result.variaveis)
        
        val resposta = result.colunas.first { it.isResposta }
        assertEquals(4, resposta.valores.size)
        
        // Ordem VV→FF: VV, VF, FV, FF (padrão brasileiro)
        // p∧q: VV→V, VF→F, FV→F, FF→F
        assertTrue("VV -> V", resposta.valores[0])
        assertFalse("VF -> F", resposta.valores[1])
        assertFalse("FV -> F", resposta.valores[2])
        assertFalse("FF -> F", resposta.valores[3])
    }

    @Test
    fun `tabela verdade com 3 variaveis`() {
        val result = LogicaEngine.avaliar("p∧q∧r")
        assertEquals(3, result.variaveis.size)
        assertEquals(8, result.colunas.first { it.isVariavel }.valores.size)
    }

    @Test
    fun `detecta tautologia`() {
        val result = LogicaEngine.avaliar("p∨¬p")
        assertEquals(TipoFormula.TAUTOLOGIA, result.tipo)
        assertTrue(result.colunas.first { it.isResposta }.valores.all { it })
    }

    @Test
    fun `detecta contradicao`() {
        val result = LogicaEngine.avaliar("p∧¬p")
        assertEquals(TipoFormula.CONTRADICAO, result.tipo)
        assertTrue(result.colunas.first { it.isResposta }.valores.none { it })
    }

    @Test
    fun `detecta contingencia`() {
        val result = LogicaEngine.avaliar("p∧q")
        assertEquals(TipoFormula.CONTINGENCIA, result.tipo)
    }

    @Test
    fun `expressao complexa com parenteses`() {
        val result = LogicaEngine.avaliar("(p∨q)∧¬p")
        assertEquals(TipoFormula.CONTINGENCIA, result.tipo)
    }

    @Test
    fun `silogismo hipotetico`() {
        val result = LogicaEngine.avaliar("(p→q)∧(q→r)→(p→r)")
        assertEquals(TipoFormula.TAUTOLOGIA, result.tipo)
    }

    @Test
    fun `lei de De Morgan`() {
        val result = LogicaEngine.avaliar("¬(p∧q)↔(¬p∨¬q)")
        assertEquals(TipoFormula.TAUTOLOGIA, result.tipo)
    }

    @Test
    fun `colunas intermediarias presentes`() {
        val result = LogicaEngine.avaliar("p∧q∨r")
        val naoResposta = result.colunas.filter { !it.isResposta }
        assertTrue(naoResposta.isNotEmpty())
    }

    @Test
    fun `passos gerados`() {
        val result = LogicaEngine.avaliar("p∧q")
        assertTrue(result.passos.isNotEmpty())
        assertTrue(result.passos.any { it.contains("variáveis") })
    }

    // ─── Testes de Erro ─────────────────────────────────────────────────────────

    @Test
    fun `erro formula vazia`() {
        try {
            LogicaEngine.avaliar("")
            assertTrue("Deveria lançar exceção", false)
        } catch (e: IllegalArgumentException) {
            assertNotNull(e.message)
        }
    }

    @Test
    fun `erro formula sem variavel`() {
        try {
            LogicaEngine.avaliar("¬¬¬")
            assertTrue("Deveria lançar exceção", false)
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.contains("variável"))
        }
    }

    @Test
    fun `erro parenteses desbalanceados`() {
        try {
            LogicaEngine.avaliar("(p∧q")
            assertTrue("Deveria lançar exceção", false)
        } catch (e: IllegalArgumentException) {
            assertNotNull(e.message)
        }
    }

    @Test
    fun `aceita v como OU`() {
        val result = LogicaEngine.avaliar("p v q")
        assertTrue(result.ok)
        assertTrue(result.colunas.isNotEmpty())
    }

    // ─── Testes de tabela verdade por operador ───────────────────

    @Test
    fun `negacao gera tabela correta`() {
        val r = LogicaEngine.avaliar("¬p")
        assertTrue(r.ok)
        val resposta = r.colunas.first { it.isResposta }
        // Ordem VV→FF: p=V→¬p=F, p=F→¬p=V
        assertEquals(listOf(false, true), resposta.valores)
    }

    @Test
    fun `conjuncao gera tabela correta`() {
        val r = LogicaEngine.avaliar("p ∧ q")
        assertTrue(r.ok)
        val resp = r.colunas.first { it.isResposta }
        // VV→V, VF→F, FV→F, FF→F
        assertEquals(listOf(true, false, false, false), resp.valores)
    }

    @Test
    fun `disjuncao gera tabela correta`() {
        val r = LogicaEngine.avaliar("p ∨ q")
        assertTrue(r.ok)
        val resp = r.colunas.first { it.isResposta }
        // VV→V, VF→V, FV→V, FF→F
        assertEquals(listOf(true, true, true, false), resp.valores)
    }

    @Test
    fun `implicacao gera tabela correta`() {
        val r = LogicaEngine.avaliar("p → q")
        assertTrue(r.ok)
        val resp = r.colunas.first { it.isResposta }
        // VV→V, VF→F, FV→V, FF→V
        assertEquals(listOf(true, false, true, true), resp.valores)
    }

    @Test
    fun `bicondicional gera tabela correta`() {
        val r = LogicaEngine.avaliar("p ↔ q")
        assertTrue(r.ok)
        val resp = r.colunas.first { it.isResposta }
        // VV→V, VF→F, FV→F, FF→V
        assertEquals(listOf(true, false, false, true), resp.valores)
    }

    @Test
    fun `xor gera tabela correta`() {
        val r = LogicaEngine.avaliar("p ⊕ q")
        assertTrue(r.ok)
        val resp = r.colunas.first { it.isResposta }
        // VV→F, VF→V, FV→V, FF→F
        assertEquals(listOf(false, true, true, false), resp.valores)
    }

    @Test
    fun `v como or funciona igual unicode`() {
        val r1 = LogicaEngine.avaliar("p v q")
        val r2 = LogicaEngine.avaliar("p ∨ q")
        assertTrue(r1.ok)
        val resp1 = r1.colunas.first { it.isResposta }
        val resp2 = r2.colunas.first { it.isResposta }
        assertEquals(resp2.valores, resp1.valores)
    }

    @Test
    fun `implicacao ascii funciona igual unicode`() {
        val r1 = LogicaEngine.avaliar("p -> q")
        val r2 = LogicaEngine.avaliar("p → q")
        assertTrue(r1.ok)
        assertEquals(
            r2.colunas.first { it.isResposta }.valores,
            r1.colunas.first { it.isResposta }.valores
        )
    }

    @Test
    fun `bicondicional ascii funciona igual unicode`() {
        val r1 = LogicaEngine.avaliar("p <-> q")
        val r2 = LogicaEngine.avaliar("p ↔ q")
        assertTrue(r1.ok)
        assertEquals(
            r2.colunas.first { it.isResposta }.valores,
            r1.colunas.first { it.isResposta }.valores
        )
    }

    @Test
    fun `tautologia detectada corretamente`() {
        val r = LogicaEngine.avaliar("p ∨ ¬p")
        assertTrue(r.ok)
        assertEquals(TipoFormula.TAUTOLOGIA, r.tipo)
        assertTrue(r.colunas.first { it.isResposta }.valores.all { it })
    }

    @Test
    fun `contradicao detectada corretamente`() {
        val r = LogicaEngine.avaliar("p ∧ ¬p")
        assertTrue(r.ok)
        assertEquals(TipoFormula.CONTRADICAO, r.tipo)
        assertTrue(r.colunas.first { it.isResposta }.valores.none { it })
    }

    @Test
    fun `formula complexa com parenteses`() {
        val r = LogicaEngine.avaliar("(p → q) ↔ (¬p ∨ q)")
        assertTrue(r.ok)
        assertEquals(TipoFormula.TAUTOLOGIA, r.tipo)
    }

    @Test
    fun `tres variaveis gera 8 linhas`() {
        val r = LogicaEngine.avaliar("p ∧ q ∧ r")
        assertTrue(r.ok)
        assertEquals(8, r.colunas.first { it.isResposta }.valores.size)
    }

    @Test
    fun `aceita string xor case insensitive`() {
        val r1 = LogicaEngine.avaliar("p XOR q")
        val r2 = LogicaEngine.avaliar("p ⊕ q")
        assertTrue(r1.ok)
        assertEquals(
            r2.colunas.first { it.isResposta }.valores,
            r1.colunas.first { it.isResposta }.valores
        )
    }

    @Test
    fun `aceita chapeu como xor`() {
        val r1 = LogicaEngine.avaliar("p ^ q")
        val r2 = LogicaEngine.avaliar("p ⊕ q")
        assertTrue(r1.ok)
        assertEquals(
            r2.colunas.first { it.isResposta }.valores,
            r1.colunas.first { it.isResposta }.valores
        )
    }

    @Test
    fun `aceita simbolo v com traco como xor`() {
        val r = LogicaEngine.avaliar("p ⊻ q")
        assertTrue(r.ok)
        val resp = r.colunas.first { it.isResposta }
        assertEquals(listOf(false, true, true, false), resp.valores)
    }

    @Test
    fun `formula invalida nao crasha`() {
        try {
            val r = LogicaEngine.avaliar("p ∧ (q")
            // Se chegou aqui, ok deve ser false ou colunas vazias
            assertTrue(!r.ok || r.colunas.isEmpty())
        } catch (e: IllegalArgumentException) {
            // Exceção de parse é aceitável - não é crash
            assertTrue(true)
        } catch (e: Exception) {
            fail("Engine não deveria propagar exceção genérica: ${e.message}")
        }
    }
}