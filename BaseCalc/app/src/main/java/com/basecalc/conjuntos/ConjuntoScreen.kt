package com.basecalc.conjuntos

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.basecalc.core.conjuntos.*

// ── Cores ────────────────────────────────────────────────────────────────────

@Composable private fun colUniao()  = MaterialTheme.colorScheme.primary
@Composable private fun colInter()  = Color(0xFF2E7D32)
@Composable private fun colDiff()   = Color(0xFFBA7517)
@Composable private fun colComp()   = MaterialTheme.colorScheme.tertiary
@Composable private fun colGreen()  = Color(0xFF2E7D32)
@Composable private fun colRed()    = MaterialTheme.colorScheme.error

// ─── Tela principal ───────────────────────────────────────────────────────────

@Composable
fun ConjuntoScreen(viewModel: ConjuntoViewModel) {
    val state by viewModel.state.collectAsState()
    Column(modifier = Modifier.fillMaxSize()) {
        // Tabs de modo
        ModoTabs(state.modo) { viewModel.setModo(it) }
        when (state.modo) {
            ModoConjunto.OPERACAO    -> OperacaoPane(state, viewModel)
            ModoConjunto.PERTINENCIA -> PertinenciaPane(state, viewModel)
            ModoConjunto.SUBCONJUNTO -> SubconjuntoPane(state, viewModel)
        }
    }
}

// ── Tabs ──────────────────────────────────────────────────────────────────────

@Composable
private fun ModoTabs(modo: ModoConjunto, onChange: (ModoConjunto) -> Unit) {
    val tabs = listOf(
        ModoConjunto.OPERACAO    to "∪ ∩ −",
        ModoConjunto.PERTINENCIA to "∈ ∉",
        ModoConjunto.SUBCONJUNTO to "⊂ ⊄",
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        tabs.forEach { (m, label) ->
            val selected = m == modo
            Button(
                onClick = { onChange(m) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selected) MaterialTheme.colorScheme.primary
                                     else MaterialTheme.colorScheme.surface,
                    contentColor   = if (selected) MaterialTheme.colorScheme.onPrimary
                                     else MaterialTheme.colorScheme.onSurface,
                ),
                contentPadding = PaddingValues(8.dp),
            ) {
                Text(
                    text  = label,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Normal,
                        fontSize   = 16.sp,
                    )
                )
            }
        }
    }
}

// ── Painel de Operações ───────────────────────────────────────────────────────

@Composable
private fun OperacaoPane(state: ConjuntoUiState, vm: ConjuntoViewModel) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Definição dos conjuntos
        item { SectionLabel("conjuntos definidos") }
        item { ConjuntosEditor(state.conjuntos) { nome, val_ -> vm.setConjunto(nome, val_) } }

        // Expressão atual
        item { SectionLabel("montar operação") }
        item { ExpressionBuilder(state.tokens) }

        // Teclado de operadores
        item { OperatorKeypad(vm) }

        // Ações
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { vm.backspace() },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) { Text("⌫", fontFamily = FontFamily.Monospace) }
                OutlinedButton(
                    onClick = { vm.clearExpression() },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) { Text("AC", fontFamily = FontFamily.Monospace) }
                Button(
                    onClick = { vm.calcular() },
                    modifier = Modifier.weight(2f),
                    shape = RoundedCornerShape(10.dp)
                ) { Text("calcular →", fontFamily = FontFamily.Monospace) }
            }
        }

        // Erro
        state.erro?.let { err ->
            item {
                Text(err, color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall)
            }
        }

        // Resultado
        state.resultadoOp?.let { r ->
            item { SectionLabel("resultado") }
            item { ResultadoCard(r) }
        }

        // Exercícios prontos
        item { SectionLabel("exercícios da lista") }
        item { ExerciciosProntos(vm) }

        // Histórico
        if (state.historico.size > 1) {
            item { SectionLabel("histórico") }
            items(state.historico.drop(1)) { r ->
                HistoricoItem(r)
            }
        }
    }
}

// ── Editor de conjuntos ───────────────────────────────────────────────────────

