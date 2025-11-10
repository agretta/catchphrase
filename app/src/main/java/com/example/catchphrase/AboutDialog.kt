package com.example.catchphrase

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun AboutDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("How to play Catchphrase") },
        text = {
            val multilineString = """
                TODO refactor this
                1. Players split into teams.
                2. One player gives verbal clues to teammates to guess the word or phrase shown, without using the word itself, rhymes, or spelling.
                3. A timer runs while teams try to guess; when the timer ends the team either scores or passes depending on rules.
                4. The goal is to get your team to guess as many phrases as possible before the buzzer.
            """.trimIndent()
            Text(multilineString)
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
