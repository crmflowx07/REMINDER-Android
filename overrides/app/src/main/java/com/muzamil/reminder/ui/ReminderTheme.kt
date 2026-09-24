package com.muzamil.reminder.ui

import androidx.compose.foundation.isSystemInDarkTheme
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
    secondary = FigmaSecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF4F1FF),
    onSecondaryContainer = FigmaInk,
    surface = Color.White,
    surfaceVariant = Color(0xFFF6F4FA),
    background = FigmaCanvas,
    onBackground = FigmaInk,
    onSurface = FigmaInk,
    onSurfaceVariant = FigmaSecondary,
    outline = Color(0xFFE8E4F0),
    error = Color(0xFFD64562),
    errorContainer = Color(0xFFFFE8EE)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFBDA9FF),
    onPrimary = Color(0xFF28116F),
    primaryContainer = Color(0xFF41268E),
    onPrimaryContainer = Color(0xFFF0EBFF),
    secondary = Color(0xFFC9C1D9),
    surface = Color(0xFF18161E),
    surfaceVariant = Color(0xFF25222D),
    background = Color(0xFF121016),
    onBackground = Color(0xFFF8F5FF),
    onSurface = Color(0xFFF8F5FF),
    onSurfaceVariant = Color(0xFFC9C1D9),
    outline = Color(0xFF484253)
)

private val FigmaTypography = Typography(
    headlineLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 28.sp, lineHeight = 34.sp),
    headlineMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 24.sp, lineHeight = 30.sp),
    titleLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 19.sp, lineHeight = 24.sp),
    titleMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 22.sp),
    bodyLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 21.sp),
    bodyMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal, fontSize = 13.sp, lineHeight = 19.sp),
    bodySmall = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal, fontSize = 11.sp, lineHeight = 16.sp),
    labelLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 18.sp),
    labelMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp),
    labelSmall = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium, fontSize = 10.sp, lineHeight = 14.sp)
)

private val FigmaShapes = Shapes(
    extraSmall = androidx.compose.foundation.shape.RoundedCornerShape(15.dp),
    small = androidx.compose.foundation.shape.RoundedCornerShape(10.dp),
    medium = androidx.compose.foundation.shape.RoundedCornerShape(15.dp),
    large = androidx.compose.foundation.shape.RoundedCornerShape(18.dp),
    extraLarge = androidx.compose.foundation.shape.RoundedCornerShape(24.dp)
)

@Composable
fun ReminderTheme(theme: String = "SYSTEM", content: @Composable () -> Unit) {
    val dark = when (theme) { "DARK" -> true; "LIGHT" -> false; else -> isSystemInDarkTheme() }
    MaterialTheme(
        colorScheme = if (dark) DarkColors else LightColors,
        typography = FigmaTypography,
        shapes = FigmaShapes,
        content = content
    )
}
