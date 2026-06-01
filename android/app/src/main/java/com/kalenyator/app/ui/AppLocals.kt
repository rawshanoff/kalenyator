package com.kalenyator.app.ui

import androidx.compose.runtime.compositionLocalOf
import androidx.lifecycle.ViewModelProvider

val LocalViewModelFactory = compositionLocalOf<ViewModelProvider.Factory> {
    error("ViewModelFactory not provided")
}
