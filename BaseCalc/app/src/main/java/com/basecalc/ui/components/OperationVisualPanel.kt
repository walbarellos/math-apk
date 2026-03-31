package com.basecalc.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.basecalc.ColorMode
import com.basecalc.core.model.OperationVisual

@Composable
fun OperationVisualPanel(
    visual: OperationVisual,
    colorMode: ColorMode,
    modifier: Modifier = Modifier
) {
    val acento = acentoDaBase(visual.base, colorMode)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Operação detalhada (Base ${visual.base})",
            style = MaterialTheme.typography.labelSmall,
            color = acento,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(verticalAlignment = Alignment.Bottom) {
            // Operador
            Text(
                text = visual.operator,
                style = MaterialTheme.typography.headlineSmall.copy(fontFamily = FontFamily.Monospace),
                color = acento,
                modifier = Modifier.padding(end = 12.dp, bottom = 4.dp)
            )

            // Colunas da operação
            Row {
                visual.columns.forEach { col ->
                    OperationColumnView(col, acento)
                }
            }
        }
    }
}

@Composable
private fun OperationColumnView(col: com.basecalc.core.model.OperationColumn, acento: androidx.compose.ui.graphics.Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 2.dp)
    ) {
        // Carry
        Text(
            text = col.carry,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            ),
            color = acento.copy(alpha = 0.7f),
            modifier = Modifier.height(14.dp)
        )

        // Operando 1
        Text(
            text = col.digitOp1.toString(),
            style = MaterialTheme.typography.headlineSmall.copy(fontFamily = FontFamily.Monospace),
        )

        // Operando 2
        Text(
            text = col.digitOp2.toString(),
            style = MaterialTheme.typography.headlineSmall.copy(fontFamily = FontFamily.Monospace),
        )

        // Linha horizontal
        Box(
            modifier = Modifier
                .padding(vertical = 4.dp)
                .width(20.dp)
                .height(1.5.dp)
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
        )

        // Resultado
        Text(
            text = col.digitRes.toString(),
            style = MaterialTheme.typography.headlineSmall.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            ),
            color = acento
        )
    }
}
