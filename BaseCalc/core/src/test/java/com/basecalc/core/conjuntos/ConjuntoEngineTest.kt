package com.basecalc.core.conjuntos

import org.junit.Assert.*
import org.junit.Test

class ConjuntoEngineTest {

    // ── Conjuntos da lista do professor ──────────────────────────────────────
    private val U = setOf("0","1","2","3","4","5","6","7","8","9","10","11")
    private val A = setOf("1","3","5","7","9","11")
    private val B = setOf("0","2","4","6","8","10")
    private val C = setOf("2","3","5","7","11")
    private val D = setOf("0","3","6","9")
    private val E = setOf("0","4","8")
    private val conj = mapOf("U" to U, "A" to A, "B" to B, "C" to C, "D" to D, "E" to E)

    private fun eval(expr: String) =
        (ConjuntoEngine.avaliar(expr, conj) as ConjuntoResult.Ok).result

    // ── Parse de conjunto ─────────────────────────────────────────────────────

    @Test fun `parse conjunto vazio`() {
        assertEquals(emptySet<String>(), ConjuntoEngine.parseSet(""))
    }

    @Test fun `parse conjunto com espacos`() {
        assertEquals(setOf("1","3","5"), ConjuntoEngine.parseSet(" 1 , 3 , 5 "))
    }

    @Test fun `format conjunto vazio retorna simbolo`() {
        assertEquals("∅", ConjuntoEngine.formatSet(emptySet()))
    }

    @Test fun `format conjunto ordenado numericamente`() {
        val s = setOf("10","2","1","9")
        val fmt = ConjuntoEngine.formatSet(s)
        assertEquals("{1, 2, 9, 10}", fmt)
    }

    // ── Operações básicas ─────────────────────────────────────────────────────

    @Test fun `uniao A e B e igual a U`() {
        assertEquals(U, ConjuntoEngine.uniao(A, B))
    }

    @Test fun `interseccao A e B e vazio`() {
        assertEquals(emptySet<String>(), ConjuntoEngine.interseccao(A, B))
    }

    @Test fun `diferenca B menos E`() {
        assertEquals(setOf("2","6","10"), ConjuntoEngine.diferenca(B, E))
    }

    @Test fun `complementar de A em U e B`() {
        assertEquals(B, ConjuntoEngine.complementar(A, U))
    }

    // ── Exercícios da lista ────────────────────────────────────────────────────

    @Test fun `3a C uniao D`() {
        assertEquals(setOf("0","2","3","5","6","7","9","11"), eval("C∪D"))
    }

    @Test fun `3b A uniao D`() {
        assertEquals(setOf("1","3","5","7","9","11","0","6"), eval("A∪D"))
    }

    @Test fun `3c B menos E`() {
        assertEquals(setOf("2","6","10"), eval("B−E"))
    }

    @Test fun `3d A menos C`() {
        assertEquals(setOf("1","9"), eval("A−C"))
    }

    @Test fun `3e C menos A`() {
        assertEquals(setOf("2"), eval("C−A"))
    }

    @Test fun `3f B uniao D uniao E`() {
        assertEquals(setOf("0","2","4","6","8","10","3","9"), eval("B∪D∪E"))
    }

    @Test fun `3g parens B menos E interseccao C`() {
        val bMenosE = setOf("2","6","10")
        assertEquals(ConjuntoEngine.interseccao(bMenosE, C), eval("(B−E)∩C"))
    }

    @Test fun `3h A uniao parens C interseccao D`() {
        val cInterD = setOf("3","9")
        assertEquals(ConjuntoEngine.uniao(A, cInterD), eval("A∪(C∩D)"))
    }

    @Test fun `3i parens A menos C uniao parens B menos E`() {
        val aMenosC = setOf("1","9")
        val bMenosE = setOf("2","6","10")
        assertEquals(ConjuntoEngine.uniao(aMenosC, bMenosE), eval("(A−C)∪(B−E)"))
    }

    @Test fun `3j parens C inter D uniao parens A menos C`() {
        val cInterD = setOf("3","9")
        val aMenosC = setOf("1","9")
        assertEquals(ConjuntoEngine.uniao(cInterD, aMenosC), eval("(C∩D)∪(A−C)"))
    }

    // ── Complementar ──────────────────────────────────────────────────────────

    @Test fun `2a complementar de E`() {
        val esperado = U - E
        assertEquals(esperado, eval("Eᶜ"))
    }

