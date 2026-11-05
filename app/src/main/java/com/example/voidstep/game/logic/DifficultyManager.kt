package com.example.voidstep.game.logic

import com.example.voidstep.game.model.DifficultyLevel

data class DifficultyConfig(
    val level: DifficultyLevel,
    val platformWidthFraction: Float,
    val maxHorizontalGap: Float,
    val decisionTimeWindowMs: Long,
    val scrollSpeedMultiplier: Float
)

object DifficultyManager {

    fun getDifficultyForScore(score: Int, platformsReached: Int): DifficultyConfig {
        return when {
            platformsReached < 10 -> DifficultyConfig(
                level = DifficultyLevel.EASY,
                platformWidthFraction = 0.44f,
                maxHorizontalGap = 0.42f,
                decisionTimeWindowMs = 3500L,
                scrollSpeedMultiplier = 1.0f
            )
            platformsReached < 30 -> DifficultyConfig(
                level = DifficultyLevel.MEDIUM,
                platformWidthFraction = 0.35f,
                maxHorizontalGap = 0.58f,
                decisionTimeWindowMs = 2800L,
                scrollSpeedMultiplier = 1.25f
            )
            else -> {
                // Hard mode scales continuously with further progress
                val extraSteps = (platformsReached - 30).coerceAtMost(50)
                val width = (0.28f - (extraSteps * 0.0015f)).coerceAtLeast(0.20f)
                val gap = (0.65f + (extraSteps * 0.002f)).coerceAtMost(0.78f)
                val timeWindow = (2200L - (extraSteps * 15L)).coerceAtLeast(1400L)

                DifficultyConfig(
                    level = DifficultyLevel.HARD,
                    platformWidthFraction = width,
                    maxHorizontalGap = gap,
                    decisionTimeWindowMs = timeWindow,
                    scrollSpeedMultiplier = 1.5f
                )
            }
        }
    }
}
