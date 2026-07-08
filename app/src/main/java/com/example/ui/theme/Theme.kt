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

private val DarkColorScheme =
  darkColorScheme(
    primary = BinanceGold,
    onPrimary = Color(0xFF000000),
    primaryContainer = BinanceGoldLight,
    onPrimaryContainer = Color(0xFF000000),
    secondary = BinanceGold,
    onSecondary = Color(0xFF000000),
    background = BinanceDarkBg,
    onBackground = BinanceTextPrimary,
    surface = BinanceDarkSurface,
    onSurface = BinanceTextPrimary,
    surfaceVariant = BinanceCardBg,
    onSurfaceVariant = BinanceTextSecondary
  )

private val LightColorScheme = DarkColorScheme // Enforce dark Binance theme for that premium trading platform feel

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Force dark mode for premium crypto terminal aesthetic
  dynamicColor: Boolean = false, // Disable Android dynamic colors to preserve our brand palette
  content: @Composable () -> Unit,
) {
  val colorScheme = DarkColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
