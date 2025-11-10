package com.example.catchphrase.objects

import android.app.Application
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import androidx.core.content.edit
import android.annotation.SuppressLint
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class PackageRepository private constructor(private val app: Application) {
    private val prefs = app.getSharedPreferences(Prefs.PREFS, 0)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val TAG = "PackageRepository"

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories

    // Flat list of enabled words (updated whenever categorys change)
    private val _enabledWords = MutableStateFlow<List<String>>(emptyList())
    val enabledWords: StateFlow<List<String>> = _enabledWords

    private val _usedWords = MutableStateFlow<List<String>>(emptyList())
    val usedWords: StateFlow<List<String>> = _usedWords

    companion object {
        lateinit var instance: PackageRepository
            private set

        fun initialize(app: Application) {
            if (::instance.isInitialized) return
            instance = PackageRepository(app)
            instance.init()
        }
    }

    /** Initializes the repository by loading categories and packages,
     *
     */
    private fun init() {
        scope.launch {
            val defaultCategorys = loadCategoriesFromAssets()
            val enabledPackages = prefs.getStringSet(Prefs.KEY_ENABLED_PACKAGES, null)
            val initial = if (enabledPackages == null) {
                val toPersist = defaultCategorys
                    .flatMap { it.packages }
                    .filter { it.enabled }
                    .map { it.fileName }
                    .toSet()
                prefs.edit { putStringSet(Prefs.KEY_ENABLED_PACKAGES, toPersist) }
                defaultCategorys.map { category ->
                    val categoryEnabled = category.packages.all { it.enabled }
                    category.copy(enabled = categoryEnabled)
                }
            } else {
                defaultCategorys.map { category ->
                    val updatedSubs = category.packages.map { wordPackage ->
                        wordPackage.copy(enabled = true)
                    }
                    val categoryEnabled = updatedSubs.isNotEmpty() && updatedSubs.all { it.enabled }
                    category.copy(packages = updatedSubs, enabled = categoryEnabled)
                }
            }
            _categories.value = initial
            Log.d(TAG, "Loaded initial wordPackage titles: ${
                _categories.value.flatMap { it.packages }.filter { it.enabled }
                    .joinToString(", ") { it.title }
            }")
            updateEnabledWords(initial)
        }
    }

    /**  Loads categories and the word packages references from the assets folder's packages.json file.
     *
     * @return A list of categories with their respective word packages.
     */
    fun loadCategoriesFromAssets(): List<Category> {
        val jsonParser = Json { ignoreUnknownKeys = true }
        return try {
            val json = app.assets.open("packages.json").bufferedReader().use { it.readText() }
            val catDTOs = jsonParser.decodeFromString<List<CategoryDto>>(json)
            Log.d("PackageLoader", "Loaded Packages")
            catDTOs.map { catDTO ->
                val pkgs = catDTO.packages.map { p ->
                    WordPackage(
                        title = p.title,
                        fileName = p.fileName,
                        description = p.description,
                        words = loadWords(p.fileName)
                    )
                }
                Category(title = catDTO.title, packages = pkgs)
            }
        } catch (e: Exception) {
            Log.e("PackageLoader", "Failed to load packages.json", e)
            emptyList()
        }
    }

    /** Loads words from a specified asset file.
     *
     * @param fileName The name of the asset file to load words from.
     * @return A list of words loaded from the file.
     */
    private fun loadWords(fileName: String): List<String> {
        return try {
            app.assets.open(fileName).bufferedReader().useLines { lines ->
                lines.map { it.trim() }.filter { it.isNotEmpty() }.toList()
            }
        } catch (e: Exception) {
            Log.e("PackageRepository", "Failed to load words from $fileName", e)
            emptyList()
        }
    }

    /** Toggles the enabled state of a word package by its file name.
     *
     * @param fileName The file name of the word package to toggle.
     */
    fun toggleWordPackage(fileName: String) {
        scope.launch {
            val current = _categories.value.toMutableList()
            val categoryIdx = current.indexOfFirst { category ->
                category.packages.any { it.fileName == fileName }
            }
            if (categoryIdx == -1) return@launch

            val category = current[categoryIdx]
            val newSubs = category.packages.map { sub ->
                if (sub.fileName == fileName) {
                    // When disabling, preserve already-loaded words; when enabling, load if needed
                    if (sub.enabled) sub.copy(enabled = false)
                    else sub.copy(enabled = true, words = sub.words.ifEmpty { loadWords(sub.fileName) })
                } else sub
            }
            val newCategoryEnabled = newSubs.isNotEmpty() && newSubs.all { it.enabled }
            current[categoryIdx] = category.copy(packages = newSubs, enabled = newCategoryEnabled)
            _categories.value = current
            persistEnabledPackages(current)
            updateEnabledWords(current)
            Log.d(TAG, "Toggled wordPackage $fileName to ${newSubs.first { it.fileName == fileName }.enabled}")
        }
    }

    /** Toggles the enabled state of a category by its title.
     *
     * @param categoryTitle The title of the category to toggle.
     */
    fun toggleCategory(categoryTitle: String) {
        scope.launch {
            val current = _categories.value.toMutableList()
            val idx = current.indexOfFirst { it.title == categoryTitle }
            if (idx == -1) return@launch

            val category = current[idx]
            val newEnabled = !category.enabled
            val newSubs = category.packages.map { sub ->
                // When disabling, keep words loaded; when enabling, load if needed
                if (newEnabled) sub.copy(enabled = true, words = sub.words.ifEmpty { loadWords(sub.fileName) })
                else sub.copy(enabled = false)
            }
            current[idx] = category.copy(packages = newSubs, enabled = newEnabled)
            _categories.value = current
            persistEnabledPackages(current)
            updateEnabledWords(current)
            Log.d("PackageRepository", "Toggled category $categoryTitle to $newEnabled (applied to ${newSubs.size} packages)")
        }
    }

    /** Persists the set of enabled word package file names to SharedPreferences.
     *
     * @param categorys The list of categories to extract enabled packages from.
     */
    private fun persistEnabledPackages(categorys: List<Category>) {
        val enabledNames = categorys
            .flatMap { it.packages }
            .filter { it.enabled }
            .map { it.fileName }
            .toSet()
        prefs.edit { putStringSet(Prefs.KEY_ENABLED_PACKAGES, enabledNames) }
    }


    /** Updates the flat list of enabled words based on the current categories and their packages.
     *
     * @param categorys The list of categories to extract enabled words from.
     */
    private fun updateEnabledWords(categorys: List<Category>) {
        val words = categorys
            .flatMap { it.packages }
            .filter { it.enabled }
            .flatMap { it.words }
        _enabledWords.value = words
        Log.d(TAG, "Updated enabled words: ${_enabledWords.value.size} words")
    }

    /** Retrieves a random enabled word.
     *
     * @return A random enabled word, or null if none are enabled.
     */
    fun getRandomWord(): String? = _enabledWords.value.randomOrNull()

    /** Retrieves a random enabled word package.
     *
     * @return A random enabled WordPackage, or null if none are enabled.
     */
    fun getRandomPackage(): WordPackage? {
        val enabledPackages = _categories.value
            .flatMap { it.packages }
            .filter { it.enabled }
        return if (enabledPackages.isEmpty()) null else enabledPackages.random()
    }

    /** Retrieves a random word from a specified word package.
     *
     * @param pkg The WordPackage to get a random word from.
     * @return A random word from the package, or null if the package is disabled.
     */
    fun getRandomWordFromPackage(pkg: WordPackage): String? {
        if (!pkg.enabled) return null
        return pkg.words.randomOrNull()
    }

    /** Marks a word as used by adding it to the used words list.
     *
     * @param word The word to mark as used.
     */
    fun markWordAsUsed(word: String) {
        val currentUsed = _usedWords.value.toMutableList()
        if (!currentUsed.contains(word)) {
            currentUsed.add(word)
            _usedWords.value = currentUsed
            Log.d(TAG, "Marked word as used: $word")
        }
    }
}

/**
 * Data Transfer Object representing a word package.
 *
 * @property title The title of the category.
 * @property fileName
 * @property description
 */
@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class PackageDto(
    val title: String,
    val fileName: String,
    val description: String = "",
)

/**
 * Data Transfer Object representing a category of word packages.
 *
 * @property title The title of the category.
 * @property packages The list of word packages in this category.
 */
@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class CategoryDto(
    val title: String,
    val packages: List<PackageDto> = emptyList()
)