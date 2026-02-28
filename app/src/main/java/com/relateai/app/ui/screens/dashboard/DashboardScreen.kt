package com.relateai.app.ui.screens.dashboard

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.relateai.app.data.model.AnalysisResult
import com.relateai.app.ui.components.ActionPlanList
import com.relateai.app.ui.components.RedFlagItem
import com.relateai.app.ui.components.ScoreCard
import com.relateai.app.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun DashboardScreen(
    result: AnalysisResult,
    onNewAnalysis: () -> Unit
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = Dark10,
                    contentColor = Purple90,
                    actionColor = Purple80,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        containerColor = Dark00
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Dark00)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(modifier = Modifier.height(56.dp))

                // ─── Header Row with Share & Copy Buttons ────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Analiz Tamamlandı",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            "İşte ilişkin hakkında öğrendiklerimiz",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Purple80.copy(alpha = 0.7f)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Share Button
                    IconButton(
                        onClick = {
                            shareResult(context, result)
                        },
                        modifier = Modifier
                            .size(42.dp)
                            .background(
                                Brush.linearGradient(listOf(Purple40.copy(alpha = 0.3f), Pink40.copy(alpha = 0.3f))),
                                CircleShape
                            )
                    ) {
                        Icon(
                            Icons.Default.Share,
                            contentDescription = "Paylaş",
                            tint = Purple80,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Copy Button
                    IconButton(
                        onClick = {
                            copyResultToClipboard(context, result)
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    message = "📋 Analiz panoya kopyalandı!",
                                    duration = SnackbarDuration.Short
                                )
                            }
                        },
                        modifier = Modifier
                            .size(42.dp)
                            .background(
                                Brush.linearGradient(listOf(Purple40.copy(alpha = 0.3f), Pink40.copy(alpha = 0.3f))),
                                CircleShape
                            )
                    ) {
                        Icon(
                            Icons.Default.ContentCopy,
                            contentDescription = "Kopyala",
                            tint = Purple80,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // ─── Score Section ───────────────────────────────────────────
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    color = Dark10
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "İlişki Sağlık Skoru",
                            style = MaterialTheme.typography.titleMedium,
                            color = Purple90.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        ScoreCard(score = result.healthScore, size = 180.dp, strokeWidth = 14.dp)
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = scoreLabel(result.healthScore),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = scoreColor(result.healthScore)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ─── Summary ─────────────────────────────────────────────────
                DashboardSection(title = "Özet", icon = "📋") {
                    Text(
                        text = result.summary,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Purple90.copy(alpha = 0.8f),
                        lineHeight = 24.sp
                    )
                }

                // ─── Message Balance ─────────────────────────────────────────
                if (result.messageBalance.personA.isNotBlank() && result.messageBalance.personB.isNotBlank()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    DashboardSection(title = "Mesaj Dengesi", icon = "⚖️") {
                        MessageBalanceBar(
                            personA = result.messageBalance.personA,
                            percentA = result.messageBalance.personAPercentage,
                            personB = result.messageBalance.personB,
                            percentB = result.messageBalance.personBPercentage
                        )
                    }
                }

                // ─── Communication Style ──────────────────────────────────────
                if (result.communicationStyle.isNotBlank()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    DashboardSection(title = "İletişim Tarzı", icon = "💬") {
                        Text(
                            text = result.communicationStyle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Purple90.copy(alpha = 0.8f),
                            lineHeight = 22.sp
                        )
                    }
                }

                // ─── Dominant Emotions ────────────────────────────────────────
                if (result.dominantEmotions.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    DashboardSection(title = "Dominant Duygular", icon = "🎭") {
                        EmotionChips(emotions = result.dominantEmotions)
                    }
                }

                // ─── Positive Aspects ─────────────────────────────────────────
                if (result.positiveAspects.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    DashboardSection(title = "Güçlü Yönler", icon = "✅") {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            result.positiveAspects.forEach { aspect ->
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .background(SuccessGreen.copy(alpha = 0.2f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("✓", fontSize = 11.sp, color = SuccessGreen)
                                    }
                                    Text(
                                        aspect,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Purple90.copy(alpha = 0.8f),
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }

                // ─── Red Flags ───────────────────────────────────────────────
                if (result.redFlags.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    DashboardSection(title = "Tehlike Sinyalleri", icon = "🚩") {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            result.redFlags.forEach { flag ->
                                RedFlagItem(text = flag)
                            }
                        }
                    }
                }

                // ─── Action Plan ─────────────────────────────────────────────
                if (result.actionPlan.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    DashboardSection(title = "Aksiyon Planı", icon = "🎯") {
                        ActionPlanList(tasks = result.actionPlan)
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // ─── Share Button (full width, bottom) ───────────────────────
                Button(
                    onClick = { shareResult(context, result) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(0.dp),
                    shape = RoundedCornerShape(26.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(listOf(Purple40, Pink40)),
                                RoundedCornerShape(26.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Share, null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Text("Analizi Paylaş", fontWeight = FontWeight.SemiBold, color = Color.White)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // New analysis button
                OutlinedButton(
                    onClick = onNewAnalysis,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Purple80),
                    border = androidx.compose.foundation.BorderStroke(
                        width = 1.dp,
                        brush = Brush.horizontalGradient(listOf(Purple80, Pink80))
                    ),
                    shape = RoundedCornerShape(26.dp)
                ) {
                    Icon(Icons.Default.Refresh, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Yeni Analiz Yap", fontWeight = FontWeight.SemiBold)
                }

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

// ─── Share via Android Share Sheet ───────────────────────────────────────────

private fun shareResult(context: Context, result: AnalysisResult) {
    val text = formatResultAsText(result)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "RelateAI — İlişki Analiz Raporu")
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(intent, "Analizi Paylaş"))
}

// ─── Copy to Clipboard ────────────────────────────────────────────────────────

private fun copyResultToClipboard(context: Context, result: AnalysisResult) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val text = formatResultAsText(result)
    val clip = ClipData.newPlainText("RelateAI Analiz Raporu", text)
    clipboard.setPrimaryClip(clip)
}

// ─── Format result as readable Turkish text ──────────────────────────────────

private fun formatResultAsText(result: AnalysisResult): String {
    return buildString {
        appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        appendLine("💬 RelateAI — İlişki Analiz Raporu")
        appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        appendLine()

        appendLine("💯 İlişki Sağlık Skoru: ${result.healthScore}/100")
        appendLine(scoreLabel(result.healthScore))
        appendLine()

        if (result.summary.isNotBlank()) {
            appendLine("📋 ÖZET")
            appendLine(result.summary)
            appendLine()
        }

        if (result.messageBalance.personA.isNotBlank() && result.messageBalance.personB.isNotBlank()) {
            appendLine("⚖️ MESAJ DENGESİ")
            appendLine("${result.messageBalance.personA}: %${result.messageBalance.personAPercentage}")
            appendLine("${result.messageBalance.personB}: %${result.messageBalance.personBPercentage}")
            appendLine()
        }

        if (result.communicationStyle.isNotBlank()) {
            appendLine("💬 İLETİŞİM TARZI")
            appendLine(result.communicationStyle)
            appendLine()
        }

        if (result.dominantEmotions.isNotEmpty()) {
            appendLine("🎭 DOMINANT DUYGULAR")
            appendLine(result.dominantEmotions.joinToString(" • "))
            appendLine()
        }

        if (result.positiveAspects.isNotEmpty()) {
            appendLine("✅ GÜÇLÜ YÖNLER")
            result.positiveAspects.forEachIndexed { i, aspect ->
                appendLine("${i + 1}. $aspect")
            }
            appendLine()
        }

        if (result.redFlags.isNotEmpty()) {
            appendLine("🚩 TEHLİKE SİNYALLERİ")
            result.redFlags.forEachIndexed { i, flag ->
                appendLine("${i + 1}. $flag")
            }
            appendLine()
        }

        if (result.actionPlan.isNotEmpty()) {
            appendLine("🎯 AKSİYON PLANI")
            result.actionPlan.forEachIndexed { i, action ->
                appendLine("${i + 1}. $action")
            }
            appendLine()
        }

        appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        appendLine("RelateAI ile analiz edildi 🤖")
    }
}

@Composable
private fun DashboardSection(
    title: String,
    icon: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Dark10
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 14.dp)
            ) {
                Text(icon, fontSize = 18.sp)
                Text(
                    title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
            content()
        }
    }
}

@Composable
private fun MessageBalanceBar(
    personA: String, percentA: Int,
    personB: String, percentB: Int
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(personA, style = MaterialTheme.typography.labelMedium, color = Purple80)
            Text("$percentA%", style = MaterialTheme.typography.labelMedium, color = Purple80)
        }

        // Balance bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Dark40)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(percentA / 100f)
                    .height(8.dp)
                    .background(
                        Brush.horizontalGradient(listOf(Purple80, Pink80))
                    )
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(personB, style = MaterialTheme.typography.labelMedium, color = Pink80)
            Text("$percentB%", style = MaterialTheme.typography.labelMedium, color = Pink80)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EmotionChips(emotions: List<String>) {
    val gradients = listOf(
        listOf(Purple40.copy(alpha = 0.3f), Pink40.copy(alpha = 0.3f)),
        listOf(Pink30.copy(alpha = 0.3f), Purple30.copy(alpha = 0.3f)),
        listOf(Purple30.copy(alpha = 0.25f), Purple40.copy(alpha = 0.25f))
    )

    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        emotions.forEachIndexed { i, emotion ->
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.horizontalGradient(gradients[i % gradients.size]),
                        RoundedCornerShape(20.dp)
                    )
                    .padding(horizontal = 14.dp, vertical = 7.dp)
            ) {
                Text(
                    emotion,
                    style = MaterialTheme.typography.labelMedium,
                    color = Purple90
                )
            }
        }
    }
}

private fun scoreLabel(score: Int): String = when {
    score >= 80 -> "Çok Sağlıklı İlişki 💚"
    score >= 60 -> "Sağlıklı İlişki ✅"
    score >= 40 -> "Geliştirilmesi Gerekiyor ⚠️"
    score >= 20 -> "Ciddi Sorunlar Var 🔴"
    else -> "Acilen Yardım Alın 🆘"
}

private fun scoreColor(score: Int): Color = when {
    score >= 60 -> SuccessGreen
    score >= 40 -> WarningAmber
    else -> ErrorRed
}
