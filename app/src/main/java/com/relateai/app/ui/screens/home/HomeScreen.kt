package com.relateai.app.ui.screens.home

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.relateai.app.ui.theme.*

@Composable
fun HomeScreen(
    uiState: UiState,
    onAnalyzeClick: () -> Unit,
    onResetClick: () -> Unit,
    onPickFile: (android.net.Uri) -> Unit,
    onHistoryClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Dark00)
    ) {
        // Top ambient gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .align(Alignment.TopCenter)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Purple30.copy(alpha = 0.25f), Color.Transparent),
                        radius = 700f
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(56.dp))

            // Header row: Logo + History button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "RelateAI",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "İlişki Analiz Asistanın",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Purple80.copy(alpha = 0.7f)
                    )
                }
                IconButton(
                    onClick = onHistoryClick,
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            Brush.linearGradient(
                                listOf(Purple40.copy(alpha = 0.25f), Pink40.copy(alpha = 0.25f))
                            ),
                            CircleShape
                        )
                ) {
                    Icon(
                        Icons.Default.History,
                        contentDescription = "Geçmiş",
                        tint = Purple80,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(64.dp))

            // State-driven content
            AnimatedContent(
                targetState = uiState,
                transitionSpec = {
                    fadeIn(animationSpec = tween(400)) togetherWith
                            fadeOut(animationSpec = tween(300))
                },
                label = "home_content"
            ) { state ->
                when (state) {
                    is UiState.Idle -> IdleContent(onPickFile = onPickFile)
                    is UiState.Parsing -> ParsingContent()
                    is UiState.ReadyToSend -> ReadyContent(
                        state = state,
                        onAnalyzeClick = onAnalyzeClick
                    )
                    is UiState.Error -> ErrorContent(
                        message = state.message,
                        onRetry = onResetClick
                    )
                    else -> Unit
                }
            }
        }
    }
}

@Composable
private fun IdleContent(onPickFile: (android.net.Uri) -> Unit) {
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { onPickFile(it) }
    }
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(160.dp)
                .scale(scale)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Purple40.copy(alpha = 0.3f), Color.Transparent)
                    ),
                    CircleShape
                )
                .border(
                    width = 1.5.dp,
                    brush = Brush.sweepGradient(
                        colors = listOf(Purple80, Pink80, Purple80)
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(Dark10, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ChatBubbleOutline,
                    contentDescription = null,
                    tint = Purple80,
                    modifier = Modifier.size(48.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Sohbet Dosyası Bekleniyor",
            style = MaterialTheme.typography.titleLarge,
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "WhatsApp'tan sohbet geçmişini\nbu uygulamayla paylaş",
            style = MaterialTheme.typography.bodyMedium,
            color = Purple90.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        // ─── File Picker Button ───────────────────────────────────────
        Button(
            onClick = { filePickerLauncher.launch("text/plain") },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            contentPadding = PaddingValues(0.dp),
            shape = RoundedCornerShape(28.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(listOf(Purple40, Pink40)),
                        RoundedCornerShape(28.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.FolderOpen,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        "Dosyadan Yükle",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            "veya WhatsApp'tan direkt paylaş",
            style = MaterialTheme.typography.bodySmall,
            color = Purple90.copy(alpha = 0.45f)
        )
    }
}

@Composable
private fun ParsingContent() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(72.dp),
            color = Purple80,
            strokeWidth = 3.dp,
            trackColor = Dark30
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            "Dosya okunuyor...",
            style = MaterialTheme.typography.titleMedium,
            color = Color.White
        )
        Text(
            "Mesajlar ayrıştırılıyor",
            style = MaterialTheme.typography.bodySmall,
            color = Purple80.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun ReadyContent(
    state: UiState.ReadyToSend,
    onAnalyzeClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Success indicator
        Box(
            modifier = Modifier
                .size(88.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(SuccessGreen.copy(alpha = 0.2f), Color.Transparent)
                    ),
                    CircleShape
                )
                .border(1.dp, SuccessGreen.copy(alpha = 0.5f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = SuccessGreen,
                modifier = Modifier.size(44.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            "Dosya Hazır! 🎉",
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            "${state.messageCount} mesaj bulundu",
            style = MaterialTheme.typography.titleMedium,
            color = Purple80
        )

        Text(
            state.fileName,
            style = MaterialTheme.typography.bodySmall,
            color = Purple90.copy(alpha = 0.5f)
        )

        Spacer(modifier = Modifier.height(40.dp))

        // Analyze button
        Button(
            onClick = onAnalyzeClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            contentPadding = PaddingValues(0.dp),
            shape = RoundedCornerShape(30.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(listOf(Purple40, Pink40)),
                        RoundedCornerShape(30.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.AutoAwesome, null, tint = Color.White, modifier = Modifier.size(20.dp))
                    Text(
                        "Analizi Başlat",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun ErrorContent(message: String, onRetry: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(88.dp)
                .background(
                    ErrorRed.copy(alpha = 0.1f), CircleShape
                )
                .border(1.dp, ErrorRed.copy(alpha = 0.4f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.ErrorOutline,
                contentDescription = null,
                tint = ErrorRed,
                modifier = Modifier.size(44.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            "Bir Hata Oluştu",
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            message,
            style = MaterialTheme.typography.bodyMedium,
            color = Purple90.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedButton(
            onClick = onRetry,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Purple80),
            border = androidx.compose.foundation.BorderStroke(1.dp, Purple80)
        ) {
            Icon(Icons.Default.Refresh, null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Tekrar Dene")
        }
    }
}
