package de.hbch.traewelling.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import com.google.accompanist.systemuicontroller.rememberSystemUiController

private val DarkColorScheme = darkColorScheme(
    primary = TraewelldroidDark,
    secondary = TraewelldroidDark,
    tertiary = TraewelldroidDark
)

private val LightColorScheme = lightColorScheme(
    primary = Traewelldroid,
    secondary = Traewelldroid,
    tertiary = Traewelldroid

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
fun MainTheme(
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    val context = LocalContext.current
    val systemUiController = rememberSystemUiController()
    val darkTheme = isSystemInDarkTheme()

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        // Set polyline color to default primary light color
        PolylineColor = dynamicLightColorScheme(context).primary
    }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    if (!view.isInEditMode) {
        DisposableEffect(systemUiController, darkTheme) {
            systemUiController.setSystemBarsColor(
                color = Color.Transparent,
                darkIcons = !darkTheme
            )

            onDispose {}
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography
    ) {
        CompositionLocalProvider(
            LocalColorScheme provides colorScheme,
            content = content
        )
    }
}

internal val LocalColorScheme = staticCompositionLocalOf { LightColorScheme }
