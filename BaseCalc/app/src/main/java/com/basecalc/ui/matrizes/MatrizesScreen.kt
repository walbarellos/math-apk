package com.basecalc.ui.matrizes

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.basecalc.CalcViewModel
import com.basecalc.OperacaoMatriz
import com.basecalc.PassoMatriz

// ─── Paleta ───────────────────────────────────────────────────────────────────

private val corDestaqueLinha = Color(0xFFE3F2FD)
private val corDestaqueColuna = Color(0xFFFCE4EC)
private val corDestaqueCelula = Color(0xFFE8F5E9)
private val corBordaGrade = Color(0xFF90A4AE)

// ─── Tela principal ───────────────────────────────────────────────────────────

@Composable
fun MatrizesScreen(viewModel: CalcViewModel) {
    val state by viewModel.uiState.collectAsState()
    val ms = state.matrizesState

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "Matrizes",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )

        // Seletor de dimensão
        DimensaoSeletorCard(
            linhas = ms.linhas,
            colunas = ms.colunas,
            onChangeDim = viewModel::setDimensaoMatriz,
        )

        // Operação
        OperacaoSeletorCard(
            operacao = ms.operacao,
            linhas = ms.linhas,
            colunas = ms.colunas,
            onChangeOp = viewModel::setOperacaoMatriz,
        )

        // Matrizes de entrada
        val precisaB = ms.operacao == OperacaoMatriz.MULTIPLICACAO
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            MatrizInputCard(
                titulo = "Matriz A",
                linhas = ms.linhas,
                colunas = ms.colunas,
                dados = ms.matrizA,
                onCelulaChange = { r, c, v -> viewModel.setCelulaMatrizA(r, c, v) },
                modifier = Modifier.weight(1f),
            )
            if (precisaB) {
                MatrizInputCard(
                    titulo = "Matriz B",
                    linhas = ms.colunas,      // B deve ter linhas = colunas de A
                    colunas = ms.colunas,
                    dados = ms.matrizB,
                    onCelulaChange = { r, c, v -> viewModel.setCelulaMatrizB(r, c, v) },
                    modifier = Modifier.weight(1f),
                )
            }
        }

        Button(
            onClick = viewModel::calcularMatriz,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Calcular  ${ms.operacao.label}")
        }

        // Resultado
        AnimatedVisibility(
            visible = ms.resultado.isNotEmpty(),
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut(),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                MatrizResultadoCard(
                    operacao = ms.operacao,
                    resultado = ms.resultado,
                    destaques = ms.passos.lastOrNull(),
                )
                PassosMatrizCard(passos = ms.passos)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

// ─── Seletor de dimensão ──────────────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DimensaoSeletorCard(
    linhas: Int,
    colunas: Int,
    onChangeDim: (Int, Int) -> Unit,
) {
    val opcoes = listOf(2 to 2, 2 to 3, 3 to 2, 3 to 3)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Dimensão da Matriz",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                opcoes.forEach { (l, c) ->
                    FilterChip(
                        selected = linhas == l && colunas == c,
                        onClick = { onChangeDim(l, c) },
                        label = { Text("${l}×${c}") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                    )
                }
            }
        }
    }
}

// ─── Seletor de operação ──────────────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun OperacaoSeletorCard(
    operacao: OperacaoMatriz,
    linhas: Int,
    colunas: Int,
    onChangeOp: (OperacaoMatriz) -> Unit,
) {
    // Determinante e Inversa só pra quadradas
    val isQuadrada = linhas == colunas

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Operação",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OperacaoMatriz.entries.forEach { op ->
                    val habilitado = when (op) {
                        OperacaoMatriz.DETERMINANTE, OperacaoMatriz.INVERSA -> isQuadrada
                        else -> true
                    }
                    FilterChip(
                        selected = operacao == op,
                        onClick = { if (habilitado) onChangeOp(op) },
                        enabled = habilitado,
                        label = { Text(op.label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                    )
                }
            }
            if (!isQuadrada) {
                Text(
                    text = "⚠ Determinante e Inversa requerem matriz quadrada",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
    }
}

// ─── Grade de entrada ─────────────────────────────────────────────────────────

@Composable
fun MatrizInputCard(
    titulo: String,
    linhas: Int,
    colunas: Int,
    dados: List<List<String>>,
    onCelulaChange: (row: Int, col: Int, value: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = titulo,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp),
            )

            // Colchete esquerdo decorativo
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "[",
                    fontSize = 36.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontFamily = FontFamily.Monospace,
                )
                Spacer(modifier = Modifier.width(4.dp))

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    for (r in 0 until linhas) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            for (c in 0 until colunas) {
                                val valor = dados.getOrNull(r)?.getOrNull(c) ?: "0"
                                CelulaEditavel(
                                    valor = valor,
                                    onChange = { onCelulaChange(r, c, it) },
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "]",
                    fontSize = 36.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontFamily = FontFamily.Monospace,
                )
            }
        }
    }
}

