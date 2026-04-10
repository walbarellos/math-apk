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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput

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
            ModoConjunto.EXERCICIO1  -> Exercicio1Pane(state)
            ModoConjunto.PROVA       -> ProvaPane(state, viewModel)
            ModoConjunto.VENN        -> VennPane(state, viewModel)
        }
    }
}

// ── Tabs ──────────────────────────────────────────────────────────────────────

@Composable
private fun ModoTabs(modo: ModoConjunto, onChange: (ModoConjunto) -> Unit) {
    val grupo1 = listOf(
        ModoConjunto.OPERACAO to "∪∩−",
        ModoConjunto.PERTINENCIA to "∈∉",
        ModoConjunto.SUBCONJUNTO to "⊂⊄",
    )
    val grupo2 = listOf(
        ModoConjunto.EXERCICIO1 to "Q4",
        ModoConjunto.PROVA to "Q5",
        ModoConjunto.VENN to "Venn",
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 6.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        listOf(grupo1, grupo2).forEach { grupo ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                grupo.forEach { (m, label) ->
                    val selected = m == modo
                    Button(
                        onClick = { onChange(m) },
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp),
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surface,
                            contentColor = if (selected) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurface,
                        ),
                        contentPadding = PaddingValues(0.dp),
                    ) {
                        Text(
                            label,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
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
        item { SectionLabel("conjuntos definidos") }
        item { ConjuntosEditor(state.conjuntos) { nome, val_ -> vm.setConjunto(nome, val_) } }
        item { SectionLabel("montar operação") }
        item { ExpressionBuilder(state.tokens) }
        item { OperatorKeypad(vm) }
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
        state.erro?.let { err ->
            item {
                Text(err, color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall)
            }
        }
        state.resultadoOp?.let { r ->
            item { SectionLabel("resultado") }
            item { ResultadoCard(r) }
        }
        item { SectionLabel("exercícios da lista") }
        item { ExerciciosProntos(vm) }
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
    val uni = MaterialTheme.colorScheme.primary
    val inter = Color(0xFF2E7D32)
    val diff = Color(0xFFBA7517)
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
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            nomes.forEach { n ->
                ConjKey(n, Modifier.weight(1f)) { vm.addToken(n) }
            }
        }
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
    when (r) {
        is ConjuntoResult.Erro -> Card(
            modifier = Modifier.fillMaxWidth(),
            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.error),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(r.message, modifier = Modifier.padding(14.dp),
                color = MaterialTheme.colorScheme.error)
        }
        is ConjuntoResult.Ok -> Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF2E7D32).copy(.08f)
                ),
                border = BorderStroke(2.dp, Color(0xFF2E7D32))
            ) {
                Column(modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("RESPOSTA", fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF2E7D32).copy(.7f),
                        letterSpacing = 1.5.sp)
                    Text(
                        "${r.expression} = ${r.notation.extensao}",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Normal,
                            color = Color(0xFF2E7D32)
                        )
                    )
                    Text(
                        "n(${r.expression}) = ${r.notation.cardinalidade}",
                        fontFamily = FontFamily.Monospace, fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(.5f)
                    )
                }
            }
            if (r.steps.isNotEmpty()) {
                Text("  resolução passo a passo ↓",
                    fontSize = 11.sp, fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurface.copy(.35f))
                r.steps.forEachIndexed { i, s -> StepExplicado(i + 1, s) }
            }
        }
    }
}