@Composable
private fun ConjuntosEditor(
    conjuntos: Map<String, String>,
    onChange: (String, String) -> Unit
) {
    val nomes = listOf("U","A","B","C","D","E")
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        nomes.forEach { nome ->
            var text by remember(conjuntos[nome]) { mutableStateOf(conjuntos[nome] ?: "") }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text  = nome,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                    ),
                    color = if (nome == "U") MaterialTheme.colorScheme.tertiary
                            else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.width(20.dp)
                )
                Text("=", color = MaterialTheme.colorScheme.onSurface.copy(.4f),
                    fontFamily = FontFamily.Monospace, fontSize = 13.sp)
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it; onChange(nome, it) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Monospace
                    ),
                    shape = RoundedCornerShape(8.dp),
                    placeholder = { Text("elementos separados por vírgula",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(.3f)) }
                )
            }
        }
    }
}

// ── Expressão atual ───────────────────────────────────────────────────────────

@Composable
private fun ExpressionBuilder(tokens: List<String>) {
    val ops = setOf("∪","∩","−","ᶜ","(",")"); val uni = MaterialTheme.colorScheme.primary
    val inter = Color(0xFF2E7D32); val diff = Color(0xFFBA7517)
    val comp = MaterialTheme.colorScheme.tertiary
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(10.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        if (tokens.isEmpty()) {
            Text("—", color = MaterialTheme.colorScheme.onSurface.copy(.3f),
                fontFamily = FontFamily.Monospace, fontSize = 20.sp)
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier.horizontalScroll(rememberScrollState())) {
                tokens.forEach { t ->
                    Text(
                        text  = t,
                        color = when (t) {
                            "∪" -> uni; "∩" -> inter; "−" -> diff
                            "ᶜ" -> comp; else -> MaterialTheme.colorScheme.onSurface
                        },
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Normal
                        )
                    )
                }
            }
        }
    }
}

// ── Teclado ───────────────────────────────────────────────────────────────────

@Composable
private fun OperatorKeypad(vm: ConjuntoViewModel) {
    val nomes = listOf("U","A","B","C","D","E")
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        // Conjuntos
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            nomes.forEach { n ->
                ConjKey(n, Modifier.weight(1f)) { vm.addToken(n) }
            }
        }
        // Operadores
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            OpKey("∪", "união",    Color(0xFF185FA5), Modifier.weight(1f)) { vm.addToken("∪") }
            OpKey("∩", "inter.",   Color(0xFF2E7D32), Modifier.weight(1f)) { vm.addToken("∩") }
            OpKey("−", "dif.",     Color(0xFFBA7517), Modifier.weight(1f)) { vm.addToken("−") }
            OpKey("ᶜ", "comp.",    Color(0xFF534AB7), Modifier.weight(1f)) { vm.addToken("ᶜ") }
            OpKey("(", "",         Color(0xFF5F5E5A), Modifier.weight(0.7f)) { vm.addToken("(") }
            OpKey(")", "",         Color(0xFF5F5E5A), Modifier.weight(0.7f)) { vm.addToken(")") }
        }
    }
}

@Composable
private fun ConjKey(label: String, modifier: Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier.height(44.dp),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor   = MaterialTheme.colorScheme.onSecondaryContainer,
        ),
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(label, fontFamily = FontFamily.Monospace,
            fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun OpKey(sym: String, sub: String, color: Color, modifier: Modifier, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(52.dp),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(.5.dp, color.copy(.4f)),
        contentPadding = PaddingValues(0.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(sym, color = color, fontFamily = FontFamily.Monospace,
                fontSize = 18.sp, fontWeight = FontWeight.Normal)
            if (sub.isNotEmpty())
                Text(sub, color = color.copy(.6f), fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace)
        }
    }
}

// ── Resultado ─────────────────────────────────────────────────────────────────

@Composable
private fun ResultadoCard(r: ConjuntoResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            1.5.dp,
            when (r) {
                is ConjuntoResult.Ok    -> MaterialTheme.colorScheme.primary
                is ConjuntoResult.Erro  -> MaterialTheme.colorScheme.error
            }
        ),
    ) {
        Column(modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)) {
            when (r) {
                is ConjuntoResult.Erro -> Text(r.message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium)
                is ConjuntoResult.Ok -> {
                    val n = r.notation
                    // Resultado principal
                    Text(
                        text  = n.extensao,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Normal,
                            color      = if (n.vazio) MaterialTheme.colorScheme.onSurface.copy(.5f)
                                         else Color(0xFF2E7D32)
                        )
                    )
                    // Notação formal
                    NotacaoBox(r)
                    // Passos
                    if (r.steps.isNotEmpty()) PassosBox(r.steps)
                }
            }
        }
    }
}

