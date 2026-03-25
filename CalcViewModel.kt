package com.basecalc

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.json.JSONObject

class CalcViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(CalcUiState())
    val uiState: StateFlow<CalcUiState> = _uiState

    // ── Entrada do teclado ────────────────────────────────────────────────────

    fun onKey(key: String) {
        val current = _uiState.value
        val ops = setOf("+", "-", "*", "/", "%")

        val newExpr = when (key) {
            "AC" -> {
                _uiState.value = current.copy(expression = "0", error = null, showStepsForBase = null)
                calculate("0")
                return
            }
            "⌫"  -> current.expression.dropLast(1).ifEmpty { "0" }
            else  -> {
                val expr = if (current.expression == "0" && key !in ops) key
                           else current.expression + key
                expr
            }
        }
        _uiState.value = current.copy(expression = newExpr, error = null)
    }

    fun onEquals() {
        val expr = _uiState.value.expression
        if (expr.isNotEmpty() && expr != "0") calculate(expr)
    }

    fun toggleSteps(base: Int) {
        val current = _uiState.value
        val next = if (current.showStepsForBase == base) null else base
        _uiState.value = current.copy(showStepsForBase = next)
    }

    // ── Cálculo ───────────────────────────────────────────────────────────────

    private fun calculate(expr: String) {
        val jsonStr = BaseConverterJNI.evaluate(expr)
        val result  = parseResult(jsonStr)
        _uiState.value = _uiState.value.copy(
            result = result,
            error  = if (!result.ok) result.error else null,
            showStepsForBase = null
        )
    }

    // ── JSON parse ────────────────────────────────────────────────────────────

    private fun parseResult(json: String): CalcResult {
        return try {
            val obj = JSONObject(json)
            if (!obj.getBoolean("ok")) {
                return CalcResult(ok = false, error = obj.optString("error", "Erro desconhecido"))
            }
            val basesArray = obj.getJSONArray("bases")
            val bases = (0 until basesArray.length()).map { i ->
                val b = basesArray.getJSONObject(i)
                BaseEntry(
                    base       = b.getInt("base"),
                    label      = b.getString("label"),
                    valid      = b.getBoolean("valid"),
                    intPart    = b.getString("int"),
                    fracPart   = b.optString("frac", ""),
                    repeats    = b.getBoolean("repeats"),
                    repeatPart = b.optString("repeat", ""),
                    steps      = b.optString("steps", "")
                )
            }
            CalcResult(
                ok      = true,
                value   = obj.getDouble("value"),
                decimal = obj.getString("decimal"),
                bases   = bases
            )
        } catch (e: Exception) {
            CalcResult(ok = false, error = "Erro ao processar resultado: ${e.message}")
        }
    }
}
