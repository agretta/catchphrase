package com.example.catchphrase

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TextField
import androidx.compose.material3.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.platform.LocalContext
import com.example.catchphrase.objects.PackageRepository

val TAG = "WordHistoryScreen"

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun WordHistoryScreen(onClose: () -> Unit) {
    val repo = PackageRepository.instance
    val clicked = repo.usedWords.collectAsState(initial = emptyList()).value
    var query by remember { mutableStateOf("") }
    val context = LocalContext.current

    Surface(color = MaterialTheme.colorScheme.background) {
        Column {
            TopAppBar(
                title = { Text("History") },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(painter = painterResource(android.R.drawable.ic_menu_close_clear_cancel), contentDescription = "Close")
                    }
                }
            )

            TextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Search history") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            )

            val filtered = clicked
                .asReversed() // show most recent first; remove asReversed() if you want oldest-first
                .filter { it.contains(query, ignoreCase = true) }

            if (filtered.isEmpty()) {
                Text(
                    text = "No history",
                    modifier = Modifier
                        .padding(16.dp)
                )
            } else {
                LazyColumn(modifier = Modifier.padding(horizontal = 8.dp)) {
                    items(filtered) { word ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    lookupWord(context, word)
                                }
                                .padding(vertical = 12.dp, horizontal = 8.dp)
                        ) {
                            Text(
                                text = word,
                                style = MaterialTheme.typography.bodyLarge,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

fun lookupWord(context: Context, word: String?) {
    if (word.isNullOrBlank()) return

    try {
        val encoded = Uri.encode(word)
        val uri = Uri.parse("https://www.google.com/search?q=$encoded")
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        Log.w(TAG, "failed to launch browser for term=$word", e)
    }
}
