package com.basecalc.ui

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.basecalc.BaseEntry
import com.basecalc.CalcUiState
import com.basecalc.CalcViewModel

// ─── Cores da UI ─────────────────────────────────────────────────────────────

private val baseColors = mapOf(
    2  to Color(0xFF185FA5),   // azul  — binário
    3  to Color(0xFF0F6E56),
    4  to Color(0xFF3B6D11),
    5  to Color(0xFF3B6D11),   // verde
    6  to Color(0xFF854F0B),
    7  to Color(0xFFBA7517),   // âmbar
    8  to Color(0xFF534AB7),   // roxo  — octal
    9  to Color(0xFF993556),
    10 to Color(0xFF2C2C2A),   // preto — decimal
    16 to Color(0xFFD4537E),   // rosa  — hex
)

// ─── Tela principal ───────────────────────────────────────────────────────────

@Composable
fun CalculatorScreen(viewModel: CalcViewModel) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Display de expressão e resultado
        DisplayPanel(
            state = state,
            onToggleSteps = { viewModel.toggleSteps(it) },
            modifier = Modifier.weight(1f)
        )

        HorizontalDivider(thickness = 0.5.dp)

        // Teclado
        KeypadPanel(
            onKey     = { viewModel.onKey(it) },
            onEquals  = { viewModel.onEquals() },
            modifier  = Modifier.padding(8.dp)
        )
    }
}

// ─── Painel de display ────────────────────────────────────────────────────────

@Composable
private fun DisplayPanel(
    state: CalcUiState,
    onToggleSteps: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier        = modifier.padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Expressão atual
        item {
            Text(
                text  = state.expression.ifEmpty { "0" },
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Normal,
                    textAlign  = TextAlign.End
                ),
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            )
        }

        // Erro
        state.error?.let { err ->
            item {
                Text(
                    text  = err,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Cartões de bases
        val bases = state.result?.bases ?: emptyList()
        items(bases, key = { it.base }) { entry ->
            BaseCard(
                entry     = entry,
                expanded  = state.showStepsForBase == entry.base,
                onToggle  = { onToggleSteps(entry.base) }
            )
        }
    }
}

// ─── Cartão de base ───────────────────────────────────────────────────────────

@Composable
private fun BaseCard(entry: BaseEntry, expanded: Boolean, onToggle: () -> Unit) {
    val accent = baseColors[entry.base] ?: Color.Gray

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(10.dp),
        colors   = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        border   = BorderStroke(width = if (entry.base == 10) 1.5.dp else 0.5.dp, color = accent.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
            // Label
            Text(
                text  = entry.label.uppercase(),
                color = accent,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight   = FontWeight.Medium,
                    letterSpacing = 0.08.sp
                )
            )
            Spacer(Modifier.height(2.dp))
            // Valor na base
            if (entry.valid) {
                Row(verticalAlignment = Alignment.Baseline) {
                    Text(
                        text  = entry.intPart,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Normal
                        )
                    )
                    if (entry.fracPart.isNotEmpty()) {
                        Text(
                            text  = ",${entry.fracPart}",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Normal,
                                color      = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        )
                    }
                    if (entry.repeats && entry.repeatPart.isNotEmpty()) {
                        Text(
                            text  = "(${entry.repeatPart}...)",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace,
                                color      = accent.copy(alpha = 0.7f)
                            ),
                            modifier = Modifier.padding(start = 2.dp)
                        )
                    }
                }
            } else {
                Text(
                    text  = entry.intPart.ifEmpty { "inválido" },
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // Passos (expansível)
            if (entry.steps.isNotEmpty() && entry.base != 10) {
                Spacer(Modifier.height(6.dp))
                TextButton(
                    onClick      = onToggle,
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text  = if (expanded) "ocultar passos" else "ver como converteu",
                        style = MaterialTheme.typography.labelSmall,
                        color = accent
                    )
                }
                AnimatedVisibility(visible = expanded) {
                    Text(
                        text       = entry.steps,
                        style      = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 20.sp
                        ),
                        color      = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        modifier   = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

// ─── Teclado ──────────────────────────────────────────────────────────────────

private val keyLayout = listOf(
    listOf("AC", "⌫", "%", "/"),
    listOf("7",  "8", "9", "*"),
    listOf("4",  "5", "6", "-"),
    listOf("1",  "2", "3", "+"),
    listOf("0",       ".", "="),   // "0" ocupa 2 colunas
)

@Composable
private fun KeypadPanel(
    onKey: (String) -> Unit,
    onEquals: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        keyLayout.forEach { row ->
            Row(
                modifier            = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                row.forEach { key ->
                    val weight = if (key == "0") 2f else 1f
                    CalcKey(
                        label    = key,
                        modifier = Modifier.weight(weight),
                        onClick  = { if (key == "=") onEquals() else onKey(key) }
                    )
                }
            }
        }
    }
}

@Composable
private fun CalcKey(label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val isOp     = label in setOf("+", "-", "*", "/", "%", "⌫", "AC")
    val isEquals = label == "="

    val containerColor = when {
        isEquals -> MaterialTheme.colorScheme.primary
        isOp     -> MaterialTheme.colorScheme.secondaryContainer
        else     -> MaterialTheme.colorScheme.surfaceVariant
    }
    val contentColor = when {
        isEquals -> MaterialTheme.colorScheme.onPrimary
        isOp     -> MaterialTheme.colorScheme.onSecondaryContainer
        else     -> MaterialTheme.colorScheme.onSurface
    }

    Button(
        onClick  = onClick,
        modifier = modifier.height(60.dp),
        shape    = RoundedCornerShape(12.dp),
        colors   = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor   = contentColor
        ),
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(
            text  = label,
            style = MaterialTheme.typography.titleMedium.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Normal
            )
        )
    }
}
