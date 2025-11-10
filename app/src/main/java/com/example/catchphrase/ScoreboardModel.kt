package com.example.catchphrase

import androidx.compose.runtime.mutableStateOf
import android.content.SharedPreferences
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import com.example.catchphrase.objects.Prefs
import com.example.catchphrase.objects.SettingsRepository
import com.example.catchphrase.objects.SettingsState
import kotlinx.coroutines.flow.first
import kotlin.math.max

class ScoreboardModel(private val prefs: SharedPreferences) {

    val teamAScore = mutableIntStateOf(0)
    val teamBScore = mutableIntStateOf(0)
    val winningTeam = mutableStateOf("")
    val showWinner = mutableStateOf(false)
    val showScoreboard = mutableStateOf(false)

    var maxScore: Int = 0

    init {
    }

    fun incrementA() {
        teamAScore.value = teamAScore.value + 1
        checkWin("Team A", teamAScore.value)
    }

    fun incrementB() {
        teamBScore.value = teamBScore.value + 1
        checkWin("Team B", teamBScore.value)
    }

    private fun checkWin(team: String, score: Int) {
        if (maxScore> 0 && score >= maxScore) {
            winningTeam.value = team
            showWinner.value = true
        }
    }

    fun reset() {
        teamAScore.value = 0
        teamBScore.value = 0
        winningTeam.value = ""
        showWinner.value = false
        val repo = SettingsRepository.instance
        maxScore = repo.getSettings().maxPoints

        hide()
    }

    fun show() {
        showScoreboard.value = true
    }

    fun hide() {
        showScoreboard.value = false
        showWinner.value = false
    }
}