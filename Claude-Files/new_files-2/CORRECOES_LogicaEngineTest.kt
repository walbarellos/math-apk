// ============================================================
// ARQUIVO: LogicaEngineTest.kt
// CORREÇÕES A APLICAR (1 mudança + novos testes parametrizados)
// ============================================================

// ── CORREÇÃO 1 ───────────────────────────────────────────────
// PROBLEMA: assertEquals(4, tokens.size) está errado.
// p<->q → [Var("p"), Bic, Var("q")] = 3 tokens, não 4.
//
// SUBSTITUIR:
//   assertEquals(4, tokens.size)
// POR:
//   assertEquals(3, tokens.size)


// ── NOVOS TESTES — adicionar ao final da classe ──────────────

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
    // p ∨ ¬p é tautologia
    val r = LogicaEngine.avaliar("p ∨ ¬p")
    assertTrue(r.ok)
    assertEquals(TipoFormula.TAUTOLOGIA, r.tipo)
    assertTrue(r.colunas.first { it.isResposta }.valores.all { it })
}

@Test
fun `contradicao detectada corretamente`() {
    // p ∧ ¬p é contradição
    val r = LogicaEngine.avaliar("p ∧ ¬p")
    assertTrue(r.ok)
    assertEquals(TipoFormula.CONTRADICAO, r.tipo)
    assertTrue(r.colunas.first { it.isResposta }.valores.none { it })
}

@Test
fun `formula complexa com parenteses`() {
    // (p → q) ↔ (¬p ∨ q) — equivalência lógica clássica = tautologia
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
fun `formula invalida nao crasha`() {
    // Parêntese sem fechar — engine deve retornar ok=false OU lista vazia,
    // nunca jogar exceção não tratada.
    try {
        val r = LogicaEngine.avaliar("p ∧ (q")
        // Se retornou, colunas devem estar vazias ou ok=false
        assertTrue(!r.ok || r.colunas.isEmpty())
    } catch (e: Exception) {
        fail("Engine não deveria propagar exceção: ${e.message}")
    }
}
