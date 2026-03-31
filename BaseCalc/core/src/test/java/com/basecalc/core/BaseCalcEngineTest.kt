package com.basecalc.core

import com.basecalc.core.engine.BaseCalcEngineImpl
import com.basecalc.core.number.ConjuntoClassifier
import com.basecalc.core.number.ConjuntoNumerico
import com.basecalc.core.number.Rational
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
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

    @Test
    fun `avalia potenciacao`() {
        val result = engine.evaluateExpression("2^3")
        assertTrue(result.ok)
        assertEquals(Rational.of(BigInteger.valueOf(8), BigInteger.ONE), result.value)
    }

    @Test
    fun `avalia potenciacao com base decimal`() {
        val result = engine.evaluateExpression("3^2")
        assertTrue(result.ok)
        assertEquals(Rational.of(BigInteger.valueOf(9), BigInteger.ONE), result.value)
    }

    @Test
    fun `avalia expressao com parenteses`() {
        val result = engine.evaluateExpression("(2+1)*3")
        assertTrue(result.ok)
        assertEquals(Rational.of(BigInteger.valueOf(9), BigInteger.ONE), result.value)
    }

    @Test
    fun `avalia subtracao`() {
        val result = engine.evaluateExpression("10-3")
        assertTrue(result.ok)
        assertEquals(Rational.of(BigInteger.valueOf(7), BigInteger.ONE), result.value)
    }

    @Test
    fun `avalia modulo`() {
        val result = engine.evaluateExpression("10%3")
        assertTrue(result.ok)
        assertEquals(Rational.of(BigInteger.valueOf(1), BigInteger.ONE), result.value)
    }

    @Test
    fun `detecta erro de divisao por zero`() {
        val result = engine.evaluateExpression("5/0")
        assertFalse(result.ok)
        assertNotNull(result.error)
    }

    @Test
    fun `converte numero negativo`() {
        val result = engine.evaluateExpression("-5+2")
        assertTrue(result.ok)
        val base10 = result.bases.first { it.base == 10 }
        assertEquals("-3", base10.intPart)
    }

    @Test
    fun `converte para base 16`() {
        val result = engine.evaluateExpression("255")
        val base16 = result.bases.first { it.base == 16 }
        assertEquals("FF", base16.intPart)
    }

    @Test
    fun `classifica numero natural`() {
        val natural = Rational.of(BigInteger.valueOf(5), BigInteger.ONE)
        assertEquals(ConjuntoNumerico.NATURAL, ConjuntoClassifier.classificar(natural))
    }

    @Test
    fun `classifica numero inteiro negativo`() {
        val inteiroNeg = Rational.of(BigInteger.valueOf(-3), BigInteger.ONE)
        assertEquals(ConjuntoNumerico.INTEIRO, ConjuntoClassifier.classificar(inteiroNeg))
    }

    @Test
    fun `classifica numero racional`() {
        val racional = Rational.of(BigInteger.valueOf(3), BigInteger.valueOf(4))
        assertEquals(ConjuntoNumerico.RACIONAL, ConjuntoClassifier.classificar(racional))
    }

    @Test
    fun `converte para base binaria`() {
        val result = engine.evaluateExpression("42")
        val base2 = result.bases.first { it.base == 2 }
        assertEquals("101010", base2.intPart)
    }

    @Test
    fun `converte para base octal`() {
        val result = engine.evaluateExpression("64")
        val base8 = result.bases.first { it.base == 8 }
        assertEquals("100", base8.intPart)
    }

    @Test
    fun `avalia multiplicacao`() {
        val result = engine.evaluateExpression("4*5")
        assertTrue(result.ok)
        assertEquals(Rational.of(BigInteger.valueOf(20), BigInteger.ONE), result.value)
    }

    @Test
    fun `avalia divisao`() {
        val result = engine.evaluateExpression("15/3")
        assertTrue(result.ok)
        assertEquals(Rational.of(BigInteger.valueOf(5), BigInteger.ONE), result.value)
    }

    @Test
    fun `converte base diferentes`() {
        val result = engine.evaluateExpression("FF", 16)
        assertTrue(result.ok)
        val base10 = result.bases.first { it.base == 10 }
        assertEquals("255", base10.intPart)
    }
}
