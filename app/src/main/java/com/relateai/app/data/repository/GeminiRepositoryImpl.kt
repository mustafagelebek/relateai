package com.relateai.app.data.repository

import com.relateai.app.BuildConfig
import com.relateai.app.data.model.AnalysisResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Calls Groq API (OpenAI-compatible) for AI analysis.
 * Groq offers a very generous free tier (14,400 req/day) with no regional restrictions.
 */
@Singleton
class GeminiRepositoryImpl @Inject constructor() : GeminiRepository {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    // Groq free models — llama-3.3-70b is fast and very capable
    private val MODEL = "llama-3.3-70b-versatile"

    override suspend fun analyzeChat(formattedChat: String): Result<AnalysisResult> {
        return withContext(Dispatchers.IO) {
            try {
                val apiKey = BuildConfig.GROQ_API_KEY
                val prompt = buildPrompt(formattedChat)
                val responseText = callGroq(apiKey, prompt)
                val cleanJson = extractJson(responseText)
                val result = json.decodeFromString<AnalysisResult>(cleanJson)
                Result.success(result)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    private fun callGroq(apiKey: String, prompt: String): String {
        val url = URL("https://api.groq.com/openai/v1/chat/completions")
        val connection = url.openConnection() as HttpURLConnection

        try {
            connection.apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Authorization", "Bearer $apiKey")
                doOutput = true
                connectTimeout = 60_000
                readTimeout = 120_000
            }

            val escapedPrompt = prompt
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t")

            val requestBody = """
                {
                  "model": "$MODEL",
                  "messages": [
                    {
                      "role": "user",
                      "content": "$escapedPrompt"
                    }
                  ],
                  "temperature": 0.3,
                  "max_tokens": 2048
                }
            """.trimIndent()

            OutputStreamWriter(connection.outputStream, "UTF-8").use { writer ->
                writer.write(requestBody)
                writer.flush()
            }

            val responseCode = connection.responseCode
            if (responseCode != HttpURLConnection.HTTP_OK) {
                val error = connection.errorStream?.bufferedReader()?.readText() ?: "Unknown error"
                throw Exception("API error $responseCode: $error")
            }

            return connection.inputStream.bufferedReader(Charsets.UTF_8).readText()
        } finally {
            connection.disconnect()
        }
    }

    private fun extractJson(rawApiResponse: String): String {
        // Groq returns same format as OpenAI: {"choices":[{"message":{"content":"..."}}]}
        val contentRegex = Regex(""""content"\s*:\s*"((?:[^"\\]|\\.)*)"""")
        val match = contentRegex.find(rawApiResponse)
            ?: throw Exception("Yanıt alınamadı. Ham: ${rawApiResponse.take(300)}")

        val text = match.groupValues[1]
            .replace("\\n", "\n")
            .replace("\\\"", "\"")
            .replace("\\\\", "\\")

        // Strip ```json ... ``` wrappers if present
        val jsonBlockRegex = Regex("```(?:json)?\\s*(.+?)\\s*```", RegexOption.DOT_MATCHES_ALL)
        val jsonMatch = jsonBlockRegex.find(text)
        return jsonMatch?.groupValues?.get(1)?.trim() ?: text.trim()
    }

    private fun buildPrompt(chatData: String): String {
        return """
Sen ilişki dinamiklerini inceleyen uzman bir psikolog ve veri analistisindir. Sana verilen WhatsApp sohbet geçmişini analiz edeceksin.

GÖREV:
Aşağıdaki WhatsApp sohbetini derinlemesine analiz et ve sonuçları TAM OLARAK belirtilen JSON formatında döndür. Başka hiçbir metin ekleme, sadece JSON döndür.

ANALİZ KRİTERLERİ:
1. İki tarafın mesaj dengesini ve iletişim sıklığını değerlendir
2. Duygusal tonu, empatiyi ve destekleyiciği belirle
3. Çatışma örüntülerini, sağlıklı ve sağlıksız dinamikleri tespit et
4. Pozitif yönleri ve güçlü noktaları not et
5. Gelişim için somut, uygulanabilir öneriler sun

SOHBET VERİSİ:
$chatData

ÇIKTI FORMATI (sadece bu JSON'u döndür, başka hiçbir şey ekleme):
{
    "health_score": <0-100 arası integer, 0=çok sağlıksız, 100=çok sağlıklı>,
    "summary": "<3-4 cümlelik Türkçe özet, ilişkinin genel durumunu açıkla>",
    "red_flags": [
        "<tespit edilen tehlike sinyali 1>",
        "<tespit edilen tehlike sinyali 2>"
    ],
    "action_plan": [
        "<somut öneri 1>",
        "<somut öneri 2>",
        "<somut öneri 3>"
    ],
    "communication_style": "<iletişim tarzı açıklaması>",
    "dominant_emotions": ["<duygu1>", "<duygu2>", "<duygu3>"],
    "positive_aspects": ["<pozitif yan 1>", "<pozitif yan 2>"],
    "message_balance": {
        "person_a": "<birinci kişinin adı>",
        "person_b": "<ikinci kişinin adı>",
        "person_a_percentage": <kişi A'nın mesaj yüzdesi, integer>,
        "person_b_percentage": <kişi B'nin mesaj yüzdesi, integer>
    }
}

ÖNEMLİ NOTLAR:
- health_score için 0-100 arası bir TAM SAYI kullan
- Tüm metinler Türkçe olsun
- Sadece JSON döndür, açıklama ekleme
- red_flags boş olabilir ama en az 1 action_plan önerisi ekle
        """.trimIndent()
    }
}
