package com.relateai.app.ui.screens.loading

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.relateai.app.ui.theme.*
import kotlinx.coroutines.delay

private val loadingMessages = listOf(
    "İlişki dinamikleri çözümleniyor...",
    "Duygusal örüntüler analiz ediliyor...",
    "İletişim tarzı değerlendiriliyor...",
    "Tehlike sinyalleri tespit ediliyor...",
    "Öneriler hazırlanıyor...",
    "Güçlü yönler belirleniyor...",
    "Rapor derleniyor..."
)

@Composable
fun LoadingScreen() {
    var currentMessageIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(2200)
            currentMessageIndex = (currentMessageIndex + 1) % loadingMessages.size
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "loading")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing)
        ),
        label = "rotation"
    )
    val outerPulse by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "outer_pulse"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Dark00),
        contentAlignment = Alignment.Center
    ) {
        // Ambient glow
        Box(
            modifier = Modifier
                .size(350.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Purple30.copy(alpha = 0.2f), Color.Transparent)
                    ),
                    CircleShape
                )
                .scale(outerPulse)
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            // Custom circular progress
            Box(contentAlignment = Alignment.Center) {
                // Outer decorative ring
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .rotate(rotation)
                        .background(
                            Brush.sweepGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Purple80.copy(alpha = 0.3f),
                                    Purple80,
                                    Color.Transparent
                                )
                            ),
                            CircleShape
                        )
                )

                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .background(Dark00, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(80.dp),
                        color = Purple80,
                        trackColor = Dark30,
                        strokeWidth = 4.dp
                    )

                    // Center heart/spark icon
                    Text("✨", fontSize = 24.sp)
                }
            }

            Spacer(modifier = Modifier.height(52.dp))

            Text(
                "Yapay Zeka Analiz Ediyor",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Rotating loading messages
            AnimatedContent(
                targetState = currentMessageIndex,
                transitionSpec = {
                    slideInVertically { it } + fadeIn() togetherWith
                            slideOutVertically { -it } + fadeOut()
                },
                label = "loading_msg"
            ) { idx ->
                Text(
                    text = loadingMessages[idx],
                    style = MaterialTheme.typography.bodyMedium,
                    color = Purple80.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Progress dots
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                repeat(3) { i ->
                    val scale by infiniteTransition.animateFloat(
                        initialValue = 0.6f, targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(600, delayMillis = i * 200),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "dot_$i"
                    )
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .scale(scale)
                            .background(Purple80, CircleShape)
                    )
                }
            }
        }
    }
}
