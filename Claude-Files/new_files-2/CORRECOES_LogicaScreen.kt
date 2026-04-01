// ============================================================
// ARQUIVO: LogicaScreen.kt
// CORREÇÃO — Destaque da coluna resposta visível em dark mode
// ============================================================

// ── SUBSTITUIR as 4 constantes de cor no topo do arquivo ────
//
// De:
//   private val corResposta = Color(0xFF1565C0)
//   private val bgResposta = Color(0xFFE3F2FD)
//
// Para o bloco abaixo (dentro do Composable que renderiza a tabela,
// usar MaterialTheme para que funcione em dark mode):

// ── No Composable ColunaTabela, adicionar este bloco de cores ─

// Cores que funcionam em AMBOS os modos (light e dark):
val corResposta = MaterialTheme.colorScheme.primary
val bgRespostaHeader = MaterialTheme.colorScheme.primary          // fundo do header
val fgRespostaHeader = MaterialTheme.colorScheme.onPrimary        // texto no header

// Célula verdadeiro na coluna resposta:
val bgRespostaTrue  = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
val fgRespostaTrue  = MaterialTheme.colorScheme.primary

// Célula falso na coluna resposta:
val bgRespostaFalse = MaterialTheme.colorScheme.error.copy(alpha = 0.12f)
val fgRespostaFalse = MaterialTheme.colorScheme.error

// ── Header da coluna resposta — SUBSTITUIR o Text existente ──
//
// De:
//   Text(text = coluna.expressao, ...)
//
// Para:
Box(
    modifier = Modifier
        .fillMaxWidth()
        .background(
            color = if (coluna.isResposta) bgRespostaHeader
                    else MaterialTheme.colorScheme.surfaceVariant
        )
        .padding(horizontal = 4.dp, vertical = 6.dp),
    contentAlignment = Alignment.Center,
) {
    Text(
        text = if (coluna.isResposta) "▶ ${coluna.expressao}" else coluna.expressao,
        style = MaterialTheme.typography.labelSmall.copy(
            fontFamily   = FontFamily.Monospace,
            fontWeight   = if (coluna.isResposta) FontWeight.Bold else FontWeight.Medium,
            fontSize     = if (coluna.isVariavel) 13.sp
                           else if (coluna.isResposta) 12.sp
                           else 10.sp,
        ),
        color = if (coluna.isResposta) fgRespostaHeader
                else MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        maxLines  = 2,
    )
}

// ── Células — SUBSTITUIR o bloco bgCell / fgCell ─────────────
//
// De:
//   val bgCell = when { ... }
//   val fgCell = ...
//
// Para:
val bgCell = when {
    coluna.isResposta && valor  -> bgRespostaTrue
    coluna.isResposta && !valor -> bgRespostaFalse
    valor                       -> Color(0xFF2E7D32).copy(alpha = 0.12f)
    else                        -> Color(0xFFC62828).copy(alpha = 0.08f)
}
val fgCell = when {
    coluna.isResposta && valor  -> fgRespostaTrue
    coluna.isResposta && !valor -> fgRespostaFalse
    valor                       -> Color(0xFF2E7D32)
    else                        -> Color(0xFFC62828)
}

// ── Borda da coluna resposta — adicionar no Modifier da Column ─
//
// Adicionar ao Modifier da Column que envolve header + células:
Modifier
    .width(largura)
    .then(
        if (coluna.isResposta) Modifier.border(
            width = 2.dp,
            color = corResposta,
            shape = RoundedCornerShape(6.dp)
        ) else Modifier
    )
