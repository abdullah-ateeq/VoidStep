package com.example.voidstep

import com.example.voidstep.game.logic.DifficultyManager
import com.example.voidstep.game.model.DifficultyLevel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DifficultyManagerTest {

    @Test
    fun testEasyDifficultyProgression() {
        val config = DifficultyManager.getDifficultyForScore(0, 5)
        assertEquals(DifficultyLevel.EASY, config.level)
        assertEquals(0.44f, config.platformWidthFraction, 0.001f)
        assertEquals(0.42f, config.maxHorizontalGap, 0.001f)
        assertEquals(3500L, config.decisionTimeWindowMs)
    }

    @Test
    fun testMediumDifficultyProgression() {
        val config = DifficultyManager.getDifficultyForScore(1500, 15)
        assertEquals(DifficultyLevel.MEDIUM, config.level)
        assertEquals(0.35f, config.platformWidthFraction, 0.001f)
        assertEquals(0.58f, config.maxHorizontalGap, 0.001f)
        assertEquals(2800L, config.decisionTimeWindowMs)
    }

    @Test
    fun testHardDifficultyProgression() {
        val configStep30 = DifficultyManager.getDifficultyForScore(4000, 30)
        assertEquals(DifficultyLevel.HARD, configStep30.level)
        assertTrue(configStep30.platformWidthFraction <= 0.28f)
        assertTrue(configStep30.maxHorizontalGap >= 0.65f)
        assertTrue(configStep30.decisionTimeWindowMs <= 2200L)

        // At higher steps, platforms become even smaller and time window shorter
        val configStep60 = DifficultyManager.getDifficultyForScore(12000, 60)
        assertTrue(configStep60.platformWidthFraction < configStep30.platformWidthFraction)
        assertTrue(configStep60.decisionTimeWindowMs < configStep30.decisionTimeWindowMs)
    }

    @Test
    fun testDecisionWindowIsAlwaysFair() {
        for (step in 0..100) {
            val config = DifficultyManager.getDifficultyForScore(step * 150, step)
            // Decision window is never less than 1.4 seconds, keeping it fair
            assertTrue("Window too short at step $step: ${config.decisionTimeWindowMs}", config.decisionTimeWindowMs >= 1400L)
            // Platform width is never less than 0.20
            assertTrue("Width too small at step $step: ${config.platformWidthFraction}", config.platformWidthFraction >= 0.20f)
        }
    }
}
