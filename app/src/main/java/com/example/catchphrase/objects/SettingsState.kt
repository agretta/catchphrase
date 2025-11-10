package com.example.catchphrase.objects

data class SettingsState(
    val buzzerVolume: Float = 1.0f,
    val beeperVolume: Float = 1.0f,
    val roundLength: Int = 120,
    val maxPoints: Int = 10,
    val selectionMode: Int = 0,
    val randomizePhase: Boolean = true
)