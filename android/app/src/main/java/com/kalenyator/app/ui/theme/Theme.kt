package com.kalenyator.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.kalenyator.app.data.model.AppThemeMode
import com.kalenyator.app.data.model.AppVisualTheme

private val TentakoLight = lightColorScheme(
    primary = TentakoPink,
    onPrimary = Color.White,
    secondary = TentakoGold,
    onSecondary = TentakoPinkDark,
    tertiary = TentakoBlush,
    background = Color(0xFFFFF9F0),
    surface = Color(0xFFFFFBF7),
    onBackground = TentakoPinkDark,
    onSurface = TentakoPinkDark,
    surfaceVariant = TentakoBlushLight
)

private val TentakoDark = darkColorScheme(
    primary = TentakoPink,
    onPrimary = Color.White,
    secondary = TentakoGold,
    tertiary = TentakoBlush,
    background = Color(0xFF1A1218),
    surface = Color(0xFF241C22),
    onBackground = TentakoBlushLight,
    onSurface = TentakoBlushLight
)

private val SpaceLight = lightColorScheme(
    primary = Color(0xFF6C5CE7),
    onPrimary = Color.White,
    secondary = Color(0xFF00CEC9),
    background = Color(0xFFEEF0FF),
    surface = Color(0xFFF8F9FF),
    onBackground = Color(0xFF2D3436),
    onSurface = Color(0xFF2D3436),
    surfaceVariant = Color(0xFFDCD6FF)
)

private val SpaceDark = darkColorScheme(
    primary = Color(0xFFA29BFE),
    onPrimary = Color(0xFF1A1A2E),
    secondary = Color(0xFF00CEC9),
    background = Color(0xFF0F0F1A),
    surface = Color(0xFF1A1A2E),
    onBackground = Color(0xFFE0E0FF),
    onSurface = Color(0xFFE0E0FF),
    surfaceVariant = Color(0xFF2D2D44)
)

private val MinimalLight = lightColorScheme(
    primary = Color(0xFF2D3436),
    onPrimary = Color.White,
    secondary = Color(0xFF636E72),
    background = Color(0xFFFAFAFA),
    surface = Color.White,
    onBackground = Color(0xFF2D3436),
    onSurface = Color(0xFF2D3436),
    surfaceVariant = Color(0xFFEEEEEE)
)

private val NavruzLight = lightColorScheme(
    primary = Color(0xFF00B894),
    onPrimary = Color.White,
    secondary = Color(0xFFFDCB6E),
    background = Color(0xFFF0FFF8),
    surface = Color(0xFFE8FFF5),
    onBackground = Color(0xFF00695C),
    onSurface = Color(0xFF00695C),
    surfaceVariant = Color(0xFFB2DFDB)
)

private val NavruzDark = darkColorScheme(
    primary = Color(0xFF55EFC4),
    onPrimary = Color(0xFF003D33),
    secondary = Color(0xFFFDCB6E),
    background = Color(0xFF0D1F1A),
    surface = Color(0xFF1A2E28),
    onBackground = Color(0xFFE0F2F1),
    onSurface = Color(0xFFE0F2F1),
    surfaceVariant = Color(0xFF2D4A42)
)

private val NewYearLight = lightColorScheme(
    primary = Color(0xFF0984E3),
    onPrimary = Color.White,
    secondary = Color(0xFFDFE6E9),
    background = Color(0xFFEEF5FF),
    surface = Color(0xFFF8FBFF),
    onBackground = Color(0xFF2D3436),
    onSurface = Color(0xFF2D3436),
    surfaceVariant = Color(0xFFBBDEFB)
)

private val NewYearDark = darkColorScheme(
    primary = Color(0xFF74B9FF),
    onPrimary = Color(0xFF0D1B2A),
    secondary = Color(0xFFB2BEC3),
    background = Color(0xFF0A1628),
    surface = Color(0xFF152238),
    onBackground = Color(0xFFE3F2FD),
    onSurface = Color(0xFFE3F2FD),
    surfaceVariant = Color(0xFF1E3A5F)
)

private val MinimalDark = darkColorScheme(
    primary = Color(0xFFB2BEC3),
    onPrimary = Color(0xFF1E1E1E),
    secondary = Color(0xFF636E72),
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    onBackground = Color(0xFFDFE6E9),
    onSurface = Color(0xFFDFE6E9),
    surfaceVariant = Color(0xFF2D2D2D)
)

@Composable
fun KalenyatorTheme(
    themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    visualTheme: AppVisualTheme = AppVisualTheme.TENTAKO,
    content: @Composable () -> Unit
) {
    val dark = when (themeMode) {
        AppThemeMode.DARK -> true
        AppThemeMode.LIGHT -> false
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    val colors = when (visualTheme) {
        AppVisualTheme.SPACE -> if (dark) SpaceDark else SpaceLight
        AppVisualTheme.MINIMAL -> if (dark) MinimalDark else MinimalLight
        AppVisualTheme.NAVRUZ -> if (dark) NavruzDark else NavruzLight
        AppVisualTheme.NEW_YEAR -> if (dark) NewYearDark else NewYearLight
        AppVisualTheme.TENTAKO -> if (dark) TentakoDark else TentakoLight
    }
    MaterialTheme(colorScheme = colors, content = content)
}
