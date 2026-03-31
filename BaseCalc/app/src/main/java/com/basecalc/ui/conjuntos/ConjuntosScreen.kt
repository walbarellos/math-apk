package com.basecalc.ui.conjuntos

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.basecalc.CalcViewModel
import com.basecalc.ConjuntoResultado
import com.basecalc.OperacaoConjunto

// ─── Paleta do diagrama de Venn ───────────────────────────────────────────────

private val corA = Color(0xFF4FC3F7)         // azul claro
private val corB = Color(0xFFEF9A9A)         // vermelho claro
private val corAB = Color(0xFFCE93D8)        // roxo (sobreposição)
private val corContorno = Color(0xFF37474F)

// ─── Tela principal ───────────────────────────────────────────────────────────

@Composable
fun ConjuntosScreen(viewModel: CalcViewModel) {
    val state by viewModel.uiState.collectAsState()
    val cs = state.conjuntosState

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Título
        Text(
            text = "Teoria dos Conjuntos",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )

        // Inputs
        ConjuntosInputCard(
            inputA = cs.inputA,
            inputB = cs.inputB,
            universo = cs.universo,
            operacao = cs.operacao,
            onChangeA = viewModel::setInputConjuntoA,
            onChangeB = viewModel::setInputConjuntoB,
            onChangeU = viewModel::setUniverso,
            onChangeOp = viewModel::setOperacaoConjunto,
            onCalcular = viewModel::calcularConjuntos,
        )

        // Resultado
        AnimatedVisibility(
            visible = cs.resultado != null,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut(),
        ) {
            cs.resultado?.let { resultado ->
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    VennDiagramCard(resultado = resultado, operacao = cs.operacao)
                    PassosCard(passos = resultado.passos)
                    ElementosResultadoCard(resultado = resultado, operacao = cs.operacao)
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

// ─── Card de inputs ───────────────────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ConjuntosInputCard(
    inputA: String,
    inputB: String,
    universo: String,
    operacao: OperacaoConjunto,
    onChangeA: (String) -> Unit,
    onChangeB: (String) -> Unit,
    onChangeU: (String) -> Unit,
    onChangeOp: (OperacaoConjunto) -> Unit,
    onCalcular: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Elementos separados por vírgula  →  ex: 1, 2, 3",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = inputA,
                    onValueChange = onChangeA,
                    label = { Text("Conjunto A") },
                    placeholder = { Text("1, 2, 3") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = inputB,
                    onValueChange = onChangeB,
                    label = { Text("Conjunto B") },
                    placeholder = { Text("2, 3, 4") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                )
            }

            AnimatedVisibility(visible = operacao == OperacaoConjunto.COMPLEMENTO) {
                OutlinedTextField(
                    value = universo,
                    onValueChange = onChangeU,
                    label = { Text("Universo U  (opcional)") },
                    placeholder = { Text("1, 2, 3, 4, 5") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
            }

            // Chips de operação
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OperacaoConjunto.entries.forEach { op ->
                    FilterChip(
                        selected = operacao == op,
                        onClick = { onChangeOp(op) },
                        label = { Text(op.label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                    )
                }
            }

            Button(
                onClick = onCalcular,
                modifier = Modifier.fillMaxWidth(),
                enabled = inputA.isNotBlank(),
            ) {
                Text("Calcular  ${operacao.simbolo}")
            }
        }
    }
}

// ─── Diagrama de Venn ─────────────────────────────────────────────────────────

@Composable
private fun VennDiagramCard(resultado: ConjuntoResultado, operacao: OperacaoConjunto) {
    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Diagrama de Venn",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.height(12.dp))

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
            ) {
                desenharVenn(resultado = resultado, operacao = operacao, isDarkTheme = isDarkTheme)
            }

            Spacer(modifier = Modifier.height(12.dp))
            LegendaVenn()
        }
    }
}

private fun androidx.compose.ui.graphics.Color.luminance(): Float {
    return (0.299f * red + 0.587f * green + 0.114f * blue)
}

