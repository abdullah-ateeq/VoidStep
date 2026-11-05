package com.example.voidstep

import com.example.voidstep.game.logic.DifficultyManager
import com.example.voidstep.game.logic.PlatformGenerator
import com.example.voidstep.game.model.Platform
import com.example.voidstep.game.model.PlatformState
import com.example.voidstep.game.model.StepDirection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.math.abs
import kotlin.random.Random

class PlatformGeneratorTest {

    @Before
    fun setUp() {
        PlatformGenerator.resetIdCounter()
    }

    @Test
    fun testInitialPlatformGeneration() {
        val platforms = PlatformGenerator.createInitialPlatforms(Random(123))

        // Must generate starting platform + 5 steps ahead
        assertTrue(platforms.isNotEmpty())
        val start = platforms.first()
        assertEquals(0, start.stepIndex)
        assertEquals(0.0f, start.normalizedX, 0.001f)
        assertEquals(PlatformState.ACTIVE_CURRENT, start.state)

        // Ensure steps 1 to 5 are present
        val steps = platforms.map { it.stepIndex }.distinct().sorted()
        assertEquals(listOf(0, 1, 2, 3, 4, 5), steps)
    }

    @Test
    fun testReachabilityClampGuaranteesValidJumps() {
        val lastX = 0.2f
        val maxGap = 0.45f

        // When raw target X is very far left, it clamps to lastX - maxGap
        val clampedFarLeft = PlatformGenerator.clampToReachable(-0.9f, lastX, maxGap)
        assertTrue(abs(clampedFarLeft - lastX) <= maxGap + 0.001f)

        // When raw target X is very far right, it clamps to lastX + maxGap
        val clampedFarRight = PlatformGenerator.clampToReachable(0.9f, lastX, maxGap)
        assertTrue(abs(clampedFarRight - lastX) <= maxGap + 0.001f)
    }

    @Test
    fun testEveryStepHasReachablePlatform() {
        val random = Random(999)
        var lastX = 0f
        var lastDir = StepDirection.LEFT

        for (step in 1..50) {
            val difficulty = DifficultyManager.getDifficultyForScore(step * 100, step)
            val stepPlatforms = PlatformGenerator.generateStepPlatforms(
                stepIndex = step,
                lastSafeX = lastX,
                lastDirection = lastDir,
                difficulty = difficulty,
                random = random
            )

            assertTrue("Step $step has no platforms", stepPlatforms.isNotEmpty())
            val safe = stepPlatforms.firstOrNull { it.state == PlatformState.REACHABLE }
            assertNotNull("Step $step has no reachable platform", safe)

            // Validate distance from last step
            val distance = abs(safe!!.normalizedX - lastX)
            assertTrue(
                "Step $step jump impossible: distance $distance > maxGap ${difficulty.maxHorizontalGap}",
                distance <= difficulty.maxHorizontalGap + 0.001f
            )

            lastX = safe.normalizedX
            lastDir = if (safe.normalizedX < 0) StepDirection.LEFT else StepDirection.RIGHT
        }
    }

    @Test
    fun testFindTargetPlatformOnLeftAndRight() {
        val platformLeft = Platform(
            id = 1L,
            stepIndex = 1,
            normalizedX = -0.35f,
            state = PlatformState.REACHABLE
        )
        val platformRight = Platform(
            id = 2L,
            stepIndex = 1,
            normalizedX = 0.40f,
            state = PlatformState.REACHABLE
        )

        val platforms = listOf(platformLeft, platformRight)

        val targetLeft = PlatformGenerator.findTargetPlatform(platforms, currentStepIndex = 0, direction = StepDirection.LEFT)
        assertNotNull(targetLeft)
        assertEquals(1L, targetLeft!!.id)

        val targetRight = PlatformGenerator.findTargetPlatform(platforms, currentStepIndex = 0, direction = StepDirection.RIGHT)
        assertNotNull(targetRight)
        assertEquals(2L, targetRight!!.id)
    }

    @Test
    fun testFindTargetPlatformDetectsEmptyVoid() {
        // Only a right platform exists for step 1; stepping left leads to void
        val platformRightOnly = Platform(
            id = 5L,
            stepIndex = 1,
            normalizedX = 0.38f,
            state = PlatformState.REACHABLE
        )
        val platforms = listOf(platformRightOnly)

        val targetLeft = PlatformGenerator.findTargetPlatform(platforms, currentStepIndex = 0, direction = StepDirection.LEFT)
        // Stepping left must return null (void / empty space!)
        assertNull("Stepping left towards empty space should return null", targetLeft)

        val targetRight = PlatformGenerator.findTargetPlatform(platforms, currentStepIndex = 0, direction = StepDirection.RIGHT)
        assertNotNull(targetRight)
        assertEquals(5L, targetRight!!.id)
    }
}
