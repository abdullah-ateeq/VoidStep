package com.example.voidstep.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.voidstep.data.UserPreferences
import com.example.voidstep.game.model.GameState
import com.example.voidstep.ui.components.GamePlayfield
import com.example.voidstep.ui.components.GlowingButton
import com.example.voidstep.ui.components.UrgencyBar
import com.example.voidstep.ui.components.VoidBackground
import com.example.voidstep.ui.theme.DangerVoid
import com.example.voidstep.ui.theme.NeonCyan
import com.example.voidstep.ui.theme.NeonCyanBright
import com.example.voidstep.ui.theme.NeonPurple
import com.example.voidstep.ui.theme.VoidSurface
import kotlinx.coroutines.delay

@Composable
fun GameScreen(
    gameState: GameState,
    preferences: UserPreferences,
    onStepLeft: () -> Unit,
    onStepRight: () -> Unit,
    onPauseClick: () -> Unit,
    onResumeClick: () -> Unit,
    onRestartClick: () -> Unit,
    onHomeClick: () -> Unit,
    onGameOverNavigate: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    // Automatically navigate to Result Screen on Game Over after the fall animation finishes
    LaunchedEffect(gameState.isGameOver) {
        if (gameState.isGameOver) {
            if (preferences.hapticsEnabled) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            }
            delay(850) // Allow smooth fall and screen shake transition
            onGameOverNavigate()
        }
    }

    val handleStepLeft = {
        if (preferences.hapticsEnabled) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
        onStepLeft()
    }

    val handleStepRight = {
        if (preferences.hapticsEnabled) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
        onStepRight()
    }

    VoidBackground(modifier = modifier) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Main Perspective Playfield
            GamePlayfield(
                gameState = gameState,
                onStepLeft = handleStepLeft,
                onStepRight = handleStepRight,
                modifier = Modifier.fillMaxSize()
            )

            // Top HUD: Score, Streak, Pause & Urgency Bar
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Score & Streak Badges
                    Column {
                        Text(
                            text = "${gameState.score}",
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Bold,
                            color = NeonCyanBright
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "STREAK ${gameState.streak}",
                                style = MaterialTheme.typography.labelLarge,
                                color = if (gameState.streak >= 5) NeonPurple else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "STEP ${gameState.platformsReached}",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp
                            )
                        }
                    }

                    // Small Pause Button
                    IconButton(
                        onClick = onPauseClick,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(VoidSurface.copy(alpha = 0.7f))
                            .border(1.dp, NeonCyan.copy(alpha = 0.4f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Pause,
                            contentDescription = "Pause",
                            tint = NeonCyan
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Stability / Decision Urgency Bar
                UrgencyBar(
                    fractionRemaining = gameState.timeRemainingFraction,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Streak Feedback Toast Overlay (Pops up on fast step or streak milestones)
            if (gameState.streakFeedbackAlpha > 0.05f && gameState.streakBonusText != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .alpha(gameState.streakFeedbackAlpha)
                        .clip(RoundedCornerShape(12.dp))
                        .background(VoidSurface.copy(alpha = 0.85f))
                        .border(1.dp, NeonCyanBright.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 18.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = gameState.streakBonusText,
                        style = MaterialTheme.typography.labelLarge,
                        color = NeonCyanBright,
                        fontSize = 13.sp,
                        letterSpacing = 1.sp
                    )
                }
            }

            // Subtle Left / Right On-Screen Lane Guide Indicators (Bottom)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 24.dp, vertical = 28.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Step Zone Hint
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(VoidSurface.copy(alpha = 0.35f))
                        .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(14.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronLeft,
                        contentDescription = null,
                        tint = NeonCyan.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "LEFT",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        fontSize = 11.sp
                    )
                }

                // Right Step Zone Hint
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(VoidSurface.copy(alpha = 0.35f))
                        .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(14.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = "RIGHT",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = NeonCyan.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Pause Dialog Overlay
            AnimatedVisibility(
                visible = gameState.isPaused,
                enter = fadeIn(tween(180)),
                exit = fadeOut(tween(180))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.75f))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { /* block background clicks */ },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .clip(RoundedCornerShape(24.dp))
                            .background(VoidSurface)
                            .border(1.dp, NeonCyan.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
                            .padding(28.dp)
                    ) {
                        Text(
                            text = "PAUSED",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = NeonCyan
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        GlowingButton(
                            text = "RESUME",
                            icon = Icons.Default.PlayArrow,
                            onClick = onResumeClick,
                            hapticsEnabled = preferences.hapticsEnabled,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        GlowingButton(
                            text = "RESTART",
                            icon = Icons.Default.Refresh,
                            isPrimary = false,
                            onClick = onRestartClick,
                            hapticsEnabled = preferences.hapticsEnabled,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        GlowingButton(
                            text = "HOME",
                            icon = Icons.Default.Home,
                            isPrimary = false,
                            onClick = onHomeClick,
                            hapticsEnabled = preferences.hapticsEnabled,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}
