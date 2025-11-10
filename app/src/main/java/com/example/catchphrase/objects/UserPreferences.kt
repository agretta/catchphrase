package com.example.catchphrase.data

data class UserPreferences(
    val enabledPackages: Set<String> = emptySet(),
    val enabledCategories: Set<String> = emptySet()
) {
    fun isPackageEnabled(fileName: String) = enabledPackages.contains(fileName)
    fun isCategoryEnabled(categoryId: String) = enabledCategories.contains(categoryId)
}