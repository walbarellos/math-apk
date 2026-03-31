// ─── PATCH: CalculatorScreen.kt ───────────────────────────────────────────────
//
// 1. Adicionar imports no topo do arquivo:
//
import com.basecalc.ui.conjuntos.ConjuntosScreen
import com.basecalc.ui.matrizes.MatrizesScreen
//
// 2. No bloco NavigationBar (ou equivalente de tabs), adicionar as novas abas.
//    Procura o enum AppTab no código e adiciona os ícones.
//    Exemplo provável (adaptar ao padrão que já existe):
//
NavigationBar {
    AppTab.entries.forEach { tab ->
        NavigationBarItem(
            selected = state.activeTab == tab,
            onClick = { viewModel.navegarParaAba(tab) },
            icon = {
                Icon(
                    imageVector = when (tab) {
                        AppTab.CALCULADORA  -> Icons.Default.Calculate
                        AppTab.CONJUNTOS    -> Icons.Default.Workspaces      // ou Bubble
                        AppTab.MATRIZES     -> Icons.Default.GridOn
                        AppTab.GRAFICO      -> Icons.Default.ShowChart
                        AppTab.HISTORICO    -> Icons.Default.History
                    },
                    contentDescription = tab.name,
                )
            },
            label = {
                Text(
                    text = when (tab) {
                        AppTab.CALCULADORA  -> "Calc"
                        AppTab.CONJUNTOS    -> "Conjuntos"
                        AppTab.MATRIZES     -> "Matrizes"
                        AppTab.GRAFICO      -> "Gráfico"
                        AppTab.HISTORICO    -> "Histórico"
                    },
                )
            },
        )
    }
}
//
// 3. No AnimatedContent (ou when(state.activeTab)) que troca os painéis,
//    adicionar os dois novos cases:
//
AnimatedContent(targetState = state.activeTab) { tab ->
    when (tab) {
        AppTab.CALCULADORA -> CalculadoraPanel(viewModel = viewModel, state = state)
        AppTab.CONJUNTOS   -> ConjuntosScreen(viewModel = viewModel)   // ← NOVO
        AppTab.MATRIZES    -> MatrizesScreen(viewModel = viewModel)     // ← NOVO
        AppTab.GRAFICO     -> GraphPanel(/* ... */)
        AppTab.HISTORICO   -> HistoryPanel(/* ... */)
    }
}
