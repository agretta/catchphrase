package com.example.catchphrase

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.graphics.toColorInt
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.catchphrase.objects.Prefs
import com.example.catchphrase.ui.theme.CatchphraseTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private lateinit var soundTicker: SoundTicker

fun String.toComposeColor(): Color {
    return Color(this.toColorInt())
}
val backgroundColor = "#55D6BE".toComposeColor()
val titleBarColor = "#2E5EAA".toComposeColor()
val accentColor = "#E3D7FF".toComposeColor()
class MainActivity : ComponentActivity() {

    // Activity-level Compose-observed states
    private val showAbout = mutableStateOf(false)
    private val flashRed = mutableStateOf(false)

    private lateinit var prefs: SharedPreferences
    private lateinit var scoreboard: ScoreboardModel
    private lateinit var wordViewModel: WordViewModel

    override fun onDestroy() {
        super.onDestroy()
        scoreboard.reset()
        soundTicker.release()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        prefs = getSharedPreferences(Prefs.PREFS, MODE_PRIVATE)

        scoreboard = ScoreboardModel(prefs)
        soundTicker = SoundTicker(this, R.raw.beep, R.raw.buzzer)
        wordViewModel = WordViewModel(application)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                soundTicker.events.collect { event ->
                    when (event) {
                        is SoundTicker.Event.Buzz -> {
                            // flash red briefly on each buzzer start
                            lifecycleScope.launch {
                                flashRed.value = true
                                delay(150) // flash duration in milliseconds
                                flashRed.value = false
                            }
                        }
                        is SoundTicker.Event.BuzzerEnded -> {
                            scoreboard.show()
                        }
                        else -> { /* ignore or handle TickPlayed if needed */ }
                    }
                }
            }
        }

        enableEdgeToEdge()
        setContent {
            CatchphraseTheme {
                Box(modifier = Modifier.fillMaxSize()
                ) {
                    Scaffold(
                        topBar = {
                            TopBarWithMenu(onOptionSelected = { option ->
                                handleMenuSelection(option, soundTicker = soundTicker)
                            })
                        }
                    ) { innerPadding ->
                        Column(modifier = Modifier.fillMaxSize().padding(innerPadding).background(backgroundColor)) {
                            WordScreen(viewModel = wordViewModel, soundTicker = soundTicker, lifecycleScope = lifecycleScope, modifier = Modifier.weight(1f))
                            if (showAbout.value) {
                                AboutDialog(onDismiss = { showAbout.value = false })
                            }
                            if (scoreboard.showScoreboard.value) {
                                ScoreboardDialog( scoreboard )
                            }

                            if (scoreboard.showWinner.value) {
                                WinnerDialog( scoreboard )
                            }
                        }
                    }

                    if (flashRed.value) {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .background(Color.Red.copy(alpha = 0.6f))
                        )
                    }
                }
            }
        }
    }

    private fun handleMenuSelection(option: String, soundTicker: SoundTicker) {
        soundTicker.stop()
        when (option) {
            "settings" -> {
                startActivity(Intent(this, SettingsActivity::class.java))
            }
            "history" -> {
                startActivity(Intent(this, WordHistoryActivity::class.java))
            }
            "reset" -> {
                scoreboard.reset()
            }
            "about" -> {
                showAbout.value = true
            }
        }
    }
}