@Composable
private fun CelulaEditavel(
    valor: String,
    onChange: (String) -> Unit,
) {
    BasicTextField(
        value = valor,
        onValueChange = { novo ->
            // Aceita negativos, decimais, vazios
            if (novo.matches(Regex("-?\\d*\\.?\\d*"))) onChange(novo)
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        textStyle = TextStyle(
            textAlign = TextAlign.Center,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface,
        ),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        modifier = Modifier
            .size(width = 44.dp, height = 36.dp)
            .border(
                width = 1.dp,
                color = corBordaGrade,
                shape = RoundedCornerShape(4.dp),
            )
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(4.dp),
            )
            .padding(horizontal = 2.dp, vertical = 4.dp),
        singleLine = true,
    )
}

// ─── Card de resultado ────────────────────────────────────────────────────────

@Composable
private fun MatrizResultadoCard(
    operacao: OperacaoMatriz,
    resultado: List<List<String>>,
    destaques: PassoMatriz?,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = when (operacao) {
                    OperacaoMatriz.DETERMINANTE -> "det(A)"
                    OperacaoMatriz.TRANSPOSTA -> "Aᵀ"
                    OperacaoMatriz.INVERSA -> "A⁻¹"
                    OperacaoMatriz.MULTIPLICACAO -> "A × B"
                },
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Determinante é escalar
            if (operacao == OperacaoMatriz.DETERMINANTE && resultado.size == 1 && resultado[0].size == 1) {
                Text(
                    text = resultado[0][0],
                    style = MaterialTheme.typography.headlineMedium,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            } else {
                MatrizDisplay(
                    dados = resultado,
                    destaques = destaques,
                )
            }
        }
    }
}

@Composable
private fun MatrizDisplay(
    dados: List<List<String>>,
    destaques: PassoMatriz?,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "[",
            fontSize = 32.sp,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
        )
        Spacer(modifier = Modifier.width(4.dp))
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            dados.forEachIndexed { r, linha ->
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    linha.forEachIndexed { c, valor ->
                        val isLinha = destaques?.linhaDestaque == r
                        val isColuna = destaques?.colunaDestaque == c
                        val bgColor = when {
                            isLinha && isColuna -> corDestaqueCelula
                            isLinha -> corDestaqueLinha
                            isColuna -> corDestaqueColuna
                            else -> Color.Transparent
                        }
                        Box(
                            modifier = Modifier
                                .widthIn(min = 44.dp)
                                .background(bgColor, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 4.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = valor,
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                            )
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "]",
            fontSize = 32.sp,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
        )
    }
}

// ─── Card de passos passo a passo ─────────────────────────────────────────────

@Composable
private fun PassosMatrizCard(passos: List<PassoMatriz>) {
    if (passos.isEmpty()) return

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
                PassoItem(numero = idx + 1, passo = passo)
                if (idx < passos.lastIndex) {
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}

@Composable
private fun PassoItem(numero: Int, passo: PassoMatriz) {
    val temDestaque = passo.linhaDestaque >= 0 || passo.colunaDestaque >= 0
    val bgSurface = if (temDestaque)
        MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
    else
        Color.Transparent

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = bgSurface,
        shape = RoundedCornerShape(6.dp),
    ) {
        Row(
            modifier = Modifier.padding(vertical = 4.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Surface(
                shape = CircleShape,
                color = if (temDestaque)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                modifier = Modifier.size(20.dp),
            ) {
                Text(
                    text = "$numero",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = passo.descricao,
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

// ─── Labels das operações ─────────────────────────────────────────────────────

private val OperacaoMatriz.label: String
    get() = when (this) {
        OperacaoMatriz.MULTIPLICACAO -> "A × B"
        OperacaoMatriz.DETERMINANTE -> "det(A)"
        OperacaoMatriz.TRANSPOSTA -> "Aᵀ"
        OperacaoMatriz.INVERSA -> "A⁻¹"
    }
