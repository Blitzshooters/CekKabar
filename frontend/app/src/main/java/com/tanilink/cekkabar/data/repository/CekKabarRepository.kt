package com.tanilink.cekkabar.data.repository

import com.tanilink.cekkabar.data.local.LocalHistoryDatabase
import com.tanilink.cekkabar.data.model.AnalysisResponse
import com.tanilink.cekkabar.data.model.DetectRequest
import com.tanilink.cekkabar.data.model.DetectResponse
import com.tanilink.cekkabar.data.model.EvaluationResponse
import com.tanilink.cekkabar.data.model.ExampleNewsItem
import com.tanilink.cekkabar.data.model.HistoryListResponse
import com.tanilink.cekkabar.data.model.WordCloudResponse
import com.tanilink.cekkabar.data.remote.CekKabarApi

class CekKabarRepository(
    private val api: CekKabarApi,
    private val localDb: LocalHistoryDatabase
) {

    suspend fun detectHoax(text: String): Result<DetectResponse> = runCatching {
        val result = api.detectHoax(DetectRequest(text = text))
        localDb.saveDetection(result)
        result
    }

    suspend fun getExamples(): Result<List<ExampleNewsItem>> = runCatching {
        api.getExamples().examples
    }

    suspend fun getAnalysis(): Result<AnalysisResponse> = runCatching {
        api.getAnalysis()
    }

    suspend fun getWordCloud(corpus: String, query: String? = null): Result<WordCloudResponse> = runCatching {
        api.getWordCloud(corpus = corpus, query = query?.takeIf { it.isNotBlank() })
    }

    suspend fun getEvaluation(): Result<EvaluationResponse> = runCatching {
        api.getEvaluation()
    }

    suspend fun getHistories(filter: String = "all", query: String? = null): Result<HistoryListResponse> = runCatching {
        try {
            val remote = api.getHistories(filter = filter, query = query?.takeIf { it.isNotBlank() })
            if (remote.items.isNotEmpty() || localDb.getHistories().counts.all == 0) {
                remote
            } else {
                localDb.getHistories(filter = filter, query = query)
            }
        } catch (_: Exception) {
            localDb.getHistories(filter = filter, query = query)
        }
    }

    suspend fun getHistoryDetail(id: Long): Result<DetectResponse> = runCatching {
        localDb.getHistoryDetail(id) ?: api.getHistoryDetail(id).also { detail ->
            localDb.saveDetection(detail)
        }
    }

    suspend fun deleteHistory(id: Long): Result<Unit> = runCatching {
        localDb.deleteHistory(id)
        runCatching { api.deleteHistory(id) }
        Unit
    }

    suspend fun clearHistories(): Result<Unit> = runCatching {
        localDb.clearHistories()
        runCatching { api.clearHistories() }
        Unit
    }
}
