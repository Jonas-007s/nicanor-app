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

private val DarkColorScheme = darkColorScheme(
    primary = PureWhite,
    secondary = Platinum,
    tertiary = GoldAccent,
    background = PureBlack,
    surface = OffBlack,
    onPrimary = PureBlack,
    onSecondary = PureBlack,
    onTertiary = PureBlack,
    onBackground = PureWhite,
    onSurface = PureWhite,
    surfaceVariant = CharcoalGray,
    onSurfaceVariant = Platinum
)

private val LightColorScheme = lightColorScheme(
    primary = PureBlack,
    secondary = CharcoalGray,
    tertiary = GoldAccent,
    background = OffWhite,
    surface = PureWhite,
    onPrimary = PureWhite,
    onSecondary = PureWhite,
    onTertiary = PureWhite,
    onBackground = PureBlack,
    onSurface = PureBlack,
    surfaceVariant = Platinum,
    onSurfaceVariant = CharcoalGray
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Force dark mode by default for premium jewelry branding
  dynamicColor: Boolean = false, // Disable dynamic colors to keep elegant designer values
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
