# BaseCalc

Calculadora Android (Jetpack Compose) com foco em uso escolar/prova, cobrindo:
- cálculo multi-base com precisão racional,
- lógica proposicional,
- conjuntos (incluindo Venn interativo),
- matrizes,
- potenciação/radiciação,
- histórico e gráfico.

Projeto modularizado em `app` (UI/ViewModel) e `core` (motor matemático/lógico).

## Screenshots
Imagens atuais em `docs/images/`:
- `tabela-verdade.png`
- `conjuntos-venn.png`
- `tabela-resultado-verdade.png`
- `alguma-coisa-traumatica.png`

![Tabela Verdade](./docs/images/tabela-verdade.png)
![Conjuntos Venn](./docs/images/conjuntos-venn.png)
![Tabela Resultado Verdade](./docs/images/tabela-resultado-verdade.png)
![Tela Extra](./docs/images/alguma-coisa-traumatica.png)

## Estado Atual (Oficial)
- Base de entrada configurável de `b2` até `b16`.
- Abas principais: `Calculadora`, `Lógica`, `Conjuntos`, `Matrizes`, `Pot/Rad`, `Gráfico`, `Histórico`.
- Módulo de conjuntos ativo em: `com.basecalc.conjuntos.ConjuntoScreen`.
- Venn no modo "Pelo Problema" (único modo ativo):
  - preenchimento por totais do enunciado,
  - sincronização visual no diagrama em tempo real,
  - toque em região para focar campo correspondente,
  - auto-resolução das regiões,
  - verificação de soma,
  - bloco de respostas automáticas.

## Arquitetura

### `core/`
Motor puro Kotlin, sem dependência de UI:
- Conversão de bases: `conversion/*`
- Parser/engine da calculadora: `parser/*`, `engine/*`
- Lógica proposicional: `logica/LogicaEngine.kt`
- Teoria dos conjuntos: `conjuntos/ConjuntoEngine.kt`
- Modelos de resultado/steps: `model/*`
- Racional exato: `number/Rational.kt`

### `app/`
Camada Android/Compose:
- Entrada principal: `MainActivity.kt`
- Estado global e navegação por abas: `CalcUiState.kt`, `CalcViewModel.kt`, `ui/CalculatorScreen.kt`
- Componentes de UI: `ui/components/*`
- Módulos de tela:
  - `conjuntos/ConjuntoScreen.kt` (ativo para conjuntos/Venn)
  - `ui/logica/LogicaScreen.kt`
  - `ui/matrizes/MatrizesScreen.kt`
  - `ui/potenciacao/PotenciacaoScreen.kt`

## Estrutura de Pastas

```text
BaseCalc/
├── app/
│   ├── src/main/java/com/basecalc/
│   │   ├── MainActivity.kt
│   │   ├── CalcUiState.kt
│   │   ├── CalcViewModel.kt
│   │   ├── conjuntos/
│   │   │   ├── ConjuntoScreen.kt
│   │   │   └── ConjuntoViewModel.kt
│   │   ├── ui/
│   │   │   ├── CalculatorScreen.kt
│   │   │   ├── components/
│   │   │   ├── logica/
│   │   │   ├── matrizes/
│   │   │   └── potenciacao/
│   │   └── data/
│   └── build.gradle.kts
├── core/
│   ├── src/main/java/com/basecalc/core/
│   │   ├── conjuntos/
│   │   ├── conversion/
│   │   ├── engine/
│   │   ├── logica/
│   │   ├── model/
│   │   ├── number/
│   │   └── parser/
│   └── src/test/java/com/basecalc/core/
├── gradle/
├── settings.gradle.kts
└── README.md
```

## Como Executar

### Android Studio
1. Abra `BaseCalc/`.
2. Aguarde sync do Gradle.
3. Rode em dispositivo/emulador.

### Linha de comando
- Compilar Kotlin do app:
```bash
./gradlew :app:compileDebugKotlin
```
- Gerar APK debug:
```bash
./gradlew :app:assembleDebug
```
- APK gerado em:
```text
app/build/outputs/apk/debug/app-debug.apk
```

## Testes

Rodar testes do core:
```bash
./gradlew :core:test
```

Suíte atual inclui:
- `BaseCalcEngineTest`
- `ConjuntoEngineTest`
- `ConjuntoVennTest`
- `LogicaEngineTest`

## Venn (Uso Rápido para Prova)

Tela: `Conjuntos -> Venn`.

Preencha os campos do enunciado:
- `U`, `A`, `B`, `C`, `A∩B`, `A∩C`, `B∩C`, `A∩B∩C`.

Comportamento:
- Campos iniciam em `0` para acelerar digitação de prova.
- O diagrama exibe os valores conforme os campos são preenchidos.
- Ao tocar em uma região no diagrama, o app foca o campo correspondente.
- Botão "Calcular e preencher diagrama" resolve as 8 regiões.
- Se `A∩B∩C` ficar em branco, é tratado como `0`.

## Performance (Resumo do que já foi aplicado)
- Cache de parse de conjuntos no `ConjuntoViewModel`.
- Cálculo de exercício com emissão única de estado.
- Persistência de estado de abas com `SaveableStateHolder`.
- I/O de histórico movido para `Dispatchers.IO`.
- Ajustes de recomposição e desenho no Venn.

Arquivo de referência interna:
- `PERF_TUNING_2026-04-09.md`

## MemPalace (Memória Operacional)
- Palace local deste workspace: `../.mempalace/palace`
- Configuração local do projeto: `mempalace.yaml`
- Comandos úteis:
```bash
/tmp/mempalace-venv/bin/mempalace --palace /home/walbarellos/mathwork/.mempalace/palace status
/tmp/mempalace-venv/bin/mempalace --palace /home/walbarellos/mathwork/.mempalace/palace mine /home/walbarellos/mathwork/BaseCalc
/tmp/mempalace-venv/bin/mempalace --palace /home/walbarellos/mathwork/.mempalace/palace search "venn performance"
```

## Limitações Conhecidas
- Ainda há warnings menores de parâmetros não usados em alguns composables.
- O projeto possui telas legadas em `app/src/main/java/com/basecalc/ui/conjuntos/ConjuntosScreen.kt`, mas o fluxo ativo de conjuntos está em `com.basecalc.conjuntos.ConjuntoScreen`.

## Guia de Contribuição Interna
Para mudanças seguras:
1. Altere apenas o módulo alvo (`app` ou `core`).
2. Rode no mínimo:
```bash
./gradlew :app:compileDebugKotlin
./gradlew :core:test
```
3. Gere APK debug quando houver mudança de UI:
```bash
./gradlew :app:assembleDebug
```

## Licença
Uso interno do projeto (não há arquivo de licença dedicado neste repositório).
