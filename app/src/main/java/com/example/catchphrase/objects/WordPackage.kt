package com.example.catchphrase.objects

/**
 * Represents a package of words sharing a theme
 * that can be enabled or disabled.
 *
 * @property title The title of the word package.
 * @property description A brief description of the content
 * @property fileName The name of the file.
 * @property words The actual list of words in the package.
 */
data class WordPackage(
    val title: String,
    val fileName: String,

    val description: String = "",
    var words: List<String> = emptyList(),

    private var _enabled: Boolean = true,
    val enabled: Boolean = _enabled
) {

    fun disable(): WordPackage {
        _enabled = false
        return this
    }

    fun enable(loader: (String) -> List<String>): WordPackage {
        words = loader(fileName)
        _enabled = true
        return this
    }
}