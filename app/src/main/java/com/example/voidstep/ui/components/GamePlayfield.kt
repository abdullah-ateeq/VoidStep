package com.example.voidstep.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import com.example.voidstep.game.model.GameState
import com.example.voidstep.game.model.Platform
import com.example.voidstep.game.model.PlatformState
import com.example.voidstep.ui.theme.DangerVoid
import com.example.voidstep.ui.theme.ElectricBlue
import com.example.voidstep.ui.theme.NeonCyan
import com.example.voidstep.ui.theme.NeonCyanBright
import com.example.voidstep.ui.theme.NeonPurple
import com.example.voidstep.ui.theme.VoidBlack
import kotlin.math.abs
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun GamePlayfield(
    gameState: GameState,
    onStepLeft: () -> Unit,
    onStepRight: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Smooth animated player X position
    val animatedPlayerX = remember { Animatable(gameState.playerX) }
    LaunchedEffect(gameState.playerX) {
        animatedPlayerX.animateTo(
            targetValue = gameState.playerX,
            animationSpec = tween(durationMillis = 140, easing = FastOutSlowInEasing)
        )
    }

    // Reachable platform subtle pulse
    val infiniteTransition = rememberInfiniteTransition(label = "platform_pulse")
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_val"
    )

    val isDark = MaterialTheme.colorScheme.background.red < 0.5f

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(gameState.isGameOver, gameState.isPaused) {
                if (gameState.isGameOver || gameState.isPaused) return@pointerInput

                var dragAccumulatorX = 0f
                var dragHandled = false

                detectDragGestures(
                    onDragStart = {
                        dragAccumulatorX = 0f
                        dragHandled = false
                    },
                    onDragEnd = {
                        dragAccumulatorX = 0f
                        dragHandled = false
                    },
                    onDragCancel = {
                        dragAccumulatorX = 0f
                        dragHandled = false
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        dragAccumulatorX += dragAmount.x
                        if (!dragHandled) {
                            if (dragAccumulatorX < -40f) {
                                dragHandled = true
                                onStepLeft()
                            } else if (dragAccumulatorX > 40f) {
                                dragHandled = true
                                onStepRight()
                            }
                        }
                    }
                )
            }
            .pointerInput(gameState.isGameOver, gameState.isPaused) {
                if (gameState.isGameOver || gameState.isPaused) return@pointerInput

                detectTapGestures { offset ->
                    val screenWidth = size.width
                    if (offset.x < screenWidth * 0.5f) {
                        onStepLeft()
                    } else {
                        onStepRight()
                    }
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val playerBaseY = height * 0.68f

            // Screen shake translation during falls
            val shakeOffsetX = if (gameState.screenShake > 0.01f) {
                (Random.nextFloat() * 2f - 1f) * gameState.screenShake * 16f
            } else 0f
            val shakeOffsetY = if (gameState.screenShake > 0.01f) {
                (Random.nextFloat() * 2f - 1f) * gameState.screenShake * 16f
            } else 0f

            // Perspective void guidelines
            drawPerspectiveGrid(
                width = width,
                height = height,
                playerBaseY = playerBaseY,
                isDark = isDark
            )

            // Render platforms from back to front (highest stepIndex to lowest)
            val sortedPlatforms = gameState.platforms.sortedByDescending { it.stepIndex }

            for (platform in sortedPlatforms) {
                val relStep = platform.stepIndex - gameState.currentStepIndex
                if (relStep < -2 || relStep > 6) continue

                drawVoidPlatform(
                    platform = platform,
                    relStep = relStep,
                    width = width,
                    height = height,
                    playerBaseY = playerBaseY,
                    pulseGlow = pulseGlow,
                    isDark = isDark,
                    shakeOffsetX = shakeOffsetX,
                    shakeOffsetY = shakeOffsetY
                )
            }

            // Render player character
            drawPlayerCharacter(
                playerXNorm = animatedPlayerX.value,
                playerYOffset = gameState.playerYOffset,
                playerScale = gameState.playerScale,
                playerAlpha = gameState.playerAlpha,
                width = width,
                playerBaseY = playerBaseY,
                shakeOffsetX = shakeOffsetX,
                shakeOffsetY = shakeOffsetY,
                isDark = isDark
            )
        }
    }
}

