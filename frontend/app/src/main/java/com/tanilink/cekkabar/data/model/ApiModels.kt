package com.tanilink.cekkabar.data.model

import com.google.gson.annotations.SerializedName

data class DetectRequest(
    val text: String
)

data class TokenItem(
    val text: String,
    val clean: String,
    @SerializedName("is_word") val isWord: Boolean,
    val score: Double,
    val sentiment: String // "hoaks", "fakta", "neutral"
)

data class InfluentialWord(
    val word: String,
    val score: Double,
    val sentiment: String,
    @SerializedName("log_p_hoaks") val logPHoaks: Double = 0.0,
    @SerializedName("log_p_fakta") val logPFakta: Double = 0.0
)

data class DetectResponse(
    val id: Long? = null,
    val text: String,
    val verdict: String, // "HOAKS" or "FAKTA"
    @SerializedName("hoax_probability") val hoaxProbability: Double,
    @SerializedName("fakta_probability") val faktaProbability: Double,
    @SerializedName("confidence_label") val confidenceLabel: String,
    @SerializedName("word_count") val wordCount: Int,
    @SerializedName("top_keywords") val topKeywords: List<String>,
    val tokens: List<TokenItem>,
    @SerializedName("top_influential_words") val topInfluentialWords: List<InfluentialWord>,
    @SerializedName("created_at") val createdAt: String? = null
)

data class ExampleNewsItem(
    val id: Int,
    val label: String, // "HOAKS" or "FAKTA"
    val category: String,
    val text: String
)

data class ExamplesResponse(
    val examples: List<ExampleNewsItem>
)

data class WordScoreItem(
    val word: String,
    val score: Double
)

data class AnalysisResponse(
    @SerializedName("vocab_size") val vocabSize: Int,
    @SerializedName("prior_hoax") val priorHoax: Double,
    @SerializedName("prior_fakta") val priorFakta: Double,
    @SerializedName("n_train") val nTrain: Int,
    @SerializedName("n_test") val nTest: Int,
    @SerializedName("top_hoax_words") val topHoaxWords: List<WordScoreItem>,
    @SerializedName("top_fakta_words") val topFaktaWords: List<WordScoreItem>
)

data class WordCloudItem(
    val word: String,
    val score: Double,
    val weight: Double
)

data class WordCloudResponse(
    val corpus: String,
    @SerializedName("total_docs") val totalDocs: Int,
    @SerializedName("unique_words") val uniqueWords: Int,
    @SerializedName("top_word") val topWord: WordCloudItem?,
    val words: List<WordCloudItem>
)

data class ConfusionMatrixData(
    val tp: Int,
    val fp: Int,
    val fn: Int,
    val tn: Int,
    val total: Int
)

data class ClassMetricData(
    val precision: Double,
    val recall: Double,
    val f1: Double
)

data class PerClassData(
    val hoaks: ClassMetricData,
    val fakta: ClassMetricData
)

data class EvaluationResponse(
    val accuracy: Double,
    val precision: Double,
    val recall: Double,
    @SerializedName("f1_score") val f1Score: Double,
    @SerializedName("cv_f1_mean") val cvF1Mean: Double,
    @SerializedName("cv_f1_std") val cvF1Std: Double,
    @SerializedName("cv_f1_per_fold") val cvF1PerFold: List<Double>,
    @SerializedName("n_train") val nTrain: Int,
    @SerializedName("n_test") val nTest: Int,
    @SerializedName("n_vocab") val nVocab: Int,
    @SerializedName("confusion_matrix") val confusionMatrix: ConfusionMatrixData,
    @SerializedName("per_class") val perClass: PerClassData
)

data class HistoryItem(
    val id: Long,
    val text: String,
    val verdict: String,
    @SerializedName("hoax_probability") val hoaxProbability: Double,
    @SerializedName("fakta_probability") val faktaProbability: Double,
    @SerializedName("confidence_label") val confidenceLabel: String,
    @SerializedName("top_keywords") val topKeywords: List<String>,
    @SerializedName("word_count") val wordCount: Int,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("formatted_time") val formattedTime: String
)

data class HistoryCounts(
    val all: Int,
    val hoaks: Int,
    val fakta: Int
)

data class HistoryListResponse(
    val counts: HistoryCounts,
    val items: List<HistoryItem>
)

data class MessageResponse(
    val message: String
)

