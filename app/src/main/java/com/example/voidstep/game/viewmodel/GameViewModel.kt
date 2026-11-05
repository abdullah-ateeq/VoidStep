package com.example.voidstep.game.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.voidstep.audio.SoundController
import com.example.voidstep.data.ThemeMode
import com.example.voidstep.data.UserPreferences
import com.example.voidstep.data.UserPreferencesRepository
import com.example.voidstep.game.logic.DifficultyManager
import com.example.voidstep.game.logic.PlatformGenerator
import com.example.voidstep.game.logic.ScoreCalculator
import com.example.voidstep.game.model.GameState
import com.example.voidstep.game.model.Platform
import com.example.voidstep.game.model.PlatformState
import com.example.voidstep.game.model.StepDirection
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

class GameViewModel(
    application: Application,
    private val preferencesRepository: UserPreferencesRepository = UserPreferencesRepository(application)
) : AndroidViewModel(application) {

    private val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    val preferences: StateFlow<UserPreferences> = preferencesRepository.preferencesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserPreferences()
        )

    val soundController = SoundController(
        scope = viewModelScope,
        isEnabled = true
    )

    private var gameLoopJob: Job? = null
    private val random = Random(System.currentTimeMillis())

    init {
        // Sync sound preferences
        viewModelScope.launch {
            preferences.collect { prefs ->
                soundController.isEnabled = prefs.soundEnabled
            }
        }
        startNewGame()
    }

    fun startNewGame() {
        gameLoopJob?.cancel()
        PlatformGenerator.resetIdCounter()

        val initialPlatforms = PlatformGenerator.createInitialPlatforms(random)
        val currentPlatform = initialPlatforms.first()

        val now = System.currentTimeMillis()
        val initialDifficulty = DifficultyManager.getDifficultyForScore(0, 0)

        _gameState.value = GameState(
            platforms = initialPlatforms,
            currentPlatformId = currentPlatform.id,
            currentStepIndex = 0,
            playerX = currentPlatform.normalizedX,
            playerYOffset = 0f,
            playerScale = 1f,
            playerAlpha = 1f,
            score = 0,
            streak = 0,
            highestStreakThisRun = 0,
            platformsReached = 0,
            isGameOver = false,
            isPaused = false,
            screenShake = 0f,
            lastStepTimestamp = now,
            timeRemainingFraction = 1.0f,
            difficultyConfig = initialDifficulty,
            streakBonusText = null,
            streakFeedbackAlpha = 0f,
            fallReason = null
        )

        startGameLoop()
    }

    fun stepLeft() {
        processStep(StepDirection.LEFT)
    }

    fun stepRight() {
        processStep(StepDirection.RIGHT)
    }

    private fun processStep(direction: StepDirection) {
        val currentState = _gameState.value
        if (currentState.isGameOver || currentState.isPaused) return

        val now = System.currentTimeMillis()
        val reactionTime = now - currentState.lastStepTimestamp

        val targetPlatform = PlatformGenerator.findTargetPlatform(
            platforms = currentState.platforms,
            currentStepIndex = currentState.currentStepIndex,
            direction = direction
        )

        if (targetPlatform != null) {
            // Safe platform found -> Land successfully!
            handleSuccessfulStep(targetPlatform, reactionTime, now)
        } else {
            // Empty space -> Fall into the void!
            handleVoidFall(direction)
        }
    }

    private fun handleSuccessfulStep(targetPlatform: Platform, reactionTimeMs: Long, timestamp: Long) {
        val state = _gameState.value
        val newStreak = state.streak + 1
        val newHighestStreak = maxOf(state.highestStreakThisRun, newStreak)
        val newPlatformsReached = state.platformsReached + 1

        val scoreResult = ScoreCalculator.calculateStepScore(
            currentStreak = newStreak,
            reactionTimeMs = reactionTimeMs,
            totalPlatformsReached = newPlatformsReached
        )
        val newScore = state.score + scoreResult.totalStepPoints

        val newDifficulty = DifficultyManager.getDifficultyForScore(newScore, newPlatformsReached)

        // Audio feedback
        soundController.playStepLand()
        if (scoreResult.streakMultiplier > 1.0f || scoreResult.milestoneBonus > 0) {
            soundController.playStreakBonus()
        }
        soundController.playDissolve()

        // Streak text feedback
        val streakText = when {
            scoreResult.milestoneBonus > 0 -> "MILESTONE +${scoreResult.milestoneBonus}!"
            scoreResult.speedBonus > 0 && newStreak >= 5 -> "SWIFT! x${scoreResult.streakMultiplier} STREAK"
            newStreak >= 5 -> "x${scoreResult.streakMultiplier} STREAK"
            scoreResult.speedBonus > 0 -> "SWIFT STEP +${scoreResult.speedBonus}"
            else -> null
        }

        // Update platforms: old platform dissolves, target platform becomes active, generate new ones ahead
        val updatedPlatforms = state.platforms.map { p ->
            when (p.id) {
                state.currentPlatformId -> p.copy(state = PlatformState.DISSOLVING, dissolveProgress = 0.05f)
                targetPlatform.id -> p.copy(state = PlatformState.ACTIVE_CURRENT, pulseProgress = 1.0f)
                else -> p
            }
        }.toMutableList()

        // Generate ahead if fewer than 5 steps ahead
        val maxStepAhead = updatedPlatforms.maxOfOrNull { it.stepIndex } ?: targetPlatform.stepIndex
        if (maxStepAhead - targetPlatform.stepIndex < 5) {
            val nextStepIndex = maxStepAhead + 1
            val lastSafe = updatedPlatforms.filter { it.stepIndex == maxStepAhead }.firstOrNull { it.state == PlatformState.REACHABLE || it.state == PlatformState.ACTIVE_CURRENT }
            val lastSafeX = lastSafe?.normalizedX ?: targetPlatform.normalizedX
            val lastDir = if (lastSafeX < 0) StepDirection.LEFT else StepDirection.RIGHT

            val generated = PlatformGenerator.generateStepPlatforms(
                stepIndex = nextStepIndex,
                lastSafeX = lastSafeX,
                lastDirection = lastDir,
                difficulty = newDifficulty,
                random = random
            )
            updatedPlatforms.addAll(generated)
        }

        // Remove platforms that are far behind to maintain smooth performance
        val prunedPlatforms = updatedPlatforms.filter { it.stepIndex >= targetPlatform.stepIndex - 2 }

        _gameState.update {
            it.copy(
                platforms = prunedPlatforms,
                currentPlatformId = targetPlatform.id,
                currentStepIndex = targetPlatform.stepIndex,
                playerX = targetPlatform.normalizedX,
                playerYOffset = -12f, // Subtle landing bounce initial offset
                score = newScore,
                streak = newStreak,
                highestStreakThisRun = newHighestStreak,
                platformsReached = newPlatformsReached,
                lastStepTimestamp = timestamp,
                timeRemainingFraction = 1.0f,
                difficultyConfig = newDifficulty,
                streakBonusText = streakText,
                streakFeedbackAlpha = if (streakText != null) 1.0f else 0f
            )
        }
    }

    private fun handleVoidFall(direction: StepDirection) {
        val state = _gameState.value
        soundController.playFall()

        val fallTargetX = if (direction == StepDirection.LEFT) {
            (state.playerX - 0.5f).coerceAtLeast(-1.0f)
        } else {
            (state.playerX + 0.5f).coerceAtMost(1.0f)
        }

        _gameState.update {
            it.copy(
                isGameOver = true,
                playerX = fallTargetX,
                screenShake = 1.0f,
                streak = 0,
                fallReason = "Fell into the Empty Void"
            )
        }

        saveRunResults()
        startFallAnimation()
    }

    private fun handleTimerExpiryFall() {
        val state = _gameState.value
        if (state.isGameOver) return

        soundController.playFall()

        // Current platform crumbles under player
        val updatedPlatforms = state.platforms.map {
            if (it.id == state.currentPlatformId) {
                it.copy(state = PlatformState.DISSOLVING, dissolveProgress = 0.5f)
            } else it
        }

        _gameState.update {
            it.copy(
                platforms = updatedPlatforms,
                isGameOver = true,
                screenShake = 1.0f,
                streak = 0,
                fallReason = "Platform Dissolved"
            )
        }

        saveRunResults()
        startFallAnimation()
    }

    private fun saveRunResults() {
        val state = _gameState.value
        viewModelScope.launch {
            val (isNewBest, isNewStreak) = preferencesRepository.recordGameResults(
                score = state.score,
                streak = state.highestStreakThisRun
            )
            _gameState.update {
                it.copy(
                    isNewBestScore = isNewBest,
                    isNewBestStreak = isNewStreak
                )
            }
        }
    }

    private fun startFallAnimation() {
        viewModelScope.launch {
            // Animate character dropping into the void
            for (i in 1..20) {
                delay(25)
                _gameState.update {
                    it.copy(
                        playerYOffset = it.playerYOffset + 24f,
                        playerScale = (it.playerScale - 0.045f).coerceAtLeast(0f),
                        playerAlpha = (it.playerAlpha - 0.05f).coerceAtLeast(0f),
                        screenShake = (it.screenShake * 0.85f).coerceAtLeast(0f)
                    )
                }
            }
        }
    }

    private fun startGameLoop() {
        gameLoopJob?.cancel()
        gameLoopJob = viewModelScope.launch {
            while (true) {
                delay(33) // ~30 fps updates for logic/timer/animations
                val state = _gameState.value
                if (state.isGameOver) {
                    break
                }
                if (state.isPaused) {
                    continue
                }

                val now = System.currentTimeMillis()
                val elapsedSinceStep = now - state.lastStepTimestamp
                val timeLimit = state.difficultyConfig.decisionTimeWindowMs
                val remainingFraction = (1.0f - (elapsedSinceStep.toFloat() / timeLimit)).coerceIn(0f, 1f)

                // If timer expired, player falls
                if (remainingFraction <= 0f) {
                    handleTimerExpiryFall()
                    break
                }

                // Smoothly decay bounce offset towards 0
                val newYOffset = if (state.playerYOffset < 0f) {
                    (state.playerYOffset + 2f).coerceAtMost(0f)
                } else {
                    0f
                }

                // Decay screen shake
                val newShake = if (state.screenShake > 0.01f) {
                    state.screenShake * 0.85f
                } else 0f

                // Fade streak feedback
                val newStreakAlpha = if (state.streakFeedbackAlpha > 0.01f) {
                    (state.streakFeedbackAlpha - 0.035f).coerceAtLeast(0f)
                } else 0f

                // Advance dissolving platforms and pulse animations
                val updatedPlatforms = state.platforms.map { p ->
                    var newDissolve = p.dissolveProgress
                    var newState = p.state
                    var newPulse = p.pulseProgress

                    if (p.state == PlatformState.DISSOLVING) {
                        newDissolve = (p.dissolveProgress + 0.05f).coerceAtMost(1f)
                        if (newDissolve >= 1f) {
                            newState = PlatformState.GONE
                        }
                    }

                    if (p.pulseProgress > 0f) {
                        newPulse = (p.pulseProgress - 0.08f).coerceAtLeast(0f)
                    }

                    p.copy(
                        dissolveProgress = newDissolve,
                        state = newState,
                        pulseProgress = newPulse
                    )
                }

                _gameState.update {
                    it.copy(
                        timeRemainingFraction = remainingFraction,
                        playerYOffset = newYOffset,
                        screenShake = newShake,
                        streakFeedbackAlpha = newStreakAlpha,
                        platforms = updatedPlatforms
                    )
                }
            }
        }
    }

    fun pauseGame() {
        _gameState.update { it.copy(isPaused = true) }
    }

    fun resumeGame() {
        val now = System.currentTimeMillis()
        val state = _gameState.value
        // Offset lastStepTimestamp so pause duration doesn't drain the decision timer
        val elapsedFraction = 1f - state.timeRemainingFraction
        val timeLimit = state.difficultyConfig.decisionTimeWindowMs
        val adjustedLastStep = now - (elapsedFraction * timeLimit).toLong()

        _gameState.update {
            it.copy(
                isPaused = false,
                lastStepTimestamp = adjustedLastStep
            )
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            preferencesRepository.setThemeMode(mode)
        }
    }

    fun setSoundEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setSoundEnabled(enabled)
            soundController.isEnabled = enabled
        }
    }

    fun setHapticsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setHapticsEnabled(enabled)
        }
    }

    fun resetBestScore() {
        viewModelScope.launch {
            preferencesRepository.resetBestScore()
        }
    }

    override fun onCleared() {
        super.onCleared()
        gameLoopJob?.cancel()
    }
}

class GameViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GameViewModel::class.java)) {
            return GameViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
