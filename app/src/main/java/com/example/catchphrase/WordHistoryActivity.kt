package com.example.catchphrase

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.catchphrase.ui.theme.CatchphraseTheme


class WordHistoryActivity: ComponentActivity() {

    private lateinit var wordViewModel: WordViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            CatchphraseTheme {
                WordHistoryScreen(onClose = { finish() })
            }
        }
    }
}