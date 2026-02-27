package com.relateai.app.data.parser

import android.util.Log
import com.relateai.app.data.model.ChatMessage
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Parses WhatsApp chat export .txt files into structured ChatMessage objects.
 *
 * Supported formats:
 *  [DD.MM.YYYY HH:MM] Sender: Message          (iOS brackets)
 *  DD.MM.YYYY HH:MM - Sender: Message           (Android TR)
 *  DD.MM.YYYY saat HH:MM - Sender: Message      (Android TR with "saat")
 *  DD/MM/YYYY, HH:MM - Sender: Message          (Android EN)
 *  DD/MM/YY, HH:MM am/pm - Sender: Message      (12h)
 */
@Singleton
class ChatParser @Inject constructor() {

    companion object {
        private const val TAG = "ChatParser"

        // Multiple patterns to cover all known WhatsApp export formats
        // Group 1 = timestamp  |  Group 2 = sender  |  Group 3 = message
        private val PATTERNS = listOf(
            // ✅ iOS Turkish: [9.04.2025 09:51:54] Süleyman: mesaj  (brackets, NO dash)
            Regex("""^\[(\d{1,2}[.\/]\d{1,2}[.\/]\d{2,4}\s+\d{1,2}:\d{2}(?::\d{2})?(?:\s?[AaPp][Mm])?)\]\s+(.+?):\s(.+)$"""),
            // iOS/Android with dash: [DD.MM.YYYY HH:MM] - Sender: Message
            Regex("""^\[[\u200e\u200f]?(\d{1,2}[.\/\-]\d{1,2}[.\/\-]\d{2,4}[,.]?\s+\d{1,2}:\d{2}(?::\d{2})?(?:\s?[AaPp][Mm])?)\]\s*[-\u2013\u2014]\s*(.+?):\s(.+)$"""),
            // Turkish "saat" keyword: DD.MM.YYYY saat HH:MM - Sender: Message
            Regex("""^[\u200e\u200f]?(\d{1,2}[.\/]\d{1,2}[.\/]\d{2,4}\s+saat\s+\d{1,2}:\d{2})\s*[-\u2013\u2014]\s*(.+?):\s(.+)$""", RegexOption.IGNORE_CASE),
            // Standard Android: DD.MM.YYYY HH:MM - Sender: Message
            Regex("""^[\u200e\u200f]?(\d{1,2}[.\/]\d{1,2}[.\/]\d{2,4}[,.]?\s+\d{1,2}:\d{2}(?::\d{2})?(?:\s?[AaPp][Mm])?)\s*[-\u2013\u2014]\s*(.+?):\s(.+)$""")
        )

        private val SYSTEM_MESSAGE_PATTERNS = listOf(
            "Messages and calls are end-to-end encrypted",
            "Mesajlar ve aramalar uçtan uca şifrelidir",
            "uçtan uca şifreleme",
            "end-to-end encryption",
            "You were added", "gruba eklendi", "ekledi",
            "changed the subject", "konuyu değiştirdi",
            "left", "ayrıldı", "gruptan ayrıldı",
            "You created group", "grubu oluşturdunuz",
            "<Media omitted>", "Medya dahil edilmedi",
            "This message was deleted", "Bu mesaj silindi",
            "image omitted", "video omitted", "audio omitted",
            "sticker omitted", "document omitted", "GIF omitted",
            "Contact card omitted", "görüntü dahil edilmedi",
            "video dahil edilmedi", "ses dahil edilmedi",
            "belge dahil edilmedi", "çıkartma dahil edilmedi",
            "konum: https", "location:", "missed voice call",
            "missed video call", "cevapsız sesli arama", "cevapsız görüntülü arama"
        )
    }

    fun parse(rawText: String): List<ChatMessage> {
        // Strip BOM and normalize line endings
        val normalized = rawText
            .removePrefix("\uFEFF")
            .replace("\r\n", "\n")
            .replace("\r", "\n")

        val lines = normalized.lines()

        // Debug: log first 5 lines to understand the format
        Log.d(TAG, "=== ChatParser: ${lines.size} lines ===")
        lines.take(5).forEachIndexed { i, line ->
            Log.d(TAG, "Line[$i]: '${line.take(120)}'")
        }

        val messages = mutableListOf<ChatMessage>()
        var currentMessage: ChatMessage? = null

        for (line in lines) {
            if (line.isBlank()) continue

            val matchResult = matchLine(line)
            if (matchResult != null) {
                currentMessage?.let { messages.add(it) }
                val (timestamp, sender, content) = matchResult

                if (isSystemMessage(content)) {
                    currentMessage = null
                    continue
                }

                currentMessage = ChatMessage(timestamp, sender, content)
            } else {
                // Continuation line of a multi-line message
                currentMessage = currentMessage?.copy(
                    content = "${currentMessage.content}\n$line"
                )
            }
        }

        currentMessage?.let { messages.add(it) }

        Log.d(TAG, "Parsed ${messages.size} messages")
        return messages
    }

    private fun matchLine(line: String): Triple<String, String, String>? {
        // Remove leading Unicode direction marks before matching
        val cleanLine = line.trimStart('\u200e', '\u200f', '\u202a', '\u202c')
        for (pattern in PATTERNS) {
            val match = pattern.matchEntire(cleanLine)
            if (match != null) {
                val timestamp = match.groupValues[1].trim()
                val sender   = match.groupValues[2].trim()
                val content  = match.groupValues[3].trim()
                if (sender.isNotBlank() && content.isNotBlank()) {
                    return Triple(timestamp, sender, content)
                }
            }
        }
        return null
    }

    fun formatForPrompt(messages: List<ChatMessage>, maxMessages: Int = 300): String {
        val selected = if (messages.size <= maxMessages) {
            messages
        } else {
            // Smart sampling: beginning (25%) + middle (25%) + end (50%)
            // Gives a full picture of the relationship arc, not just recent messages
            val beginCount = maxMessages / 4
            val middleCount = maxMessages / 4
            val endCount = maxMessages - beginCount - middleCount

            val begin = messages.take(beginCount)
            val midStart = (messages.size / 2) - (middleCount / 2)
            val middle = messages.subList(midStart, midStart + middleCount)
            val end = messages.takeLast(endCount)

            (begin + middle + end).distinctBy { it.timestamp + it.sender }
        }
        return buildString {
            if (messages.size > maxMessages) {
                appendLine("// Toplam ${messages.size} mesajdan ${selected.size} tanesi örneklendi (başlangıç, orta, son)")
            }
            selected.forEach { appendLine("[${it.timestamp}] ${it.sender}: ${it.content}") }
        }
    }

    private fun isSystemMessage(content: String): Boolean =
        SYSTEM_MESSAGE_PATTERNS.any { content.contains(it, ignoreCase = true) }

    fun getStats(messages: List<ChatMessage>): ParserStats {
        val senderCounts = messages.groupBy { it.sender }.mapValues { it.value.size }
        return ParserStats(
            totalMessages = messages.size,
            uniqueSenders = senderCounts.keys.toList(),
            messageCountBySender = senderCounts,
            totalWords = messages.sumOf { it.content.split(" ").size }
        )
    }
}

data class ParserStats(
    val totalMessages: Int,
    val uniqueSenders: List<String>,
    val messageCountBySender: Map<String, Int>,
    val totalWords: Int
)
