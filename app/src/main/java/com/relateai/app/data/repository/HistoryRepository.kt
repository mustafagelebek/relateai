package com.relateai.app.data.repository

import com.relateai.app.data.db.AnalysisDao
import com.relateai.app.data.db.AnalysisRecord
import com.relateai.app.data.model.AnalysisResult
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HistoryRepository @Inject constructor(
    private val dao: AnalysisDao
) {
    private val json = Json { ignoreUnknownKeys = true }

    val allRecords: Flow<List<AnalysisRecord>> = dao.getAllRecords()

    suspend fun saveAnalysis(result: AnalysisResult) {
        val record = AnalysisRecord(
            personA = result.messageBalance.personA,
            personB = result.messageBalance.personB,
            healthScore = result.healthScore,
            summary = result.summary,
            communicationStyle = result.communicationStyle,
            redFlagsJson = json.encodeToString(result.redFlags),
            actionPlanJson = json.encodeToString(result.actionPlan),
            positiveAspectsJson = json.encodeToString(result.positiveAspects),
            dominantEmotionsJson = json.encodeToString(result.dominantEmotions),
            personAPercentage = result.messageBalance.personAPercentage,
            personBPercentage = result.messageBalance.personBPercentage
        )
        dao.insert(record)
    }

    suspend fun delete(record: AnalysisRecord) = dao.delete(record)

    suspend fun deleteAll() = dao.deleteAll()
}