@Composable
private fun StepExplicado(num: Int, s: Step) {
    val lhs = ConjuntoEngine.formatSet(s.esquerda)
    val rhs = s.direita?.let { ConjuntoEngine.formatSet(it) }
    val res = ConjuntoEngine.formatSet(s.resultado)
    val cor = when (s.operacao) {
        "∪" -> Color(0xFF185FA5)
        "∩" -> Color(0xFF2E7D32)
        "−" -> Color(0xFFBA7517)
        "ᶜ" -> Color(0xFF534AB7)
        else -> Color(0xFF666666)
    }
    val (titulo, regra, calculo) = when (s.operacao) {
        "∪" -> Triple(
            "União  ∪  (REUNIÃO)",
            "Junte TODOS os elementos dos dois conjuntos em um só, sem repetir nenhum.",
            "$lhs\n∪  $rhs\n=  $res"
        )
        "∩" -> Triple(
            "Intersecção  ∩",
            "Fique só com os elementos que aparecem nos DOIS conjuntos ao mesmo tempo.",
            "$lhs\n∩  $rhs\n=  $res"
        )
        "−" -> Triple(
            "Diferença  −",
            "Pegue o 1º conjunto e RISQUE os elementos que também estão no 2º.\n⚠ A ordem importa: A−B ≠ B−A",
            "$lhs\n−  $rhs\n=  $res"
        )
        "ᶜ" -> Triple(
            "Complementar  ᶜ  (também escrito como Ā ou Cᴬᵤ)",
            "Pegue o Universo U e RETIRE os elementos do conjunto.\nO complementar é tudo que está em U mas NÃO está no conjunto.",
            "U − $lhs\n=  $res"
        )
        else -> Triple(s.operacao, "", "$lhs = $res")
    }
    val isUnicoPasso = num == 1
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, cor.copy(.3f)),
        colors = CardDefaults.cardColors(containerColor = cor.copy(.05f))
    ) {
        Column(modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(shape = RoundedCornerShape(4.dp), color = cor) {
                    Text("  $num  ", color = Color.White,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Text(titulo, fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp, fontWeight = FontWeight.Bold, color = cor)
            }
            if (isUnicoPasso || num <= 2) {
                Text(
                    regra,
                    fontFamily = FontFamily.Monospace, fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(.7f),
                    lineHeight = 18.sp
                )
            }
            Surface(shape = RoundedCornerShape(6.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()) {
                Text(calculo,
                    modifier = Modifier.padding(12.dp),
                    fontFamily = FontFamily.Monospace, fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 24.sp)
            }
        }
    }
}

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
                repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

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
    val simbolo = if (r.contido) "⊂" else "⊄"
    val simboloReverso = if (r.contido) "⊃" else "⊅"
    val textoA = if (r.contido) "${r.nomeA} está contido em ${r.nomeB}" else "${r.nomeA} NÃO está contido em ${r.nomeB}"
    val textoB = if (r.contido) "${r.nomeB} contém ${r.nomeA}" else "${r.nomeB} NÃO contém ${r.nomeA}"
    Card(
        modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(1.5.dp, color),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(20.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("${r.nomeA} $simbolo ${r.nomeB}", style = MaterialTheme.typography.headlineSmall.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Normal, color = color))
                Text("${r.nomeB} $simboloReverso ${r.nomeA}", style = MaterialTheme.typography.titleMedium.copy(fontFamily = FontFamily.Monospace, color = color.copy(.6f)))
            }
            Text(textoA, fontFamily = FontFamily.Monospace, fontSize = 13.sp, color = color.copy(.9f))
            Text(textoB, fontFamily = FontFamily.Monospace, fontSize = 13.sp, color = color.copy(.75f))
            if (!r.contido && r.elementosFaltando.isNotEmpty()) {
                Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                    Text("elementos de ${r.nomeA} que faltam em ${r.nomeB}: {${r.elementosFaltando.joinToString(", ")}}", modifier = Modifier.padding(10.dp), fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = MaterialTheme.colorScheme.error, lineHeight = 18.sp)
                }
            }
        }
    }
}

