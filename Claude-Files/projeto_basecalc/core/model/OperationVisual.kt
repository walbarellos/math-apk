package com.basecalc.core.model

/**
 * Representa os detalhes visuais de uma operação vertical (adição/subtração).
 */
data class OperationVisual(
    val base: Int,
    val op1: String,
    val op2: String,
    val operator: String,
    val result: String,
    val carries: List<Int>, // Carries para cada posição (0 ou valor transportado)
    val columns: List<OperationColumn>
)

data class OperationColumn(
    val carry: String, // "1", "0" (empty), ou valor em bases maiores
    val digitOp1: Char,
    val digitOp2: Char,
    val digitRes: Char
)
