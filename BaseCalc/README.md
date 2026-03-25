# BaseCalc — Calculadora Multi-Base com Precisão Exata

Aplicativo Android para estudantes que avalia expressões e mostra o resultado em múltiplas bases com passos.
O motor é 100% Kotlin e utiliza **números racionais exatos** (sem arredondamento).

## Requisitos Funcionais (RF)
1. Avaliar expressões com `+ - * / %` e parênteses.
2. Exibir resultado nas bases 2, 3, 4, 5, 6, 7, 8, 9, 10 e 16.
3. Mostrar passos de divisão sucessiva para conversão de base.
4. Detectar dízima periódica na parte fracionária.
5. Validar e converter números informados em qualquer base suportada.

## Requisitos Não Funcionais (RNF)
1. Precisão exata por racional (sem ponto flutuante).
2. Modularização em camadas (motor isolado do app).
3. Nomenclatura clara e funções com responsabilidade única.
4. Baixo acoplamento entre UI e motor.
5. Tempo de resposta adequado para uso em sala (cálculo local).

## Arquitetura (Sommerville: separação de preocupações)
- **core**: motor matemático (Rational, parser, conversão, modelos).
- **app**: UI Compose, ViewModel e tema.

## Estrutura do projeto
```
BaseCalc/
├── app/
│   ├── src/main/java/com/basecalc/
│   │   ├── CalcModels.kt
│   │   ├── CalcViewModel.kt
│   │   └── MainActivity.kt
│   └── src/main/java/com/basecalc/ui/
│       ├── CalculatorScreen.kt
│       └── theme/
│           ├── BaseCalcTheme.kt
│           └── Typography.kt
├── core/
│   └── src/main/java/com/basecalc/core/
│       ├── conversion/
│       ├── engine/
│       ├── model/
│       ├── number/
│       ├── parser/
│       └── util/
└── gradle/
```

## Como rodar
1. Abra o Android Studio.
2. `File > Open` e selecione a pasta `BaseCalc/`.
3. Aguarde o Gradle sincronizar (vai baixar dependências).
4. Conecte um dispositivo ou inicie um emulador.
5. Clique em **Run**.

## Observações
- Dígitos válidos em base 4: `0, 1, 2, 3`. Portanto `(171)₄` é inválido.
- A parte fracionária é exibida **sem arredondamento**; se não houver término, a dízima é marcada.

## Testes
- Testes unitários do motor em `core/src/test/java`.
- Execute: `./gradlew :core:test`.
