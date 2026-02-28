package com.relateai.app.ui.screens.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.relateai.app.data.db.AnalysisRecord
import com.relateai.app.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    records: List<AnalysisRecord>,
    onBack: () -> Unit,
    onDeleteRecord: (AnalysisRecord) -> Unit,
    onDeleteAll: () -> Unit
) {
    var showDeleteAllDialog by remember { mutableStateOf(false) }

    if (showDeleteAllDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAllDialog = false },
            title = { Text("Tüm Geçmişi Sil", color = Color.White) },
            text = {
                Text(
                    "Tüm analiz geçmişi kalıcı olarak silinecek. Emin misin?",
                    color = Purple90.copy(alpha = 0.7f)
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteAll()
                    showDeleteAllDialog = false
                }) {
                    Text("Sil", color = ErrorRed, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAllDialog = false }) {
                    Text("İptal", color = Purple80)
                }
            },
            containerColor = Dark10,
            shape = RoundedCornerShape(20.dp)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Dark00)
    ) {
        // Top ambient gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .align(Alignment.TopCenter)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Purple30.copy(alpha = 0.2f), Color.Transparent),
                        radius = 600f
                    )
                )
        )

        Column(modifier = Modifier.fillMaxSize()) {
            // ── Top Bar ──────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp)
                    .statusBarsPadding(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Geri",
                        tint = Purple80
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Analiz Geçmişi",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    if (records.isNotEmpty()) {
                        Text(
                            "${records.size} analiz",
                            style = MaterialTheme.typography.bodySmall,
                            color = Purple80.copy(alpha = 0.6f)
                        )
                    }
                }
                if (records.isNotEmpty()) {
                    IconButton(onClick = { showDeleteAllDialog = true }) {
                        Icon(
                            Icons.Default.DeleteSweep,
                            contentDescription = "Tümünü Sil",
                            tint = ErrorRed.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            // ── Content ───────────────────────────────────────────────────
            if (records.isEmpty()) {
                EmptyHistoryContent()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        horizontal = 20.dp,
                        vertical = 8.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(records, key = { it.id }) { record ->
                        HistoryCard(
                            record = record,
                            onDelete = { onDeleteRecord(record) }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(32.dp)) }
                }
            }
        }
    }
}

@Composable
private fun EmptyHistoryContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(Purple40.copy(alpha = 0.2f), Color.Transparent)
                        ),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.History,
                    contentDescription = null,
                    tint = Purple80.copy(alpha = 0.5f),
                    modifier = Modifier.size(48.dp)
                )
            }
            Text(
                "Henüz Analiz Yok",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
            Text(
                "İlk analizini yaptıktan sonra\nsonuçlar burada görünecek",
                style = MaterialTheme.typography.bodyMedium,
                color = Purple90.copy(alpha = 0.5f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
private fun HistoryCard(
    record: AnalysisRecord,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Analizi Sil", color = Color.White) },
            text = {
                Text(
                    "Bu analiz kaydı silinecek. Emin misin?",
                    color = Purple90.copy(alpha = 0.7f)
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onDelete()
                    showDeleteDialog = false
                }) {
                    Text("Sil", color = ErrorRed, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("İptal", color = Purple80)
                }
            },
            containerColor = Dark10,
            shape = RoundedCornerShape(20.dp)
        )
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Dark10,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // ── Row 1: date + score + delete ─────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Score bubble
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Purple40, Pink40)
                            ),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "${record.healthScore}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    // Person names
                    val namesLabel = when {
                        record.personA.isNotBlank() && record.personB.isNotBlank() ->
                            "${record.personA} & ${record.personB}"
                        record.personA.isNotBlank() -> record.personA
                        else -> "Sohbet Analizi"
                    }
                    Text(
                        namesLabel,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = Purple80.copy(alpha = 0.5f),
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            formatDate(record.timestamp),
                            style = MaterialTheme.typography.labelSmall,
                            color = Purple80.copy(alpha = 0.5f)
                        )
                    }
                }

                // Score label badge
                val (scoreColor, scoreBg) = scoreStyle(record.healthScore)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(scoreBg)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        scoreBadgeText(record.healthScore),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = scoreColor,
                        fontSize = 10.sp
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.DeleteOutline,
                        contentDescription = "Sil",
                        tint = ErrorRed.copy(alpha = 0.5f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // ── Divider ───────────────────────────────────────────────────
            if (record.summary.isNotBlank()) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 14.dp),
                    color = Dark40,
                    thickness = 1.dp
                )

                // ── Summary ───────────────────────────────────────────────
                Text(
                    record.summary,
                    style = MaterialTheme.typography.bodySmall,
                    color = Purple90.copy(alpha = 0.65f),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 18.sp
                )
            }

            // ── Message balance mini-bar ───────────────────────────────
            if (record.personA.isNotBlank() && record.personB.isNotBlank()) {
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        record.personA,
                        style = MaterialTheme.typography.labelSmall,
                        color = Purple80,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        "${record.personAPercentage}% — ${record.personBPercentage}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = Purple80.copy(alpha = 0.5f),
                        fontSize = 10.sp
                    )
                    Text(
                        record.personB,
                        style = MaterialTheme.typography.labelSmall,
                        color = Pink80,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = androidx.compose.ui.text.style.TextAlign.End
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Dark40)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(record.personAPercentage / 100f)
                            .height(4.dp)
                            .background(
                                Brush.horizontalGradient(listOf(Purple80, Pink80))
                            )
                    )
                }
            }
        }
    }
}

private fun formatDate(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    return when {
        diff < 60_000 -> "Az önce"
        diff < 3_600_000 -> "${diff / 60_000} dakika önce"
        diff < 86_400_000 -> "${diff / 3_600_000} saat önce"
        diff < 604_800_000 -> "${diff / 86_400_000} gün önce"
        else -> SimpleDateFormat("d MMM yyyy", Locale("tr")).format(Date(timestamp))
    }
}

private fun scoreStyle(score: Int): Pair<Color, Color> = when {
    score >= 60 -> SuccessGreen to SuccessGreen.copy(alpha = 0.15f)
    score >= 40 -> WarningAmber to WarningAmber.copy(alpha = 0.15f)
    else -> ErrorRed to ErrorRed.copy(alpha = 0.15f)
}

private fun scoreBadgeText(score: Int): String = when {
    score >= 80 -> "Çok Sağlıklı"
    score >= 60 -> "Sağlıklı"
    score >= 40 -> "Geliştirilmeli"
    score >= 20 -> "Sorunlu"
    else -> "Kritik"
}
