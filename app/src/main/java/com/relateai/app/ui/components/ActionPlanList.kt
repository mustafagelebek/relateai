package com.relateai.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.relateai.app.ui.theme.*

@Composable
fun ActionPlanList(
    tasks: List<String>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        tasks.forEachIndexed { index, task ->
            ActionPlanItem(index = index + 1, text = task)
        }
    }
}

@Composable
private fun ActionPlanItem(index: Int, text: String) {
    var checked by remember { mutableStateOf(false) }
    val bgColor by animateColorAsState(
        targetValue = if (checked) SuccessGreen.copy(alpha = 0.1f) else Dark20,
        label = "bg_color"
    )
    val borderColor by animateColorAsState(
        targetValue = if (checked) SuccessGreen.copy(alpha = 0.4f) else Dark40,
        label = "border_color"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable { checked = !checked }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Checkbox circle
        Box(
            modifier = Modifier
                .size(26.dp)
                .background(
                    if (checked) SuccessGreen else Dark30,
                    CircleShape
                )
                .border(1.dp, if (checked) SuccessGreen else Dark40, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (checked) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            } else {
                Text(
                    "$index",
                    style = MaterialTheme.typography.labelSmall,
                    color = Purple80.copy(alpha = 0.7f)
                )
            }
        }

        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = if (checked) SuccessGreen.copy(alpha = 0.7f)
            else Purple90.copy(alpha = 0.85f),
            modifier = Modifier.weight(1f)
        )
    }
}
