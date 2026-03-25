package com.basecalc.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.basecalc.*
import com.basecalc.core.conversion.StepsFormatter
import com.basecalc.core.model.BaseEntry
import com.basecalc.core.model.CalcResult
import com.basecalc.core.util.DigitSymbols
import java.math.BigInteger

// ─── Cores por base (clara / escura) ─────────────────────────────────────────

@Composable
private fun acentoDaBase(base: Int, colorMode: ColorMode): Color {
    val escuro = isSystemInDarkTheme()
    return when (colorMode) {
        ColorMode.PADRAO -> when (base) {
            2  -> if (escuro) Color(0xFF6BB4FF) else Color(0xFF185FA5)
            3  -> if (escuro) Color(0xFF4EC9A0) else Color(0xFF0D6B52)
            4  -> if (escuro) Color(0xFF8BD158) else Color(0xFF3B6D11)
            5  -> if (escuro) Color(0xFFAAD96A) else Color(0xFF4F7A1C)
            6  -> if (escuro) Color(0xFFEFA94A) else Color(0xFF854F0B)
            7  -> if (escuro) Color(0xFFFFCC4A) else Color(0xFFBA7517)
            8  -> if (escuro) Color(0xFFA99EFF) else Color(0xFF534AB7)
            9  -> if (escuro) Color(0xFFEE8BB5) else Color(0xFF993556)
            10 -> if (escuro) Color(0xFFCCCCCC) else Color(0xFF555555)
            16 -> if (escuro) Color(0xFFFF9EC4) else Color(0xFFD4537E)
            else -> Color.Gray
        }
        ColorMode.ALTO_CONTRASTE -> when (base) {
            2  -> if (escuro) Color(0xFFFFFFFF) else Color(0xFF000000)
            3  -> if (escuro) Color(0xFF00E5FF) else Color(0xFF004E7A)
            4  -> if (escuro) Color(0xFFB9FF3C) else Color(0xFF2E6B00)
            5  -> if (escuro) Color(0xFFFFE85A) else Color(0xFF7A5A00)
            6  -> if (escuro) Color(0xFFFFB74D) else Color(0xFF7A3F00)
            7  -> if (escuro) Color(0xFFFF5252) else Color(0xFF7A0000)
            8  -> if (escuro) Color(0xFFB388FF) else Color(0xFF4A148C)
            9  -> if (escuro) Color(0xFF80D8FF) else Color(0xFF01579B)
            10 -> if (escuro) Color(0xFFFFFFFF) else Color(0xFF000000)
            16 -> if (escuro) Color(0xFFFF80AB) else Color(0xFF880E4F)
            else -> if (escuro) Color.White else Color.Black
        }
        ColorMode.DALTONISMO -> when (base) {
            2  -> Color(0xFF0072B2)
            3  -> Color(0xFFE69F00)
            4  -> Color(0xFF009E73)
            5  -> Color(0xFFF0E442)
            6  -> Color(0xFF56B4E9)
            7  -> Color(0xFFD55E00)
            8  -> Color(0xFFCC79A7)
            9  -> Color(0xFF000000)
            10 -> Color(0xFF666666)
            16 -> Color(0xFF999999)
            else -> Color.Gray
        }
    }
}

// ─── Rótulos das abas ─────────────────────────────────────────────────────────

private fun rotuloAba(aba: AppTab) = when (aba) {
    AppTab.CALCULADORA -> "Calcular"
    AppTab.GRAFICO     -> "Gráfico"
    AppTab.HISTORICO   -> "Histórico"
}

// ─── Tela principal ───────────────────────────────────────────────────────────

