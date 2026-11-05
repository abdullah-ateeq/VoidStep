package com.example.voidstep.game.logic

data class StepScoreResult(
    val baseScore: Int,
    val streakMultiplier: Float,
    val speedBonus: Int,
    val milestoneBonus: Int,
    val totalStepPoints: Int
)

object ScoreCalculator {
    const val BASE_POINTS_PER_PLATFORM = 100

    fun getStreakMultiplier(streak: Int): Float {
        return when {
            streak >= 30 -> 3.0f
            streak >= 20 -> 2.5f
            streak >= 10 -> 2.0f
            streak >= 5 -> 1.5f
            else -> 1.0f
        }
    }

    fun calculateSpeedBonus(reactionTimeMs: Long): Int {
        return when {
            reactionTimeMs in 1..600 -> 50
            reactionTimeMs in 601..1100 -> 25
            else -> 0
        }
    }

    fun calculateMilestoneBonus(platformsReached: Int): Int {
        return if (platformsReached > 0 && platformsReached % 10 == 0) {
            250
        } else {
            0
        }
    }

    fun calculateStepScore(
        currentStreak: Int,
        reactionTimeMs: Long,
        totalPlatformsReached: Int
    ): StepScoreResult {
        val multiplier = getStreakMultiplier(currentStreak)
        val speedBonus = calculateSpeedBonus(reactionTimeMs)
        val milestoneBonus = calculateMilestoneBonus(totalPlatformsReached)
        val total = ((BASE_POINTS_PER_PLATFORM * multiplier).toInt()) + speedBonus + milestoneBonus

        return StepScoreResult(
            baseScore = BASE_POINTS_PER_PLATFORM,
            streakMultiplier = multiplier,
            speedBonus = speedBonus,
            milestoneBonus = milestoneBonus,
            totalStepPoints = total
        )
    }
}
