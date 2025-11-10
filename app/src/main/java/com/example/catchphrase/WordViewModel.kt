package com.example.catchphrase

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.example.catchphrase.objects.PackageRepository
import com.example.catchphrase.objects.Prefs
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class WordViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = application.getSharedPreferences(Prefs.PREFS, 0)
    private val TAG = "WordViewModel"

    private val _currentWord = MutableStateFlow<String?>(null)
    val currentWord: StateFlow<String?> = _currentWord

    init {
        reload()
    }

    fun next() {
        val selectionMode = prefs.getInt(Prefs.KEY_SELECTION_MODE, 0)
        val repo = PackageRepository.instance

        var chosen: String? = null
        when (selectionMode) {
            0 -> {
                chosen = repo.getRandomWord()
                Log.d(TAG, "Next Random Word: ${chosen ?: "No word chosen"}")
            }

            1 -> {
                val pkg = repo.getRandomPackage()
                chosen = pkg?.let { repo.getRandomWordFromPackage(it) }
                Log.d(
                    TAG,
                    "Next Word from Package: ${pkg?.title ?: "None"} ${chosen ?: "No word chosen"}"
                )
            }
            else -> {
                Log.w(TAG, "Unknown selection mode: $selectionMode")
            }
        }

        if (chosen == null) {
            Log.w(TAG, "No word could be chosen, probably all words used")
            _currentWord.value = "No more words available.\nPlease reload."
            return
        }
        repo.markWordAsUsed(chosen)
        _currentWord.value = chosen
    }

    fun reload() {
        Log.d(TAG, "Reloading word packages (via repository)")
        _currentWord.value = "Tap to start"
    }

    fun lookupWord(word: String?) {
        if (word.isNullOrBlank()) return

        try {
            val encoded = Uri.encode(word)
            val uri = Uri.parse("https://www.google.com/search?q=$encoded")
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            getApplication<Application>().startActivity(intent)
        } catch (e: Exception) {
            Log.w(TAG, "failed to launch browser for term=$word", e)
        }
    }
}