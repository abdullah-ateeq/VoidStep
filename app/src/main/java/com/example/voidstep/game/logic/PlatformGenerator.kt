package com.example.voidstep.game.logic

import com.example.voidstep.game.model.Platform
import com.example.voidstep.game.model.PlatformState
import com.example.voidstep.game.model.StepDirection
import java.util.concurrent.atomic.AtomicLong
import kotlin.math.abs
import kotlin.random.Random

object PlatformGenerator {
    private val idCounter = AtomicLong(1L)

    fun resetIdCounter() {
        idCounter.set(1L)
    }

    /**
     * Creates the initial set of platforms for starting a run.
     * Platform 0 is the starting platform right under the player.
     * Platforms 1..5 are generated ahead.
     */
    fun createInitialPlatforms(random: Random = Random.Default): List<Platform> {
        val platforms = mutableListOf<Platform>()

        // Starting platform: centered, stable
        val startPlatform = Platform(
            id = idCounter.getAndIncrement(),
            stepIndex = 0,
            normalizedX = 0f,
            widthFraction = 0.46f,
            heightFraction = 0.08f,
            state = PlatformState.ACTIVE_CURRENT
        )
        platforms.add(startPlatform)

        // Generate the next 5 steps ahead
        var lastX = 0f
        var lastDirection = if (random.nextBoolean()) StepDirection.LEFT else StepDirection.RIGHT

        for (step in 1..5) {
            val difficulty = DifficultyManager.getDifficultyForScore(0, step)
            // Generate next step
            val nextPlatforms = generateStepPlatforms(
                stepIndex = step,
                lastSafeX = lastX,
                lastDirection = lastDirection,
                difficulty = difficulty,
                random = random
            )
            platforms.addAll(nextPlatforms)

            // Track safe target for next iteration
            val safeTarget = nextPlatforms.firstOrNull { it.state == PlatformState.REACHABLE }
            if (safeTarget != null) {
                lastX = safeTarget.normalizedX
                lastDirection = if (safeTarget.normalizedX < 0) StepDirection.LEFT else StepDirection.RIGHT
            }
        }

        return platforms
    }

