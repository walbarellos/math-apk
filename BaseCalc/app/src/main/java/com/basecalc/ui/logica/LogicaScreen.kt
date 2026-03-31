package com.basecalc.ui.logica

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.basecalc.CalcViewModel
import com.basecalc.core.logica.ColunaLogica
import com.basecalc.core.logica.TabelaVerdade
import com.basecalc.core.logica.TipoFormula

// ─── Paleta ───────────────────────────────────────────────────────────────────

private val corVerdadeiro = Color(0xFF2E7D32)
private val corFalso = Color(0xFFC62828)
private val bgVerdadeiro = Color(0xFFE8F5E9)
private val bgFalso = Color(0xFFFFEBEE)
private val corResposta = Color(0xFF1565C0)
private val bgResposta = Color(0xFFE3F2FD)

// ─── Símbolos do teclado lógico ───────────────────────────────────────────────

private data class SimboloKey(val display: String, val insert: String, val desc: String)

private val teclado1 = listOf(
    SimboloKey("¬", "¬", "NÃO"),
    SimboloKey("∧", "∧", "E"),
    SimboloKey("∨", "∨", "OU"),
    SimboloKey("⊕", "⊕", "XOR"),
    SimboloKey("→", "→", "SE...ENTÃO"),
    SimboloKey("↔", "↔", "SE E SÓ SE"),
)
private val teclado2 = listOf(
    SimboloKey("(", "(", "Abre"),
    SimboloKey(")", ")", "Fecha"),
    SimboloKey("p", "p", "var p"),
    SimboloKey("q", "q", "var q"),
    SimboloKey("r", "r", "var r"),
    SimboloKey("s", "s", "var s"),
)

// ─── Tela principal ───────────────────────────────────────────────────────────

@Composable
fun LogicaScreen(viewModel: CalcViewModel) {
    val state by viewModel.uiState.collectAsState()
    val ls = state.logicaState

    // Controla o TextFieldValue localmente para manipular cursor
    var tfv by remember { mutableStateOf(TextFieldValue(ls.formula)) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(
            text = "Lógica Proposicional",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )

        // Input da fórmula
        FormulaInputCard(
            tfv = tfv,
            onTfvChange = { novo ->
                tfv = novo
                viewModel.setFormulaLogica(novo.text)
            },
            onSimbolo = { simbolo ->
                val t = tfv.text
                val cursor = tfv.selection.end.coerceIn(0, t.length)
                val novoTexto = t.substring(0, cursor) + simbolo + t.substring(cursor)
                val novoCursor = cursor + simbolo.length
                tfv = TextFieldValue(novoTexto, TextRange(novoCursor))
                viewModel.setFormulaLogica(novoTexto)
            },
            onLimpar = {
                tfv = TextFieldValue("")
                viewModel.setFormulaLogica("")
            },
            onCalcular = { viewModel.calcularLogica() },
        )

        // Erro de sintaxe
        AnimatedVisibility(visible = ls.erro != null) {
            ls.erro?.let { erro ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = "⚠  $erro",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(12.dp),
                    )
                }
            }
        }

        // Resultado
        AnimatedVisibility(
            visible = ls.tabela != null,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut(),
        ) {
            ls.tabela?.let { tabela ->
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    TipoFormulaCard(tabela.tipo)
                    PassosCard(tabela.passos)
                    TabelaVerdadeCard(tabela)
                    LegendaCard()
                }
            }
        }

        Spacer(Modifier.height(32.dp))
    }
}

// ─── Card de input + teclado simbólico ───────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FormulaInputCard(
    tfv: TextFieldValue,
    onTfvChange: (TextFieldValue) -> Unit,
    onSimbolo: (String) -> Unit,
    onLimpar: () -> Unit,
    onCalcular: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "Digite a fórmula  (ex: (p ∨ q) ∧ (¬p ∨ q))",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            OutlinedTextField(
                value = tfv,
                onValueChange = onTfvChange,
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 18.sp,
                ),
                placeholder = { Text("(p ∨ q) ∧ (¬p ∨ q)", fontFamily = FontFamily.Monospace) },
                singleLine = true,
            )

            // Fila 1: conectivos
            Text(
                text = "Conectivos",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp),
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                teclado1.forEach { key ->
                    SimboloButton(
                        display = key.display,
                        desc = key.desc,
                        onClick = { onSimbolo(key.insert) },
                        isPrimary = true,
                    )
                }
            }

            // Fila 2: parênteses + variáveis
            Text(
                text = "Variáveis e parênteses",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                teclado2.forEach { key ->
                    SimboloButton(
                        display = key.display,
                        desc = key.desc,
                        onClick = { onSimbolo(key.insert) },
                        isPrimary = false,
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                TextButton(onClick = onLimpar, modifier = Modifier.weight(1f)) {
                    Text("Limpar")
                }
                Button(
                    onClick = onCalcular,
                    modifier = Modifier.weight(2f),
                    enabled = tfv.text.isNotBlank(),
                ) {
                    Text("Gerar Tabela-Verdade")
                }
            }
        }
    }
}

