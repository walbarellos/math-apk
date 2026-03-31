# Plano de Implementação

## Prioridades do Projeto

### Fase 1: Módulo de Conjuntos (HIGH)

**Objetivo**: Criar operações de teoria dos conjuntos com visualização

#### Estrutura proposta:
```
core/src/main/java/com/basecalc/core/conjuntos/
├── ConjuntosEngine.kt           # Lógica de operações
├── OperacaoConjunto.kt          # Enum: UNIAO, INTERSECCAO, DIFERENCA, COMPLEMENTO
├── ConjuntoVisual.kt            # Modelo para visualização
└── VennDiagramRenderer.kt       # Canvas/SVG para diagrama

app/src/main/java/com/basecalc/ui/
└── conjuntos/
    ├── ConjuntosScreen.kt       # Tela do módulo
    ├── ConjuntosViewModel.kt    # ViewModel específico
    └── VennDiagram.kt           # Composable do diagrama
```

#### Operações a implementar:
- **União (A ∪ B)**: Elementos em A ou B
- **Interseção (A ∩ B)**: Elementos em ambos
- **Diferença (A - B)**: Elementos em A mas não em B
- **Complemento (A')**: Elementos não em A (relativo ao universo)

#### UI:
- Entrada de elementos (ex: "1,2,3" para conjunto {1,2,3})
- Seletor de operação
- Diagrama de Venn renderizado em Canvas
- Lista de elementos resultado com highlight

---

### Fase 2: Módulo de Matrizes (HIGH)

**Objetivo**: Operações matriciais com visualização passo a passo

#### Estrutura proposta:
```
core/src/main/java/com/basecalc/core/matrizes/
├── MatrizEngine.kt              # Todas as operações
├── Matriz.kt                    # Classe de dados
├── OperacaoMatriz.kt            # Enum: MULTIPLICACAO, DETERMINANTE, TRANSPOSTA, INVERSA
└── DeterminantePasso.kt          # Passos para determinante (Laplace)

app/src/main/java/com/basecalc/ui/
└── matrizes/
    ├── MatrizesScreen.kt
    ├── MatrizesViewModel.kt
    ├── MatrizGrid.kt             # Grade de entrada
    └── DeterminantePassos.kt    # Exibição de passos
```

#### Operações a implementar:
- **Multiplicação**: Mostrar linha × coluna com highlight
- **Determinante**: Expansão de Laplace passo a passo
- **Transposta**: Simples rearranjo visual
- **Inversa**: Gauss-Jordan ou adjunta

---

### Fase 3: Potenciação/Radiciação (MEDIUM)

**Objetivo**: Decomposição em fatores e propriedades visíveis

#### Em `OperationVisualizer.kt`:
- Adicionar geração para `a^b` mostrando:
  - Fatoração de `a` se aplicável
  - Expoente decomposto (ex: 2³ = 2×2×2)
- Adicionar geração para `√a` mostrando:
  - Fatoração de `a`
  - Simplificação (ex: √12 = 2√3)

---

### Fase 4: Modo "Cola Discreta" (MEDIUM)

**Objetivo**: Feature oculta para provas

#### Funcionalidades:
- Tema escuro (OLED black)
- Fonte menor (12sp)
- Swipe horizontal para trocar de módulo
- Sem animações
- Ícone discreto ou gesto para ativar

#### Implementação:
- Adicionar `ColorMode.ESCURO` em `CalcUiState.kt`
- Novo tema em `BaseCalcTheme.kt`
- Gesture detector na `CalculatorScreen.kt`

---

### Fase 5: Melhorias de Performance (LOW)

**Objetivo**: Otimizações para Android 14

```kotlin
// Em ResultsPanel.kt - usar derivedStateOf
val bases by remember(result) {
    derivedStateOf { result?.bases ?: emptyList() }
}
```

---

## Ordem de Implementação Sugerida

```
1. ConjuntosScreen + ConjuntosEngine
2. VennDiagram (Canvas)
3. MatrizesScreen + MatrizesEngine
4. DeterminantePassos
5. Potenciação/Radiciação visual
6. Modo Cola Discreta
7. Performance (derivedStateOf)
```

---

## Dependencies necessárias

Provavelmente já estão no `build.gradle.kts`:
- Compose Canvas
- Compose Animation
- Kotlin Coroutines

Novas deps possivelmente necessárias:
- Nenhuma nova dependência para Canvas ( Compose tem nativamente)

---

## Configuração de Navegação

Adicionar ao `AppTab`:
```kotlin
enum class AppTab { 
    CALCULADORA, 
    CONJUNTOS,    // NOVO
    MATRIZES,     // NOVO
    GRAFICO, 
    HISTORICO 
}
```

---

*Plano gerado com base na análise do código existente em `/home/walbarellos/mathwork/BaseCalc/`*
