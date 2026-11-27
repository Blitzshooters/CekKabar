package com.tanilink.cekkabar.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val EditorialLightColorScheme = lightColorScheme(
    primary = IndigoPrimary,
    onPrimary = Color.White,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = OnPrimaryContainer,
    secondary = FactGreen,
    onSecondary = Color.White,
    secondaryContainer = FactGreenContainer,
    onSecondaryContainer = OnFactGreenContainer,
    tertiary = HoaxRed,
    onTertiary = Color.White,
    tertiaryContainer = HoaxRedContainer,
    onTertiaryContainer = OnHoaxRedContainer,
    background = BackgroundLight,
    onBackground = OnSurfaceDark,
    surface = SurfaceWhite,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceContainerHigh,
    onSurfaceVariant = OnSurfaceVariant,
    outline = OutlineBorder,
    error = HoaxRed,
    onError = Color.White,
    errorContainer = HoaxRedContainer,
    onErrorContainer = OnHoaxRedContainer
)

@Composable
fun CekKabarTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = EditorialLightColorScheme,
        typography = Typography,
        content = content
    )
}
