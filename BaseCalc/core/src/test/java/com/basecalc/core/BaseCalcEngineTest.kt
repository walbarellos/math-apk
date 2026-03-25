package com.basecalc.core

import com.basecalc.core.engine.BaseCalcEngineImpl
import com.basecalc.core.number.Rational
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigInteger

class BaseCalcEngineTest {

    private val engine = BaseCalcEngineImpl()

    @Test
    fun `avalia expressao com precedencia`() {
        val result = engine.evaluateExpression("1+2*3")
        assertTrue(result.ok)
        assertEquals(Rational.of(BigInteger.valueOf(7), BigInteger.ONE), result.value)
    }

    @Test
    fun `converte inteiro para base 4`() {
        val result = engine.evaluateExpression("37")
        val base4 = result.bases.first { it.base == 4 }
        assertEquals("211", base4.intPart)
    }

    @Test
    fun `detecta dizima periodica em base 10`() {
        val result = engine.evaluateExpression("1/3")
        val base10 = result.bases.first { it.base == 10 }
        assertTrue(base10.repeats)
        assertEquals("3", base10.repeatPart)
    }

    @Test
    fun `converte fracao simples em binario`() {
        val result = engine.evaluateExpression("0.5")
        val base2 = result.bases.first { it.base == 2 }
        assertEquals("0", base2.intPart)
        assertEquals("1", base2.fracPart)
    }
}
