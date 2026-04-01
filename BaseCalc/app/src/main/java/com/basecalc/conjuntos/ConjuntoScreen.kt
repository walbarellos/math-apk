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
            ModoConjunto.EXERCICIO1  -> Exercicio1Pane(state)
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
        ModoConjunto.EXERCICIO1  to "Ex.1",
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
            // ── Caixa de resposta final ──────────────────────────────────
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
            // ── Passos ────────────────────────────────────────────────────
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

            Text(regra,
                fontFamily = FontFamily.Monospace, fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(.7f),
                lineHeight = 18.sp)

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
    val simbolo = if (r.contido) "⊂" else "⊄"
    val simboloReverso = if (r.contido) "⊃" else "⊅"
    val textoA = if (r.contido)
        "${r.nomeA} está contido em ${r.nomeB}"
    else
        "${r.nomeA} NÃO está contido em ${r.nomeB}"
    val textoB = if (r.contido)
        "${r.nomeB} contém ${r.nomeA}"
    else
        "${r.nomeB} NÃO contém ${r.nomeA}"

    Card(
        modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(1.5.dp, color),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Notação principal: A ⊂ B  e  B ⊃ A
            Row(horizontalArrangement = Arrangement.spacedBy(20.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "${r.nomeA} $simbolo ${r.nomeB}",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Normal, color = color
                    )
                )
                Text(
                    "${r.nomeB} $simboloReverso ${r.nomeA}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = FontFamily.Monospace, color = color.copy(.6f)
                    )
                )
            }
            // Texto humano simples
            Text(textoA, fontFamily = FontFamily.Monospace, fontSize = 13.sp, color = color.copy(.9f))
            Text(textoB, fontFamily = FontFamily.Monospace, fontSize = 13.sp, color = color.copy(.75f))
            // Só mostra detalhe se falso
            if (!r.contido && r.elementosFaltando.isNotEmpty()) {
                Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                    Text(
                        "elementos de ${r.nomeA} que faltam em ${r.nomeB}: {${r.elementosFaltando.joinToString(", ")}}",
                        modifier = Modifier.padding(10.dp),
                        fontFamily = FontFamily.Monospace, fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.error, lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

// ── Exercício 1 da Apostila ───────────────────────────────────────────────────

@Composable
private fun Exercicio1Pane(state: ConjuntoUiState) {
    val sets = state.conjuntos.mapValues { ConjuntoEngine.parseSet(it.value) }

    data class Item(val label: String, val left: String, val right: String, val tipo: String)
    val itens = listOf(
        Item("a)", "4",  "A", "elem"),
        Item("b)", "11", "C", "elem"),
        Item("c)", "D",  "U", "sub"),
        Item("d)", "C",  "A", "sub"),
        Item("e)", "E",  "B", "sub"),
        Item("f)", "6",  "D", "elem"),
        Item("g)", "12", "E", "elem"),
        Item("h)", "U",  "A", "sub"),
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text("Complete com ∈, ∉, ⊂ ou ⊄",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(.5f))
        }
        items(itens) { item ->
            val conj1 = sets[item.left]
            val conj2 = sets[item.right] ?: emptySet()
            val conj1fmt = if (conj1 != null) ConjuntoEngine.formatSet(conj1) else item.left
            val conj2fmt = ConjuntoEngine.formatSet(conj2)

            val (simbolo, verdadeiro, explicacao) = if (item.tipo == "elem") {
                val pertence = conj2.contains(item.left)
                Triple(
                    if (pertence) "∈" else "∉",
                    pertence,
                    if (pertence)
                        "${item.left} está dentro de ${item.right} = $conj2fmt\n→ usa ∈ (pertence)"
                    else
                        "${item.left} NÃO está em ${item.right} = $conj2fmt\n→ usa ∉ (não pertence)"
                )
            } else {
                val cA = conj1 ?: emptySet()
                val contido = cA.all { it in conj2 }
                val faltando = (cA - conj2).toList()
                Triple(
                    if (contido) "⊂" else "⊄",
                    contido,
                    if (contido)
                        "${item.left} = $conj1fmt\n${item.right} = $conj2fmt\nTodos os elementos de ${item.left} estão em ${item.right}.\n→ usa ⊂ (está contido)"
                    else
                        "${item.left} = $conj1fmt\n${item.right} = $conj2fmt\nOs elementos {${faltando.joinToString(", ")}} estão em ${item.left} mas NÃO estão em ${item.right}.\n→ usa ⊄ (não está contido)"
                )
            }

            val cor = if (verdadeiro) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.5.dp, cor.copy(.4f))
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(item.label, fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(.4f))
                        // RESPOSTA EM DESTAQUE
                        Text(
                            "${item.left} $simbolo ${item.right}",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Normal,
                                color = cor
                            )
                        )
                    }
                    // EXPLICAÇÃO SIMPLES
                    Surface(shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()) {
                        Text(
                            explicacao,
                            modifier = Modifier.padding(10.dp),
                            fontFamily = FontFamily.Monospace, fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(.7f),
                            lineHeight = 18.sp
                        )
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