@Composable
private fun Exercicio1Pane(state: ConjuntoUiState) {
    val sets = remember(state.conjuntos) {
        state.conjuntos.mapValues { ConjuntoEngine.parseSet(it.value) }
    }
    data class Item(val label: String, val left: String, val right: String, val tipo: String)
    val itens = listOf(
        Item("a)", "12", "A", "elem"),
        Item("b)", "C", "A", "sub"),
        Item("c)", "D", "U", "sub"),
        Item("d)", "13", "C", "elem"),
        Item("e)", "15", "D", "elem"),
        Item("f)", "B", "U", "sub"),
        Item("g)", "A", "D", "sub"),
        Item("h)", "6", "B", "elem"),
    )
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item { Text("Complete com ∈, ∉, ⊂ ou ⊄", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface.copy(.5f)) }
        items(itens) { item ->
            val conj1 = sets[item.left]; val conj2 = sets[item.right] ?: emptySet()
            val (simbolo, verdadeiro, explicacao) = if (item.tipo == "elem") {
                val pertence = conj2.contains(item.left)
                Triple(if (pertence) "∈" else "∉", pertence, if (pertence) "${item.left} está em ${item.right}" else "${item.left} NÃO está em ${item.right}")
            } else {
                val cA = conj1 ?: emptySet(); val contido = cA.all { it in conj2 }
                Triple(if (contido) "⊂" else "⊄", contido, if (contido) "Todos de ${item.left} estão em ${item.right}" else "Faltam elementos de ${item.left} em ${item.right}")
            }
            val cor = if (verdadeiro) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), border = BorderStroke(1.5.dp, cor.copy(.4f))) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(item.label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(.4f))
                        Text("${item.left} $simbolo ${item.right}", style = MaterialTheme.typography.headlineSmall.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Normal, color = cor))
                    }
                    Surface(shape = RoundedCornerShape(6.dp), color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.fillMaxWidth()) {
                        Text(explicacao, modifier = Modifier.padding(10.dp), fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(.7f), lineHeight = 18.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun ProvaPane(state: ConjuntoUiState, _vm: ConjuntoViewModel) {
    val sets = remember(state.conjuntos) {
        state.conjuntos.mapValues { ConjuntoEngine.parseSet(it.value) }
    }
    data class ItemQ4(val label: String, val left: String, val right: String, val tipo: String)
    val q4 = listOf(
        ItemQ4("a)", "12", "A", "elem"), ItemQ4("b)", "C",  "A", "sub"),
        ItemQ4("c)", "D",  "U", "sub"), ItemQ4("d)", "13", "C", "elem"),
        ItemQ4("e)", "15", "D", "elem"), ItemQ4("f)", "B",  "U", "sub"),
        ItemQ4("g)", "A",  "D", "sub"), ItemQ4("h)", "6",  "B", "elem"),
    )
    val q5 = listOf(
        "a" to "D∪E", "b" to "A∩C", "c" to "E−B", "d" to "B−E",
        "e" to "B∩D∩E", "f" to "(E−B)∪(A∩C)", "g" to "(A∩E)∪D", "h" to "(A−E)∩(B−D)",
    )
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("CONJUNTOS DA PROVA", fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(.6f), letterSpacing = 1.sp)
                    listOf("U","A","B","C","D","E").forEach { nome ->
                        val v = state.conjuntos[nome] ?: ""
                        Text("$nome = {$v}", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
            }
        }
        item { Text("QUESTÃO 4", fontFamily = FontFamily.Monospace, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) }
        items(q4) { item ->
            val conj2 = sets[item.right] ?: emptySet(); val conj1 = sets[item.left]
            val (simbolo, verdadeiro, explicacao) = if (item.tipo == "elem") {
                val pertence = conj2.contains(item.left)
                Triple(if (pertence) "∈" else "∉", pertence, if (pertence) "${item.left} está em ${item.right}" else "${item.left} NÃO está em ${item.right}")
            } else {
                val cA = conj1 ?: emptySet(); val contido = cA.all { it in conj2 }
                Triple(if (contido) "⊂" else "⊄", contido, if (contido) "Todos de ${item.left} em ${item.right}" else "Elementos de ${item.left} faltam em ${item.right}")
            }
            val cor = if (verdadeiro) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp), border = BorderStroke(1.5.dp, cor.copy(.4f))) {
                Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(item.label, fontSize = 11.sp, modifier = Modifier.width(22.dp))
                    Text("${item.left} $simbolo ${item.right}", style = MaterialTheme.typography.titleLarge.copy(fontFamily = FontFamily.Monospace, color = cor), modifier = Modifier.width(90.dp))
                    Text(explicacao, fontFamily = FontFamily.Monospace, fontSize = 11.sp, modifier = Modifier.weight(1f))
                }
            }
        }
        item { Text("QUESTÃO 5", fontFamily = FontFamily.Monospace, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) }
        items(q5) { (label, expr) ->
            val resultado = ConjuntoEngine.avaliar(expr, sets)
            val (resTexto, cor) = when (resultado) {
                is ConjuntoResult.Ok -> resultado.notation.extensao to Color(0xFF2E7D32)
                is ConjuntoResult.Erro -> "ERRO" to MaterialTheme.colorScheme.error
            }
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp), border = BorderStroke(1.dp, cor.copy(.3f))) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("$label)", fontSize = 11.sp); Text(expr, fontFamily = FontFamily.Monospace); Text("="); Text(resTexto, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = cor)
                    }
                }
            }
        }
    }
}

data class VennRegion(
    val id: String,
    val label: String,
    val valor: Int? = null,
    val derivado: Boolean = false,
)

data class VennData(
    val regioes: Map<String, VennRegion> = defaultRegioes(),
    val totalU: String = "0",
    val totalA: String = "0",
    val totalB: String = "0",
    val totalC: String = "0",
    val totalAB: String = "0",
    val totalAC: String = "0",
    val totalBC: String = "0",
    val totalABC: String = "0",
    val nomeA: String = "A",
    val nomeB: String = "B",
    val nomeC: String = "C",
)

private fun defaultRegioes() = mapOf(
    "A" to VennRegion("A", "Só A"),
    "B" to VennRegion("B", "Só B"),
    "C" to VennRegion("C", "Só C"),
    "AB" to VennRegion("AB", "A∩B (sem C)"),
    "AC" to VennRegion("AC", "A∩C (sem B)"),
    "BC" to VennRegion("BC", "B∩C (sem A)"),
    "ABC" to VennRegion("ABC", "Centro A∩B∩C"),
    "FORA" to VennRegion("FORA", "Fora de tudo"),
)

private data class VennGeometry(val size: Float) {
    val r get() = size * 0.285f
    val cA get() = Offset(size * 0.385f, size * 0.375f)
    val cB get() = Offset(size * 0.615f, size * 0.375f)
    val cC get() = Offset(size * 0.500f, size * 0.590f)

