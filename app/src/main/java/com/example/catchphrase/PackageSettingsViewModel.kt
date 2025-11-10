// Kotlin
package com.example.catchphrase

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.catchphrase.objects.Category
import com.example.catchphrase.objects.PackageRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PackageSettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application
    private lateinit var repo: PackageRepository

    private val _categorys = MutableStateFlow<List<Category>>(emptyList())
    val categorys: StateFlow<List<Category>> = _categorys

    init {
        repo = PackageRepository.instance

        viewModelScope.launch {
            repo.categories.collect { _categorys.value = it }
        }
    }

    fun toggleWordPackage(fileName: String) {
        repo.toggleWordPackage(fileName)
    }

    fun toggleCategory(categoryId: String) {
        repo.toggleCategory(categoryId)
    }
}