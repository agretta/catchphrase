package com.example.catchphrase

import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.catchphrase.objects.Prefs
import com.example.catchphrase.SoundTicker
import com.example.catchphrase.WordViewModel
import android.content.Context
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.LifecycleOwner

@Composable
fun WordScreen(
    viewModel: WordViewModel,
    soundTicker: SoundTicker,
    lifecycleScope: androidx.lifecycle.LifecycleCoroutineScope,
    modifier: Modifier = Modifier,
) {
    val word by viewModel.currentWord.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _: LifecycleOwner, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.reload()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .clickable {
                Log.d("WordScreen", "Box clicked, getting next word")
                viewModel.next()

                if (soundTicker.isDone.value) {
                    val prefs = context.getSharedPreferences(Prefs.PREFS, Context.MODE_PRIVATE)
                    val tickVolume = prefs.getFloat(Prefs.KEY_BEEPER, 1.0f)
                    val buzzerVolume = prefs.getFloat(Prefs.KEY_BUZZER, 1.0f)
                    val roundLengthSec = prefs.getInt(Prefs.KEY_GAME_LENGTH, 120)
                    val randomizeLength = prefs.getBoolean(Prefs.KEY_RANDOMIZE_PHASE, true)

                    val roundLengthMs = (roundLengthSec * 1000L).coerceAtLeast(0L)

                    soundTicker.start(
                        lifecycleScope,
                        roundLengthMs,
                        randomizeLength,
                        tickVolume,
                        buzzerVolume
                    )
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = word ?: "No words",
            style = MaterialTheme.typography.headlineMedium
        )
    }
}