@Composable
private fun NotacaoBox(r: ConjuntoResult.Ok) {
    val n = r.notation
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("notação formal", style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(.5f))
            // Extensão
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically) {
                Text("(${r.expression}) =", fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                Text(n.extensao, fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp, color = Color(0xFF2E7D32))
            }
            // Cardinalidade
            Text("n(${r.expression}) = ${n.cardinalidade}",
                fontFamily = FontFamily.Monospace, fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(.6f))
            // Vazio
            if (n.vazio) Text("(${r.expression}) = ∅",
                fontFamily = FontFamily.Monospace, fontSize = 12.sp,
                color = MaterialTheme.colorScheme.error)
            // Pertinências (se poucos elementos)
            if (n.pertinencias.isNotEmpty()) {
                Text(n.pertinencias.take(6).joinToString("   "),
                    fontFamily = FontFamily.Monospace, fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(.5f),
                    lineHeight = 18.sp)
            }
        }
    }
}

@Composable
private fun PassosBox(steps: List<Step>) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(.5.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("passos", style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(.5f))
            steps.forEach { s ->
                val direita = s.direita?.let { ConjuntoEngine.formatSet(it) } ?: ""
                val lhs = ConjuntoEngine.formatSet(s.esquerda)
                val res = ConjuntoEngine.formatSet(s.resultado)
                val stepStr = if (s.direita != null)
                    "$lhs ${s.operacao} $direita = $res"
                else
                    "${s.operacao}($lhs) = $res"
                Text(stepStr, fontFamily = FontFamily.Monospace, fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(.7f), lineHeight = 18.sp)
            }
        }
    }
}

// ── Exercícios prontos ────────────────────────────────────────────────────────

@Composable
private fun ExerciciosProntos(vm: ConjuntoViewModel) {
    val items = listOf(
        "3a" to "C∪D", "3b" to "A∪D", "3c" to "B−E", "3d" to "A−C",
        "3e" to "C−A", "3f" to "B∪D∪E", "3g" to "(B−E)∩C",
        "3h" to "A∪(C∩D)", "3i" to "(A−C)∪(B−E)", "3j" to "(C∩D)∪(A−C)",
        "2a" to "Eᶜ", "2b" to "Bᶜ", "2c" to "(Aᶜ)ᶜ",
    )
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        items.chunked(3).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                row.forEach { (label, expr) ->
                    OutlinedButton(
                        onClick = { vm.rodarExercicio(expr) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(6.dp),
                        border = BorderStroke(.5.dp,
                            MaterialTheme.colorScheme.outline.copy(.4f))
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(label, fontSize = 9.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(.5f))
                            Text(expr, fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp, textAlign = TextAlign.Center)
                        }
                    }
                }
                // Completar linha se < 3 itens
                repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

// ── Histórico ─────────────────────────────────────────────────────────────────

@Composable
private fun HistoricoItem(r: ConjuntoResult.Ok) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(r.expression, fontFamily = FontFamily.Monospace, fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(.7f),
            modifier = Modifier.weight(1f))
        Text(r.notation.extensao, fontFamily = FontFamily.Monospace, fontSize = 13.sp,
            color = Color(0xFF2E7D32))
    }
}

// ── Pertinência ───────────────────────────────────────────────────────────────

@Composable
private fun PertinenciaPane(state: ConjuntoUiState, vm: ConjuntoViewModel) {
    var elem by remember { mutableStateOf("") }
    var conj by remember { mutableStateOf("A") }
    val nomes = listOf("U","A","B","C","D","E")

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SectionLabel("verificar pertinência (∈ / ∉)")
        OutlinedTextField(
            value = elem, onValueChange = { elem = it },
            label = { Text("elemento") },
            singleLine = true, modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace)
        )
        SectionLabel("em qual conjunto?")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            nomes.forEach { n ->
                ConjKey(n, Modifier.weight(1f)) { conj = n }
            }
        }
        Text(
            "verificando: $elem ∈ $conj ?",
            fontFamily = FontFamily.Monospace, fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(.6f)
        )
        Button(
            onClick = { vm.verificarPertinencia(elem, conj) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
        ) { Text("verificar →", fontFamily = FontFamily.Monospace) }

        state.resultadoMember?.let { r ->
            MembershipCard(r)
        }
    }
}

