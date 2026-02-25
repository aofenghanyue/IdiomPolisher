package com.example.idiompolisher.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = Cinnabar,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFEDE9),
    onPrimaryContainer = CinnabarDark,
    secondary = Stone,
    onSecondary = Color.White,
    secondaryContainer = StoneLight,
    onSecondaryContainer = StoneDark,
    tertiary = AccentGold,
    onTertiary = Color.White,
    tertiaryContainer = AccentGoldLight,
    onTertiaryContainer = Color(0xFF3D2E00),
    background = RicePaper,
    onBackground = Ink,
    surface = RicePaper,
    onSurface = Ink,
    surfaceVariant = PaperTexture,
    onSurfaceVariant = InkLight,
    outline = StoneLight,
    outlineVariant = Color(0xFFE7E0D8),
    error = SealRed,
    onError = Color.White,
)

private val DarkColorScheme = darkColorScheme(
    primary = CinnabarLight,
    onPrimary = Color(0xFF3B0A00),
    primaryContainer = CinnabarDark,
    onPrimaryContainer = Color(0xFFFFDAD1),
    secondary = StoneLight,
    onSecondary = Color(0xFF2C2620),
    secondaryContainer = StoneDark,
    onSecondaryContainer = StoneLight,
    tertiary = AccentGoldLight,
    onTertiary = Color(0xFF3D2E00),
    tertiaryContainer = Color(0xFF584400),
    onTertiaryContainer = AccentGoldLight,
    background = RicePaperDark,
    onBackground = Color(0xFFEDE0D4),
    surface = RicePaperDark,
    onSurface = Color(0xFFEDE0D4),
    surfaceVariant = BambooDark,
    onSurfaceVariant = Color(0xFFD6D3D1),
    outline = Stone,
    outlineVariant = StoneDark,
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
)

@Composable
fun IdiomPolisherTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

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
        typography = Typography,
        content = content
    )
}