    val labelPos get() = mapOf(
        "A" to Offset(size * 0.24f, size * 0.34f),
        "B" to Offset(size * 0.76f, size * 0.34f),
        "C" to Offset(size * 0.50f, size * 0.75f),
        "AB" to Offset(size * 0.50f, size * 0.28f),
        "AC" to Offset(size * 0.355f, size * 0.545f),
        "BC" to Offset(size * 0.645f, size * 0.545f),
        "ABC" to Offset(size * 0.50f, size * 0.46f),
        "FORA" to Offset(size * 0.10f, size * 0.12f),
    )

    fun hitTest(p: Offset): String {
        val inA = (p - cA).getDistance() < r
        val inB = (p - cB).getDistance() < r
        val inC = (p - cC).getDistance() < r
        return when {
            inA && inB && inC -> "ABC"
            inA && inB && !inC -> "AB"
            inA && !inB && inC -> "AC"
            !inA && inB && inC -> "BC"
            inA && !inB && !inC -> "A"
            !inA && inB && !inC -> "B"
            !inA && !inB && inC -> "C"
            else -> "FORA"
        }
    }
}

private fun resolverVenn(d: VennData): VennData {
    val U = d.totalU.toIntOrNull() ?: return d
    val tA = d.totalA.toIntOrNull() ?: return d
    val tB = d.totalB.toIntOrNull() ?: return d
    val tC = d.totalC.toIntOrNull() ?: return d
    val tAB = d.totalAB.toIntOrNull() ?: return d
    val tAC = d.totalAC.toIntOrNull() ?: return d
    val tBC = d.totalBC.toIntOrNull() ?: return d
    val abc = d.totalABC.toIntOrNull() ?: 0

    val soAB = tAB - abc
    val soAC = tAC - abc
    val soBC = tBC - abc
    val soA = tA - soAB - soAC - abc
    val soB = tB - soAB - soBC - abc
    val soC = tC - soAC - soBC - abc
    val fora = U - soA - soB - soC - soAB - soAC - soBC - abc

    val regioes = mapOf(
        "ABC" to VennRegion("ABC", "Centro A∩B∩C", abc, derivado = true),
        "AB" to VennRegion("AB", "A∩B (sem C)", soAB, derivado = true),
        "AC" to VennRegion("AC", "A∩C (sem B)", soAC, derivado = true),
        "BC" to VennRegion("BC", "B∩C (sem A)", soBC, derivado = true),
        "A" to VennRegion("A", "Só ${d.nomeA}", soA, derivado = true),
        "B" to VennRegion("B", "Só ${d.nomeB}", soB, derivado = true),
        "C" to VennRegion("C", "Só ${d.nomeC}", soC, derivado = true),
        "FORA" to VennRegion("FORA", "Fora de tudo", fora, derivado = true),
    )
    return d.copy(regioes = regioes)
}

private fun montarCalculoVenn(venn: VennData): String? {
    val U = venn.totalU.toIntOrNull() ?: return null
    val tA = venn.totalA.toIntOrNull() ?: return null
    val tB = venn.totalB.toIntOrNull() ?: return null
    val tC = venn.totalC.toIntOrNull() ?: return null
    val tAB = venn.totalAB.toIntOrNull() ?: return null
    val tAC = venn.totalAC.toIntOrNull() ?: return null
    val tBC = venn.totalBC.toIntOrNull() ?: return null
    val abc = venn.totalABC.toIntOrNull() ?: 0

    val soAB = tAB - abc
    val soAC = tAC - abc
    val soBC = tBC - abc
    val soA = tA - soAB - soAC - abc
    val soB = tB - soAB - soBC - abc
    val soC = tC - soAC - soBC - abc
    val somaDentro = soA + soB + soC + soAB + soAC + soBC + abc
    val fora = U - somaDentro

    return buildString {
        appendLine("Centro (${venn.nomeA}∩${venn.nomeB}∩${venn.nomeC}) = $abc")
        appendLine("Só ${venn.nomeA}∩${venn.nomeB} = $tAB - $abc = $soAB")
        appendLine("Só ${venn.nomeA}∩${venn.nomeC} = $tAC - $abc = $soAC")
        appendLine("Só ${venn.nomeB}∩${venn.nomeC} = $tBC - $abc = $soBC")
        appendLine("Só ${venn.nomeA} = $tA - $soAB - $soAC - $abc = $soA")
        appendLine("Só ${venn.nomeB} = $tB - $soAB - $soBC - $abc = $soB")
        appendLine("Só ${venn.nomeC} = $tC - $soAC - $soBC - $abc = $soC")
        appendLine("Soma interna = $soA + $soB + $soC + $soAB + $soAC + $soBC + $abc = $somaDentro")
        append("Nenhum = $U - $somaDentro = $fora")
    }
}