private fun DrawScope.desenharVenn(resultado: ConjuntoResultado, operacao: OperacaoConjunto, isDarkTheme: Boolean) {
    val w = size.width
    val h = size.height
    val raio = h * 0.38f
    val centroY = h / 2f
    val offset = raio * 0.55f        // sobreposição
    val centroA = Offset(w / 2f - offset, centroY)
    val centroB = Offset(w / 2f + offset, centroY)

    // Determina quais regiões destacar baseado na operação
    val destacarA = when (operacao) {
        OperacaoConjunto.UNIAO, OperacaoConjunto.DIFERENCA -> true
        OperacaoConjunto.COMPLEMENTO -> false
        else -> false
    }
    val destacarB = operacao == OperacaoConjunto.UNIAO
    val destacarAB = when (operacao) {
        OperacaoConjunto.UNIAO, OperacaoConjunto.INTERSECCAO -> true
        else -> false
    }
    val destacarFora = operacao == OperacaoConjunto.COMPLEMENTO

    // Retângulo de fundo (universo)
    drawRoundRect(
        color = if (destacarFora) corAB.copy(alpha = 0.25f) else Color(0xFFECEFF1),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f),
    )
    drawRoundRect(
        color = corContorno.copy(alpha = 0.3f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f),
        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f),
    )

    // Círculo A
    drawCircle(
        color = if (destacarA) corA.copy(alpha = 0.7f) else corA.copy(alpha = 0.3f),
        radius = raio,
        center = centroA,
    )
    drawCircle(
        color = corContorno.copy(alpha = 0.6f),
        radius = raio,
        center = centroA,
        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.5f),
    )

    // Círculo B
    drawCircle(
        color = if (destacarB) corB.copy(alpha = 0.7f) else corB.copy(alpha = 0.3f),
        radius = raio,
        center = centroB,
    )
    drawCircle(
        color = corContorno.copy(alpha = 0.6f),
        radius = raio,
        center = centroB,
        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.5f),
    )

    // Sobreposição A ∩ B — desenhada por cima com blendMode
    if (destacarAB && resultado.elementosComuns.isNotEmpty()) {
        drawCircle(
            color = corAB.copy(alpha = 0.65f),
            radius = raio,
            center = centroB,
            blendMode = if (isDarkTheme) BlendMode.Screen else BlendMode.Multiply,
        )
    }

    // Labels A e B - cor diferente para tema escuro
    val corTexto = if (isDarkTheme) android.graphics.Color.WHITE else android.graphics.Color.parseColor("#37474F")
    val corTextoPequeno = if (isDarkTheme) android.graphics.Color.LTGRAY else android.graphics.Color.parseColor("#455A64")
    
    drawContext.canvas.nativeCanvas.apply {
        val paint = android.graphics.Paint().apply {
            color = corTexto
            textSize = 42f
            isFakeBoldText = true
            textAlign = android.graphics.Paint.Align.CENTER
        }
        drawText("A", centroA.x - offset * 0.5f, centroY - raio * 0.6f, paint)
        drawText("B", centroB.x + offset * 0.5f, centroY - raio * 0.6f, paint)

        // Contagem de elementos por região
        val paintSmall = android.graphics.Paint().apply {
            color = corTextoPequeno
            textSize = 32f
            textAlign = android.graphics.Paint.Align.CENTER
        }
        val apenasA = resultado.elementosApenasA.size
        val apenasB = resultado.elementosApenasB.size
        val comuns = resultado.elementosComuns.size
        if (apenasA > 0) drawText("($apenasA)", centroA.x - offset * 0.6f, centroY + 14f, paintSmall)
        if (apenasB > 0) drawText("($apenasB)", centroB.x + offset * 0.6f, centroY + 14f, paintSmall)
        if (comuns > 0) drawText("($comuns)", w / 2f, centroY + 14f, paintSmall)
    }
}

@Composable
private fun LegendaVenn() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LegendaItem(cor = corA, texto = "Apenas A")
        Spacer(modifier = Modifier.width(16.dp))
        LegendaItem(cor = corAB, texto = "A ∩ B")
        Spacer(modifier = Modifier.width(16.dp))
        LegendaItem(cor = corB, texto = "Apenas B")
    }
}

@Composable
private fun LegendaItem(cor: Color, texto: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(cor),
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = texto, style = MaterialTheme.typography.labelSmall)
    }
}

// ─── Card de passos ───────────────────────────────────────────────────────────

@Composable
private fun PassosCard(passos: List<String>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Resolução passo a passo",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Spacer(modifier = Modifier.height(8.dp))

            passos.forEachIndexed { idx, passo ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    // Número do passo
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp),
                    ) {
                        Text(
                            text = "${idx + 1}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 2.dp),
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = passo,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

// ─── Card com resultado e elementos por região ────────────────────────────────

@Composable
private fun ElementosResultadoCard(resultado: ConjuntoResultado, operacao: OperacaoConjunto) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "Resultado",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )

            // Resultado principal em destaque
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = RoundedCornerShape(8.dp),
            ) {
                Text(
                    text = "${operacao.simbolo}  =  ${resultado.elementos.formatConjunto()}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(12.dp),
                )
            }

            // Cardinalidade
            Text(
                text = "n(resultado) = ${resultado.elementos.size}",
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            // Detalhamento por região (só mostra quando relevante)
            if (resultado.elementosComuns.isNotEmpty() && operacao != OperacaoConjunto.COMPLEMENTO) {
                RegionRow(
                    label = "Apenas em A",
                    elementos = resultado.elementosApenasA,
                    cor = corA,
                )
                RegionRow(
                    label = "A ∩ B",
                    elementos = resultado.elementosComuns,
                    cor = corAB,
                )
                RegionRow(
                    label = "Apenas em B",
                    elementos = resultado.elementosApenasB,
                    cor = corB,
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RegionRow(label: String, elementos: Set<String>, cor: Color) {
    if (elementos.isEmpty()) return
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(cor)
                .align(Alignment.CenterVertically),
        )
        Spacer(modifier = Modifier.width(6.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                elementos.forEach { el ->
                    Surface(
                        color = cor.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(4.dp),
                    ) {
                        Text(
                            text = el,
                            style = MaterialTheme.typography.labelMedium,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        )
                    }
                }
            }
        }
    }
}

// ─── Extensões e helpers ──────────────────────────────────────────────────────

private fun Set<String>.formatConjunto(): String =
    if (isEmpty()) "∅" else "{ ${sorted().joinToString(", ")} }"

private val OperacaoConjunto.label: String
    get() = when (this) {
        OperacaoConjunto.UNIAO -> "União (∪)"
        OperacaoConjunto.INTERSECCAO -> "Interseção (∩)"
        OperacaoConjunto.DIFERENCA -> "Diferença (−)"
        OperacaoConjunto.COMPLEMENTO -> "Complemento (A')"
    }

private val OperacaoConjunto.simbolo: String
    get() = when (this) {
        OperacaoConjunto.UNIAO -> "A ∪ B"
        OperacaoConjunto.INTERSECCAO -> "A ∩ B"
        OperacaoConjunto.DIFERENCA -> "A − B"
        OperacaoConjunto.COMPLEMENTO -> "A'"
    }