private fun DrawScope.drawPerspectiveGrid(
    width: Float,
    height: Float,
    playerBaseY: Float,
    isDark: Boolean
) {
    val vanishingPoint = Offset(width * 0.5f, height * 0.08f)
    val gridColor = if (isDark) {
        Color(0xFF1E293B).copy(alpha = 0.35f)
    } else {
        Color(0xFFCBD5E1).copy(alpha = 0.45f)
    }

    // Perspective converging rays into the void horizon
    val rayXPoints = listOf(
        width * -0.2f,
        width * 0.1f,
        width * 0.3f,
        width * 0.7f,
        width * 0.9f,
        width * 1.2f
    )

    for (targetX in rayXPoints) {
        drawLine(
            color = gridColor,
            start = vanishingPoint,
            end = Offset(targetX, height * 1.1f),
            strokeWidth = 1.2f
        )
    }

    // Horizontal depth lines
    for (i in 1..8) {
        val t = i / 9f
        val lineY = vanishingPoint.y + (height - vanishingPoint.y) * (t * t)
        val alpha = if (lineY > playerBaseY) 0.12f else 0.25f * t
        drawLine(
            color = if (isDark) NeonPurple.copy(alpha = alpha) else ElectricBlue.copy(alpha = alpha * 0.5f),
            start = Offset(0f, lineY),
            end = Offset(width, lineY),
            strokeWidth = 1f
        )
    }
}

private fun DrawScope.drawVoidPlatform(
    platform: Platform,
    relStep: Int,
    width: Float,
    height: Float,
    playerBaseY: Float,
    pulseGlow: Float,
    isDark: Boolean,
    shakeOffsetX: Float,
    shakeOffsetY: Float
) {
    val stepSpacing = height * 0.095f

    // Perspective depth calculations
    val perspectiveScale = when {
        relStep >= 0 -> (1.0f - (relStep * 0.09f)).coerceIn(0.45f, 1.0f)
        else -> 1.05f
    }

    val platWidth = platform.widthFraction * width * perspectiveScale
    val platHeight = 22f * perspectiveScale
    val platDepth = 14f * perspectiveScale // Isometric 3D platform thickness

    val centerY = when {
        relStep > 0 -> playerBaseY - (relStep * stepSpacing)
        relStep == 0 -> playerBaseY + 12f
        else -> {
            // RelStep < 0: Dissolving platform dropping into the void
            val dropY = platform.dissolveProgress * (height * 0.18f)
            playerBaseY + (height * 0.11f) + dropY
        }
    }

    val centerX = (width * 0.5f) + (platform.normalizedX * (width * 0.5f) * perspectiveScale) + shakeOffsetX
    val finalCenterY = centerY + shakeOffsetY

    val left = centerX - (platWidth * 0.5f)
    val top = finalCenterY - (platHeight * 0.5f)

    val baseAlpha = when (platform.state) {
        PlatformState.DISSOLVING -> (1.0f - platform.dissolveProgress).coerceIn(0f, 1f)
        PlatformState.GONE -> 0f
        PlatformState.REACHABLE -> if (relStep == 1) 0.95f else 0.75f - (relStep * 0.08f)
        PlatformState.ACTIVE_CURRENT -> 1.0f
    }
    if (baseAlpha <= 0.01f) return

    val cornerRadius = CornerRadius(10f * perspectiveScale, 10f * perspectiveScale)

    // Colors
    val isCandidate = relStep == 1 && platform.state == PlatformState.REACHABLE
    val isActive = platform.state == PlatformState.ACTIVE_CURRENT

    val primaryBorderColor = when {
        isActive -> NeonCyanBright.copy(alpha = baseAlpha)
        isCandidate -> NeonCyan.copy(alpha = baseAlpha * pulseGlow)
        platform.state == PlatformState.DISSOLVING -> DangerVoid.copy(alpha = baseAlpha)
        else -> NeonPurple.copy(alpha = baseAlpha * 0.7f)
    }

    val surfaceColor = when {
        isActive -> if (isDark) Color(0xFF142436).copy(alpha = baseAlpha) else Color(0xFFE0F2FE).copy(alpha = baseAlpha)
        isCandidate -> if (isDark) Color(0xFF101B2E).copy(alpha = baseAlpha) else Color(0xFFF0F9FF).copy(alpha = baseAlpha)
        platform.state == PlatformState.DISSOLVING -> if (isDark) Color(0xFF25111B).copy(alpha = baseAlpha) else Color(0xFFFFE4E6).copy(alpha = baseAlpha)
        else -> if (isDark) Color(0xFF0F1522).copy(alpha = baseAlpha) else Color(0xFFF8FAFC).copy(alpha = baseAlpha)
    }

    val depthColor = when {
        isActive -> if (isDark) Color(0xFF0B1724).copy(alpha = baseAlpha) else Color(0xFFBAE6FD).copy(alpha = baseAlpha)
        platform.state == PlatformState.DISSOLVING -> if (isDark) Color(0xFF1A0A10).copy(alpha = baseAlpha) else Color(0xFFFECDD3).copy(alpha = baseAlpha)
        else -> if (isDark) Color(0xFF090D17).copy(alpha = baseAlpha) else Color(0xFFE2E8F0).copy(alpha = baseAlpha)
    }

    // 1. Draw 3D Depth Underneath (Isometric block thickness)
    val depthPath = Path().apply {
        moveTo(left, top + platHeight * 0.5f)
        lineTo(left, top + platHeight + platDepth)
        lineTo(left + platWidth, top + platHeight + platDepth)
        lineTo(left + platWidth, top + platHeight * 0.5f)
        close()
    }
    drawPath(path = depthPath, color = depthColor)

    // 2. Draw Platform Top Face
    drawRoundRect(
        color = surfaceColor,
        topLeft = Offset(left, top),
        size = Size(platWidth, platHeight),
        cornerRadius = cornerRadius
    )

    // 3. Draw Neon Border
    drawRoundRect(
        color = primaryBorderColor,
        topLeft = Offset(left, top),
        size = Size(platWidth, platHeight),
        cornerRadius = cornerRadius,
        style = Stroke(width = if (isActive || isCandidate) 2.5f * perspectiveScale else 1.5f * perspectiveScale)
    )

    // 4. If Reachable Candidate (Next Step), draw subtle guide ring / pulse chevron
    if (isCandidate) {
        val chevronY = top - 12f * perspectiveScale
        val chevronAlpha = baseAlpha * pulseGlow * 0.9f
        val chevronPath = Path().apply {
            moveTo(centerX - 8f * perspectiveScale, chevronY - 5f * perspectiveScale)
            lineTo(centerX, chevronY)
            lineTo(centerX + 8f * perspectiveScale, chevronY - 5f * perspectiveScale)
        }
        drawPath(
            path = chevronPath,
            color = NeonCyan.copy(alpha = chevronAlpha),
            style = Stroke(width = 2.5f * perspectiveScale)
        )
    }

    // 5. Landing pulse glow ring
    if (platform.pulseProgress > 0f) {
        val extra = (1.0f - platform.pulseProgress) * 20f * perspectiveScale
        drawRoundRect(
            color = NeonCyan.copy(alpha = platform.pulseProgress * 0.8f),
            topLeft = Offset(left - extra, top - extra),
            size = Size(platWidth + (extra * 2f), platHeight + (extra * 2f)),
            cornerRadius = CornerRadius(cornerRadius.x + extra, cornerRadius.y + extra),
            style = Stroke(width = 2f)
        )
    }

    // 6. Dissolve cracks
    if (platform.state == PlatformState.DISSOLVING) {
        val crackProgress = platform.dissolveProgress
        drawLine(
            color = DangerVoid.copy(alpha = baseAlpha * 0.9f),
            start = Offset(left + platWidth * 0.3f, top + 2f),
            end = Offset(left + platWidth * 0.45f, top + platHeight - 2f),
            strokeWidth = 2f * crackProgress
        )
        drawLine(
            color = DangerVoid.copy(alpha = baseAlpha * 0.9f),
            start = Offset(left + platWidth * 0.7f, top + 2f),
            end = Offset(left + platWidth * 0.6f, top + platHeight - 2f),
            strokeWidth = 2f * crackProgress
        )
    }
}

