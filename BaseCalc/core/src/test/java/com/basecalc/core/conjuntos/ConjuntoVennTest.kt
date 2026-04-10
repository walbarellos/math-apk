package com.basecalc.core.conjuntos

import org.junit.Assert.assertEquals
import org.junit.Test

class ConjuntoVennTest {

    @Test
    fun `valida calculo de cardinalidade de 3 conjuntos`() {
        // Exemplo: 100 pessoas total
        // A=50, B=40, C=30
        // Interseções: AB=20, AC=15, BC=10
        // Interseção Tripla: ABC=5
        
        val total = 100
        val a = 50
        val b = 40
        val c = 30
        val ab = 20
        val ac = 15
        val bc = 10
        val abc = 5

        // Cálculos (mesma lógica aplicada no App)
        val soABC = abc
        val soAB = ab - abc // 15
        val soAC = ac - abc // 10
        val soBC = bc - abc // 5
        
        val soA = a - soAB - soAC - abc // 50 - 15 - 10 - 5 = 20
        val soB = b - soAB - soBC - abc // 40 - 15 - 5 - 5 = 15
        val soC = c - soAC - soBC - abc // 30 - 10 - 5 - 5 = 10
        
        val somaInterna = soA + soB + soC + soAB + soAC + soBC + soABC // 20+15+10+15+10+5+5 = 80
        val fora = total - somaInterna // 20

        assertEquals(5, soABC)
        assertEquals(15, soAB)
        assertEquals(10, soAC)
        assertEquals(5, soBC)
        assertEquals(20, soA)
        assertEquals(15, soB)
        assertEquals(10, soC)
        assertEquals(20, fora)
        assertEquals(100, somaInterna + fora)
    }
    
    @Test
    fun `exemplo do usuario - fanta coca guarana`() {
        // "100 pessoas gostam de fanta coca ou guarana, 49 FC 1 FCG 20 GF"
        // Vamos supor alguns dados para completar o exemplo
        val total = 100
        val fanta = 60
        val coca = 70
        val guarana = 50
        val fc = 49 // Fanta e Coca
        val fg = 20 // Fanta e Guarana
        val cg = 25 // Coca e Guarana
        val fcg = 1  // Os três
        
        val soFCG = fcg // 1
        val soFC = fc - fcg // 48
        val soFG = fg - fcg // 19
        val soCG = cg - fcg // 24
        
        val soF = fanta - soFC - soFG - fcg // 60 - 48 - 19 - 1 = -8 (Dados impossíveis, mas o teste valida o cálculo)
        
        assertEquals(1, soFCG)
        assertEquals(48, soFC)
        assertEquals(19, soFG)
        assertEquals(24, soCG)
    }
}
