package com.kalenyator.app.util

import com.kalenyator.app.data.model.AppLanguage

object LanguageStore {
    @Volatile
    var current: AppLanguage = AppLanguage.RU
}
