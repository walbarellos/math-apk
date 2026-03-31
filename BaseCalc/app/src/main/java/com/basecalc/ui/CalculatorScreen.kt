package com.basecalc.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.basecalc.*
import com.basecalc.ui.components.*
import com.basecalc.ui.conjuntos.ConjuntosScreen
import com.basecalc.ui.matrizes.MatrizesScreen
import com.basecalc.ui.logica.LogicaScreen

// ─── Rótulos das abas ─────────────────────────────────────────────────────────

private fun rotuloAba(aba: AppTab) = when (aba) {
    AppTab.CALCULADORA -> "Calcular"
    AppTab.LOGICA      -> "Lógica"
    AppTab.CONJUNTOS   -> "Conjuntos"
    AppTab.MATRIZES    -> "Matrizes"
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
            .background(MaterialTheme.colorScheme.background)
            .pointerInput(Unit) {
                detectTapGestures(onLongPress = { viewModel.toggleModoDiscreta() })
            }
    ) {

        // BaseSelector visível apenas em Calculadora
        if (state.activeTab == AppTab.CALCULADORA) {
            BaseSelector(
                selectedBase = state.inputBase,
                onBaseSelected = { viewModel.setInputBase(it) }
            )
        }

        // DisplayExpressao visível em Calculadora e Gráfico
        if (state.activeTab == AppTab.CALCULADORA || state.activeTab == AppTab.GRAFICO) {
            DisplayExpressao(
                expressao = expression,
                erro = error,
            )
        }

        // Seletor de abas
        ScrollableTabRow(
            selectedTabIndex = state.activeTab.ordinal,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.primary,
            edgePadding = 8.dp,
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

                    AppTab.LOGICA -> LogicaScreen(viewModel = viewModel)

                    AppTab.CONJUNTOS -> ConjuntosScreen(viewModel = viewModel)

                    AppTab.MATRIZES -> MatrizesScreen(viewModel = viewModel)

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

        // Teclado — visível apenas em Calculadora
        AnimatedVisibility(
            visible = state.activeTab == AppTab.CALCULADORA,
            enter = if (reduceMotion) EnterTransition.None else slideInVertically { it },
            exit = if (reduceMotion) ExitTransition.None else slideOutVertically { it },
        ) {
            Column(
                modifier = Modifier.navigationBarsPadding()
            ) {
                HorizontalDivider(thickness = 0.5.dp)
                PainelTeclado(
                    onKey = { viewModel.onKey(it) },
                    onEquals = { viewModel.onEquals() },
                    hapticsEnabled = hapticsEnabled,
                    inputBase = state.inputBase,
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

// ─── Seletor de Base ──────────────────────────────────────────────────────────

@Composable
private fun BaseSelector(selectedBase: Int, onBaseSelected: (Int) -> Unit) {
    val bases = listOf(2, 8, 10, 16)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Base de entrada:",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        bases.forEach { base ->
            FilterChip(
                selected = selectedBase == base,
                onClick = { onBaseSelected(base) },
                label = { Text("b$base", style = MaterialTheme.typography.labelSmall) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
        
        // Botão para outras bases (opcional)
        if (selectedBase !in bases) {
            FilterChip(
                selected = true,
                onClick = { },
                label = { Text("b$selectedBase", style = MaterialTheme.typography.labelSmall) }
            )
        }
    }
}
