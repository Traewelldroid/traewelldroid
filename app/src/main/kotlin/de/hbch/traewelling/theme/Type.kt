package de.hbch.traewelling.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import de.hbch.traewelling.R

val raleway = FontFamily(Font(R.font.raleway))

private val defaultTypography = Typography()
val AppTypography = Typography(
    displayLarge = defaultTypography.displayLarge.copy(fontFamily = raleway),
    displayMedium = defaultTypography.displayMedium.copy(fontFamily = raleway),
    displaySmall = defaultTypography.displaySmall.copy(fontFamily = raleway),
    headlineLarge = defaultTypography.headlineLarge.copy(fontFamily = raleway),
    headlineMedium = defaultTypography.headlineMedium.copy(fontFamily = raleway),
    headlineSmall = defaultTypography.headlineSmall.copy(fontFamily = raleway),
    titleLarge = defaultTypography.titleLarge.copy(fontFamily = raleway),
    titleMedium = defaultTypography.titleMedium.copy(fontFamily = raleway),
    titleSmall = defaultTypography.titleSmall.copy(fontFamily = raleway),
    bodyLarge = defaultTypography.bodyLarge.copy(fontFamily = raleway),
    bodyMedium = defaultTypography.bodyMedium.copy(fontFamily = raleway),
    bodySmall = defaultTypography.bodySmall.copy(fontFamily = raleway),
    labelLarge = defaultTypography.labelLarge.copy(fontFamily = raleway),
    labelMedium = defaultTypography.labelMedium.copy(fontFamily = raleway),
    labelSmall = defaultTypography.labelSmall.copy(fontFamily = raleway)
)
