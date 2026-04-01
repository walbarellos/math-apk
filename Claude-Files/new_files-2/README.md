# Correções LogicaEngine — Guia de Aplicação

## Ordem de aplicação

### 1. LogicaEngine.kt — 3 mudanças

**a) Adicionar campo `ok` em `TabelaVerdade`:**
```kotlin
data class TabelaVerdade(
    val variaveis: List<String>,
    val colunas: List<ColunaLogica>,
    val tipo: TipoFormula,
    val passos: List<String>,
    val ok: Boolean = true,   // ← ADICIONAR esta linha
)
```

**b) Corrigir ordem das linhas da tabela (buscar `atribuicoes` no arquivo):**
```kotlin
// REMOVER esta versão errada:
val atribuicoes = (0 until numLinhas).map { i ->
    variaveis.mapIndexed { idx, v ->
        v to ((i shr (n - 1 - idx)) and 1 == 1)
    }.toMap()
}

// COLOCAR esta versão correta:
val atribuicoes = (0 until numLinhas).map { i ->
    val row = numLinhas - 1 - i
    variaveis.mapIndexed { idx, v ->
        v to (((row shr (n - 1 - idx)) and 1) == 1)
    }.toMap()
}
```

**c) Reordenar o tokenizer** — ver arquivo CORRECOES_LogicaEngine.kt:
Substituir o bloco `when{}` inteiro na função `tokenizar()` pelo que está no arquivo.
A chave: operadores ASCII/Unicode ANTES do bloco `isLetter()`.

---

### 2. LogicaEngineTest.kt — 1 fix + 14 novos testes

**Fix:**
```kotlin
// Linha do teste tokeniza bicondicional com ASCII
assertEquals(3, tokens.size)   // era 4 — p<->q = [Var, Bic, Var] = 3 tokens
```

**Novos testes:** copiar todo o bloco de `CORRECOES_LogicaEngineTest.kt`
e colar antes do `}` que fecha a classe de teste.

---

### 3. LogicaScreen.kt — destaque coluna resposta

Ver `CORRECOES_LogicaScreen.kt`. As mudanças são no Composable `ColunaTabela`:
- Trocar constantes hardcoded de cor por `MaterialTheme.colorScheme.*`
- Adicionar `▶ ` no header da coluna resposta
- Adicionar borda de 2dp com `corResposta`

---

## Rodar após as correções

```bash
./gradlew :core:test --no-daemon
```

Resultado esperado: **14 testes passando, 0 falhas**.

---

## Por que a sessão anterior não resolveu tudo

| Problema | O que a sessão fez | O que estava errado |
|---|---|---|
| Tokenizer 'v' | Moveu a regra, mas deixou duplicatas | Precisa limpar todo o bloco `when{}` |
| Ordem das linhas | Adicionou parênteses na precedência | Parênteses não mudam o resultado aqui — precisa inverter o row |
| Destaque da coluna | Trocou cores por hardcoded hex | Hex `#E3F2FD` parece cinza em dark mode — precisa usar `MaterialTheme` |
