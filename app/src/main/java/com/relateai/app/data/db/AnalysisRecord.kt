package com.relateai.app.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "analysis_history")
data class AnalysisRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val personA: String = "",
    val personB: String = "",
    val healthScore: Int = 0,
    val summary: String = "",
    val communicationStyle: String = "",
    // Stored as JSON strings for simplicity
    val redFlagsJson: String = "[]",
    val actionPlanJson: String = "[]",
    val positiveAspectsJson: String = "[]",
    val dominantEmotionsJson: String = "[]",
    val personAPercentage: Int = 50,
    val personBPercentage: Int = 50
)