@Composable
fun CalculatorScreen(viewModel: CalcViewModel) {
    val state by viewModel.uiState.collectAsState()
    val result = state.result
    val showSteps = state.showStepsForBase
    val history = state.history
    val expression = state.expression
    val error = state.error
    val colorMode = state.colorMode
    val reduceMotion = state.reduceMotion
    val hapticsEnabled = state.hapticsEnabled

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {

        // Expressão atual — visível em Calcular e Gráfico
        if (state.activeTab != AppTab.HISTORICO) {
            DisplayExpressao(
                expressao = expression,
                erro = error,
            )
        }

        // Seletor de abas
        TabRow(
            selectedTabIndex = state.activeTab.ordinal,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.primary,
        ) {
            AppTab.entries.forEach { aba ->
                Tab(
                    selected = state.activeTab == aba,
                    onClick = { viewModel.navegarParaAba(aba) },
                    text = {
                        Text(
                            text = rotuloAba(aba),
                            style = MaterialTheme.typography.labelSmall,
                        )
                    },
                )
            }
        }

        // Conteúdo da aba ativa
        Box(modifier = Modifier.weight(1f)) {
            AnimatedContent(
                targetState = state.activeTab,
                transitionSpec = {
                    val enter = if (reduceMotion) fadeIn(tween(0)) else fadeIn(tween(200))
                    val exit = if (reduceMotion) fadeOut(tween(0)) else fadeOut(tween(100))
                    enter togetherWith exit
                },
                label = "troca_aba",
            ) { aba ->
                when (aba) {
                    AppTab.CALCULADORA -> PainelResultados(
                        result = result,
                        showStepsForBase = showSteps,
                        onToggleSteps = { viewModel.toggleSteps(it) },
                        colorMode = colorMode,
                        modifier = Modifier.fillMaxSize(),
                    )

                    AppTab.GRAFICO -> PainelGrafico(
                        resultado = result,
                        colorMode = colorMode,
                        reduceMotion = reduceMotion,
                        modifier = Modifier.fillMaxSize(),
                    )

                    AppTab.HISTORICO -> PainelHistorico(
                        historico = history,
                        hapticsEnabled = hapticsEnabled,
                        reduceMotion = reduceMotion,
                        colorMode = colorMode,
                        onItemClick = { viewModel.restaurarDoHistorico(it) },
                        onLimpar = { viewModel.limparHistorico() },
                        onHapticsChange = { viewModel.setHapticsEnabled(it) },
                        onReduceMotionChange = { viewModel.setReduceMotion(it) },
                        onColorModeChange = { viewModel.setColorMode(it) },
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }

        // Teclado — visível em Calcular e Gráfico (atualiza gráfico em tempo real)
        AnimatedVisibility(
            visible = state.activeTab != AppTab.HISTORICO,
            enter = if (reduceMotion) androidx.compose.animation.EnterTransition.None else slideInVertically { it },
            exit = if (reduceMotion) androidx.compose.animation.ExitTransition.None else slideOutVertically { it },
        ) {
            Column(
                modifier = Modifier.navigationBarsPadding()
            ) {
                HorizontalDivider(thickness = 0.5.dp)
                PainelTeclado(
                    onKey = { viewModel.onKey(it) },
                    onEquals = { viewModel.onEquals() },
                    hapticsEnabled = hapticsEnabled,
                    modifier = Modifier.padding(8.dp),
                )
            }
        }
    }
}

// ─── Display da expressão ─────────────────────────────────────────────────────

@Composable
private fun DisplayExpressao(expressao: String, erro: String?) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Text(
            text = expressao.ifEmpty { "0" },
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.End,
            modifier = Modifier.fillMaxWidth(),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        if (erro != null) {
            Text(
                text = erro,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

// ─── Aba: Calcular ────────────────────────────────────────────────────────────

@Composable
private fun PainelResultados(
    result: CalcResult?,
    showStepsForBase: Int?,
    onToggleSteps: (Int) -> Unit,
    colorMode: ColorMode,
    modifier: Modifier = Modifier,
) {
    val bases = result?.bases ?: emptyList()

    if (bases.isEmpty()) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text(
                text = "Digite uma expressão e pressione =",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(32.dp),
            )
        }
        return
    }

    LazyColumn(
        modifier = modifier.padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        contentPadding = PaddingValues(vertical = 8.dp),
    ) {
        items(bases, key = { it.base }) { entrada ->
            CartaoBase(
                entrada = entrada,
                expandido = showStepsForBase == entrada.base,
                onToggle = { onToggleSteps(entrada.base) },
                colorMode = colorMode,
            )
        }
    }
}

// ─── Cartão de base ───────────────────────────────────────────────────────────

@Composable
private fun CartaoBase(
    entrada: BaseEntry,
    expandido: Boolean,
    onToggle: () -> Unit,
    colorMode: ColorMode
) {
    val acento = acentoDaBase(entrada.base, colorMode)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        border = BorderStroke(
            width = if (entrada.base == 10) 1.5.dp else 0.5.dp,
            color = acento.copy(alpha = 0.45f),
        ),
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {

            // Rótulo da base
            Text(
                text = entrada.label.uppercase(),
                color = acento,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.6.sp,
                ),
            )
            Spacer(Modifier.height(2.dp))

            // Valor ou erro
            if (entrada.valid) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = entrada.intPart,
                        style = MaterialTheme.typography.titleLarge,
                    )
                    if (entrada.fracPart.isNotEmpty()) {
                        Text(
                            text = ",${entrada.fracPart}",
                            style = MaterialTheme.typography.titleLarge.copy(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                            ),
                        )
                    }
                    if (entrada.repeats && entrada.repeatPart.isNotEmpty()) {
                        Text(
                            text = "(${entrada.repeatPart}…)",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = acento.copy(alpha = 0.75f),
                            ),
                            modifier = Modifier.padding(start = 3.dp),
                        )
                    }
                }
            } else {
                Text(
                    text = entrada.error ?: "inválido",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            // Botão e texto dos passos
            val textoPassos = StepsFormatter.format(entrada.steps, entrada.fracSteps)
            if (textoPassos.isNotEmpty() && entrada.base != 10) {
                Spacer(Modifier.height(4.dp))
                TextButton(
                    onClick = onToggle,
                    contentPadding = PaddingValues(0.dp),
                ) {
                    Text(
                        text = if (expandido) "ocultar passos" else "ver como converteu",
                        style = MaterialTheme.typography.labelSmall,
                        color = acento,
                    )
                }
                AnimatedVisibility(visible = expandido) {
                    Text(
                        text = textoPassos,
                        style = MaterialTheme.typography.bodySmall.copy(lineHeight = 20.sp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
            }
        }
    }
}

// ─── Aba: Gráfico ─────────────────────────────────────────────────────────────

@Composable
private fun PainelGrafico(
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

/** Barra horizontal animada representando o número de dígitos de uma base. */
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

/** Tabela mostrando cada dígito × base^posição para um resultado inteiro. */
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

// ─── Aba: Histórico ───────────────────────────────────────────────────────────

@Composable
private fun PainelHistorico(
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
                    Text(text = labelColorMode(mode))
                }
            }
        }
    }
}

private fun labelColorMode(mode: ColorMode): String = when (mode) {
    ColorMode.PADRAO -> "Padrão"
    ColorMode.ALTO_CONTRASTE -> "Alto contraste"
    ColorMode.DALTONISMO -> "Daltonismo"
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

// ─── Teclado ──────────────────────────────────────────────────────────────────

private val layoutTeclado = listOf(
    listOf("AC", "⌫", "%", "/"),
    listOf("7", "8", "9", "*"),
    listOf("4", "5", "6", "-"),
    listOf("1", "2", "3", "+"),
    listOf("0", ".", "="),
)

@Composable
private fun PainelTeclado(
    onKey: (String) -> Unit,
    onEquals: () -> Unit,
    hapticsEnabled: Boolean,
    modifier: Modifier = Modifier,
) {
    val haptics = LocalHapticFeedback.current
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        layoutTeclado.forEach { linha ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                linha.forEach { tecla ->
                    TeclaCalc(
                        label = tecla,
                        modifier = Modifier.weight(if (tecla == "0") 2f else 1f),
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
private fun TeclaCalc(label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val ehOperador = label in setOf("+", "-", "*", "/", "%", "⌫", "AC")
    val ehIgual = label == "="

    Button(
        onClick = onClick,
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
