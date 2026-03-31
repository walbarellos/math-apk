package com.basecalc.data

import android.content.Context
import android.content.SharedPreferences

class HistoryRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME, Context.MODE_PRIVATE
    )

    fun saveHistory(history: List<HistoryEntry>) {
        val json = history.map { "${it.expression}|${it.result}" }.joinToString(SEPARATOR)
        prefs.edit().putString(KEY_HISTORY, json).apply()
    }

    fun loadHistory(): List<HistoryEntry> {
        val json = prefs.getString(KEY_HISTORY, "") ?: ""
        if (json.isEmpty()) return emptyList()
        
        return json.split(SEPARATOR).mapNotNull { item ->
            val parts = item.split("|")
            if (parts.size == 2) {
                HistoryEntry(parts[0], parts[1])
            } else null
        }
    }

    fun clearHistory() {
        prefs.edit().remove(KEY_HISTORY).apply()
    }

    companion object {
        private const val PREFS_NAME = "basecalc_history"
        private const val KEY_HISTORY = "history"
        private const val SEPARATOR = ";;"
    }
}

data class HistoryEntry(
    val expression: String,
    val result: String,
)
