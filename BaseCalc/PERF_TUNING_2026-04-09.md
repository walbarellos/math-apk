# Perf Tuning - Conjuntos/Venn (2026-04-09)

Applied:
- Replaced AnimatedContent tab swap with persistent tab containers to avoid re-creating ConjuntoScreen/LogicaScreen on tab return.
- Added remember(state.conjuntos) for parseSet caching in Exercicio1Pane and ProvaPane.
- Refactored rodarExercicio to single state emission path.
- Hoisted tokenizer operators into fixed Set in ConjuntoViewModel.
- Venn UX: tapping region maps to matching "Pelo Problema" field with focus and bring-into-view.

Expected impact:
- Major reduction in lag when returning to Conjuntos tab.
- Fewer recompositions and repeated parse work.
- Slightly higher memory due to persistent tab composition for Conjuntos/Logica.

Next:
- Cache parsed sets once in ViewModel and reuse across calculate/member/subset/exercise paths.
- Optimize Venn drawing with drawWithCache.
- Evaluate SaveableStateHolder approach for state persistence with lower background composition cost.