    /**
     * Generates platforms for a specific step ahead.
     * Guarantees at least one reachable safe platform within maxHorizontalGap.
     */
    fun generateStepPlatforms(
        stepIndex: Int,
        lastSafeX: Float,
        lastDirection: StepDirection,
        difficulty: DifficultyConfig,
        random: Random = Random.Default
    ): List<Platform> {
        val platforms = mutableListOf<Platform>()
        val width = difficulty.platformWidthFraction
        val maxGap = difficulty.maxHorizontalGap

        // Determine target direction for the primary safe platform
        // In Easy, alternate mostly; in Medium/Hard, mix it up
        val primaryDirection = when (difficulty.level) {
            com.example.voidstep.game.model.DifficultyLevel.EASY -> {
                // 80% alternation for gentle rhythmic feel
                if (random.nextFloat() < 0.8f) {
                    if (lastDirection == StepDirection.LEFT) StepDirection.RIGHT else StepDirection.LEFT
                } else {
                    lastDirection
                }
            }
            com.example.voidstep.game.model.DifficultyLevel.MEDIUM -> {
                if (random.nextFloat() < 0.6f) {
                    if (lastDirection == StepDirection.LEFT) StepDirection.RIGHT else StepDirection.LEFT
                } else {
                    lastDirection
                }
            }
            com.example.voidstep.game.model.DifficultyLevel.HARD -> {
                if (random.nextBoolean()) StepDirection.LEFT else StepDirection.RIGHT
            }
        }

        // Calculate primary safe X
        val rawX = if (primaryDirection == StepDirection.LEFT) {
            -random.nextFloat() * 0.45f - 0.18f // between -0.18 and -0.63
        } else {
            random.nextFloat() * 0.45f + 0.18f  // between +0.18 and +0.63
        }

        // Ensure reachability: clamp rawX within maxGap from lastSafeX
        val clampedX = clampToReachable(rawX, lastSafeX, maxGap)

        val primaryPlatform = Platform(
            id = idCounter.getAndIncrement(),
            stepIndex = stepIndex,
            normalizedX = clampedX,
            widthFraction = width,
            heightFraction = 0.075f,
            state = PlatformState.REACHABLE,
            isDeceptiveGap = false
        )
        platforms.add(primaryPlatform)

        // Occasionally generate a secondary alternative platform (branching path) in Easy/Medium
        // but avoid clutter so the player clearly knows where they can step
        val shouldGenerateAlternative = when (difficulty.level) {
            com.example.voidstep.game.model.DifficultyLevel.EASY -> random.nextFloat() < 0.25f
            com.example.voidstep.game.model.DifficultyLevel.MEDIUM -> random.nextFloat() < 0.35f
            com.example.voidstep.game.model.DifficultyLevel.HARD -> random.nextFloat() < 0.15f
        }

        if (shouldGenerateAlternative) {
            val altDirection = if (primaryDirection == StepDirection.LEFT) StepDirection.RIGHT else StepDirection.LEFT
            val altRawX = if (altDirection == StepDirection.LEFT) {
                -random.nextFloat() * 0.35f - 0.22f
            } else {
                random.nextFloat() * 0.35f + 0.22f
            }
            val altClampedX = clampToReachable(altRawX, lastSafeX, maxGap)

            // Only add if there's sufficient separation between the two platforms
            if (abs(altClampedX - clampedX) > (width * 1.15f)) {
                platforms.add(
                    Platform(
                        id = idCounter.getAndIncrement(),
                        stepIndex = stepIndex,
                        normalizedX = altClampedX,
                        widthFraction = width * 0.95f,
                        heightFraction = 0.075f,
                        state = PlatformState.REACHABLE,
                        isDeceptiveGap = false
                    )
                )
            }
        }

        return platforms
    }

    /**
     * Clamps target X so the distance from lastSafeX never exceeds maxGap.
     */
    fun clampToReachable(targetX: Float, lastSafeX: Float, maxGap: Float): Float {
        val minX = (lastSafeX - maxGap).coerceAtLeast(-0.70f)
        val maxX = (lastSafeX + maxGap).coerceAtMost(0.70f)
        return targetX.coerceIn(minX, maxX)
    }

    /**
     * Finds which platform (if any) is reached when stepping in direction [direction]
     * for the immediately next step ([currentStepIndex] + 1).
     * Returns null if player stepped into empty space.
     */
    fun findTargetPlatform(
        platforms: List<Platform>,
        currentStepIndex: Int,
        direction: StepDirection
    ): Platform? {
        val nextStepIndex = currentStepIndex + 1
        val candidates = platforms.filter {
            it.stepIndex == nextStepIndex && it.state == PlatformState.REACHABLE
        }

        return when (direction) {
            StepDirection.LEFT -> {
                // Find candidates strictly on the left (x < 0 or clearly to left)
                candidates.filter { it.normalizedX < 0.05f }.minByOrNull { it.normalizedX }
                    ?: candidates.filter { it.normalizedX <= 0.1f }.minByOrNull { it.normalizedX }
            }
            StepDirection.RIGHT -> {
                // Find candidates strictly on the right (x > 0 or clearly to right)
                candidates.filter { it.normalizedX > -0.05f }.maxByOrNull { it.normalizedX }
                    ?: candidates.filter { it.normalizedX >= -0.1f }.maxByOrNull { it.normalizedX }
            }
        }
    }

    /**
     * Validates whether a platform is within jump reach from a given position.
     */
    fun isReachable(
        fromX: Float,
        targetPlatform: Platform,
        maxGap: Float
    ): Boolean {
        val distance = abs(targetPlatform.normalizedX - fromX)
        return distance <= (maxGap + (targetPlatform.widthFraction / 2f))
    }
}
