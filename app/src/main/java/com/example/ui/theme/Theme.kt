package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val ElegantColorScheme = darkColorScheme(
    primary = ServicePrimaryDark,
    secondary = ServiceSecondaryDark,
    background = ServiceBackgroundDark,
    surface = ServiceSurfaceDark,
    onPrimary = ServiceOnPrimaryDark,
    onSecondary = ServiceOnSecondaryDark,
    onBackground = ElegantOnBackground,
    onSurface = ElegantOnSurface,
    error = ServiceError,
    surfaceVariant = ElegantSurfacePanel,
    onSurfaceVariant = ElegantOnBackground,
    outline = ElegantBorderMedium,
    outlineVariant = ElegantBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force Elegant Dark as the default design theme
    dynamicColor: Boolean = false, // Set to false to enforce our customized beautiful branding colors
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        else -> ElegantColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