@Composable
private fun MembershipCard(r: MembershipResult) {
    val color = if (r.pertence) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
    Card(
        modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(1.5.dp, color),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(r.notacao, style = MaterialTheme.typography.headlineSmall.copy(
                fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Normal,
                color = color))
            Text(if (r.pertence) "VERDADEIRO" else "FALSO",
                style = MaterialTheme.typography.labelMedium,
                color = color)
            Surface(shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()) {
                Text(r.justificativa,
                    modifier = Modifier.padding(10.dp),
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Monospace),
                    color = MaterialTheme.colorScheme.onSurface.copy(.7f),
                    lineHeight = 18.sp)
            }
        }
    }
}

// ── Subconjunto ───────────────────────────────────────────────────────────────

@Composable
private fun SubconjuntoPane(state: ConjuntoUiState, vm: ConjuntoViewModel) {
    var selA by remember { mutableStateOf("E") }
    var selB by remember { mutableStateOf("B") }
    val nomes = listOf("U","A","B","C","D","E")

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SectionLabel("verificar subconjunto (⊂ / ⊄)")
        SectionLabel("primeiro conjunto")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            nomes.forEach { n ->
                val selected = n == selA
                OutlinedButton(
                    onClick = { selA = n },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(if (selected) 2.dp else .5.dp,
                        if (selected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outline.copy(.4f)),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(n, fontFamily = FontFamily.Monospace,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                        color = if (selected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurface)
                }
            }
        }
        SectionLabel("segundo conjunto")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            nomes.forEach { n ->
                val selected = n == selB
                OutlinedButton(
                    onClick = { selB = n },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(if (selected) 2.dp else .5.dp,
                        if (selected) MaterialTheme.colorScheme.tertiary
                        else MaterialTheme.colorScheme.outline.copy(.4f)),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(n, fontFamily = FontFamily.Monospace,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                        color = if (selected) MaterialTheme.colorScheme.tertiary
                                else MaterialTheme.colorScheme.onSurface)
                }
            }
        }
        Text("verificando: $selA ⊂ $selB ?",
            fontFamily = FontFamily.Monospace, fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(.6f))
        Button(
            onClick = { vm.verificarSubconjunto(selA, selB) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
        ) { Text("verificar →", fontFamily = FontFamily.Monospace) }

        state.resultadoSubset?.let { r ->
            SubsetCard(r)
        }
    }
}

@Composable
private fun SubsetCard(r: SubsetResult) {
    val color = if (r.contido) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
    Card(
        modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(1.5.dp, color),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(r.notacao, style = MaterialTheme.typography.headlineSmall.copy(
                fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Normal,
                color = color))
            Text(if (r.contido) "VERDADEIRO" else "FALSO",
                style = MaterialTheme.typography.labelMedium, color = color)
            Surface(shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("${r.nomeA} = ${ConjuntoEngine.formatSet(r.conjA)}",
                        fontFamily = FontFamily.Monospace, fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(.7f))
                    Text("${r.nomeB} = ${ConjuntoEngine.formatSet(r.conjB)}",
                        fontFamily = FontFamily.Monospace, fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(.7f))
                    Text(r.justificativa,
                        fontFamily = FontFamily.Monospace, fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(.6f),
                        lineHeight = 18.sp)
                    if (r.elementosFaltando.isNotEmpty()) {
                        Text("faltam: {${r.elementosFaltando.joinToString(", ")}}",
                            fontFamily = FontFamily.Monospace, fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

@Composable
private fun SectionLabel(text: String) {
    Text(
        text  = text,
        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = .08.sp),
        color = MaterialTheme.colorScheme.onSurface.copy(.4f),
        modifier = Modifier.padding(top = 4.dp)
    )
}
