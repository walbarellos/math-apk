package com.basecalc.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.basecalc.ColorMode
import com.basecalc.HistoricoItem

@Composable
fun PainelHistorico(
    historico: List<HistoricoItem>,
    hapticsEnabled: Boolean,
    reduceMotion: Boolean,
    colorMode: ColorMode,
    onItemClick: (HistoricoItem) -> Unit,
    onLimpar: () -> Unit,
    onHapticsChange: (Boolean) -> Unit,
    onReduceMotionChange: (Boolean) -> Unit,
    onColorModeChange: (ColorMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        PreferenciasSection(
            hapticsEnabled = hapticsEnabled,
            reduceMotion = reduceMotion,
            colorMode = colorMode,
            onHapticsChange = onHapticsChange,
            onReduceMotionChange = onReduceMotionChange,
            onColorModeChange = onColorModeChange
        )

        if (historico.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "Nenhum cálculo no histórico ainda.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(32.dp),
                )
            }
            return@Column
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Histórico (${historico.size})",
                style = MaterialTheme.typography.titleMedium,
            )
            TextButton(onClick = onLimpar) {
                Text("Limpar", color = MaterialTheme.colorScheme.error)
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            items(historico, key = { it.expressao }) { item ->
                ItemHistorico(item = item, onClick = { onItemClick(item) })
            }
        }
    }
}

@Composable
private fun PreferenciasSection(
    hapticsEnabled: Boolean,
    reduceMotion: Boolean,
    colorMode: ColorMode,
    onHapticsChange: (Boolean) -> Unit,
    onReduceMotionChange: (Boolean) -> Unit,
    onColorModeChange: (ColorMode) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "Preferências",
            style = MaterialTheme.typography.titleMedium,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Feedback tátil",
                style = MaterialTheme.typography.bodyMedium,
            )
            Switch(
                checked = hapticsEnabled,
                onCheckedChange = onHapticsChange,
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Reduzir animações",
                style = MaterialTheme.typography.bodyMedium,
            )
            Switch(
                checked = reduceMotion,
                onCheckedChange = onReduceMotionChange,
            )
        }

        Text(
            text = "Modo de cores",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ColorMode.entries.forEach { mode ->
                val selected = mode == colorMode
                OutlinedButton(
                    onClick = { onColorModeChange(mode) },
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                        contentColor = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                    ),
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                    )
                ) {
                    val label = when (mode) {
                        ColorMode.PADRAO -> "Padrão"
                        ColorMode.ALTO_CONTRASTE -> "Alto contraste"
                        ColorMode.DALTONISMO -> "Daltonismo"
                    }
                    Text(text = label)
                }
            }
        }
    }
}

@Composable
private fun ItemHistorico(item: HistoricoItem, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = item.expressao,
                style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = "= ${item.resultadoDecimal}",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold,
                ),
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
            )
        }
    }
}
