package com.basecalc.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

// ─── Layout do Teclado ────────────────────────────────────────────────────────

private val layoutBase = listOf(
    listOf("AC", "⌫", "^", "/"),
    listOf("7", "8", "9", "*"),
    listOf("4", "5", "6", "-"),
    listOf("1", "2", "3", "+"),
    listOf("0", ".", "%", "="),
)

private val hexDigits = listOf("A", "B", "C", "D", "E", "F")

@Composable
fun PainelTeclado(
    onKey: (String) -> Unit,
    onEquals: () -> Unit,
    hapticsEnabled: Boolean,
    inputBase: Int,
    modifier: Modifier = Modifier,
) {
    val haptics = LocalHapticFeedback.current
    
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        // Se a base for > 10, mostramos os dígitos hexadecimais em uma linha extra ou grade
        if (inputBase > 10) {
            val digitsToShow = hexDigits.take(inputBase - 10)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                digitsToShow.forEach { tecla ->
                    TeclaCalc(
                        label = tecla,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            if (hapticsEnabled) haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onKey(tecla)
                        }
                    )
                }
            }
        }

        layoutBase.forEach { linha ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                linha.forEach { tecla ->
                    val weight = 1f
                    
                    // Desabilita dígitos não permitidos pela base (exceto operadores)
                    val isDigit = tecla.length == 1 && tecla[0] in '0'..'9'
                    val enabled = if (isDigit) {
                        tecla.toInt() < inputBase
                    } else true

                    TeclaCalc(
                        label = tecla,
                        enabled = enabled,
                        modifier = Modifier.weight(weight),
                        onClick = {
                            if (hapticsEnabled) {
                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                            if (tecla == "=") onEquals() else onKey(tecla)
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun TeclaCalc(
    label: String, 
    modifier: Modifier = Modifier, 
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val ehOperador = label in setOf("+", "-", "*", "/", "%", "^", "⌫", "AC")
    val ehIgual = label == "="

    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(58.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = when {
                ehIgual    -> MaterialTheme.colorScheme.primary
                ehOperador -> MaterialTheme.colorScheme.secondaryContainer
                else       -> MaterialTheme.colorScheme.surfaceVariant
            },
            contentColor = when {
                ehIgual    -> MaterialTheme.colorScheme.onPrimary
                ehOperador -> MaterialTheme.colorScheme.onSecondaryContainer
                else       -> MaterialTheme.colorScheme.onSurface
            },
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
        ),
        contentPadding = PaddingValues(0.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Normal,
            ),
        )
    }
}
