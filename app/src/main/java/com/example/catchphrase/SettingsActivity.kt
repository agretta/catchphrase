package com.example.catchphrase

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.catchphrase.objects.Prefs
import com.example.catchphrase.objects.SettingsRepository
import com.example.catchphrase.ui.theme.CatchphraseTheme

class SettingsActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val repo = SettingsRepository.instance
            val settings by repo.settingsFlow.collectAsState()

            CatchphraseTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        TopAppBar(title = { Text("Settings") })
                    }
                ) { innerPadding ->
                    Column(modifier = Modifier
                        .padding(innerPadding)
                        .padding(16.dp)) {

                        OutlinedCard {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Buzzer Volume: ${(settings.buzzerVolume * 100).toInt()}%", style = MaterialTheme.typography.bodyLarge)
                                Slider(
                                    value = settings.buzzerVolume,
                                    onValueChange = { repo.setBuzzerVolume(it) },
                                    valueRange = 0f..1f
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                Text("Beeper Volume: ${(settings.beeperVolume * 100).toInt()}%", style = MaterialTheme.typography.bodyLarge)
                                Slider(
                                    value = settings.beeperVolume,
                                    onValueChange = { repo.setBeeperVolume(it) },
                                    valueRange = 0f..1f
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                Text("Round Length: ${settings.roundLength} s", style = MaterialTheme.typography.bodyLarge)
                                Slider(
                                    value = settings.roundLength.toFloat(),
                                    onValueChange = {
                                        val snapped = (((it / 15f).toInt()) * 15).coerceIn(10, 180)
                                        repo.setRoundLength(snapped)
                                    },
                                    valueRange = 10f..180f,
                                    steps = 9
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // Points to Win stepper (1..20, step 1)
                                Text("Points to Win", style = MaterialTheme.typography.bodyLarge)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Button(onClick = { if (settings.maxPoints > 1) repo.setMaxPoints(settings.maxPoints - 1) }) { Text("-") }
                                    Text("${settings.maxPoints}", modifier = Modifier.padding(horizontal = 16.dp))
                                    Button(onClick = { if (settings.maxPoints < 20) repo.setMaxPoints(settings.maxPoints + 1) }) { Text("+") }
                                }
                                Spacer(modifier = Modifier.height(8.dp))

                                // Selection mode radio buttons
                                Text("Selection Mode", style = MaterialTheme.typography.bodyLarge)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(selected = settings.selectionMode == 0, onClick = { repo.setSelectionMode(0) })
                                    Text("Random Word", modifier = Modifier.padding(end = 12.dp))
                                    RadioButton(selected = settings.selectionMode == 1, onClick = { repo.setSelectionMode(1) })
                                    Text("Random Category")
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Randomize Round Phase checkbox (default true)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(checked = settings.randomizePhase, onCheckedChange = { repo.setRandomizePhase(it) })
                                    Text("Randomize Round Phase length", modifier = Modifier.padding(start = 8.dp))
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Button(onClick = {
                                    startActivity(Intent(this@SettingsActivity, PackageSettingsActivity::class.java))
                                }) {
                                    Text("Open Advanced Settings")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}