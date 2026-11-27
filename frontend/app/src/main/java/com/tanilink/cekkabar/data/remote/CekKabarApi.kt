package com.tanilink.cekkabar.data.remote

import com.tanilink.cekkabar.data.model.AnalysisResponse
import com.tanilink.cekkabar.data.model.DetectRequest
import com.tanilink.cekkabar.data.model.DetectResponse
import com.tanilink.cekkabar.data.model.EvaluationResponse
import com.tanilink.cekkabar.data.model.ExamplesResponse
import com.tanilink.cekkabar.data.model.HistoryListResponse
import com.tanilink.cekkabar.data.model.MessageResponse
import com.tanilink.cekkabar.data.model.WordCloudResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface CekKabarApi {

    @POST("detect")
    suspend fun detectHoax(@Body request: DetectRequest): DetectResponse

    @GET("examples")
    suspend fun getExamples(): ExamplesResponse

    @GET("analysis")
    suspend fun getAnalysis(): AnalysisResponse

    @GET("wordcloud")
    suspend fun getWordCloud(
        @Query("corpus") corpus: String,
        @Query("q") query: String? = null
    ): WordCloudResponse

    @GET("evaluation")
    suspend fun getEvaluation(): EvaluationResponse

    @GET("histories")
    suspend fun getHistories(
        @Query("filter") filter: String = "all",
        @Query("q") query: String? = null
    ): HistoryListResponse

    @GET("histories/{id}")
    suspend fun getHistoryDetail(@Path("id") id: Long): DetectResponse

    @DELETE("histories/{id}")
    suspend fun deleteHistory(@Path("id") id: Long): MessageResponse

    @DELETE("histories")
    suspend fun clearHistories(): MessageResponse
}

