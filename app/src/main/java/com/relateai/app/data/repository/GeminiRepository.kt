package com.relateai.app.data.repository

import com.relateai.app.data.model.AnalysisResult

interface GeminiRepository {
    suspend fun analyzeChat(formattedChat: String): Result<AnalysisResult>
}