private fun campoPorRegiao(regiao: String): String? = when (regiao) {
    "A" -> "A"
    "B" -> "B"
    "C" -> "C"
    "AB" -> "AB"
    "AC" -> "AC"
    "BC" -> "BC"
    "ABC" -> "ABC"
    "FORA" -> "U"
    else -> null
}

@Composable
private fun VennPane(_state: ConjuntoUiState, _vm: ConjuntoViewModel) {
    var venn by remember { mutableStateOf(VennData()) }
    var regiaoSelecionada by remember { mutableStateOf<String?>(null) }
    var campoProblemaSelecionado by remember { mutableStateOf<String?>(null) }
    var showPerguntas by remember { mutableStateOf(false) }

    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    val transformState = rememberTransformableState { zoomChange, panChange, _ ->
        scale = (scale * zoomChange).coerceIn(0.5f, 4f)
        offset += panChange
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp))
                .transformable(transformState)
        ) {
            val colorScheme = MaterialTheme.colorScheme
            val previewTotais = mapOf(
                "A" to venn.totalA.toIntOrNull(),
                "B" to venn.totalB.toIntOrNull(),
                "C" to venn.totalC.toIntOrNull(),
                "AB" to venn.totalAB.toIntOrNull(),
                "AC" to venn.totalAC.toIntOrNull(),
                "BC" to venn.totalBC.toIntOrNull(),
                "ABC" to venn.totalABC.toIntOrNull(),
                "FORA" to venn.totalU.toIntOrNull(),
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = scale, scaleY = scale,
                        translationX = offset.x, translationY = offset.y
                    )
                    .pointerInput(Unit) {
                        detectTapGestures { tapOffset ->
                            val canvasSize = size.width.toFloat().coerceAtMost(size.height.toFloat()) * 0.95f
                            val originX = (size.width - canvasSize) / 2f
                            val originY = (size.height - canvasSize) / 2f
                            val local = Offset(
                                (tapOffset.x / scale - offset.x / scale - originX),
                                (tapOffset.y / scale - offset.y / scale - originY)
                            )
                            val geo = VennGeometry(canvasSize)
                            val regiao = geo.hitTest(local)
                            regiaoSelecionada = regiao
                            campoProblemaSelecionado = campoPorRegiao(regiao)
                        }
                    }
                    .drawWithCache {
                        val canvasSize = size.width.coerceAtMost(size.height) * 0.95f
                        val originX = (size.width - canvasSize) / 2f
                        val originY = (size.height - canvasSize) / 2f
                        val geo = VennGeometry(canvasSize)
                        val colorA = Color(0xFF185FA5).copy(.18f)
                        val colorB = Color(0xFF2E7D32).copy(.18f)
                        val colorC = Color(0xFFBA7517).copy(.18f)
                        val stroke = Stroke(width = 3f)
                        val circleColor = colorScheme.onSurface.copy(.6f)
                        val circleNamePaint = android.graphics.Paint().apply {
                            textSize = canvasSize * 0.07f
                            isFakeBoldText = true
                        }
                        val regionPaint = android.graphics.Paint().apply {
                            textAlign = android.graphics.Paint.Align.CENTER
                        }

                        onDrawBehind {
                            translate(originX, originY) {
                                drawCircle(colorA, geo.r, geo.cA)
                                drawCircle(colorB, geo.r, geo.cB)
                                drawCircle(colorC, geo.r, geo.cC)

                                drawCircle(circleColor, geo.r, geo.cA, style = stroke)
                                drawCircle(circleColor, geo.r, geo.cB, style = stroke)
                                drawCircle(circleColor, geo.r, geo.cC, style = stroke)

                                drawRoundRect(
                                    color = colorScheme.onSurface.copy(.3f),
                                    size = androidx.compose.ui.geometry.Size(canvasSize, canvasSize * 0.92f),
                                    topLeft = Offset(0f, canvasSize * 0.04f),
                                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(16f),
                                    style = stroke
                                )

                                drawContext.canvas.nativeCanvas.apply {
                                    circleNamePaint.color = android.graphics.Color.parseColor("#185FA5")
                                    drawText(venn.nomeA, geo.cA.x - geo.r * 0.85f, geo.cA.y - geo.r * 0.75f, circleNamePaint)
                                    circleNamePaint.color = android.graphics.Color.parseColor("#2E7D32")
                                    drawText(venn.nomeB, geo.cB.x + geo.r * 0.4f, geo.cB.y - geo.r * 0.75f, circleNamePaint)
                                    circleNamePaint.color = android.graphics.Color.parseColor("#BA7517")
                                    drawText(venn.nomeC, geo.cC.x + geo.r * 0.4f, geo.cC.y + geo.r * 0.85f, circleNamePaint)
                                }

                                geo.labelPos.forEach { (id, pos) ->
                                    val reg = venn.regioes[id]
                                    val valor = reg?.valor
                                    val valorExibido = valor ?: previewTotais[id]
                                    val txt = valorExibido?.toString() ?: "?"
                                    val isSelected = id == regiaoSelecionada

                                    regionPaint.textSize = canvasSize * if (isSelected) 0.075f else 0.065f
                                    regionPaint.isFakeBoldText = valorExibido != null
                                    regionPaint.color = when {
                                        isSelected -> android.graphics.Color.parseColor("#6200EE")
                                        valorExibido == null -> android.graphics.Color.parseColor("#AAAAAA")
                                        reg?.derivado == true -> android.graphics.Color.parseColor("#185FA5")
                                        else -> android.graphics.Color.parseColor("#222222")
                                    }
                                    drawContext.canvas.nativeCanvas.drawText(txt, pos.x, pos.y, regionPaint)

                                    if (isSelected) {
                                        drawCircle(
                                            color = Color(0xFF6200EE).copy(.2f),
                                            radius = canvasSize * 0.06f,
                                            center = pos
                                        )
                                    }
                                }
                            }
                        }
                    }
            )

            Text(
                "🔍 pinch para zoom",
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(6.dp),
                fontSize = 9.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(.3f)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Spacer(Modifier.height(4.dp))

            VennInputPorTotais(
                venn = venn,
                campoSelecionado = campoProblemaSelecionado,
                onCampoSelecionadoConsumido = { campoProblemaSelecionado = null },
                onLiveChange = { novoVenn -> venn = novoVenn }
            ) { novoVenn ->
                venn = resolverVenn(novoVenn)
            }

            VennStatus(venn)
            VennCalculoResolvido(venn)

            val totalU = venn.totalU.toIntOrNull()
                ?: venn.regioes.values.mapNotNull { it.valor }.sum().takeIf { it > 0 }
            if (venn.regioes.values.all { it.valor != null }) {
                Button(
                    onClick = { showPerguntas = !showPerguntas },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    Text(
                        if (showPerguntas) "▲ Ocultar respostas" else "▼ Ver respostas das perguntas",
                        fontFamily = FontFamily.Monospace
                    )
                }
                if (showPerguntas) {
                    VennPerguntas(venn, totalU)
                }
            }

            OutlinedButton(
                onClick = {
                    venn = VennData()
                    regiaoSelecionada = null
                    scale = 1f
                    offset = Offset.Zero
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            ) { Text("Limpar tudo", fontFamily = FontFamily.Monospace, fontSize = 12.sp) }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun VennInputPorTotais(
    venn: VennData,
    campoSelecionado: String?,
    onCampoSelecionadoConsumido: () -> Unit,
    onLiveChange: (VennData) -> Unit,
    onUpdate: (VennData) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            "Dica: toque numa região do diagrama para abrir o campo correspondente aqui.",
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(.6f)
        )

        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    "PASSO 1 - Nome dos grupos",
                    fontFamily = FontFamily.Monospace, fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    "Ex: Globo, SBT, Band   ou   A, B, C",
                    fontFamily = FontFamily.Monospace, fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(.6f)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedTextField(
                        value = venn.nomeA, onValueChange = { onLiveChange(venn.copy(nomeA = it)) },
                        label = { Text("Grupo A", fontSize = 10.sp) },
                        singleLine = true, modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace)
                    )
                    OutlinedTextField(
                        value = venn.nomeB, onValueChange = { onLiveChange(venn.copy(nomeB = it)) },
                        label = { Text("Grupo B", fontSize = 10.sp) },
                        singleLine = true, modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace)
                    )
                    OutlinedTextField(
                        value = venn.nomeC, onValueChange = { onLiveChange(venn.copy(nomeC = it)) },
                        label = { Text("Grupo C", fontSize = 10.sp) },
                        singleLine = true, modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace)
                    )
                }
            }
        }

        VennGrupo(
            titulo = "PASSO 2 - Total do universo",
            descricao = "\"Uma pesquisa com 1000 pessoas...\" -> digita 1000",
        ) {
            VennNumField(
                label = "Total de pessoas/elementos",
                value = venn.totalU,
                selected = campoSelecionado == "U",
                onSelectedConsumed = onCampoSelecionadoConsumido
            ) { onLiveChange(venn.copy(totalU = it)) }
        }

        VennGrupo(
            titulo = "PASSO 3 - Total de cada grupo",
            descricao = "\"450 gostam de ${venn.nomeA}\" -> digita 450 no campo ${venn.nomeA}\n" +
                "Inclui quem gosta de outros também - e o total bruto do grupo."
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                VennNumField(
                    label = "Total ${venn.nomeA} (todos que estão em A)",
                    value = venn.totalA,
                    selected = campoSelecionado == "A",
                    onSelectedConsumed = onCampoSelecionadoConsumido
                ) { onLiveChange(venn.copy(totalA = it)) }
                VennNumField(
                    label = "Total ${venn.nomeB} (todos que estão em B)",
                    value = venn.totalB,
                    selected = campoSelecionado == "B",
                    onSelectedConsumed = onCampoSelecionadoConsumido
                ) { onLiveChange(venn.copy(totalB = it)) }
                VennNumField(
                    label = "Total ${venn.nomeC} (todos que estão em C)",
                    value = venn.totalC,
                    selected = campoSelecionado == "C",
                    onSelectedConsumed = onCampoSelecionadoConsumido
                ) { onLiveChange(venn.copy(totalC = it)) }
            }
        }

        VennGrupo(
            titulo = "PASSO 4 - Quem está em DOIS grupos",
            descricao = "\"200 gostam de ${venn.nomeA} e ${venn.nomeB}\" -> digita 200\n" +
                "Inclui quem está nos três - não se preocupe, o cálculo desconta."
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                VennNumField(
                    label = "${venn.nomeA} e ${venn.nomeB} (os dois juntos, incluindo quem tem C)",
                    value = venn.totalAB,
                    selected = campoSelecionado == "AB",
                    onSelectedConsumed = onCampoSelecionadoConsumido
                ) { onLiveChange(venn.copy(totalAB = it)) }
                VennNumField(
                    label = "${venn.nomeA} e ${venn.nomeC} (os dois juntos, incluindo quem tem B)",
                    value = venn.totalAC,
                    selected = campoSelecionado == "AC",
                    onSelectedConsumed = onCampoSelecionadoConsumido
                ) { onLiveChange(venn.copy(totalAC = it)) }
                VennNumField(
                    label = "${venn.nomeB} e ${venn.nomeC} (os dois juntos, incluindo quem tem A)",
                    value = venn.totalBC,
                    selected = campoSelecionado == "BC",
                    onSelectedConsumed = onCampoSelecionadoConsumido
                ) { onLiveChange(venn.copy(totalBC = it)) }
            }
        }

        VennGrupo(
            titulo = "PASSO 5 - Quem está nos TRÊS grupos",
            descricao = "\"30 gostam dos três canais\" -> digita 30\n" +
                "É o centro do diagrama. Se o enunciado não falar nada, digita 0."
        ) {
            VennNumField(
                label = "${venn.nomeA} e ${venn.nomeB} e ${venn.nomeC} (os três ao mesmo tempo)",
                value = venn.totalABC,
                selected = campoSelecionado == "ABC",
                onSelectedConsumed = onCampoSelecionadoConsumido
            ) { onLiveChange(venn.copy(totalABC = it)) }
        }

        Button(
            onClick = { onUpdate(venn) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            enabled = listOf(venn.totalU, venn.totalA, venn.totalB, venn.totalC, venn.totalAB, venn.totalAC, venn.totalBC)
                .all { it.isNotBlank() }
        ) {
            Text("Calcular e preencher diagrama ->", fontFamily = FontFamily.Monospace)
        }

        val faltam = listOf(
            venn.totalU to "Total universo",
            venn.totalA to "Total ${venn.nomeA}",
            venn.totalB to "Total ${venn.nomeB}",
            venn.totalC to "Total ${venn.nomeC}",
            venn.totalAB to "${venn.nomeA} e ${venn.nomeB}",
            venn.totalAC to "${venn.nomeA} e ${venn.nomeC}",
            venn.totalBC to "${venn.nomeB} e ${venn.nomeC}",
        ).filter { it.first.isBlank() }.map { it.second }

        if (faltam.isNotEmpty()) {
            Text(
                "Faltam: ${faltam.joinToString(", ")}",
                fontFamily = FontFamily.Monospace, fontSize = 11.sp,
                color = MaterialTheme.colorScheme.error
            )
        } else if (venn.totalABC.isBlank()) {
            Text(
                "Observação: campo dos três em branco será considerado 0.",
                fontFamily = FontFamily.Monospace, fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(.6f)
            )
        }
    }
}

