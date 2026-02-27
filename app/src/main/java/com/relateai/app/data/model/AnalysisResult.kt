package com.relateai.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AnalysisResult(
    @SerialName("health_score") val healthScore: Int = 0,
    @SerialName("summary") val summary: String = "",
    @SerialName("red_flags") val redFlags: List<String> = emptyList(),
    @SerialName("action_plan") val actionPlan: List<String> = emptyList(),
    @SerialName("communication_style") val communicationStyle: String = "",
    @SerialName("dominant_emotions") val dominantEmotions: List<String> = emptyList(),
    @SerialName("positive_aspects") val positiveAspects: List<String> = emptyList(),
    @SerialName("message_balance") val messageBalance: MessageBalance = MessageBalance()
)

@Serializable
data class MessageBalance(
    @SerialName("person_a") val personA: String = "",
    @SerialName("person_b") val personB: String = "",
    @SerialName("person_a_percentage") val personAPercentage: Int = 50,
    @SerialName("person_b_percentage") val personBPercentage: Int = 50
)
