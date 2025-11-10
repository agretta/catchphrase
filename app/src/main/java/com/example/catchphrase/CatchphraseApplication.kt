package com.example.catchphrase

import android.app.Application
import com.example.catchphrase.objects.PackageRepository
import com.example.catchphrase.objects.SettingsRepository

class CatchphraseApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        PackageRepository.initialize(this)
        SettingsRepository.initialize(this)
    }
}