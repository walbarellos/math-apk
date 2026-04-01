# Como integrar o módulo de Conjuntos no BaseCalc existente

## Estrutura dos arquivos

```
Novos arquivos a copiar:
  core/.../conjuntos/ConjuntoEngine.kt     → motor puro (sem Android)
  core/.../conjuntos/ConjuntoEngineTest.kt → 28 testes unitários
  app/.../conjuntos/ConjuntoViewModel.kt   → lógica da tela
  app/.../conjuntos/ConjuntoScreen.kt      → UI Compose completa
```

---

## Passo 1 — Copiar os arquivos

Copie os 4 arquivos para dentro do seu projeto BaseCalc
mantendo a estrutura de pacotes.

---

## Passo 2 — Adicionar aba de Conjuntos na navegação

No seu arquivo de navegação principal (ex: `MainActivity.kt`
ou onde você define as tabs/rotas), adicione:

```kotlin
// Se usar bottom navigation:
val conjuntoViewModel: ConjuntoViewModel by viewModels()

// Na NavHost ou quando montar as tabs:
ConjuntoScreen(viewModel = conjuntoViewModel)
```

Se o seu app já tem tabs (ex: Calculadora / Lógica),
adicione mais uma tab "Conj" ou "{∪}" que abre ConjuntoScreen.

---

## Passo 3 — Rodar os testes

```bash
./gradlew :core:test --tests "com.basecalc.core.conjuntos.*"
```

Resultado esperado: **28 testes passando, 0 falhas**.

Testes cobertos:
- Parse de conjunto (vazio, com espaços, ordenação)
- Todas as operações primitivas (∪ ∩ − ᶜ)
- Todos os exercícios 3a–3j + 2a–2c da lista do professor
- Pertinência (∈ e ∉)
- Subconjunto (⊂ e ⊄) incluindo conjunto vazio
- Questões V/F 4c–4g (De Morgan, etc.)
- Expressão inválida não crasha

---

## O que a tela faz

### Aba ∪ ∩ − (Operações)
- Editar os conjuntos U, A, B, C, D, E diretamente na tela
- Montar expressões com botões (∪ ∩ − ᶜ parênteses)
- Resultado com **notação formal completa**:
  - Forma por extensão: `{2, 3, 5}`
  - Cardinalidade: `n(C∪D) = 8`
  - Pertinências: `2 ∈ (C∪D)   3 ∈ (C∪D) ...`
  - Conjunto vazio: `∅`
- Passos da operação expandíveis
- 13 exercícios da lista prontos como botões
- Histórico das últimas 20 operações

### Aba ∈ ∉ (Pertinência)
- Digita um elemento, escolhe o conjunto
- Mostra: `5 ∈ A` ou `12 ∉ A`
- Justificativa textual (como o professor pede)

### Aba ⊂ ⊄ (Subconjunto)
- Seleciona dois conjuntos
- Mostra: `E ⊂ B` (VERDADEIRO) ou `C ⊄ A` (FALSO)
- Mostra quais elementos estão faltando quando falso
- Justificativa com notação ∀x(x ∈ E → x ∈ B)
