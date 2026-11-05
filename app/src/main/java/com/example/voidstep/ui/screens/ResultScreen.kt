package com.example.voidstep.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.voidstep.data.UserPreferences
import com.example.voidstep.game.model.GameState
import com.example.voidstep.ui.components.GlowingButton
import com.example.voidstep.ui.components.VoidBackground
import com.example.voidstep.ui.theme.DangerVoid
import com.example.voidstep.ui.theme.ElectricBlue
import com.example.voidstep.ui.theme.NeonCyan
import com.example.voidstep.ui.theme.NeonCyanBright
import com.example.voidstep.ui.theme.NeonPurple
import com.example.voidstep.ui.theme.VoidSurface

@Composable
fun ResultScreen(
    gameState: GameState,
    preferences: UserPreferences,
    onPlayAgainClick: () -> Unit,
    onHomeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Animated score count-up
    val animatedScore = remember { Animatable(0f) }
    LaunchedEffect(gameState.score) {
        animatedScore.animateTo(
            targetValue = gameState.score.toFloat(),
            animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
        )
    }

    VoidBackground(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Game Over Header & Score Display
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "VOID CONSUMED",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = DangerVoid,
                    letterSpacing = 2.sp
                )

                if (gameState.fallReason != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = gameState.fallReason,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Final Score Big Display
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(VoidSurface.copy(alpha = 0.7f))
                        .border(1.dp, NeonCyan.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
                        .padding(vertical = 24.dp)
                ) {
                    Text(
                        text = "FINAL SCORE",
                        style = MaterialTheme.typography.labelLarge,
                        color = NeonCyan,
                        letterSpacing = 1.5.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${animatedScore.value.toInt()}",
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.Bold,
                        color = NeonCyanBright
                    )

                    if (gameState.isNewBestScore) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(NeonCyan.copy(alpha = 0.2f))
                                .border(1.dp, NeonCyan, RoundedCornerShape(8.dp))
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "★ NEW BEST SCORE! ★",
                                style = MaterialTheme.typography.labelLarge,
                                color = NeonCyanBright,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Stats Grid: Platforms Reached, Streak, Best Score
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Platforms Reached
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(VoidSurface.copy(alpha = 0.6f))
                            .border(1.dp, ElectricBlue.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                            .padding(14.dp)
                    ) {
                        Text(
                            text = "PLATFORMS",
                            style = MaterialTheme.typography.labelLarge,
                            color = ElectricBlue,
                            fontSize = 10.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${gameState.platformsReached}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Highest Streak
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(VoidSurface.copy(alpha = 0.6f))
                            .border(1.dp, NeonPurple.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                            .padding(14.dp)
                    ) {
                        Text(
                            text = "BEST STREAK",
                            style = MaterialTheme.typography.labelLarge,
                            color = NeonPurple,
                            fontSize = 10.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${gameState.highestStreakThisRun}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // All-Time Best
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(VoidSurface.copy(alpha = 0.6f))
                            .border(1.dp, NeonCyan.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                            .padding(14.dp)
                    ) {
                        Text(
                            text = "ALL-TIME",
                            style = MaterialTheme.typography.labelLarge,
                            color = NeonCyan,
                            fontSize = 10.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${preferences.bestScore}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Action Buttons
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                GlowingButton(
                    text = "PLAY AGAIN",
                    icon = Icons.Default.Refresh,
                    onClick = onPlayAgainClick,
                    hapticsEnabled = preferences.hapticsEnabled,
                    modifier = Modifier.fillMaxWidth(0.85f)
                )

                Spacer(modifier = Modifier.height(14.dp))

                GlowingButton(
                    text = "HOME",
                    icon = Icons.Default.Home,
                    isPrimary = false,
                    onClick = onHomeClick,
                    hapticsEnabled = preferences.hapticsEnabled,
                    modifier = Modifier.fillMaxWidth(0.85f)
                )
            }
        }
    }
}
