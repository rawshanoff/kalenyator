package com.kalenyator.app.util

import android.content.Context
import android.content.res.Configuration
import com.kalenyator.app.data.model.AppLanguage
import java.util.Locale

object LocaleHelper {
    fun wrap(context: Context, language: AppLanguage): Context {
        val locale = when (language) {
            AppLanguage.RU -> Locale.forLanguageTag("ru")
            AppLanguage.UZ -> Locale.forLanguageTag("uz")
        }
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }
}
