# Arquivos para Melhorar/Adicionar

## Arquivos Existentes que Precisam de Modificação

| Arquivo | Modificação Necessária |
|---------|------------------------|
| `CalcUiState.kt` | Adicionar `CONJUNTOS` e `MATRIZES` ao enum `AppTab` |
| `CalcViewModel.kt` | Adicionar navegação para novos módulos |
| `CalculatorScreen.kt` | Adicionar novos painéis ao AnimatedContent |
| `KeyboardPanel.kt` | Adicionar botão para modo "Conjuntos" / "Matrizes" |

---

## Novos Arquivos a Criar

### Módulo Conjuntos

```
BaseCalc/core/src/main/java/com/basecalc/core/conjuntos/
├── ConjuntosEngine.kt
├── OperacaoConjunto.kt
├── ConjuntoVisual.kt
└── VennDiagramRenderer.kt

BaseCalc/app/src/main/java/com/basecalc/ui/conjuntos/
├── ConjuntosScreen.kt
├── ConjuntosViewModel.kt
└── VennDiagram.kt
```

### Módulo Matrizes

```
BaseCalc/core/src/main/java/com/basecalc/core/matrizes/
├── MatrizEngine.kt
├── Matriz.kt
├── OperacaoMatriz.kt
└── DeterminantePasso.kt

BaseCalc/app/src/main/java/com/basecalc/ui/matrizes/
├── MatrizesScreen.kt
├── MatrizesViewModel.kt
├── MatrizGrid.kt
└── DeterminantePassos.kt
```

---

## Referências dos Arquivos Atuais

### Principais (precisam modificar)
- `/home/walbarellos/mathwork/BaseCalc/app/src/main/java/com/basecalc/CalcUiState.kt`
- `/home/walbarellos/mathwork/BaseCalc/app/src/main/java/com/basecalc/CalcViewModel.kt`
- `/home/walbarellos/mathwork/BaseCalc/app/src/main/java/com/basecalc/ui/CalculatorScreen.kt`
- `/home/walbarellos/mathwork/BaseCalc/app/src/main/java/com/basecalc/ui/components/KeyboardPanel.kt`

### Referência para novos módulos
- `/home/walbarellos/mathwork/BaseCalc/core/src/main/java/com/basecalc/core/engine/OperationVisualizer.kt` - Exemplo de visualização passo a passo
- `/home/walbarellos/mathwork/BaseCalc/app/src/main/java/com/basecalc/ui/components/ResultsPanel.kt` - Exemplo de UI com LazyColumn
- `/home/walbarellos/mathwork/BaseCalc/app/src/main/java/com/basecalc/ui/components/GraphPanel.kt` - Exemplo de painel com Canvas

---

## Ordem de Alteração

1. **Primeiro**: `CalcUiState.kt` - Adicionar novos tabs
2. **Segundo**: `CalculatorScreen.kt` - Adicionar cases no when
3. **Terceiro**: Criar novos arquivos de módulo
4. **Quarto**: `KeyboardPanel.kt` - Adicionar atalhos
5. **Quinto**: `CalcViewModel.kt` - Conectar novos ViewModels

---

*Lista baseada na estrutura atual do projeto em `/home/walbarellos/mathwork/BaseCalc/`*
