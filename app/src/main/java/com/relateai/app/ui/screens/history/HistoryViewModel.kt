package com.relateai.app.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.relateai.app.data.db.AnalysisRecord
import com.relateai.app.data.repository.HistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val historyRepository: HistoryRepository
) : ViewModel() {

    val records: StateFlow<List<AnalysisRecord>> = historyRepository.allRecords
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun delete(record: AnalysisRecord) {
        viewModelScope.launch {
            historyRepository.delete(record)
        }
    }

    fun deleteAll() {
        viewModelScope.launch {
            historyRepository.deleteAll()
        }
    }
}