    @Test fun `2b complementar de B e igual a A`() {
        assertEquals(A, eval("Bᶜ"))
    }

    @Test fun `2c complementar do complementar de A e igual a A`() {
        assertEquals(A, eval("(Aᶜ)ᶜ"))
    }

    // ── Pertinência ───────────────────────────────────────────────────────────

    @Test fun `4 pertence a B`() {
        val r = ConjuntoEngine.verificarPertinencia("4", "B", B)
        assertTrue(r.pertence)
        assertEquals("4 ∈ B", r.notacao)
    }

    @Test fun `12 nao pertence a E`() {
        val r = ConjuntoEngine.verificarPertinencia("12", "E", E)
        assertFalse(r.pertence)
        assertEquals("12 ∉ E", r.notacao)
    }

    @Test fun `11 pertence a C`() {
        val r = ConjuntoEngine.verificarPertinencia("11", "C", C)
        assertTrue(r.pertence)
    }

    @Test fun `6 nao pertence a D`() {
        // 6 ∈ D = {0,3,6,9} → pertence sim
        val r = ConjuntoEngine.verificarPertinencia("6", "D", D)
        assertTrue(r.pertence)
    }

    // ── Subconjunto ───────────────────────────────────────────────────────────

    @Test fun `4a E subconjunto de B`() {
        val r = ConjuntoEngine.verificarSubconjunto("E", E, "B", B)
        assertTrue(r.contido)
        assertEquals("E ⊂ B", r.notacao)
    }

    @Test fun `4b C nao e subconjunto de A`() {
        val r = ConjuntoEngine.verificarSubconjunto("C", C, "A", A)
        assertFalse(r.contido)
        assertEquals("C ⊄ A", r.notacao)
        assertTrue(r.elementosFaltando.isNotEmpty())
    }

    @Test fun `todo conjunto e subconjunto de U`() {
        for ((nome, s) in conj) {
            if (nome == "U") continue
            val r = ConjuntoEngine.verificarSubconjunto(nome, s, "U", U)
            assertTrue("$nome deve ser ⊂ U", r.contido)
        }
    }

    @Test fun `conjunto vazio e subconjunto de qualquer conjunto`() {
        val r = ConjuntoEngine.verificarSubconjunto("∅", emptySet(), "A", A)
        assertTrue(r.contido)
    }

    // ── Questões verdadeiro/falso da lista ────────────────────────────────────

    @Test fun `4c B inter E igual E`() {
        val bInterE = ConjuntoEngine.interseccao(B, E)
        assertEquals(E, bInterE)
    }

    @Test fun `4d complementar de A uniao B igual Ac inter Bc`() {
        val aUb = ConjuntoEngine.uniao(A, B)
        val compAuB = ConjuntoEngine.complementar(aUb, U)
        val compA = ConjuntoEngine.complementar(A, U)
        val compB = ConjuntoEngine.complementar(B, U)
        val compAinterCompB = ConjuntoEngine.interseccao(compA, compB)
        // De Morgan: (A∪B)ᶜ = Aᶜ∩Bᶜ
        assertEquals(compAuB, compAinterCompB)
    }

    @Test fun `4e complementar de A inter C igual Ac uniao Cc`() {
        val aInterC = ConjuntoEngine.interseccao(A, C)
        val compAinterC = ConjuntoEngine.complementar(aInterC, U)
        val compA = ConjuntoEngine.complementar(A, U)
        val compC = ConjuntoEngine.complementar(C, U)
        val compAunionCompC = ConjuntoEngine.uniao(compA, compC)
        // De Morgan: (A∩C)ᶜ = Aᶜ∪Cᶜ
        assertEquals(compAinterC, compAunionCompC)
    }

    @Test fun `4f complementar do universo e o vazio`() {
        val compU = ConjuntoEngine.complementar(U, U)
        assertEquals(emptySet<String>(), compU)
    }

    @Test fun `4g complementar de B e igual a A`() {
        val compB = ConjuntoEngine.complementar(B, U)
        assertEquals(A, compB)
    }

    // ── Erro de expressão inválida ─────────────────────────────────────────────

    @Test fun `conjunto nao definido retorna erro`() {
        val r = ConjuntoEngine.avaliar("A∪Z", conj)
        assertTrue(r is ConjuntoResult.Erro)
    }

    @Test fun `expressao vazia retorna erro ou vazio`() {
        val r = ConjuntoEngine.avaliar("", conj)
        assertTrue(r is ConjuntoResult.Erro)
    }
}
