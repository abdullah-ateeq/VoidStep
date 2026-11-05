package com.example.voidstep

import com.example.voidstep.game.logic.ScoreCalculator
import org.junit.Assert.assertEquals
import org.junit.Test

class ScoreCalculatorTest {

    @Test
    fun testBaseScoreValue() {
        assertEquals(100, ScoreCalculator.BASE_POINTS_PER_PLATFORM)
    }

    @Test
    fun testStreakMultipliers() {
        // Streaks < 5: 1.0x
        assertEquals(1.0f, ScoreCalculator.getStreakMultiplier(0), 0.001f)
        assertEquals(1.0f, ScoreCalculator.getStreakMultiplier(1), 0.001f)
        assertEquals(1.0f, ScoreCalculator.getStreakMultiplier(4), 0.001f)

        // Streaks 5-9: 1.5x
        assertEquals(1.5f, ScoreCalculator.getStreakMultiplier(5), 0.001f)
        assertEquals(1.5f, ScoreCalculator.getStreakMultiplier(9), 0.001f)

        // Streaks 10-19: 2.0x
        assertEquals(2.0f, ScoreCalculator.getStreakMultiplier(10), 0.001f)
        assertEquals(2.0f, ScoreCalculator.getStreakMultiplier(19), 0.001f)

        // Streaks 20-29: 2.5x
        assertEquals(2.5f, ScoreCalculator.getStreakMultiplier(20), 0.001f)
        assertEquals(2.5f, ScoreCalculator.getStreakMultiplier(29), 0.001f)

        // Streaks >= 30: 3.0x
        assertEquals(3.0f, ScoreCalculator.getStreakMultiplier(30), 0.001f)
        assertEquals(3.0f, ScoreCalculator.getStreakMultiplier(50), 0.001f)
    }

    @Test
    fun testSpeedBonus() {
        // Fast reaction (< 600ms): 50 points
        assertEquals(50, ScoreCalculator.calculateSpeedBonus(300))
        assertEquals(50, ScoreCalculator.calculateSpeedBonus(600))

        // Moderate reaction (601ms - 1100ms): 25 points
        assertEquals(25, ScoreCalculator.calculateSpeedBonus(750))
        assertEquals(25, ScoreCalculator.calculateSpeedBonus(1100))

        // Slow reaction (> 1100ms): 0 points
        assertEquals(0, ScoreCalculator.calculateSpeedBonus(1500))
    }

    @Test
    fun testMilestoneBonus() {
        // Every 10 platforms awards +250 points
        assertEquals(0, ScoreCalculator.calculateMilestoneBonus(0))
        assertEquals(0, ScoreCalculator.calculateMilestoneBonus(5))
        assertEquals(250, ScoreCalculator.calculateMilestoneBonus(10))
        assertEquals(0, ScoreCalculator.calculateMilestoneBonus(15))
        assertEquals(250, ScoreCalculator.calculateMilestoneBonus(20))
        assertEquals(250, ScoreCalculator.calculateMilestoneBonus(30))
    }

    @Test
    fun testCalculateStepScoreCombined() {
        // Step with 1x multiplier, fast speed (+50), non-milestone: 100 + 50 = 150
        val normalStep = ScoreCalculator.calculateStepScore(
            currentStreak = 1,
            reactionTimeMs = 400,
            totalPlatformsReached = 1
        )
        assertEquals(100, normalStep.baseScore)
        assertEquals(1.0f, normalStep.streakMultiplier, 0.001f)
        assertEquals(50, normalStep.speedBonus)
        assertEquals(0, normalStep.milestoneBonus)
        assertEquals(150, normalStep.totalStepPoints)

        // Milestone 10 with streak 10 (2x multiplier) and fast speed (+50):
        // (100 * 2.0) + 50 + 250 = 500
        val milestoneStep = ScoreCalculator.calculateStepScore(
            currentStreak = 10,
            reactionTimeMs = 500,
            totalPlatformsReached = 10
        )
        assertEquals(2.0f, milestoneStep.streakMultiplier, 0.001f)
        assertEquals(50, milestoneStep.speedBonus)
        assertEquals(250, milestoneStep.milestoneBonus)
        assertEquals(500, milestoneStep.totalStepPoints)
    }
}
