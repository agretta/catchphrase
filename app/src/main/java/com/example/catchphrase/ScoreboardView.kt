package com.example.catchphrase

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp

@Composable
fun ScoreboardDialog(
    scoreboardModel: ScoreboardModel
) {
    AlertDialog(
        onDismissRequest = {scoreboardModel.hide()},
        title = { Text("Scoreboard") },
        text = {
            Column {
                Row {
                    Text("Team A: ${scoreboardModel.teamAScore.value}")
                    Spacer(modifier = Modifier.width(Dp(16f)))
                    Text("Team B: ${scoreboardModel.teamBScore.value}")
                    Spacer(modifier = Modifier.width(Dp(16f)))
                    Text("Winning Score: ${scoreboardModel.maxScore}")
                }
            }
        },
        confirmButton = {
            Row {
                TextButton(onClick = {
                    scoreboardModel.incrementA()
                }) {
                    Text("+ Team A")
                }
                TextButton(onClick = {
                    scoreboardModel.incrementB()
                }) {
                    Text("+ Team B")
                }
                TextButton(onClick = {scoreboardModel.hide()}) {
                    Text("Close")
                }
            }
        }
    )
}

@Composable
fun WinnerDialog(
    scoreboard: ScoreboardModel
) {
    AlertDialog(
        onDismissRequest = {scoreboard.hide()},
        title = { Text("${scoreboard.winningTeam.value} wins!") },
        text = {
            Text("Congratulations — ${scoreboard.winningTeam.value} reached the target score.")
        },
        confirmButton = {
            Row {
                TextButton(onClick = {scoreboard.hide()}) {
                    Text("Close")
                }
            }
        }
    )
}
