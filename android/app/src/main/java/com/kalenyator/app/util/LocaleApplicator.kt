package com.kalenyator.app.util

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.kalenyator.app.data.model.AppLanguage

object LocaleApplicator {
    fun apply(language: AppLanguage) {
        LanguageStore.current = language
        val tags = when (language) {
            AppLanguage.RU -> "ru"
            AppLanguage.UZ -> "uz"
        }
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(tags))
    }

    fun applyFromContext(context: Context, language: AppLanguage) {
        apply(language)
    }
}