@Composable
private fun SimboloButton(
    display: String,
    desc: String,
    isPrimary: Boolean,
    onClick: () -> Unit,
) {
    val containerColor = if (isPrimary)
        MaterialTheme.colorScheme.primaryContainer
    else
        MaterialTheme.colorScheme.secondaryContainer

    val contentColor = if (isPrimary)
        MaterialTheme.colorScheme.onPrimaryContainer
    else
        MaterialTheme.colorScheme.onSecondaryContainer

    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
        ),
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
        modifier = Modifier.height(40.dp),
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = display,
                fontFamily = FontFamily.Monospace,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 18.sp,
            )
            Text(
                text = desc,
                fontSize = 7.sp,
                lineHeight = 8.sp,
                maxLines = 1,
            )
        }
    }
}

// ─── Card de classificação ────────────────────────────────────────────────────

@Composable
private fun TipoFormulaCard(tipo: TipoFormula) {
    val (bg, fg) = when (tipo) {
        TipoFormula.TAUTOLOGIA   -> Color(0xFFE8F5E9) to corVerdadeiro
        TipoFormula.CONTRADICAO  -> Color(0xFFFFEBEE) to corFalso
        TipoFormula.CONTINGENCIA -> Color(0xFFFFF8E1) to Color(0xFFE65100)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = bg),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Surface(
                shape = CircleShape,
                color = fg,
                modifier = Modifier.size(36.dp),
            ) {
                Text(
                    text = tipo.emoji,
                    modifier = Modifier.padding(6.dp),
                    textAlign = TextAlign.Center,
                    fontSize = 16.sp,
                )
            }
            Column {
                Text(
                    text = tipo.label,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = fg,
                )
                Text(
                    text = when (tipo) {
                        TipoFormula.TAUTOLOGIA   -> "A coluna resposta é sempre V"
                        TipoFormula.CONTRADICAO  -> "A coluna resposta é sempre F"
                        TipoFormula.CONTINGENCIA -> "A coluna resposta varia com os valores"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = fg.copy(alpha = 0.8f),
                )
            }
        }
    }
}

// ─── Card de passos ───────────────────────────────────────────────────────────

@Composable
private fun PassosCard(passos: List<String>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "Ordem de resolução",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Spacer(Modifier.height(8.dp))
            passos.forEach { passo ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Text(
                        text = "→",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(end = 6.dp, top = 1.dp),
                    )
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

// ─── Tabela-verdade ───────────────────────────────────────────────────────────

@Composable
fun TabelaVerdadeCard(tabela: TabelaVerdade) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Tabela-Verdade",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "${tabela.colunas.first { it.isVariavel }.valores.size} linhas · ${tabela.colunas.size} colunas",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(Modifier.height(10.dp))

            // Scroll horizontal para tabelas largas
            Box(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                    tabela.colunas.forEachIndexed { idx, coluna ->
                        val isUltimaVariavel = coluna.isVariavel &&
                            tabela.colunas.getOrNull(idx + 1)?.isVariavel == false
                        ColunaTabela(
                            coluna = coluna,
                            sepDireita = isUltimaVariavel,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ColunaTabela(coluna: ColunaLogica, sepDireita: Boolean) {
    val largura = when {
        coluna.isVariavel -> 36.dp
        coluna.expressao.length <= 5 -> 52.dp
        coluna.expressao.length <= 10 -> 80.dp
        else -> 110.dp
    }

    val bgHeader = when {
        coluna.isResposta -> corResposta
        coluna.isVariavel -> MaterialTheme.colorScheme.surfaceVariant
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val fgHeader = when {
        coluna.isResposta -> Color.White
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Column(
        modifier = Modifier
            .width(largura)
            .then(
                if (sepDireita) Modifier.border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(0.dp)
                ) else Modifier
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Cabeçalho
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(bgHeader)
                .padding(horizontal = 4.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = coluna.expressao,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = if (coluna.isResposta) FontWeight.Bold else FontWeight.Medium,
                    fontSize = if (coluna.isVariavel) 13.sp else 10.sp,
                ),
                color = fgHeader,
                textAlign = TextAlign.Center,
                maxLines = 2,
            )
        }

        // Divisor
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
        )

        // Células
        coluna.valores.forEachIndexed { linhaIdx, valor ->
            val bgCell = when {
                coluna.isResposta && valor  -> bgResposta
                coluna.isResposta && !valor -> bgFalso.copy(alpha = 0.4f)
                valor  -> bgVerdadeiro.copy(alpha = 0.25f)
                else   -> bgFalso.copy(alpha = 0.15f)
            }
            val fgCell = if (valor) corVerdadeiro else corFalso

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(bgCell)
                    .padding(vertical = 6.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = if (valor) "V" else "F",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = if (coluna.isResposta) FontWeight.ExtraBold else FontWeight.Bold,
                        fontSize = if (coluna.isResposta) 15.sp else 13.sp,
                    ),
                    color = fgCell,
                )
            }

            // Linha divisória fina entre linhas
            if (linhaIdx < coluna.valores.lastIndex) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(0.5.dp)
                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
                )
            }
        }
    }
}

// ─── Legenda ──────────────────────────────────────────────────────────────────

@Composable
private fun LegendaCard() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LegendaItem(cor = corVerdadeiro, bg = bgVerdadeiro, texto = "V = Verdadeiro")
        LegendaItem(cor = corFalso, bg = bgFalso, texto = "F = Falso")
        LegendaItem(cor = corResposta, bg = bgResposta, texto = "Coluna resposta")
    }
}

@Composable
private fun LegendaItem(cor: Color, bg: Color, texto: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(cor),
        )
        Text(
            text = texto,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
