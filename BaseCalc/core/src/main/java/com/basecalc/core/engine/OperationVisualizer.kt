package com.basecalc.core.engine

import com.basecalc.core.model.OperationColumn
import com.basecalc.core.model.OperationVisual
import com.basecalc.core.util.DigitSymbols
import java.math.BigInteger

/**
 * Gera a visualização passo a passo de operações básicas em uma base específica.
 */
object OperationVisualizer {

    fun generate(expr: String, base: Int): OperationVisual? {
        val pattern = Regex("""^([0-9a-fA-F,.]+)\s*([+\-])\s*([0-9a-fA-F,.]+)$""")
        val match = pattern.find(expr.trim()) ?: return null
        
        val sOp1 = match.groupValues[1]
        val operator = match.groupValues[2]
        val sOp2 = match.groupValues[3]
        
        // Só suportamos inteiros por enquanto no "Carry Visual"
        if (sOp1.contains('.') || sOp1.contains(',') || sOp2.contains('.') || sOp2.contains(',')) return null
        
        return try {
            val vOp1 = BigInteger(sOp1, base)
            val vOp2 = BigInteger(sOp2, base)
            
            if (operator == "+") {
                val vRes = vOp1 + vOp2
                val sRes = vRes.toString(base).uppercase()
                generateAddition(sOp1.uppercase(), sOp2.uppercase(), sRes, base)
            } else {
                if (vOp1 < vOp2) return null // Não suportamos resultado negativo ainda
                val vRes = vOp1 - vOp2
                val sRes = vRes.toString(base).uppercase()
                generateSubtraction(sOp1.uppercase(), sOp2.uppercase(), sRes, base)
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun generateAddition(op1: String, op2: String, res: String, base: Int): OperationVisual {
        val maxLen = maxOf(op1.length, op2.length, res.length)
        val pOp1 = op1.padStart(maxLen, '0')
        val pOp2 = op2.padStart(maxLen, '0')
        val pRes = res.padStart(maxLen, '0')
        
        val columns = mutableListOf<OperationColumn>()
        val carries = IntArray(maxLen + 1) { 0 }
        
        var currentCarry = 0
        for (i in maxLen - 1 downTo 0) {
            val d1 = DigitSymbols.charToDigit(pOp1[i])
            val d2 = DigitSymbols.charToDigit(pOp2[i])
            val sum = d1 + d2 + currentCarry
            
            currentCarry = sum / base
            if (i > 0) {
                carries[i - 1] = currentCarry
            }
        }
        
        for (i in 0 until maxLen) {
            columns.add(
                OperationColumn(
                    carry = if (carries[i] > 0) DigitSymbols.digitToChar(carries[i]).toString() else "",
                    digitOp1 = pOp1[i],
                    digitOp2 = pOp2[i],
                    digitRes = pRes[i]
                )
            )
        }
        
        return OperationVisual(
            base = base,
            op1 = op1,
            op2 = op2,
            operator = "+",
            result = res,
            carries = carries.toList(),
            columns = columns
        )
    }

    private fun generateSubtraction(op1: String, op2: String, res: String, base: Int): OperationVisual {
        val maxLen = maxOf(op1.length, op2.length)
        val pOp1 = op1.padStart(maxLen, '0')
        val pOp2 = op2.padStart(maxLen, '0')
        
        val columns = mutableListOf<OperationColumn>()
        val carries = IntArray(maxLen) { 0 } // carries aqui representa borrows
        
        // Cálculo dos borrows (da direita para a esquerda)
        var borrow = 0
        for (i in maxLen - 1 downTo 0) {
            val d1 = DigitSymbols.charToDigit(pOp1[i])
            val d2 = DigitSymbols.charToDigit(pOp2[i])
            var diff = d1 - d2 - borrow
            
            if (diff < 0) {
                diff += base
                borrow = 1
            } else {
                borrow = 0
            }
            
            if (borrow == 1 && i > 0) {
                carries[i - 1] = 1 // Marca borrow na posição anterior
            }
        }
        
        // Monta o resultado efetuando a subtração corretamente
        var currentBorrow = 0
        val resultDigits = StringBuilder()
        for (i in maxLen - 1 downTo 0) {
            val d1 = DigitSymbols.charToDigit(pOp1[i])
            val d2 = DigitSymbols.charToDigit(pOp2[i])
            var diff = d1 - d2 - currentBorrow
            
            if (diff < 0) {
                diff += base
                currentBorrow = 1
            } else {
                currentBorrow = 0
            }
            
            resultDigits.insert(0, DigitSymbols.digitToChar(diff))
        }
        
        val pRes = resultDigits.toString()
        
        for (i in 0 until maxLen) {
            columns.add(
                OperationColumn(
                    carry = if (carries[i] > 0) "1" else "",
                    digitOp1 = pOp1[i],
                    digitOp2 = pOp2[i],
                    digitRes = pRes[i]
                )
            )
        }
        
        return OperationVisual(
            base = base,
            op1 = op1,
            op2 = op2,
            operator = "-",
            result = pRes,
            carries = carries.toList(),
            columns = columns
        )
    }
}
