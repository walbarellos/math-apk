package com.basecalc.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.basecalc.ColorMode
import com.basecalc.core.model.BaseEntry
import com.basecalc.core.model.CalcResult
import com.basecalc.core.util.DigitSymbols
import java.math.BigInteger

@Composable
fun PainelGrafico(
    resultado: CalcResult?,
    colorMode: ColorMode,
    reduceMotion: Boolean,
    modifier: Modifier = Modifier
) {
    if (resultado == null || !resultado.ok) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text(
                text = "Calcule um valor para ver o gráfico.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(32.dp),
            )
        }
        return
    }

    val entradasValidas = resultado.bases.filter { it.valid }
    val maxDigitos = entradasValidas.maxOf { it.intPart.trimStart('-').length }.coerceAtLeast(1)
    val ehInteiro = resultado.value.isInteger()

    val zoomState = rememberSaveable { androidx.compose.runtime.mutableStateOf(1f) }
    val zoom = zoomState.value
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val barAreaWidth = (screenWidth * 0.55f) * zoom
    val scrollState = rememberScrollState()

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {

        // ── Seção 1: comprimento da representação ──────────────────────────
        item {
            Text(
                text = "Comprimento da representação",
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = "Quantos dígitos cada base precisa para representar o mesmo valor.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Zoom horizontal: ${String.format("%.1f", zoom)}x",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            )
            Slider(
                value = zoom,
                onValueChange = { zoomState.value = it },
                valueRange = 0.8f..1.6f,
                steps = 3,
                modifier = Modifier.padding(bottom = 6.dp),
            )
            LegendRow(entradasValidas = entradasValidas, colorMode = colorMode)
            Spacer(Modifier.height(6.dp))
        }

        item {
            Column(modifier = Modifier.horizontalScroll(scrollState)) {
                entradasValidas.forEach { entrada ->
                    BarraComparacao(
                        label = "b${entrada.base}",
                        valor = entrada.displayWithComma(),
                        nDigitos = entrada.intPart.trimStart('-').length,
                        maxDigitos = maxDigitos,
                        base = entrada.base,
                        barAreaWidth = barAreaWidth,
                        reduceMotion = reduceMotion,
                        colorMode = colorMode,
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }
        }

        // ── Seção 2: decomposição posicional (só inteiros) ─────────────────
        if (ehInteiro) {
            item {
                Spacer(Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Decomposição posicional",
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = "Cada dígito multiplicado pelo peso da sua posição (dígito × baseⁿ).",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                )
                Spacer(Modifier.height(8.dp))
            }

            items(
                entradasValidas.filter { it.base != 10 },
                key = { "dec_${it.base}" },
            ) { entrada ->
                DecomposicaoPosicional(entrada, colorMode)
            }
        }
    }
}

@Composable
private fun BarraComparacao(
    label: String,
    valor: String,
    nDigitos: Int,
    maxDigitos: Int,
    base: Int,
    barAreaWidth: Dp,
    reduceMotion: Boolean,
    colorMode: ColorMode,
) {
    val acento = acentoDaBase(base, colorMode)
    val fracao by animateFloatAsState(
        targetValue = nDigitos.toFloat() / maxDigitos.toFloat(),
        animationSpec = tween(
            durationMillis = if (reduceMotion) 0 else 450,
            delayMillis = if (reduceMotion) 0 else base * 20
        ),
        label = "barra_b$base",
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // Rótulo da base
        Text(
            text = label,
            modifier = Modifier.width(36.dp),
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = acento,
            textAlign = TextAlign.End,
        )

        // Trilho da barra
        Box(
            modifier = Modifier
                .width(barAreaWidth)
                .height(24.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(MaterialTheme.colorScheme.surface),
        ) {
            // Preenchimento animado
            Box(
                modifier = Modifier
                    .fillMaxWidth(fracao)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(6.dp))
                    .background(acento.copy(alpha = 0.7f)),
            )
            // Contagem de dígitos
            Text(
                text = "$nDigitos",
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 8.dp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            )
        }

        // Valor representado
        Text(
            text = valor,
            modifier = Modifier.width(88.dp),
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun LegendRow(
    entradasValidas: List<BaseEntry>,
    colorMode: ColorMode
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        items(entradasValidas, key = { "legend_${it.base}" }) { entrada ->
            val cor = acentoDaBase(entrada.base, colorMode)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(cor)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "b${entrada.base}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                )
            }
        }
    }
}

@Composable
private fun DecomposicaoPosicional(entrada: BaseEntry, colorMode: ColorMode) {
    val acento = acentoDaBase(entrada.base, colorMode)
    val digitosInvertidos = entrada.intPart.trimStart('-').reversed()
    val isNegativo = entrada.intPart.startsWith('-')
    val baseBig = BigInteger.valueOf(entrada.base.toLong())

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(0.5.dp, acento.copy(alpha = 0.35f)),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = entrada.label.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = acento,
            )
            Spacer(Modifier.height(6.dp))

            digitosInvertidos.forEachIndexed { pos, char ->
                val valorDigito = DigitSymbols.charToDigit(char)
                if (valorDigito < 0) return@forEachIndexed

                val valorPosicional = BigInteger.valueOf(valorDigito.toLong()) * baseBig.pow(pos)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "$char × ${entrada.base}^$pos",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                    )
                    Text(
                        text = "= $valorPosicional",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = acento,
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = if (isNegativo) "Total (−)" else "Total",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                )
                Text(
                    text = entrada.intPart,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Monospace,
                    ),
                    color = acento,
                )
            }
        }
    }
}