private fun DrawScope.drawPlayerCharacter(
    playerXNorm: Float,
    playerYOffset: Float,
    playerScale: Float,
    playerAlpha: Float,
    width: Float,
    playerBaseY: Float,
    shakeOffsetX: Float,
    shakeOffsetY: Float,
    isDark: Boolean
) {
    if (playerAlpha <= 0.01f || playerScale <= 0.01f) return

    val charCenterX = (width * 0.5f) + (playerXNorm * (width * 0.5f)) + shakeOffsetX
    val charCenterY = playerBaseY - 14f + playerYOffset + shakeOffsetY
    val charRadius = 16f * playerScale

    // Outer Neon Glow Aura
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                NeonCyan.copy(alpha = 0.55f * playerAlpha),
                NeonCyan.copy(alpha = 0.15f * playerAlpha),
                Color.Transparent
            ),
            center = Offset(charCenterX, charCenterY),
            radius = charRadius * 2.8f
        ),
        radius = charRadius * 2.8f,
        center = Offset(charCenterX, charCenterY)
    )

    // Geometric Diamond / Octagon Character Body
    val bodySize = charRadius * 1.8f
    val bodyHalf = bodySize * 0.5f
    val diamondPath = Path().apply {
        moveTo(charCenterX, charCenterY - bodyHalf)
        lineTo(charCenterX + bodyHalf, charCenterY)
        lineTo(charCenterX, charCenterY + bodyHalf)
        lineTo(charCenterX - bodyHalf, charCenterY)
        close()
    }

    // Body fill (Bright white-cyan crystal core)
    drawPath(
        path = diamondPath,
        color = Color.White.copy(alpha = playerAlpha)
    )

    // Inner Neon Core
    drawCircle(
        color = NeonCyanBright.copy(alpha = playerAlpha),
        radius = charRadius * 0.45f,
        center = Offset(charCenterX, charCenterY)
    )

    // Diamond Border
    drawPath(
        path = diamondPath,
        color = NeonCyan.copy(alpha = playerAlpha),
        style = Stroke(width = 2.5f * playerScale)
    )
}
