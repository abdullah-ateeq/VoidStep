package com.example.voidstep.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.voidstep.ui.theme.NeonCyan
import com.example.voidstep.ui.theme.NeonPurple
import kotlin.random.Random

private data class StarParticle(
    val xRatio: Float,
    val yRatio: Float,
    val radius: Float,
    val alpha: Float,
    val speed: Float
)

@Composable
fun VoidBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit = {}
) {
    val bgColor = MaterialTheme.colorScheme.background
    val isDark = MaterialTheme.colorScheme.background.red < 0.5f

    val infiniteTransition = rememberInfiniteTransition(label = "void_dust")
    val animatedProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 18000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "dust_progress"
    )

    val particles = remember {
        val rand = Random(42)
        List(40) {
            StarParticle(
                xRatio = rand.nextFloat(),
                yRatio = rand.nextFloat(),
                radius = rand.nextFloat() * 1.8f + 0.8f,
                alpha = rand.nextFloat() * 0.45f + 0.15f,
                speed = rand.nextFloat() * 0.5f + 0.5f
            )
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Soft cosmic gradient overlay
            val gradientBrush = Brush.verticalGradient(
                colors = if (isDark) {
                    listOf(
                        Color(0xFF0D1322),
                        Color(0xFF080B12),
                        Color(0xFF04060A)
                    )
                } else {
                    listOf(
                        Color(0xFFF1F5F9),
                        Color(0xFFE2E8F0),
                        Color(0xFFCBD5E1)
                    )
                }
            )
            drawRect(brush = gradientBrush)

            // Subtle colored ambient glows
            if (isDark) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(NeonPurple.copy(alpha = 0.08f), Color.Transparent),
                        center = Offset(width * 0.2f, height * 0.25f),
                        radius = width * 0.7f
                    ),
                    radius = width * 0.7f,
                    center = Offset(width * 0.2f, height * 0.25f)
                )

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(NeonCyan.copy(alpha = 0.06f), Color.Transparent),
                        center = Offset(width * 0.8f, height * 0.75f),
                        radius = width * 0.65f
                    ),
                    radius = width * 0.65f,
                    center = Offset(width * 0.8f, height * 0.75f)
                )
            }

            // Drifting dust particles in the deep void
            particles.forEach { p ->
                val currentY = ((p.yRatio + (animatedProgress * p.speed)) % 1.0f) * height
                val currentX = p.xRatio * width
                val particleColor = if (isDark) {
                    Color.White.copy(alpha = p.alpha)
                } else {
                    Color(0xFF64748B).copy(alpha = p.alpha * 0.6f)
                }
                drawCircle(
                    color = particleColor,
                    radius = p.radius,
                    center = Offset(currentX, currentY)
                )
            }
        }

        content()
    }
}
