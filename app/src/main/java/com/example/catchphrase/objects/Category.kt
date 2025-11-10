package com.example.catchphrase.objects

import com.example.catchphrase.objects.WordPackage

/**
 * Represents a category of word packages.
 *
 * @property title The title of the category.
 * @property packages The list of word packages in this category.
 * @property enabled Whether the category is enabled or not.
 */
data class Category(
    val title: String,
    val packages: List<WordPackage>,
    val enabled: Boolean = false
)