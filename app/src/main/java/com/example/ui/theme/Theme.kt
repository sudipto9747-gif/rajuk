package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimaryTeal,
    onPrimary = DarkOnPrimaryTeal,
    primaryContainer = DarkPrimaryContainerTeal,
    onPrimaryContainer = DarkOnPrimaryContainerTeal,
    secondary = DarkSecondaryAmber,
    onSecondary = DarkOnSecondaryAmber,
    secondaryContainer = DarkSecondaryContainerAmber,
    onSecondaryContainer = DarkOnSecondaryContainerAmber,
    tertiary = DarkTertiaryEmerald,
    onTertiary = DarkOnTertiaryEmerald,
    tertiaryContainer = DarkTertiaryContainerEmerald,
    onTertiaryContainer = DarkOnTertiaryContainerEmerald,
    background = DarkBackgroundSlate,
    onBackground = DarkOnBackgroundSlate,
    surface = DarkSurfaceSlate,
    onSurface = DarkOnSurfaceSlate,
    surfaceVariant = DarkSurfaceVariantSlate,
    onSurfaceVariant = DarkOnSurfaceVariantSlate,
    outline = DarkOutlineSteel
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryTeal,
    onPrimary = OnPrimaryTeal,
    primaryContainer = PrimaryContainerTeal,
    onPrimaryContainer = OnPrimaryContainerTeal,
    secondary = SecondaryAmber,
    onSecondary = OnSecondaryAmber,
    secondaryContainer = SecondaryContainerAmber,
    onSecondaryContainer = OnSecondaryContainerAmber,
    tertiary = TertiaryEmerald,
    onTertiary = OnTertiaryEmerald,
    tertiaryContainer = TertiaryContainerEmerald,
    onTertiaryContainer = OnTertiaryContainerEmerald,
    error = ErrorCrimson,
    onError = OnErrorCrimson,
    errorContainer = ErrorContainerCrimson,
    onErrorContainer = OnErrorContainerCrimson,
    background = BackgroundAlabaster,
    onBackground = OnBackgroundSlate,
    surface = SurfacePure,
    onSurface = OnSurfaceSlate,
    surfaceVariant = SurfaceVariantMist,
    onSurfaceVariant = OnSurfaceVariantSlate,
    outline = OutlineSteel
)

val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun OfficeReportTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = AppShapes,
        content = content
    )
}