@Composable
private fun VennGrupo(
    titulo: String,
    descricao: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(.25f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                titulo, fontFamily = FontFamily.Monospace,
                fontSize = 12.sp, fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    descricao,
                    modifier = Modifier.padding(8.dp),
                    fontFamily = FontFamily.Monospace, fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(.6f),
                    lineHeight = 17.sp
                )
            }
            content()
        }
    }
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun VennNumField(
    label: String, value: String,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    onSelectedConsumed: () -> Unit = {},
    onChange: (String) -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    val bringIntoViewRequester = remember { BringIntoViewRequester() }

    LaunchedEffect(selected) {
        if (selected) {
            bringIntoViewRequester.bringIntoView()
            focusRequester.requestFocus()
            onSelectedConsumed()
        }
    }

    OutlinedTextField(
        value = value, onValueChange = onChange,
        label = { Text(label, fontSize = 9.sp, maxLines = 1) },
        singleLine = true,
        modifier = modifier
            .fillMaxWidth()
            .bringIntoViewRequester(bringIntoViewRequester)
            .focusRequester(focusRequester),
        shape = RoundedCornerShape(8.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
        ),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace)
    )
}

@Composable
private fun VennStatus(venn: VennData) {
    val regioes = venn.regioes.values.toList()
    val preenchidas = regioes.count { it.valor != null }
    val soma = regioes.mapNotNull { it.valor }.sum()
    val totalU = venn.totalU.toIntOrNull()
    val ok = totalU != null && soma == totalU

    if (preenchidas == 0) return

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                preenchidas < 8 -> MaterialTheme.colorScheme.surfaceVariant
                ok -> Color(0xFF2E7D32).copy(.1f)
                else -> MaterialTheme.colorScheme.errorContainer
            }
        ),
        border = BorderStroke(1.dp, when {
            preenchidas < 8 -> MaterialTheme.colorScheme.outline.copy(.3f)
            ok -> Color(0xFF2E7D32)
            else -> MaterialTheme.colorScheme.error
        })
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            regioes.filter { it.valor != null }.forEach { reg ->
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        reg.label, fontFamily = FontFamily.Monospace, fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(.7f)
                    )
                    Text(
                        "${reg.valor}",
                        fontFamily = FontFamily.Monospace, fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (reg.derivado) Color(0xFF185FA5)
                        else MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Soma das regiões", fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp, fontWeight = FontWeight.Bold
                )
                Text(
                    "$soma", fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp, fontWeight = FontWeight.Bold
                )
            }

            if (totalU != null) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Total U", fontFamily = FontFamily.Monospace, fontSize = 13.sp)
                    Text("$totalU", fontFamily = FontFamily.Monospace, fontSize = 13.sp)
                }
                Text(
                    if (ok) "✓ CORRETO - soma bate com o total"
                    else "✗ ERRO - diferença: ${soma - totalU}",
                    fontFamily = FontFamily.Monospace, fontSize = 12.sp,
                    color = if (ok) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun VennCalculoResolvido(venn: VennData) {
    val calculo = montarCalculoVenn(venn) ?: return

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(.25f)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                "CÁLCULO DA RESOLUÇÃO",
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                calculo,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(.8f),
                lineHeight = 17.sp
            )
        }
    }
}

