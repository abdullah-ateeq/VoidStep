package com.example.voidstep.game.model

enum class PlatformState {
    ACTIVE_CURRENT,
    REACHABLE,
    DISSOLVING,
    GONE
}

data class Platform(
    val id: Long,
    val stepIndex: Int,
    val normalizedX: Float, // -0.8f (left) to 0.8f (right), 0.0f = center
    val widthFraction: Float = 0.36f,
    val heightFraction: Float = 0.075f,
    val state: PlatformState = PlatformState.REACHABLE,
    val dissolveProgress: Float = 0f,
    val pulseProgress: Float = 0f,
    val isDeceptiveGap: Boolean = false // Marker for empty spaces or void traps
) {
    val leftBound: Float get() = normalizedX - (widthFraction / 2f)
    val rightBound: Float get() = normalizedX + (widthFraction / 2f)

    fun containsX(x: Float, tolerance: Float = 0.08f): Boolean {
        return x in (leftBound - tolerance)..(rightBound + tolerance)
    }
}
