package com.example.voidstep.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.voidstep.data.UserPreferences
import com.example.voidstep.ui.components.GlowingButton
import com.example.voidstep.ui.components.VoidBackground
import com.example.voidstep.ui.theme.ElectricBlue
import com.example.voidstep.ui.theme.NeonCyan
import com.example.voidstep.ui.theme.NeonPurple
import com.example.voidstep.ui.theme.VoidSurface

@Composable
fun HomeScreen(
    preferences: UserPreferences,
    onPlayClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onAboutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "home_preview")
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "preview_float"
    )

    VoidBackground(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Bar: Settings & About Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onAboutClick,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(VoidSurface.copy(alpha = 0.6f))
                        .border(1.dp, NeonPurple.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "About",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                IconButton(
                    onClick = onSettingsClick,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(VoidSurface.copy(alpha = 0.6f))
                        .border(1.dp, NeonCyan.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Center: Title + Animated floating-platform preview + Tagline
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "VOIDSTEP",
                    style = MaterialTheme.typography.displayLarge,
                    color = NeonCyan,
                    letterSpacing = 4.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Step forward. Don’t fall.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(36.dp))

                // Interactive Animated Floating Platform Preview
                Box(
                    modifier = Modifier
                        .size(width = 240.dp, height = 150.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height

                        // Step 1: Platform Ahead Left
                        val p1Left = w * 0.15f
                        val p1Top = h * 0.18f + floatOffset * 0.5f
                        val p1Width = w * 0.45f
                        val p1Height = 18f
                        drawRoundRect(
                            color = NeonPurple.copy(alpha = 0.65f),
                            topLeft = Offset(p1Left, p1Top),
                            size = Size(p1Width, p1Height),
                            cornerRadius = CornerRadius(8f, 8f),
                            style = Stroke(width = 2f)
                        )

                        // Step 0: Current Platform Under Character
                        val p0Left = w * 0.28f
                        val p0Top = h * 0.58f + floatOffset
                        val p0Width = w * 0.55f
                        val p0Height = 22f

                        // Platform body
                        drawRoundRect(
                            color = NeonCyan.copy(alpha = 0.15f),
                            topLeft = Offset(p0Left, p0Top),
                            size = Size(p0Width, p0Height),
                            cornerRadius = CornerRadius(10f, 10f)
                        )
                        // Platform border
                        drawRoundRect(
                            color = NeonCyan,
                            topLeft = Offset(p0Left, p0Top),
                            size = Size(p0Width, p0Height),
                            cornerRadius = CornerRadius(10f, 10f),
                            style = Stroke(width = 2.5f)
                        )

                        // Player Character Hovering
                        val charX = p0Left + (p0Width * 0.5f)
                        val charY = p0Top - 18f + (floatOffset * 0.3f)
                        val charRadius = 14f

                        val diamond = Path().apply {
                            moveTo(charX, charY - charRadius)
                            lineTo(charX + charRadius, charY)
                            lineTo(charX, charY + charRadius)
                            lineTo(charX - charRadius, charY)
                            close()
                        }
                        drawPath(path = diamond, color = Color.White)
                        drawCircle(color = NeonCyan, radius = 5f, center = Offset(charX, charY))
                        drawPath(path = diamond, color = NeonCyan, style = Stroke(width = 2f))
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Stats: Best Score & Best Streak Badges
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    // Best Score Badge
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(VoidSurface.copy(alpha = 0.7f))
                            .border(1.dp, NeonCyan.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
                            .padding(horizontal = 22.dp, vertical = 14.dp)
                    ) {
                        Text(
                            text = "BEST SCORE",
                            style = MaterialTheme.typography.labelLarge,
                            color = NeonCyan,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${preferences.bestScore}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Best Streak Badge
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(VoidSurface.copy(alpha = 0.7f))
                            .border(1.dp, NeonPurple.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
                            .padding(horizontal = 22.dp, vertical = 14.dp)
                    ) {
                        Text(
                            text = "BEST STREAK",
                            style = MaterialTheme.typography.labelLarge,
                            color = NeonPurple,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${preferences.highestStreak}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Bottom: Large Play Button
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                GlowingButton(
                    text = "PLAY",
                    icon = Icons.Default.PlayArrow,
                    onClick = onPlayClick,
                    hapticsEnabled = preferences.hapticsEnabled,
                    modifier = Modifier
                        .fillMaxWidth(0.75f)
                        .height(60.dp)
                )
            }
        }
    }
}
