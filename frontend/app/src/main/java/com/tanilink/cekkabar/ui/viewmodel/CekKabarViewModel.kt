package com.tanilink.cekkabar.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tanilink.cekkabar.data.model.AnalysisResponse
import com.tanilink.cekkabar.data.model.DetectResponse
import com.tanilink.cekkabar.data.model.EvaluationResponse
import com.tanilink.cekkabar.data.model.ExampleNewsItem
import com.tanilink.cekkabar.data.model.HistoryCounts
import com.tanilink.cekkabar.data.model.HistoryItem
import com.tanilink.cekkabar.data.model.WordCloudResponse
import com.tanilink.cekkabar.data.repository.CekKabarRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CekKabarUiState(
    // Tab Deteksi
    val inputText: String = "",
    val isDetecting: Boolean = false,
    val detectError: String? = null,
    val examples: List<ExampleNewsItem> = emptyList(),
    val latestResult: DetectResponse? = null,

    // Tab Analisis
    val analysis: AnalysisResponse? = null,
    val isLoadingAnalysis: Boolean = false,
    val analysisModeGlobal: Boolean = true,

    // Tab WordCloud
    val wordCloud: WordCloudResponse? = null,
    val isLoadingWordCloud: Boolean = false,
    val wordCloudCorpus: String = "hoaks",
    val wordCloudQuery: String = "",

    // Tab Evaluasi
    val evaluation: EvaluationResponse? = null,
    val isLoadingEvaluation: Boolean = false,

    // Tab Riwayat
    val histories: List<HistoryItem> = emptyList(),
    val historyCounts: HistoryCounts = HistoryCounts(0, 0, 0),
    val historyFilter: String = "all",
    val historyQuery: String = "",
    val isLoadingHistories: Boolean = false,

    val globalError: String? = null
)

class CekKabarViewModel(
    private val repository: CekKabarRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CekKabarUiState())
    val uiState: StateFlow<CekKabarUiState> = _uiState.asStateFlow()

    init {
        loadExamples()
        loadAnalysis()
        loadWordCloud()
        loadEvaluation()
        loadHistories()
    }

    fun onInputTextChanged(newText: String) {
        if (newText.length <= 2000) {
            _uiState.update { it.copy(inputText = newText, detectError = null) }
        }
    }

    fun clearInput() {
        _uiState.update { it.copy(inputText = "", detectError = null) }
    }

    fun detectNews(textOverride: String? = null, onSuccess: (DetectResponse) -> Unit = {}) {
        val targetText = (textOverride ?: _uiState.value.inputText).trim()
        if (targetText.length < 10) {
            _uiState.update {
                it.copy(detectError = "Masukkan minimal 10 karakter agar analisis akurat.")
            }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    inputText = targetText,
                    isDetecting = true,
                    detectError = null
                )
            }
            repository.detectHoax(targetText)
                .onSuccess { result ->
                    _uiState.update {
                        it.copy(
                            isDetecting = false,
                            latestResult = result
                        )
                    }
                    loadHistories()
                    onSuccess(result)
                }
                .onFailure { err ->
                    _uiState.update {
                        it.copy(
                            isDetecting = false,
                            detectError = err.localizedMessage ?: "Gagal terhubung ke server Laravel."
                        )
                    }
                }
        }
    }

    fun openHistoryDetail(id: Long, onLoaded: (DetectResponse) -> Unit) {
        viewModelScope.launch {
            repository.getHistoryDetail(id)
                .onSuccess { detail ->
                    _uiState.update { it.copy(latestResult = detail) }
                    onLoaded(detail)
                }
                .onFailure { err ->
                    _uiState.update { it.copy(globalError = err.localizedMessage) }
                }
        }
    }

    fun loadExamples() {
        viewModelScope.launch {
            repository.getExamples().onSuccess { list ->
                _uiState.update { it.copy(examples = list) }
            }
        }
    }

    fun setAnalysisModeGlobal(isGlobal: Boolean) {
        _uiState.update { it.copy(analysisModeGlobal = isGlobal) }
    }

    fun loadAnalysis() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingAnalysis = true) }
            repository.getAnalysis()
                .onSuccess { data ->
                    _uiState.update { it.copy(analysis = data, isLoadingAnalysis = false) }
                }
                .onFailure {
                    _uiState.update { it.copy(isLoadingAnalysis = false) }
                }
        }
    }

    fun switchWordCloudCorpus(corpus: String) {
        _uiState.update { it.copy(wordCloudCorpus = corpus) }
        loadWordCloud(corpus = corpus, query = _uiState.value.wordCloudQuery)
    }

    fun onWordCloudQueryChanged(query: String) {
        _uiState.update { it.copy(wordCloudQuery = query) }
        loadWordCloud(corpus = _uiState.value.wordCloudCorpus, query = query)
    }

    fun loadWordCloud(
        corpus: String = _uiState.value.wordCloudCorpus,
        query: String = _uiState.value.wordCloudQuery
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingWordCloud = true) }
            repository.getWordCloud(corpus, query)
                .onSuccess { wc ->
                    _uiState.update { it.copy(wordCloud = wc, isLoadingWordCloud = false) }
                }
                .onFailure {
                    _uiState.update { it.copy(isLoadingWordCloud = false) }
                }
        }
    }

    fun loadEvaluation() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingEvaluation = true) }
            repository.getEvaluation()
                .onSuccess { ev ->
                    _uiState.update { it.copy(evaluation = ev, isLoadingEvaluation = false) }
                }
                .onFailure {
                    _uiState.update { it.copy(isLoadingEvaluation = false) }
                }
        }
    }

    fun setHistoryFilter(filter: String) {
        _uiState.update { it.copy(historyFilter = filter) }
        loadHistories(filter = filter, query = _uiState.value.historyQuery)
    }

    fun onHistoryQueryChanged(query: String) {
        _uiState.update { it.copy(historyQuery = query) }
        loadHistories(filter = _uiState.value.historyFilter, query = query)
    }

    fun loadHistories(
        filter: String = _uiState.value.historyFilter,
        query: String = _uiState.value.historyQuery
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingHistories = true) }
            repository.getHistories(filter, query)
                .onSuccess { res ->
                    _uiState.update {
                        it.copy(
                            histories = res.items,
                            historyCounts = res.counts,
                            isLoadingHistories = false
                        )
                    }
                }
                .onFailure {
                    _uiState.update { it.copy(isLoadingHistories = false) }
                }
        }
    }

    fun deleteHistory(id: Long) {
        viewModelScope.launch {
            repository.deleteHistory(id).onSuccess {
                loadHistories()
            }
        }
    }

    fun clearAllHistories() {
        viewModelScope.launch {
            repository.clearHistories().onSuccess {
                loadHistories()
            }
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(globalError = null, detectError = null) }
    }

    companion object {
        fun provideFactory(repository: CekKabarRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return CekKabarViewModel(repository) as T
                }
            }
    }
}

