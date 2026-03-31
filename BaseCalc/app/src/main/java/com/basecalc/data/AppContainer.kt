package com.basecalc.data

import android.content.Context

object AppContainer {
    private var historyRepository: HistoryRepository? = null

    fun init(context: Context) {
        historyRepository = HistoryRepository(context.applicationContext)
    }

    fun getHistoryRepository(): HistoryRepository {
        return historyRepository ?: throw IllegalStateException("AppContainer not initialized")
    }
}