@Composable
private fun VennPerguntas(venn: VennData, totalU: Int?) {
    val r = venn.regioes
    fun v(id: String) = r[id]?.valor ?: 0

    val nA = venn.nomeA
    val nB = venn.nomeB
    val nC = venn.nomeC

    val soA = v("A")
    val soB = v("B")
    val soC = v("C")
    val soAB = v("AB")
    val soAC = v("AC")
    val soBC = v("BC")
    val centro = v("ABC")
    val fora = v("FORA")
    val totalA = soA + soAB + soAC + centro
    val totalB = soB + soAB + soBC + centro
    val totalC = soC + soAC + soBC + centro

    val perguntas = buildList {
        add("Nenhum dos três" to "$fora")
        add("Pelo menos um" to "${(totalU ?: 0) - fora}")
        add("Exatamente um (só um grupo)" to "${soA + soB + soC}")
        add("Exatamente dois grupos" to "${soAB + soAC + soBC}")
        add("Os três ao mesmo tempo" to "$centro")
        add("Só $nA (sem $nB e sem $nC)" to "$soA")
        add("Só $nB (sem $nA e sem $nC)" to "$soB")
        add("Só $nC (sem $nA e sem $nB)" to "$soC")
        add("$nA ou $nC (A∪C)" to "${totalA + totalC - soAC - centro}")
        add("$nA e $nB (A∩B total)" to "${soAB + centro}")
        add("$nA mas não $nB" to "${soA + soAC}")
        add("$nB mas não $nC" to "${soB + soAB}")
        add("Total $nA" to "$totalA")
        add("Total $nB" to "$totalB")
        add("Total $nC" to "$totalC")
    }

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            "RESPOSTAS AUTOMÁTICAS",
            fontFamily = FontFamily.Monospace, fontSize = 11.sp,
            letterSpacing = 1.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(.4f)
        )
        perguntas.forEach { (pergunta, resposta) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant,
                        RoundedCornerShape(6.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    pergunta, fontFamily = FontFamily.Monospace, fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(.7f),
                    modifier = Modifier.weight(1f)
                )
                Text(
                    resposta,
                    fontFamily = FontFamily.Monospace, fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF185FA5)
                )
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text = text, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(.4f), modifier = Modifier.padding(top = 4.dp))
}
