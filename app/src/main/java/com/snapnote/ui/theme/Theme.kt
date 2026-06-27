package com.snapnote.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = TextOnPrimary,
    primaryContainer = PrimaryLight,
    onPrimaryContainer = PrimaryDark,
    secondary = Accent,
    onSecondary = TextOnPrimary,
    secondaryContainer = AccentLight,
    onSecondaryContainer = Accent,
    tertiary = Info,
    background = Background,
    onBackground = TextPrimary,
    surface = Surface,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = TextTertiary,
    outline = Border,
    error = Error,
    onError = TextOnPrimary,
    errorContainer = Error.copy(alpha = 0.1f),
    onErrorContainer = Error
)

private val DarkColorScheme = darkColorScheme(
    primary = Primary,
    onPrimary = TextOnPrimary,
    primaryContainer = PrimaryDark,
    onPrimaryContainer = PrimaryLight,
    secondary = Accent,
    onSecondary = TextOnPrimary,
    secondaryContainer = Accent.copy(alpha = 0.15f),
    onSecondaryContainer = AccentLight,
    tertiary = Info,
    background = TextPrimary,
    onBackground = Surface,
    surface = TextSecondary,
    onSurface = Surface,
    surfaceVariant = TextPrimary.copy(alpha = 0.5f),
    onSurfaceVariant = SurfaceVariant,
    outline = BorderStrong,
    error = Error,
    onError = TextOnPrimary,
    errorContainer = Error.copy(alpha = 0.2f),
    onErrorContainer = Error.copy(alpha = 0.8f)
)

@Composable
fun SnapNoteTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
