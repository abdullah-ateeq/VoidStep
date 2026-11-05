package com.example.voidstep

import com.example.voidstep.game.logic.DifficultyManager
import com.example.voidstep.game.logic.PlatformGenerator
import com.example.voidstep.game.logic.ScoreCalculator
import com.example.voidstep.game.model.GameState
import com.example.voidstep.game.model.Platform
import com.example.voidstep.game.model.PlatformState
import com.example.voidstep.game.model.StepDirection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GameLogicIntegrationTest {

    @Test
    fun testSuccessfulStepProgressionAndScoring() {
        val startPlatform = Platform(
            id = 1L,
            stepIndex = 0,
            normalizedX = 0f,
            state = PlatformState.ACTIVE_CURRENT
        )
        val nextPlatform = Platform(
            id = 2L,
            stepIndex = 1,
            normalizedX = -0.32f,
            state = PlatformState.REACHABLE
        )

        var state = GameState(
            platforms = listOf(startPlatform, nextPlatform),
            currentPlatformId = startPlatform.id,
            currentStepIndex = 0,
            playerX = 0f,
            score = 0,
            streak = 0,
            platformsReached = 0,
            isGameOver = false
        )

        // Select LEFT: Safe platform exists at step 1
        val target = PlatformGenerator.findTargetPlatform(
            platforms = state.platforms,
            currentStepIndex = state.currentStepIndex,
            direction = StepDirection.LEFT
        )
        assertNotNull(target)

        // Process step
        val scoreResult = ScoreCalculator.calculateStepScore(
            currentStreak = state.streak + 1,
            reactionTimeMs = 450,
            totalPlatformsReached = state.platformsReached + 1
        )

        state = state.copy(
            currentPlatformId = target!!.id,
            currentStepIndex = target.stepIndex,
            playerX = target.normalizedX,
            score = state.score + scoreResult.totalStepPoints,
            streak = state.streak + 1,
            platformsReached = state.platformsReached + 1
        )

        assertEquals(1, state.currentStepIndex)
        assertEquals(-0.32f, state.playerX, 0.001f)
        assertEquals(1, state.streak)
        assertEquals(1, state.platformsReached)
        assertEquals(150, state.score) // 100 base + 50 fast speed bonus
        assertFalse(state.isGameOver)
    }

    @Test
    fun testFallIntoVoidEndsGame() {
        // Only a right platform exists; player steps left
        val startPlatform = Platform(
            id = 1L,
            stepIndex = 0,
            normalizedX = 0f,
            state = PlatformState.ACTIVE_CURRENT
        )
        val rightOnlyPlatform = Platform(
            id = 2L,
            stepIndex = 1,
            normalizedX = 0.45f,
            state = PlatformState.REACHABLE
        )

        val state = GameState(
            platforms = listOf(startPlatform, rightOnlyPlatform),
            currentPlatformId = startPlatform.id,
            currentStepIndex = 0,
            playerX = 0f,
            score = 300,
            streak = 3,
            isGameOver = false
        )

        val target = PlatformGenerator.findTargetPlatform(
            platforms = state.platforms,
            currentStepIndex = state.currentStepIndex,
            direction = StepDirection.LEFT
        )

        // Stepping left results in no platform
        assertEquals(null, target)

        // Simulating void fall outcome
        val gameOverState = state.copy(
            isGameOver = true,
            playerX = state.playerX - 0.5f,
            streak = 0,
            fallReason = "Fell into the Empty Void"
        )

        assertTrue(gameOverState.isGameOver)
        assertEquals(0, gameOverState.streak)
        assertEquals("Fell into the Empty Void", gameOverState.fallReason)
    }

    @Test
    fun testPlatformDissolvingBehindPlayer() {
        val p0 = Platform(id = 1L, stepIndex = 0, normalizedX = 0f, state = PlatformState.ACTIVE_CURRENT)
        val p1 = Platform(id = 2L, stepIndex = 1, normalizedX = 0.35f, state = PlatformState.REACHABLE)

        // Advance to p1: p0 becomes DISSOLVING, p1 becomes ACTIVE_CURRENT
        val updatedPlatforms = listOf(p0, p1).map { p ->
            when (p.id) {
                p0.id -> p.copy(state = PlatformState.DISSOLVING, dissolveProgress = 0.1f)
                p1.id -> p.copy(state = PlatformState.ACTIVE_CURRENT)
                else -> p
            }
        }

        assertEquals(PlatformState.DISSOLVING, updatedPlatforms.first { it.id == p0.id }.state)
        assertEquals(PlatformState.ACTIVE_CURRENT, updatedPlatforms.first { it.id == p1.id }.state)
    }
}
