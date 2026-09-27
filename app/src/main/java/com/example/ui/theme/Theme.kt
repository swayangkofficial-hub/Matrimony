package com.example.ui.theme

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
    primary = DarkPrimary,
    onPrimary = BurgundyDeep,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = RoseLight,
    secondary = DarkSecondary,
    onSecondary = BurgundyDeep,
    secondaryContainer = BurgundyDark,
    onSecondaryContainer = GoldLight,
    tertiary = TrustTealLight,
    onTertiary = BurgundyDeep,
    background = DarkBackground,
    onBackground = Color(0xFFF3E8E8),
    surface = DarkSurface,
    onSurface = Color(0xFFF3E8E8),
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFD4C2C3),
    outline = Color(0xFF5A4449)
)

private val LightColorScheme = lightColorScheme(
    primary = BurgundyPrimary,
    onPrimary = Color.White,
    primaryContainer = RoseBlush,
    onPrimaryContainer = BurgundyDeep,
    secondary = GoldSecondary,
    onSecondary = Color.White,
    secondaryContainer = GoldLight,
    onSecondaryContainer = BurgundyDeep,
    tertiary = TrustTeal,
    onTertiary = Color.White,
    tertiaryContainer = TrustTealLight,
    onTertiaryContainer = BurgundyDeep,
    background = BackgroundIvory,
    onBackground = NeutralDark,
    surface = SurfacePure,
    onSurface = NeutralDark,
    surfaceVariant = SurfaceSubtle,
    onSurfaceVariant = NeutralMedium,
    outline = CardBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep brand aesthetic
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
