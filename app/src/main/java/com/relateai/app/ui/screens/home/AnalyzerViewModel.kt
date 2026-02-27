package com.relateai.app.ui.screens.home

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.relateai.app.data.model.AnalysisResult
import com.relateai.app.data.parser.ChatParser
import com.relateai.app.data.repository.GeminiRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface UiState {
    data object Idle : UiState
    data object Parsing : UiState
    data class ReadyToSend(
        val messageCount: Int,
        val fileName: String,
        val formattedChat: String
    ) : UiState
    data object Analyzing : UiState
    data class Success(val result: AnalysisResult) : UiState
    data class Error(val message: String) : UiState
}

@HiltViewModel
class AnalyzerViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val chatParser: ChatParser,
    private val geminiRepository: GeminiRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    /**
     * Called when a file URI is received from Android's share/open Intent.
     */
    fun processFileUri(uri: Uri) {
        viewModelScope.launch {
            _uiState.value = UiState.Parsing

            try {
                val rawText = readTextFromUri(uri, context)
                if (rawText.isNullOrBlank()) {
                    _uiState.value = UiState.Error("Dosya okunamadı veya boş.")
                    return@launch
                }

                val messages = chatParser.parse(rawText)
                if (messages.isEmpty()) {
                    _uiState.value = UiState.Error(
                        "WhatsApp mesajı bulunamadı. Lütfen geçerli bir sohbet dışa aktarma dosyası seçin."
                    )
                    return@launch
                }

                val formatted = chatParser.formatForPrompt(messages)
                val fileName = uri.lastPathSegment ?: "sohbet.txt"

                _uiState.value = UiState.ReadyToSend(
                    messageCount = messages.size,
                    fileName = fileName,
                    formattedChat = formatted
                )
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Dosya işlenirken hata: ${e.localizedMessage}")
            }
        }
    }

    /**
     * Starts the Gemini AI analysis.
     */
    fun startAnalysis() {
        val currentState = _uiState.value
        if (currentState !is UiState.ReadyToSend) return

        viewModelScope.launch {
            _uiState.value = UiState.Analyzing

            val result = geminiRepository.analyzeChat(currentState.formattedChat)
            _uiState.value = result.fold(
                onSuccess = { UiState.Success(it) },
                onFailure = { UiState.Error("Analiz başarısız: ${it.localizedMessage}") }
            )
        }
    }

    /**
     * Resets the state to Idle so user can try another file.
     */
    fun reset() {
        _uiState.value = UiState.Idle
    }

    private fun readTextFromUri(uri: Uri, context: Context): String? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.bufferedReader(Charsets.UTF_8).readText()
            }
        } catch (e: Exception) {
            null
        }
    }
}
