package com.relateai.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.relateai.app.ui.theme.*

@Composable
fun ScoreCard(
    score: Int,
    modifier: Modifier = Modifier,
    size: Dp = 160.dp,
    strokeWidth: Dp = 12.dp
) {
    val animatedScore by animateIntAsState(
        targetValue = score,
        animationSpec = tween(durationMillis = 1200, easing = EaseOutCubic),
        label = "score_anim"
    )
    val sweepAngle = (animatedScore / 100f) * 270f

    val trackColor = Dark30
    val scoreColor = when {
        score <= 30 -> ScoreLow
        score <= 60 -> ScoreMedium
        else -> ScoreHigh
    }
    val gradientColors = when {
        score <= 30 -> listOf(ScoreLow, Color(0xFFFF8C00))
        score <= 60 -> listOf(ScoreMedium, Color(0xFFFFD700))
        else -> listOf(ScoreHigh, Color(0xFF00C897))
    }

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val padding = strokeWidth.toPx() / 2
            val arcSize = Size(
                width = this.size.width - padding * 2,
                height = this.size.height - padding * 2
            )
            val topLeft = Offset(padding, padding)

            // Background track arc
            drawArc(
                color = trackColor,
                startAngle = 135f,
                sweepAngle = 270f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            )

            // Score arc with gradient
            if (sweepAngle > 0) {
                drawArc(
                    brush = Brush.sweepGradient(gradientColors),
                    startAngle = 135f,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
                )
            }
        }

        // Center text
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$animatedScore",
                fontSize = (size.value * 0.28f).sp,
                fontWeight = FontWeight.ExtraBold,
                color = scoreColor
            )
            Text(
                text = "/100",
                fontSize = (size.value * 0.1f).sp,
                fontWeight = FontWeight.Medium,
                color = scoreColor.copy(alpha = 0.6f)
            )
        }
    }
}
