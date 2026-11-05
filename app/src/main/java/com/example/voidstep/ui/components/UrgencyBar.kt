package com.example.voidstep.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.voidstep.ui.theme.DangerVoid
import com.example.voidstep.ui.theme.NeonCyan
import com.example.voidstep.ui.theme.WarningPulse

@Composable
fun UrgencyBar(
    fractionRemaining: Float,
    modifier: Modifier = Modifier
) {
    val barColor by animateColorAsState(
        targetValue = when {
            fractionRemaining > 0.45f -> NeonCyan
            fractionRemaining > 0.20f -> WarningPulse
            else -> DangerVoid
        },
        animationSpec = tween(150),
        label = "urgency_color"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(4.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(Color.White.copy(alpha = 0.12f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(fractionRemaining.coerceIn(0f, 1f))
                .clip(RoundedCornerShape(2.dp))
                .background(barColor)
        )
    }
}
