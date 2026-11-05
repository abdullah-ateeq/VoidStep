package com.example.voidstep.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.voidstep.ui.components.VoidBackground
import com.example.voidstep.ui.theme.NeonCyan
import com.example.voidstep.ui.theme.NeonPurple
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onNavigateToHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale = remember { Animatable(0.7f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing)
        )
        alpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 700)
        )
        delay(1100)
        onNavigateToHome()
    }

    VoidBackground(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onNavigateToHome() },
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .scale(scale.value)
                    .alpha(alpha.value)
            ) {
                // Geometric Logo Glyph
                Box(
                    modifier = Modifier.size(90.dp),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                        val stroke = 3.dp.toPx()
                        val w = size.width
                        val h = size.height
                        // Floating geometric platform motif
                        drawRoundRect(
                            color = NeonPurple,
                            topLeft = androidx.compose.ui.geometry.Offset(w * 0.15f, h * 0.48f),
                            size = androidx.compose.ui.geometry.Size(w * 0.7f, h * 0.22f),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = stroke)
                        )
                        // Floating diamond player core
                        val path = androidx.compose.ui.graphics.Path().apply {
                            moveTo(w * 0.5f, h * 0.20f)
                            lineTo(w * 0.62f, h * 0.35f)
                            lineTo(w * 0.5f, h * 0.50f)
                            lineTo(w * 0.38f, h * 0.35f)
                            close()
                        }
                        drawPath(path = path, color = NeonCyan)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "VOIDSTEP",
                    style = MaterialTheme.typography.displayLarge,
                    color = NeonCyan,
                    letterSpacing = 6.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "STEP FORWARD. DON'T FALL.",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    letterSpacing = 2.sp
                )
            }
        }
    }
}
