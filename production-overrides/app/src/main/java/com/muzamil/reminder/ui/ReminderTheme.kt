package com.muzamil.reminder.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val LightColors = lightColorScheme(
    primary = FigmaPurple,
    onPrimary = Color.White,
    primaryContainer = FigmaPurpleSoft,
    onPrimaryContainer = FigmaInk,
    secondary = Color(0xFF7258B5),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF4F1FF),
    onSecondaryContainer = FigmaInk,
    tertiary = Color(0xFF4F7D73),
    onTertiary = Color.White,
    surface = Color.White,
    surfaceVariant = Color(0xFFF6F4FA),
    background = FigmaCanvas,
    onBackground = FigmaInk,
    onSurface = FigmaInk,
    onSurfaceVariant = FigmaSecondary,
    outline = Color(0xFFE3DEEB),
    outlineVariant = Color(0xFFEEEAF4),
    error = Color(0xFFD64562),
    errorContainer = Color(0xFFFFE8EE),
    onErrorContainer = Color(0xFF6A1326)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFB9A4FF),
    onPrimary = Color(0xFF24105F),
    primaryContainer = Color(0xFF38276A),
    onPrimaryContainer = Color(0xFFF2EDFF),
    secondary = Color(0xFFCEC5E5),
    onSecondary = Color(0xFF312A3D),
    secondaryContainer = Color(0xFF322C3E),
    onSecondaryContainer = Color(0xFFF4EEFF),
    tertiary = Color(0xFF9FD4C5),
    onTertiary = Color(0xFF12372F),
    surface = Color(0xFF1B1821),
    surfaceVariant = Color(0xFF28242F),
    background = Color(0xFF121016),
    onBackground = Color(0xFFF8F5FF),
    onSurface = Color(0xFFF8F5FF),
    onSurfaceVariant = Color(0xFFC9C1D9),
    outline = Color(0xFF4B4457),
    outlineVariant = Color(0xFF332E3C),
    error = Color(0xFFFFB2C0),
    errorContainer = Color(0xFF6C2737),
    onErrorContainer = Color(0xFFFFD9E0)
)

private val FigmaTypography = Typography(
    headlineLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 28.sp, lineHeight = 35.sp),
    headlineMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 24.sp, lineHeight = 30.sp),
    headlineSmall = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 21.sp, lineHeight = 27.sp),
    titleLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 19.sp, lineHeight = 25.sp),
    titleMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 22.sp),
    titleSmall = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp),
    bodyLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 21.sp),
    bodyMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal, fontSize = 13.sp, lineHeight = 19.sp),
    bodySmall = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal, fontSize = 11.sp, lineHeight = 16.sp),
    labelLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 18.sp),
    labelMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp),
    labelSmall = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium, fontSize = 10.sp, lineHeight = 14.sp)
)

private val FigmaShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(15.dp),
    large = RoundedCornerShape(18.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

@Composable
fun ReminderTheme(theme: String = "SYSTEM", content: @Composable () -> Unit) {
    val dark = when (theme) {
        "DARK" -> true
        "LIGHT" -> false
        else -> isSystemInDarkTheme()
    }
    MaterialTheme(
        colorScheme = if (dark) DarkColors else LightColors,
        typography = FigmaTypography,
        shapes = FigmaShapes,
        content = content
    )
}
