package com.csd3156.mobileproject.MovieReviewApp.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext


private val DarkColorScheme = darkColorScheme(
    primary = CinemaRedDark,
    onPrimary = Color.White,
    primaryContainer = CinemaRedContainerDark,
    onPrimaryContainer = Color(0xFFE6F5FB),
    secondary = CinemaGoldDark,
    onSecondary = Color(0xFF002733),
    tertiary = CinemaGoldDark,
    background = CinemaBgDark,
    onBackground = CinemaOnDark,
    surface = CinemaSurfaceDark,
    onSurface = CinemaOnDark,
    surfaceVariant = CinemaSurfaceVariantDark,
    onSurfaceVariant = CinemaOnVariantDark
)

private val LightColorScheme = lightColorScheme(
    primary = CinemaBlueLight,
    onPrimary = Color.White,
    primaryContainer = CinemaBlueContainerLight,
    onPrimaryContainer = Color(0xFF001D36),
    secondary = CinemaCyanLight,
    onSecondary = Color.White,
    tertiary = CinemaCyanLight,
    background = CinemaBgLight,
    onBackground = CinemaOnLight,
    surface = CinemaSurfaceLight,
    onSurface = CinemaOnLight,
    surfaceVariant = CinemaSurfaceVariantLight,
    onSurfaceVariant = CinemaOnVariantLight

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun MovieReviewAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
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

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
