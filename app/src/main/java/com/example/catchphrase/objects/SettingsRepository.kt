package com.example.catchphrase.objects

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsRepository private constructor(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(Prefs.PREFS, Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(readFromPrefs())
    val settingsFlow: StateFlow<SettingsState> = _settings.asStateFlow()

    private val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
        _settings.value = readFromPrefs()
    }

    init {
        prefs.registerOnSharedPreferenceChangeListener(listener)
    }

    private fun readFromPrefs(): SettingsState {
        return SettingsState(
            buzzerVolume = prefs.getFloat(Prefs.KEY_BUZZER, 1.0f),
            beeperVolume = prefs.getFloat(Prefs.KEY_BEEPER, 1.0f),
            roundLength = prefs.getInt(Prefs.KEY_GAME_LENGTH, 120),
            maxPoints = prefs.getInt(Prefs.KEY_MAX_POINTS, 10),
            selectionMode = prefs.getInt(Prefs.KEY_SELECTION_MODE, 0),
            randomizePhase = prefs.getBoolean(Prefs.KEY_RANDOMIZE_PHASE, true)
        )
    }

    fun getSettings(): SettingsState {
        return readFromPrefs()
    }

    fun setBuzzerVolume(value: Float) {
        prefs.edit().putFloat(Prefs.KEY_BUZZER, value).apply()
        _settings.value = _settings.value.copy(buzzerVolume = value)
    }

    fun setBeeperVolume(value: Float) {
        prefs.edit().putFloat(Prefs.KEY_BEEPER, value).apply()
        _settings.value = _settings.value.copy(beeperVolume = value)
    }

    fun setRoundLength(value: Int) {
        prefs.edit().putInt(Prefs.KEY_GAME_LENGTH, value).apply()
        _settings.value = _settings.value.copy(roundLength = value)
    }

    fun setMaxPoints(value: Int) {
        prefs.edit().putInt(Prefs.KEY_MAX_POINTS, value).apply()
        _settings.value = _settings.value.copy(maxPoints = value)
    }

    fun setSelectionMode(value: Int) {
        prefs.edit().putInt(Prefs.KEY_SELECTION_MODE, value).apply()
        _settings.value = _settings.value.copy(selectionMode = value)
    }

    fun setRandomizePhase(value: Boolean) {
        prefs.edit().putBoolean(Prefs.KEY_RANDOMIZE_PHASE, value).apply()
        _settings.value = _settings.value.copy(randomizePhase = value)
    }

    companion object {
        @Volatile
        lateinit var instance: SettingsRepository
            private set

        fun initialize(context: Context) {
            if (::instance.isInitialized) return
            instance = SettingsRepository(context)
            instance.readFromPrefs()
        }
    }
}