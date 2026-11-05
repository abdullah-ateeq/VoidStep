package com.example.voidstep.game.model

import com.example.voidstep.game.logic.DifficultyConfig
import com.example.voidstep.game.logic.DifficultyManager

data class GameState(
    val platforms: List<Platform> = emptyList(),
    val currentPlatformId: Long = 0L,
    val currentStepIndex: Int = 0,
    val playerX: Float = 0f,
    val playerYOffset: Float = 0f,
    val playerScale: Float = 1f,
    val playerAlpha: Float = 1f,
    val score: Int = 0,
    val streak: Int = 0,
    val highestStreakThisRun: Int = 0,
    val platformsReached: Int = 0,
    val isGameOver: Boolean = false,
    val isPaused: Boolean = false,
    val isNewBestScore: Boolean = false,
    val isNewBestStreak: Boolean = false,
    val screenShake: Float = 0f,
    val lastStepTimestamp: Long = 0L,
    val timeRemainingFraction: Float = 1.0f,
    val difficultyConfig: DifficultyConfig = DifficultyManager.getDifficultyForScore(0, 0),
    val streakBonusText: String? = null,
    val streakFeedbackAlpha: Float = 0f,
    val fallReason: String? = null
)
