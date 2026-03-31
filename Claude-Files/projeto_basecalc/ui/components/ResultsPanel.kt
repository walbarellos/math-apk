package com.basecalc.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.basecalc.ColorMode
import com.basecalc.core.conversion.StepsFormatter
import com.basecalc.core.model.BaseEntry
import com.basecalc.core.model.CalcResult
import com.basecalc.core.number.ConjuntoNumerico
import com.basecalc.ui.components.OperationVisualPanel
import com.basecalc.ui.components.acentoDaBase

@Composable
fun PainelResultados(
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
        // Carry Visual (se disponível para a base de entrada)
        result?.operationVisual?.let { visual ->
            item {
                OperationVisualPanel(
                    visual = visual,
                    colorMode = colorMode,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
        }

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
                
                // Informações adicionais
                if (entrada.additionalInfo.isNotEmpty()) {
                    Text(
                        text = entrada.additionalInfo.joinToString(" • "),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            color = acento.copy(alpha = 0.8f)
                        ),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                // Conjunto numérico (apenas na base 10)
                if (entrada.base == 10) {
                    BadgeConjunto(entrada.conjunto, MaterialTheme.colorScheme)
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
            if (entrada.valid && entrada.base != 10) {
                Spacer(Modifier.height(4.dp))
                TextButton(
                    onClick = onToggle,
                    contentPadding = PaddingValues(0.dp),
                ) {
                    Text(
                        text = if (expandido) "ocultar detalhes" else "ver detalhes e passos",
                        style = MaterialTheme.typography.labelSmall,
                        color = acento,
                    )
                }
                AnimatedVisibility(visible = expandido) {
                    Column {
                        if (textoPassos.isNotEmpty()) {
                            Text(
                                text = "Passos da conversão:",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                            )
                            Text(
                                text = textoPassos,
                                style = MaterialTheme.typography.bodySmall.copy(lineHeight = 20.sp),
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            )
                        }
                        
                        // Tabela de Potências
                        TabelaPotencias(entrada.base, acento)
                        
                        // Mapeamento de dígitos (se base > 10)
                        if (entrada.base > 10) {
                            MapeamentoDigitos(entrada.base, acento)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MapeamentoDigitos(base: Int, acento: androidx.compose.ui.graphics.Color) {
    Column(modifier = Modifier.padding(top = 8.dp)) {
        Text(
            text = "Dígitos extras:",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
        )
        Spacer(Modifier.height(4.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            (10 until base).forEach { d ->
                val char = com.basecalc.core.util.DigitSymbols.digitToChar(d)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = char.toString(),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = acento
                    )
                    Text(
                        text = d.toString(),
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
private fun TabelaPotencias(base: Int, acento: androidx.compose.ui.graphics.Color) {
    Column(modifier = Modifier.padding(top = 8.dp)) {
        Text(
            text = "Potências de $base:",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
        )
        Spacer(Modifier.height(4.dp))
        
        // Vamos mostrar de 0 a 10 (ou menos dependendo da base para não ficar gigante)
        val limit = when(base) {
            2 -> 10
            3 -> 8
            in 4..6 -> 6
            in 7..10 -> 5
            else -> 4
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            (0..limit).forEach { n ->
                val res = java.math.BigInteger.valueOf(base.toLong()).pow(n)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$base${toSuperscript(n)}",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = acento
                    )
                    Text(
                        text = res.toString(),
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

private fun toSuperscript(n: Int): String {
    val chars = n.toString().map {
        when (it) {
            '0' -> '⁰'
            '1' -> '¹'
            '2' -> '²'
            '3' -> '³'
            '4' -> '⁴'
            '5' -> '⁵'
            '6' -> '⁶'
            '7' -> '⁷'
            '8' -> '⁸'
            '9' -> '⁹'
            else -> it
        }
    }
    return chars.joinToString("")
}

@Composable
private fun BadgeConjunto(conjunto: ConjuntoNumerico, colorScheme: androidx.compose.material3.ColorScheme) {
    val corConjunto = when (conjunto) {
        ConjuntoNumerico.NATURAL -> androidx.compose.ui.graphics.Color(0xFF2196F3)
        ConjuntoNumerico.INTEIRO -> androidx.compose.ui.graphics.Color(0xFF4CAF50)
        ConjuntoNumerico.RACIONAL -> androidx.compose.ui.graphics.Color(0xFFFF9800)
        ConjuntoNumerico.IRRACIONAL -> androidx.compose.ui.graphics.Color(0xFFE91E63)
        ConjuntoNumerico.REAL -> androidx.compose.ui.graphics.Color(0xFF9C27B0)
    }
    
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = corConjunto.copy(alpha = 0.15f),
        modifier = Modifier.padding(top = 4.dp)
    ) {
        Text(
            text = "${conjunto.simbolo} ⊂ ${ConjuntoNumerico.REAL.simbolo}",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
            ),
            color = corConjunto,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
        )
    }
}
